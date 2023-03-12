package org.tradeexpert.tradeexpert;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.tradeexpert.tradeexpert.BinanceUs.BinanceUs;
import org.tradeexpert.tradeexpert.Coinbase.Coinbase;
import org.tradeexpert.tradeexpert.oanda.Oanda;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javafx.scene.paint.Color.rgb;
import static org.tradeexpert.tradeexpert.CandleStickChartUtils.*;
import static org.tradeexpert.tradeexpert.ChartColors.*;
import static org.tradeexpert.tradeexpert.NewsManager.news;

/**
 * A resizable chart that allows for analyzing the trading activity of a commodity over time. The chart is made up of
 * fixed-duration bars that range vertically from the price of the commodity at the beginning of the duration
 * (the open price) to the price at the end of the duration (the close price). Superimposed on these bars is a
 * line that ranges from the lowest price the commodity reached during the duration, to the highest price
 * reached. Hence the name candle-stick chart (the line being the wick of a candle...although in this case it's
 * a double-ended wick!). The candles are color-coded to represent the type of activity that occurred during the
 * duration of the candle, if the price of the commodity increased during the duration, the candle is colored
 * green and represents a "bullish" trading period. Conversely, if the price decreased then the candle is colored
 * red which represents a "bearish" period. To display a {@code CandleStickChart} in a scene one must use
 * a {@link CandleStickChartContainer}. To enforce this usage, the constructors for this class are package-private.
 * <p>
 * JavaFX offers various charts in it's javafx.scene.chart package, but does not offer a candle-stick
 * chart out-of-the-box. It does however offer an XYChart which could be used as a starting-point for a candle-stick
 * chart. This is the <a href="http://hg.openjdk.java.net/openjfx/9-dev/rt/file/tip/apps/samples/Ensemble8/
 * src/samples/java/ensemble/samples/charts/candlestick/CandleStickChart.java">approach</a>
 * taken by the JavaFX developers for the <a href="http://www.oracle.com/technetwork/java/javase/overview/
 * javafx-samples-2158687.html">Ensemble demos</a> and also by <a href="https://github.com/rterp/StockChartsFX"
 * >StockChartsFX</a>. Indeed, this is the approach that we went with originally but decided to switch to the
 * present {@link Canvas}-based implementation that is contained herein.
 * <p>
 * The main reason for choosing a Canvas-based implementation is that by using a Canvas we obtain pixel-perfect
 * drawing capabilities and precise control over what should be displayed in response to panning and zooming. With the
 * old approach the drawing of the volume bars and the panning and zooming capabilities were all extremely ad-hoc and
 * buggy. For example the panning was simulated by using a ScrollPane which functioned very poorly when paging in
 * new candles (as the bounds of the pane were changing while scrolling was happening so "jumps" would occur).
 * Also in order to implement panning and zooming we needed access to all the chart's internal data (and then some)
 * and so the encapsulation of the chart's data by the Chart class was being completely bypassed.
 *
 * @author NOEL M NGUEMECHIEU
 */
public class CandleStickChart extends Region {
    private static final DecimalFormat MARKER_FORMAT = new DecimalFormat("#.000");
    CandleDataPager candleDataPager;
    CandleDataSupplier candleDataSupplier;
    CandleStickChartOptions chartOptions;
    /**
     * Maps an open time (as a Unix timestamp) to the computed candle data (high price, low price, etc.) for a trading
     * period beginning with that opening time. Thus, the key "1601798498" would be mapped to the candle data for trades
     * from the period of 1601798498 to 1601798498 + secondsPerCandle.
     */
    NavigableMap<Integer, CandleData> data;

    Exchange exchange;
    String tradePair;
    boolean liveSyncing;
    Map<Integer, ZoomLevel> zoomLevelMap;
    Consumer<List<CandleData>> candlePageConsumer;
    ScheduledExecutorService updateInProgressCandleExecutor;
    UpdateInProgressCandleTask updateInProgressCandleTask;
    InProgressCandle inProgressCandle;
    StableTicksAxis xAxis;
    StableTicksAxis yAxis;
    StableTicksAxis extraAxis;
    ProgressIndicator progressIndicator;

    Line extraAxisExtension;
    EventHandler<MouseEvent> mouseDraggedHandler;
    EventHandler<ScrollEvent> scrollHandler;
    EventHandler<KeyEvent> keyHandler;
    private final Font canvasNumberFont;
    int secondsPerCandle;

    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private int candleWidth = 10;
    private double mousePrevX = -1;
    private double mousePrevY = -1;
    private double scrollDeltaXSum;
    private double chartWidth = 1100;
    private double chartHeight =730;
    private int inProgressCandleLastDraw = -1;
    private volatile ZoomLevel currZoomLevel;
    private volatile boolean paging;
    private boolean animated;

    TelegramClient   telegramClient = new TelegramClient("2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo");
    private boolean autoTrading;

    public CandleStickChart(Exchange exchange, CandleDataSupplier candleDataSupplier, String tradePair,
                            boolean liveSyncing, int secondsPerCandle, ObservableNumberValue containerWidth,
                            ObservableNumberValue containerHeight) throws TelegramApiException {
        Objects.requireNonNull(exchange);
        Objects.requireNonNull(candleDataSupplier);
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(containerWidth);
        Objects.requireNonNull(containerHeight);
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalArgumentException("CandleStickChart must be constructed on the JavaFX Application " +
                    "Thread but was called from \"" + Thread.currentThread() + "\".");
        }
        this.exchange = exchange;
        this.tradePair = tradePair;
        this.secondsPerCandle = secondsPerCandle;
        this.liveSyncing = liveSyncing;
        zoomLevelMap = new ConcurrentHashMap<>();
        candleDataPager = new CandleDataPager(this, candleDataSupplier);
        data = Collections.synchronizedNavigableMap(new TreeMap<>(Integer::compare));
        chartOptions = new CandleStickChartOptions();
        canvasNumberFont = Font.font(FXUtils.getMonospacedFont(), 13);
        progressIndicator = new ProgressIndicator(-1);
        getStyleClass().add("candle-chart");
        xAxis = new StableTicksAxis();
        yAxis = new StableTicksAxis();

        extraAxis = new StableTicksAxis();
        xAxis.setAnimated(true);
        yAxis.setAnimated(true);
        extraAxis.setAnimated(false);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        extraAxis.setAutoRanging(false);
        xAxis.setSide(Side.BOTTOM);
        yAxis.setSide(Side.LEFT);
        extraAxis.setSide(Side.RIGHT);
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(InstantAxisFormatter.of(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
        )));
        yAxis.setTickLabelFormatter(new MoneyAxisFormatter(Currency.of(tradePair.substring(3, tradePair.length() - 1))));
        extraAxis.setTickLabelFormatter(new MoneyAxisFormatter(Currency.of(tradePair.substring(3, tradePair.length() - 1))));
        Font axisFont = Font.font(FXUtils.getMonospacedFont(), 15);
        yAxis.setTickLabelFont(axisFont);

        xAxis.setTickLabelFont(axisFont);
        extraAxis.setTickLabelFont(axisFont);



        extraAxisExtension = new Line();
        Label symbolLabel = new Label();
        symbolLabel.setText(tradePair + "  " + Currency.of(tradePair.substring(0, 3)) + " " + Currency.of(tradePair.substring(0, 3)).getSymbol());
        GridPane grid = new GridPane();
        grid.setTranslateX(110);
        grid.setTranslateY(70);
        grid.setVgap(10);
        Spinner<Double> spinner = new Spinner<>(0.01, 1000000, 0, 0.01);
        spinner.decrement(1);
        spinner.decrement(-1);
        spinner.setEditable(true);

        grid.add(spinner, 1, 0);
        Button btnBuy = new Button("BUY");
        grid.add(btnBuy, 0, 2);
        grid.add(new Label("BID :"), 1, 2);

        Button btnSell = new Button("SELL");
        grid.add(btnSell, 3, 2);
        grid.add(new Label("ASK :"), 2, 2);

        grid.setPrefSize(150, 100);


        Label loadingLabel = new Label("");

        if (!progressIndicator.isVisible()) {
            loadingLabel.setText("Loading...");
            loadingLabel.setVisible(true);
            loadingLabel.setFont(Font.font("Arial", 300));
        }


        VBox loadingIndicatorContainer = new VBox(progressIndicator, loadingLabel);
        progressIndicator.setPrefSize(50, 50);

        loadingIndicatorContainer.setFillWidth(true);
        loadingIndicatorContainer.setSpacing(10);
        loadingIndicatorContainer.setTranslateX(50);
        loadingIndicatorContainer.setAlignment(Pos.CENTER);
        loadingIndicatorContainer.setMouseTransparent(true);
        //
        // We want to extend the extra axis (volume) visually so that it encloses the chart area.
        extraAxisExtension = new Line();
        Paint lineColor = Color.WHEAT;
        extraAxisExtension.setFill(lineColor);
        extraAxisExtension.setStroke(lineColor);
        extraAxisExtension.setSmooth(false);
        extraAxisExtension.setStrokeWidth(2);

        Line newLines=new Line();
        newLines.setStroke(lineColor);
        newLines.setStrokeWidth(2);
        newLines.setSmooth(false);
        newLines.setVisible(true);
        newLines.setRotate(90);
        newLines.setStartX(
                extraAxisExtension.getLayoutBounds().getMinX() + extraAxisExtension.getLayoutBounds().getWidth() / 2
        );
        newLines.setStartY(
                extraAxisExtension.getLayoutBounds().getMinY() + extraAxisExtension.getLayoutBounds().getHeight() / 2
        );
        newLines.setEndX(
                extraAxisExtension.getLayoutBounds().getMaxX() - extraAxisExtension.getLayoutBounds().getWidth() / 2
        );
        newLines.setEndY(
                extraAxisExtension.getLayoutBounds().getMaxY() - extraAxisExtension.getLayoutBounds().getHeight() / 2
        );
        getChildren().addAll(xAxis, yAxis,newLines, extraAxis, extraAxisExtension);
        BooleanProperty gotFirstSize = new SimpleBooleanProperty(false);
        final ChangeListener<Number> sizeListener = new SizeChangeListener(gotFirstSize, containerWidth,
                containerHeight);
        containerWidth.addListener(sizeListener);
        containerHeight.addListener(sizeListener);

        if (liveSyncing) {
            inProgressCandle = new InProgressCandle();
            updateInProgressCandleTask = new UpdateInProgressCandleTask();
            updateInProgressCandleExecutor = Executors.newSingleThreadScheduledExecutor(
                    new LogOnExceptionThreadFactory("UPDATE-CURRENT-CANDLE"));

            CompletableFuture.runAsync(() -> {
                boolean websocketInitialized = false;
                try {
                    websocketInitialized = exchange.getWebsocketClient().getInitializationLatch().await(
                            10, SECONDS);
                } catch (InterruptedException ex) {
                    Log.error("Interrupted while waiting for websocket client to be initialized: " + ex);
                }

                if (!websocketInitialized) {
                    Log.error("websocket client: " + exchange.getWebsocketClient().getURI().getHost() +
                            " was not initialized after 10 seconds");
                } else {
                    if (exchange.getWebsocketClient().supportsStreamingTrades(tradePair)) {
                        exchange.getWebsocketClient().streamLiveTrades(tradePair, updateInProgressCandleTask);
                    }

                    updateInProgressCandleExecutor.scheduleAtFixedRate(updateInProgressCandleTask, 5, 5, SECONDS);
                }
            });
        } else {
            inProgressCandle = null;
            updateInProgressCandleTask = null;
            updateInProgressCandleExecutor = null;
        }

        candlePageConsumer = new CandlePageConsumer();
        mouseDraggedHandler = new MouseDraggedHandler();
        scrollHandler = new ScrollEventHandler();
        keyHandler = new KeyEventHandler();

        // When the application starts up and tries to initialize a candle stick chart the size can
        // fluctuate. So we wait to get the "final" size before laying out the chart. After we get
        // the size, we remove this listener from the gotFirstSize property.
        ChangeListener<? super Boolean> gotFirstSizeChangeListener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                double numberOfVisibleWholeCandles = Math.floor(containerWidth.getValue().doubleValue() / candleWidth);
                chartWidth = (numberOfVisibleWholeCandles * candleWidth) - 60 + (float) (candleWidth / 2);
                chartWidth = (Math.floor(containerWidth.getValue().doubleValue() / candleWidth) * candleWidth) - 60 +
                        (float) (candleWidth / 2);
                chartHeight = containerHeight.getValue().doubleValue();
                canvas = new Canvas(chartWidth-60, chartHeight);

                canvas.applyCss();
                canvas.getGraphicsContext2D().setFont(Font.font("Helvetica", 13));

                // Label infosNews = new Label("Infos :");



//                btnSell.setOnAction(e -> {
//                    try {
//                        CreateOrder("SELL", spinner.getValue(), PLATFORM.BINANCE_US);
//                    } catch (OandaException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                });
//                btnBuy.setOnAction(e -> {
//                    try {
//                        CreateOrder("BUY", spinner.getValue(), PLATFORM.COINBASE_PRO);
//                    } catch (OandaException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                });


                loadingIndicatorContainer.setTranslateX(580);
                loadingIndicatorContainer.setTranslateY(250);
                symbolLabel.setTranslateY(200);
                symbolLabel.setTranslateX(450);
                symbolLabel.setFont(Font.font(20));

                Label telegramLabel = new Label();

                telegramLabel.setTranslateY(0);
                Circle isConnected = new Circle(5);
                try {
                    telegramClient.run();
                    isConnected.setFill(Color.GREEN);
                    isConnected.setStroke(Color.GREEN);


                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!telegramClient.getUsername().equals("")) {

                    isConnected.setFill(Color.YELLOW);
                    isConnected.setStroke(Color.  GREEN);
                    isConnected.setStrokeWidth(4);

                }

                telegramLabel.setText(" Bot :" + telegramClient.getUsername() + "  " + TelegramClient.getLast_name());
                telegramLabel.setTranslateX(1200);
                isConnected.setTranslateX(1350);
                isConnected.setTranslateY(7);


                GridPane gridPaneOrderBook = new GridPane();
                gridPaneOrderBook.setAlignment(Pos.CENTER);
                gridPaneOrderBook.setHgap(10);
                gridPaneOrderBook.setVgap(10);
                gridPaneOrderBook.setPadding(new Insets(10, 10, 10, 10));
                gridPaneOrderBook.add(new Label("Time"), 0, 0);
                int time = inProgressCandle.getOpenTime();
                Label timeText = new Label();
                timeText.setText(      DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss").format(LocalDateTime.ofInstant(Instant.from(
                        Instant.ofEpochSecond(time)), ZoneId.systemDefault())));



                gridPaneOrderBook.add(timeText, 1, 0);
                gridPaneOrderBook.setTranslateX(canvas.getWidth()/12);
                gridPaneOrderBook.add(new Label("Open"), 2, 0);
                String  open = String.valueOf(0);
                Label openText = new Label(open);
                gridPaneOrderBook.add(openText, 3, 0);

                gridPaneOrderBook.add(new Label("High"), 4, 0);
                String high = String.valueOf(
                        inProgressCandle.getHighPriceSoFar()
                );
                Label highText = new Label(high);
                gridPaneOrderBook.add(highText, 5, 0);
                String low = String.valueOf(inProgressCandle.getLowPriceSoFar());
                gridPaneOrderBook.add(new Label("Low"), 6, 0);
                Label lowText = new Label(low);
                gridPaneOrderBook.add(lowText, 7, 0);
                String close = String.valueOf(0);
                gridPaneOrderBook.add(new Label("Close"), 8, 0);
                Label closeText = new Label(close);
                gridPaneOrderBook.add(closeText, 9, 0);
                String volume = String.valueOf(
                        inProgressCandle.getVolumeSoFar()
                );
                gridPaneOrderBook.add(new Label("Volume"), 10, 0);
                Label volumeText = new Label(volume);
                gridPaneOrderBook.add(volumeText, 11, 0);


                Line currentPriceLine = new Line();
                currentPriceLine.setStroke(Color.RED);
                currentPriceLine.setStrokeLineJoin(StrokeLineJoin.ROUND);
                currentPriceLine.setStrokeLineCap(StrokeLineCap.ROUND);
                currentPriceLine.setStrokeWidth(2);
                currentPriceLine.setRotate(180);

                currentPriceLine.setStartX(0);
                currentPriceLine.setStartY(0);
                currentPriceLine.setEndX(canvas.getWidth());
                currentPriceLine.setStyle("-fx-stroke: WHITE; -fx-stroke-width: 2;");
                currentPriceLine.setEndY(candleDataSupplier.secondsPerCandle);
                getChildren().addAll( canvas,gridPaneOrderBook, telegramLabel,isConnected, symbolLabel, loadingIndicatorContainer);
                // grid.setPrefSize(300, 250);
                //  grid.setTranslateX(0);
                //grid.setTranslateY(500);
                graphicsContext = canvas.getGraphicsContext2D();
                graphicsContext.setFill(Color.BLACK.brighter());
                graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                graphicsContext.setLineWidth(2);

                graphicsContext.setLineCap(StrokeLineCap.ROUND);
                graphicsContext.setLineJoin(StrokeLineJoin.ROUND);

                canvas.setId("canvas");



                layoutChart();
                initializeEventHandlers();

                CompletableFuture.supplyAsync(candleDataPager.getCandleDataSupplier()).thenAccept(
                        candleDataPager.getCandleDataPreProcessor());
                gotFirstSize.removeListener(this);
            }
        };

        gotFirstSize.addListener(gotFirstSizeChangeListener);

        chartOptions.horizontalGridLinesVisibleProperty().addListener((observable, oldValue, newValue) ->
                drawChartContents(true));
        chartOptions.verticalGridLinesVisibleProperty().addListener((observable, oldValue, newValue) ->
                drawChartContents(true));
        chartOptions.showVolumeProperty().addListener((observable, oldValue, newValue) -> drawChartContents(true));
        chartOptions.alignOpenCloseProperty().addListener((observable, oldValue, newValue) -> drawChartContents(true));
    }


    private void CreateOrder(String side, double size, @NotNull Exchange exchangeName) {
        exchange = exchangeName;

        switch (exchangeName) {
            case Coinbase coinbase -> coinbase.createMarketOrder(tradePair, side, size);
            case BinanceUs binance -> BinanceUs.createMarketOrder(tradePair, side, size);
            case Oanda oanda -> Oanda.createMarketOrder(tradePair, side, size);
            default -> {
            }
        }


    }

    private void initializeEventHandlers() {
        if (canvas.getParent() != null) {
            canvas.getParent().addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                mousePrevX = -1;
                mousePrevY = -1;
            });

            canvas.getParent().addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
            canvas.getParent().addEventFilter(ScrollEvent.SCROLL, scrollHandler);
            canvas.getParent().addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        } else {
            canvas.parentProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                        mousePrevX = -1;
                        mousePrevY = -1;
                    });

                    newValue.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
                    newValue.addEventFilter(ScrollEvent.SCROLL, scrollHandler);
                    newValue.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
                }
            });
        }
    }

    private void moveAlongX(int deltaX, boolean skipDraw) {
        if (deltaX != 1 && deltaX != -1) {
            throw new RuntimeException("deltaX must be 1 or -1 but was: " + deltaX);
        }
        CompletableFuture<Boolean> progressIndicatorVisibleFuture = new CompletableFuture<>();
        Platform.runLater(() -> progressIndicatorVisibleFuture.complete(progressIndicator.isVisible()));
        progressIndicatorVisibleFuture.thenAccept(progressIndicatorVisible -> {
            // This is run on the JavaFX application thread.
            if (!progressIndicatorVisible) {
                int desiredXLowerBound = (int) xAxis.getLowerBound() + (deltaX == 1 ? secondsPerCandle : -secondsPerCandle);

                // Prevent moving in the positive direction past the point where only "minCandlesRemaining" candles
                // remain on the left-most part of the chart.
                int minCandlesRemaining = 3;
                if (desiredXLowerBound <= data.lastEntry().getValue().getOpenTime() -
                        (minCandlesRemaining - 1) * secondsPerCandle) {
                    if (desiredXLowerBound <= currZoomLevel.getMinXValue()) {
                        CompletableFuture.supplyAsync(candleDataPager.getCandleDataSupplier()).thenAccept(
                                candleDataPager.getCandleDataPreProcessor()).whenComplete((result, throwable) -> {
                            // Show the loading indicator and freeze the chart during the time that the new data is
                            // being paged in.
                            if (throwable != null) {
                                Log.error("exception: " + throwable);
                            }
                            paging = true;
                            progressIndicator.setVisible(true);
                            setAxisBoundsForMove(deltaX);
                            setYAndExtraAxisBounds();
                            if (!skipDraw) {
                                drawChartContents(true);
                            }
                            progressIndicator.setVisible(false);
                            paging = false;
                        });
                    } else {
                        setAxisBoundsForMove(deltaX);
                        setYAndExtraAxisBounds();
                        if (!skipDraw) {
                            drawChartContents(true);
                            progressIndicator.setVisible(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets the bounds of the x-axis either one full candle to the right or left, depending on the sign
     * of deltaX. Currently the magnitude of deltaX does not matter (each call to this method only moves
     * the duration of one full candle).
     *
     * @param deltaX set the bounds either one candle over to the right or left from the current position
     */
    private void setAxisBoundsForMove(int deltaX) {
        if (deltaX == 1) {
            xAxis.setUpperBound(xAxis.getUpperBound() + secondsPerCandle);
            xAxis.setLowerBound(xAxis.getLowerBound() + secondsPerCandle);
        } else if (deltaX == -1) {
            xAxis.setUpperBound(xAxis.getUpperBound() - secondsPerCandle);
            xAxis.setLowerBound(xAxis.getLowerBound() - secondsPerCandle);
        } else {
            throw new IllegalArgumentException("deltaX must be 1 or -1 but was: " + deltaX);
        }
    }

    /**
     * Sets the y-axis and extra axis bounds using only the x-axis lower bound.
     */
    private void setYAndExtraAxisBounds() {
        Log.info("xAxis lower bound: " , String.valueOf(xAxis.getLowerBound()));
        final double idealBufferSpaceMultiplier = 0.35;
        if (!currZoomLevel.getExtremaForCandleRangeMap().containsKey((int) xAxis.getLowerBound())) {
            // TODO(mike): Does this *always* represent a coding error on our end or can this happen during
            // normal chart functioning, and could we handle it more gracefully?
            Log.error("The extrema map did not contain extrema for x-value: " + (int) xAxis.getLowerBound());
            Log.error("extrema map: " + new TreeMap<>(currZoomLevel.getExtremaForCandleRangeMap()));

        }

        // The y-axis and extra axis extrema are obtained using a key offset by minus one candle duration. This makes
        // the chart work correctly. I don't fully understand the logic behind it, so I am leaving a note for
        // my future self.
        Pair<Extrema<Integer>, Extrema<Integer>> extremaForRange = currZoomLevel.getExtremaForCandleRangeMap().get(
                (int) xAxis.getLowerBound() - secondsPerCandle);

        if (extremaForRange == null) {
            Log.error("extremaForRange was null!");
            return;
        }
        final Integer yAxisMax = extremaForRange.getValue().getMax();
        final Integer yAxisMin = extremaForRange.getValue().getMin();
        final double yAxisDelta = yAxisMax - yAxisMin;
        yAxis.setUpperBound(yAxisMax + (yAxisDelta * idealBufferSpaceMultiplier));
        yAxis.setLowerBound(Math.max(0, yAxisMin - (yAxisDelta * idealBufferSpaceMultiplier)));

        extraAxis.setUpperBound(currZoomLevel.getExtremaForCandleRangeMap().get(
                (int) xAxis.getLowerBound() - secondsPerCandle).getKey().getMax());
    }

    private void layoutChart() {
        Log.info("CandleStickChart.layoutChart start", news.toString());
        extraAxisExtension.setStartX(chartWidth - 2);
        extraAxisExtension.setEndX(chartWidth - 2);
        extraAxisExtension.setStartY(10);
        extraAxisExtension.setEndY((chartHeight - 2) * 0.75);

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(10, 0, chartWidth - 2, chartHeight - 2);
        double top = snappedTopInset();
        double left = snappedLeftInset();
        top = snapPositionY(top);
        left = snapPositionX(left);

        // try and work out width and height of axes
        double xAxisWidth;
        double xAxisHeight = 25; // guess x axis height to start with
        double yAxisWidth = 0;
        double yAxisHeight;
        for (int count = 0; count < 3; count++) {
            yAxisHeight = snapSizeY(chartHeight - xAxisHeight);
            if (yAxisHeight < 0) {
                yAxisHeight = 0;
            }
            yAxisWidth = yAxis.prefWidth(yAxisHeight);
            xAxisWidth = snapSizeX(chartWidth - yAxisWidth);
            if (xAxisWidth < 0) {
                xAxisWidth = 0;
            }
            double newXAxisHeight = xAxis.prefHeight(xAxisWidth);
            if (newXAxisHeight == xAxisHeight) {
                break;
            }
            xAxisHeight = newXAxisHeight;
        }

        xAxisHeight = Math.ceil(xAxisHeight);
        yAxisWidth = Math.ceil(yAxisWidth);

        // calc yAxis x-pos
        double yAxisX = left + 2;
        left += yAxisWidth;
        xAxis.setLayoutX(left);
        yAxis.setLayoutX(yAxisX);
        xAxis.setPrefSize(chartWidth - 2, xAxisHeight);
        yAxis.setPrefSize(yAxisWidth, chartHeight -2);
        extraAxis.setPrefSize(yAxisWidth, (chartHeight -    2) * 0.25);
        xAxis.setLayoutY(chartHeight);
        yAxis.setLayoutY(top);
        extraAxis.setLayoutX(chartWidth - 2);
        extraAxis.setLayoutY((chartHeight - 2) * 0.75);
        xAxis.requestAxisLayout();
        xAxis.layout();
        yAxis.requestAxisLayout();
        yAxis.layout();
        extraAxis.requestAxisLayout();
        extraAxis.layout();
        canvas.setLayoutX(left);
        canvas.setLayoutY(top);
        Log.info("CandleStickChart.layoutChart end", news.toString());
    }

    /**
     * Draws the chart contents on the canvas corresponding to the current x-axis, y-axis, and extra (volume) axis
     * bounds.
     */
    private void drawChartContents(boolean clearCanvas) {
            // This value allows for us to go past the highest x-value by skipping the drawing of some candles.
        int numCandlesToSkip = (int) Math.max((xAxis.getUpperBound() - data.lastEntry().getValue().getOpenTime()) /
                secondsPerCandle, 0);

        if (liveSyncing && inProgressCandleLastDraw != inProgressCandle.getOpenTime()) {
            // The duration of the last in-progress candle has ended, see if it is visible on screen.
            if (xAxis.getUpperBound() >= inProgressCandleLastDraw && xAxis.getUpperBound() <
                    inProgressCandleLastDraw + (canvas.getWidth() * secondsPerCandle)) {
                // If the new in-progress candle would be drawn off-screen, first move one candle duration
                // in the positive direction (so that the newest data is kept on-screen).
                if (numCandlesToSkip == 0) {
                    // Make room for the new in-progress candle.
                    moveAlongX(1, true);
                    numCandlesToSkip = (int) Math.max((xAxis.getUpperBound() -
                            data.lastEntry().getValue().getOpenTime()) / secondsPerCandle, 0);
                }
            }
            inProgressCandleLastDraw = inProgressCandle.getOpenTime();
        }

        if (clearCanvas) {
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.fillRect(0, 0, chartWidth - 2, chartHeight - 2);
        }

        double monetaryUnitsPerPixel = (yAxis.getUpperBound() - yAxis.getLowerBound()) / canvas.getHeight();
        double pixelsPerMonetaryUnit = 1d / monetaryUnitsPerPixel;
        NavigableMap<Integer, CandleData> candlesToDraw = data.subMap(((int) xAxis.getUpperBound() - secondsPerCandle) -
                        (((int) currZoomLevel.getNumVisibleCandles()) * secondsPerCandle), true,
                ((int) xAxis.getUpperBound() - secondsPerCandle) - (numCandlesToSkip * secondsPerCandle), true);


        Log.info("Drawing " , candlesToDraw.size() + " candles.");
        if (chartOptions.isHorizontalGridLinesVisible()) {
            // Draw horizontal grid lines aligned with y-axis major tick marks
            for (Axis.TickMark<Number> tickMark : yAxis.getTickMarks()) {
                graphicsContext.setStroke(rgb(120, 119, 112, 1));
                graphicsContext.setLineWidth(2);
                graphicsContext.strokeLine(0, tickMark.getPosition(), canvas.getWidth(), tickMark.getPosition());
            }
        }

        if (chartOptions.isVerticalGridLinesVisible()) {
            // Draw vertical grid lines aligned with x-axis major tick marks
            for (Axis.TickMark<Number> tickMark : xAxis.getTickMarks()) {
                graphicsContext.setStroke(rgb(120, 119, 112, 1));
                graphicsContext.setLineWidth(2);
                graphicsContext.strokeLine(tickMark.getPosition(), 0, tickMark.getPosition(), canvas.getHeight());


            }
        }

        int candleIndex = numCandlesToSkip;
        double highestCandleValue = Double.MIN_VALUE;
        double lowestCandleValue = Double.MAX_VALUE;
        int candleIndexOfHighest = -1;
        int candleIndexOfLowest = -1;
        int volumeBarMaxHeight = 150;
        double volumeScale = volumeBarMaxHeight / extraAxis.getUpperBound();
        double halfCandleWidth = candleWidth * 0.5;
        double lastClose = -1;
        for (CandleData candleDatum : candlesToDraw.descendingMap().values()) {

            // and use that here instead of iterating over the candle data again.
            if (candleIndex < currZoomLevel.getNumVisibleCandles() + 2) {
                // We don't want to draw the high/low markers off-screen, so we guard it with the above condition.
                if (candleDatum.getHighPrice() > highestCandleValue) {
                    highestCandleValue = candleDatum.getHighPrice();
                    candleIndexOfHighest = candleIndex;
                }

                if (candleDatum.getLowPrice() < lowestCandleValue) {
                    lowestCandleValue = candleDatum.getLowPrice();
                    candleIndexOfLowest = candleIndex;
                }
            }

            if (candleDatum.isPlaceHolder()) {
                // A placeholder candle is placed in a duration where no trading activity occurred.
                graphicsContext.beginPath();
                double candleOpenPrice = candleDatum.getOpenPrice();
                if (chartOptions.isAlignOpenClose() && lastClose != -1) {
                    candleOpenPrice = lastClose;
                }

                double candleYOrigin = cartesianToScreenCords((candleOpenPrice - yAxis.getLowerBound()) *
                        pixelsPerMonetaryUnit);

                graphicsContext.beginPath();
                graphicsContext.moveTo((canvas.getWidth() - (candleIndex * candleWidth)) + 2, candleYOrigin);
                graphicsContext.rect(canvas.getWidth() - (candleIndex * candleWidth), candleYOrigin,
                        candleWidth - 2, 2);
                graphicsContext.setFill(PLACE_HOLDER_FILL_COLOR);
                graphicsContext.fill();
                graphicsContext.setStroke(PLACE_HOLDER_BORDER_COLOR);
                graphicsContext.setLineWidth(2);
                graphicsContext.stroke();
            } else {
                Paint candleBorderColor;
                Paint candleFillColor;

                double candleOpenPrice = candleDatum.getOpenPrice();
                if (chartOptions.isAlignOpenClose() && lastClose != -1) {
                    candleOpenPrice = lastClose;
                }

                boolean openAboveClose = candleOpenPrice > candleDatum.getClosePrice();

                if (openAboveClose) {
                    candleBorderColor = BEAR_CANDLE_BORDER_COLOR;
                    candleFillColor = BEAR_CANDLE_FILL_COLOR;
                } else {
                    candleBorderColor = BULL_CANDLE_BORDER_COLOR;
                    candleFillColor = BULL_CANDLE_FILL_COLOR;
                }

                double candleYOrigin;
                if (openAboveClose) {
                    candleYOrigin = cartesianToScreenCords((candleOpenPrice -
                            yAxis.getLowerBound()) * pixelsPerMonetaryUnit);
                } else {
                    candleYOrigin = cartesianToScreenCords((candleDatum.getClosePrice() -
                            yAxis.getLowerBound()) * pixelsPerMonetaryUnit);
                }

                double candleHeight = Math.abs(candleOpenPrice - candleDatum.getClosePrice()) * pixelsPerMonetaryUnit;

                // draw the candle bar
                graphicsContext.beginPath();
                graphicsContext.moveTo((canvas.getWidth() - (candleIndex * candleWidth)) + 2, candleYOrigin);
                graphicsContext.rect(canvas.getWidth() - (candleIndex * candleWidth), candleYOrigin,
                        candleWidth - 2, candleHeight -2);
                graphicsContext.setFill(candleFillColor);
                graphicsContext.fill();
                graphicsContext.setStroke(candleBorderColor);
                graphicsContext.setLineWidth(2);
                graphicsContext.stroke();
                graphicsContext.beginPath(); // TODO(mike): Delete this line?
                // Draw high line (skip draw if the open (or close) is the same as the high.
                boolean drawHighLine = true;
                if (openAboveClose) {
                    if (candleOpenPrice == candleDatum.getHighPrice()) {
                        drawHighLine = false;
                    }

                    if (chartOptions.isAlignOpenClose()) {
                        if (candleOpenPrice > candleDatum.getHighPrice()) {
                            drawHighLine = false;
                        }
                    }
                } else {
                    if (candleDatum.getClosePrice() == candleDatum.getHighPrice()) {
                        drawHighLine = false;
                    }
                }

                if (drawHighLine) {
                    double candleHighValue = cartesianToScreenCords((candleDatum.getHighPrice() -
                            yAxis.getLowerBound()) * pixelsPerMonetaryUnit);
                    graphicsContext.moveTo(((canvas.getWidth() - (candleIndex * candleWidth)) + halfCandleWidth) - 1,
                            candleYOrigin);
                    graphicsContext.lineTo(((canvas.getWidth() - (candleIndex * candleWidth)) + halfCandleWidth) - 1,
                            candleHighValue);
                    graphicsContext.stroke();
                }

                // Draw low line (skip draw if the close (or open) is the same as the low.
                boolean drawLowLine = true;
                if (openAboveClose) {
                    if (candleDatum.getClosePrice() == candleDatum.getLowPrice()) {
                        drawLowLine = false;
                    }
                } else {
                    if (candleOpenPrice == candleDatum.getLowPrice()) {
                        drawLowLine = false;
                    }

                    if (chartOptions.isAlignOpenClose()) {
                        if (candleOpenPrice < candleDatum.getLowPrice()) {
                            drawLowLine = false;
                        }
                    }

                }
                if (drawLowLine) {
                    double candleLowValue = cartesianToScreenCords((candleDatum.getLowPrice() -
                            yAxis.getLowerBound()) * pixelsPerMonetaryUnit);
                    graphicsContext.moveTo(((canvas.getWidth() - (candleIndex * candleWidth)) + halfCandleWidth) - 1,
                            candleYOrigin + candleHeight);
                    graphicsContext.lineTo(((canvas.getWidth() - (candleIndex * candleWidth)) + halfCandleWidth) - 1,
                            candleLowValue);
                    graphicsContext.stroke();
                }

                // draw volume bar
                if (chartOptions.isShowVolume()) {
                    double candleVolumeYOrigin = cartesianToScreenCords(candleDatum.getVolume() * volumeScale);
                    graphicsContext.beginPath();
                    graphicsContext.moveTo((canvas.getWidth() - (candleIndex * candleWidth)) + 2, candleVolumeYOrigin);
                    graphicsContext.rect(canvas.getWidth() - (candleIndex * candleWidth), candleVolumeYOrigin,
                            candleWidth - 2, candleVolumeYOrigin - 2);
                    graphicsContext.setFill(candleFillColor);
                    graphicsContext.fill();
                    graphicsContext.setStroke(candleBorderColor);
                    graphicsContext.setLineWidth(2);
                    graphicsContext.stroke();
                }
            }

            lastClose = candleDatum.getClosePrice();
            candleIndex++;
        }

        // Draw arrows to the extrema for the currently visible candles (helps to easily see the highs and lows of
        // the current range without needing to visually trace to the axis).
        graphicsContext.setFont(canvasNumberFont);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFill(AXIS_TICK_LABEL_COLOR);
        graphicsContext.setFontSmoothingType(FontSmoothingType.LCD);
        double highMarkYPos = cartesianToScreenCords((highestCandleValue - yAxis.getLowerBound()) *
                pixelsPerMonetaryUnit) - 1;
        double lowMarkYPos = cartesianToScreenCords((lowestCandleValue - yAxis.getLowerBound()) *
                pixelsPerMonetaryUnit) + 1;

        // Prevent the high and low markers from overlapping (this can happen if there is very little volatility
        // between candles and very few candles are on-screen).
        boolean skipLowMark = lowMarkYPos - highMarkYPos < canvasNumberFont.getSize() &&
                candleIndexOfHighest == candleIndexOfLowest;
        if (candleIndexOfHighest > currZoomLevel.getNumVisibleCandles() * 0.5) {
            // draw high marker to the right of the candle (arrow points to the left)
            double xPos = ((canvas.getWidth() - (candleIndexOfHighest * candleWidth)) + halfCandleWidth) + 2;
            graphicsContext.setTextAlign(TextAlignment.LEFT);
            graphicsContext.fillText("←-- " + MARKER_FORMAT.format(highestCandleValue), xPos, highMarkYPos);
        } else {
            // draw high marker to the left of the candle (arrow points to the right)
            double xPos = ((canvas.getWidth() - (candleIndexOfHighest * candleWidth)) + halfCandleWidth) - 3;
            graphicsContext.setTextAlign(TextAlignment.RIGHT);
            graphicsContext.fillText(MARKER_FORMAT.format(highestCandleValue) + " --→", xPos, highMarkYPos);
        }

        if (!skipLowMark) {
            if (candleIndexOfLowest > currZoomLevel.getNumVisibleCandles() * 0.5) {
                // draw low marker to the right of the candle (arrow points to the left)
                double xPos = ((canvas.getWidth() - (candleIndexOfLowest * candleWidth)) + halfCandleWidth) + 2;
                graphicsContext.setTextAlign(TextAlignment.LEFT);
                graphicsContext.fillText("←-- " + MARKER_FORMAT.format(lowestCandleValue), xPos, lowMarkYPos);
            } else {
                // draw low marker to the left of the candle (arrow points to the right)
                double xPos = ((canvas.getWidth() - (candleIndexOfLowest * candleWidth)) + halfCandleWidth) - 2;
                graphicsContext.setTextAlign(TextAlignment.RIGHT);
                graphicsContext.fillText(MARKER_FORMAT.format(lowestCandleValue) + " --→", xPos, lowMarkYPos);
            }
        }
    }

    private double cartesianToScreenCords(double yCoordinate) {
        return -yCoordinate + canvas.getHeight();
    }


    void changeZoom(ZoomDirection zoomDirection) {
        final int multiplier = zoomDirection == ZoomDirection.IN ? -1 : 1;
        if (currZoomLevel == null) {
            Log.error("currZoomLevel was null!");
        }
        int newCandleWidth = currZoomLevel.getCandleWidth() - multiplier;
        if (newCandleWidth <= 1) {
            // Can't go below one pixel for candle width.
            return;
        }

        int newLowerBoundX = (int) (xAxis.getUpperBound() - ((int) (canvas.getWidth() /
                newCandleWidth) * secondsPerCandle));
        if (newLowerBoundX > data.lastEntry().getValue().getOpenTime() - (2 * secondsPerCandle)) {
            return;
        }

        final int nextZoomLevelId = ZoomLevel.getNextZoomLevelId(currZoomLevel, zoomDirection);
        int currMinXValue = currZoomLevel.getMinXValue();

        if (!zoomLevelMap.containsKey(nextZoomLevelId)) {
            // We can use the minXValue of the current zoom level here because, given a sequence of zoom-levels
            // z(0), z(1), ... z(n) that the chart has gone through, z(x).minXValue <= z(y).minXValue for all x > y.
            // That is, if we are currently at a max/min zoom-level in zoomLevelMap, there is no other zoom-level that
            // has a lower minXValue (assuming we did not start at the maximum or minimum zoom level).
            ZoomLevel newZoomLevel = new ZoomLevel(nextZoomLevelId, newCandleWidth, secondsPerCandle,
                    canvas.widthProperty(), getXAxisFormatterForRange(xAxis.getUpperBound() - newLowerBoundX),
                    currMinXValue);

            int numCandlesToSkip = Math.max((((int) xAxis.getUpperBound()) -
                    data.lastEntry().getValue().getOpenTime()) / secondsPerCandle, 0);

            // If there are less than numVisibleCandles on the screen, we want to be sure and check against what the
            // lower bound *would be* if we had the full amount. Otherwise we won't be able to calculate the correct
            // extrema because the window size will be greater than the number of candles we have data for.
            if (newLowerBoundX - (numCandlesToSkip * secondsPerCandle) < currZoomLevel.getMinXValue()) {
                // We need to try and request more data so that we can properly zoom out to this level.
                paging = true;
                progressIndicator.setVisible(true);
                CompletableFuture.supplyAsync(candleDataPager.getCandleDataSupplier()).thenAccept(
                        candleDataPager.getCandleDataPreProcessor()).whenComplete((result, throwable) -> {
                    List<CandleData> candleData = new ArrayList<>(data.values());
                    putSlidingWindowExtrema(newZoomLevel.getExtremaForCandleRangeMap(),
                            candleData, (int) newZoomLevel.getNumVisibleCandles());
                    putExtremaForRemainingElements(newZoomLevel.getExtremaForCandleRangeMap(),
                            candleData.subList(candleData.size() - (int) Math.floor(
                                    newZoomLevel.getNumVisibleCandles()), candleData.size()));
                    zoomLevelMap.put(nextZoomLevelId, newZoomLevel);
                    currZoomLevel = newZoomLevel;
                    Platform.runLater(() -> {
                        xAxis.setTickLabelFormatter(currZoomLevel.getXAxisFormatter());
                        candleWidth = currZoomLevel.getCandleWidth();
                        xAxis.setLowerBound(newLowerBoundX);
                        setYAndExtraAxisBounds();
                        drawChartContents(true);
                        progressIndicator.setVisible(false);
                        paging = false;
                    });
                });
                return;
            } else {
                List<CandleData> candleData = new ArrayList<>(data.values());
                putSlidingWindowExtrema(newZoomLevel.getExtremaForCandleRangeMap(),
                        candleData, (int) newZoomLevel.getNumVisibleCandles());
                putExtremaForRemainingElements(newZoomLevel.getExtremaForCandleRangeMap(), candleData.subList(
                        candleData.size() - (int) Math.floor(newZoomLevel.getNumVisibleCandles()),
                        candleData.size()));
                zoomLevelMap.put(nextZoomLevelId, newZoomLevel);
                currZoomLevel = newZoomLevel;
            }
        } else {

            currZoomLevel = zoomLevelMap.get(nextZoomLevelId);
            List<CandleData> candleData = new ArrayList<>(data.values());
            putSlidingWindowExtrema(currZoomLevel.getExtremaForCandleRangeMap(), candleData,
                    (int) currZoomLevel.getNumVisibleCandles());
            putExtremaForRemainingElements(currZoomLevel.getExtremaForCandleRangeMap(), candleData.subList(
                    candleData.size() - (int) Math.floor(currZoomLevel.getNumVisibleCandles()), candleData.size()));
        }

        xAxis.setTickLabelFormatter(currZoomLevel.getXAxisFormatter());
        candleWidth = currZoomLevel.getCandleWidth();
        xAxis.setLowerBound(newLowerBoundX);
        setYAndExtraAxisBounds();
        drawChartContents(true);
    }

    CandleStickChartOptions getChartOptions() {
        return chartOptions;
    }

    Consumer<List<CandleData>> getCandlePageConsumer() {
        return candlePageConsumer;
    }

    @Override
    protected double computeMinWidth(double height) {
        return 1500;
    }

    @Override
    protected double computeMinHeight(double width) {
        return 550;
    }

    @Override
    protected double computePrefWidth(double height) {
        return chartWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        return chartHeight;
    }





    private void setInitialState(@NotNull List<CandleData> candleData) {

        if (liveSyncing) {
            candleData.add(candleData.size(), inProgressCandle.snapshot());
        }

        xAxis.setUpperBound(candleData.get(candleData.size() - 1).getOpenTime() + secondsPerCandle);
        xAxis.setLowerBound((candleData.get(candleData.size() - 1).getOpenTime() + secondsPerCandle) -
                (int) (Math.floor(canvas.getWidth() / candleWidth) * secondsPerCandle));
        currZoomLevel = new ZoomLevel(0, candleWidth, secondsPerCandle, canvas.widthProperty(),
                getXAxisFormatterForRange(xAxis.getUpperBound() - xAxis.getLowerBound()),
                candleData.get(0).getOpenTime());
        zoomLevelMap.put(0, currZoomLevel);
        xAxis.setTickLabelFormatter(currZoomLevel.getXAxisFormatter());
        putSlidingWindowExtrema(currZoomLevel.getExtremaForCandleRangeMap(), candleData,
                (int) Math.round(currZoomLevel.getNumVisibleCandles()));
        putExtremaForRemainingElements(currZoomLevel.getExtremaForCandleRangeMap(), candleData.subList(
                candleData.size() - (int) Math.floor(currZoomLevel.getNumVisibleCandles() - (liveSyncing ? 1 : 0)),
                candleData.size()));
        setYAndExtraAxisBounds();
        data.putAll(candleData.stream().collect(Collectors.toMap(CandleData::getOpenTime, Function.identity())));
        drawChartContents(false);
        progressIndicator.setVisible(false);
        updateInProgressCandleTask.setReady(true);
    }

    public void setAnimated(boolean b) {
        animated = b;
    }

    public boolean isAnimated() {
        return animated;
    }

    public List<Currency> getSymbols() {


        return  new CryptoCurrencyDataProvider().coinsToRegister;

    }

    public void setAutoTrading(boolean b) {
        autoTrading = b;
    }

    public boolean isAutoTrading() {
        return autoTrading;
    }

    public void setAreaChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Area Chart"));
        vBox.getChildren().add(new Button("Start"));
        AreaChart <Number, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setAnimated(animated);
        vBox.getChildren().add(areaChart);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Reset"));
        getChildren().add(vBox);
    }

    public void setVolumeChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Volume Chart"));
        vBox.getChildren().add(new Button("Start"));
        BarChart<Number, Number> volumeChart = new BarChart<>(xAxis, yAxis);
        volumeChart.setAnimated(animated);
        vBox.getChildren().add(volumeChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setBarChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Bar Chart"));
        vBox.getChildren().add(new Button("Start"));
        BarChart<Number, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(animated);
        vBox.getChildren().add(barChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setLineChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Line Chart"));
        vBox.getChildren().add(new Button("Start"));
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(animated);
        vBox.getChildren().add(lineChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setPieChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Pie Chart"));
        vBox.getChildren().add(new Button("Start"));
        ObservableList<PieChart.Data> data00=
                FXCollections.observableArrayList(
                        new PieChart.Data("BTC", 100),
                        new PieChart.Data("ETH", 100),
                        new PieChart.Data("LTC", 100),
                        new PieChart.Data("XRP", 100),
                        new PieChart.Data("BCH", 100),
                        new PieChart.Data("DASH", 100),
                        new PieChart.Data("EOS", 100),
                        new PieChart.Data("ETC", 100),
                        new PieChart.Data("XMR", 100),
                        new PieChart.Data("ZEC", 100)
                );

        PieChart pieChart = new PieChart(data00);
        pieChart.setAnimated(animated);
        vBox.getChildren().add(pieChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setScatterChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Scatter Chart"));
        vBox.getChildren().add(new Button("Start"));
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setAnimated(animated);
        vBox.getChildren().add(scatterChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setHistogramChart() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().add(new Label("Histogram Chart"));
        vBox.getChildren().add(new Button("Start"));
        HistogramChart<Number, Number> histogramChart = new HistogramChart<>(xAxis, yAxis);
        histogramChart.setAnimated(animated);
        vBox.getChildren().add(histogramChart);
        vBox.getChildren().add(progressIndicator);
        vBox.getChildren().add(new Button("Stop"));
        vBox.getChildren().add(new Button("Reset"));
        vBox.getChildren().add(new Label("Volume"));
        getChildren().add(vBox);
    }

    public void setCandlestickChart() {

        setPadding(new Insets(10, 10, 10, 10));getChildren().add(new Label("Candlestick Chart"));
        getChildren().add(new Button("Start"));

        getChildren().setAll(canvas);
    }

    public void getNews() throws ParseException {

        news = NewsManager.getNewsList();

        setPadding(new Insets(10, 10, 10, 10));

        Line newsLine = new Line();
        newsLine.setStroke(Color.BLUE);
        newsLine.setStrokeWidth(2);
        newsLine.setStrokeLineCap(StrokeLineCap.ROUND);
        newsLine.setStrokeLineJoin(StrokeLineJoin.ROUND);
        newsLine.setOpacity(0.5);
        newsLine.setRotate(90);
        newsLine.setTranslateX(10);
        newsLine.setTranslateY(10);
        newsLine.setStrokeType(StrokeType.INSIDE);
        newsLine.setStyle("-fx-stroke-width: 2; -fx-stroke-line-cap:");
        newsLine.setStyle("-fx-stroke-line-join:");

        newsLine.setStartY(10);
        newsLine.setEndX(canvas.getHeight()-10);
        newsLine.setEndY(10);

        for (News value : news) {
            Text text = new Text(value.getTitle());
            text.setFont(Font.font("Arial"));
            text.setFill(Color.WHITE);
            text.setTextAlignment(TextAlignment.CENTER);

            newsLine.setStartX(0);
            newsLine.setStartY(10);

            if (value.getImpact().equals("High")) {
                newsLine.setStroke(Color.RED);
                text.setFill(Color.RED);
            }
            if (value.getImpact().equals("Low")) {
                newsLine.setStroke(Color.GREEN);
                text.setFill(Color.GREEN);
            }
            if (value.getImpact().equals("Medium")) {
                newsLine.setStroke(Color.YELLOW);
                text.setFill(Color.YELLOW);
            }
            out.println(value.getImpact());
            getChildren().add(text);
            getChildren().setAll(newsLine);
        }


    }

    private class UpdateInProgressCandleTask implements LiveTradesConsumer, Runnable {
        private final BlockingQueue<Trade> liveTradesQueue;
        private boolean ready;

        UpdateInProgressCandleTask() {
            liveTradesQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void acceptTrades(List<Trade> trades) {
            liveTradesQueue.addAll(trades);
        }

        @Override
        public void onConnectionEstablished() {

            if (liveTradesQueue.isEmpty()) {
                return;
            }
            Trade trade = liveTradesQueue.poll();
            if (trade == null) {
                out.println("No live trades to process");
                return;
            }
            data.put((int) trade.getTimestamp().getEpochSecond(),candleDataSupplier.getCandleData().get(0));
        }

        @Override
        public void onConnectionFailed() {
            Platform.runLater(() -> progressIndicator.setVisible(false));
            assert updateInProgressCandleTask != null;
            updateInProgressCandleTask.setReady(true);

            liveTradesQueue.clear();

        }

        @Override
        public void onMessage(String message) {

            if (liveTradesQueue.isEmpty()) {
                out.println("No live trades to process " +message);
                return;
            }
            Trade trade = liveTradesQueue.poll();
            if (trade == null) {
                return;
            }
            data.put((int) trade.getTimestamp().getEpochSecond(), candleDataSupplier.getCandleData().get(0));
            drawChartContents(false);
            progressIndicator.setVisible(false);
            assert updateInProgressCandleTask != null;
            updateInProgressCandleTask.setReady(true);




        }

        @Override
        public void run() {
            if (inProgressCandle == null) {
                throw new RuntimeException("inProgressCandle was null in live syncing mode.");
            }
            if (!ready) {

                return;
            }

            int currentTill = (int) Instant.now().getEpochSecond();
            List<Trade> liveTrades = new ArrayList<>();
            liveTradesQueue.drainTo(liveTrades);

            // Get rid of trades we already know about
            List<Trade> newTrades = liveTrades.stream().filter(trade -> trade.getTimestamp().getEpochSecond() >
                    inProgressCandle.getCurrentTill()).toList();

            // Partition the trades between the current in-progress candle and the candle after that (which we may
            // have entered after last update).
            Map<Boolean, List<Trade>> candlePartitionedNewTrades = newTrades.stream().collect(
                    Collectors.partitioningBy(trade -> trade.getTimestamp().getEpochSecond() >=
                            inProgressCandle.getOpenTime() + secondsPerCandle));

            // Update the in-progress candle with new trades partitioned in the in-progress candle's duration
            List<Trade> currentCandleTrades = candlePartitionedNewTrades.get(false);

            if (!currentCandleTrades.isEmpty()) {
                inProgressCandle.setHighPriceSoFar(Math.max(currentCandleTrades.stream().mapToDouble(trade ->
                                trade.getPrice().toDouble()).max().getAsDouble(),
                        inProgressCandle.getHighPriceSoFar()));
                inProgressCandle.setLowPriceSoFar(Math.max(currentCandleTrades.stream().mapToDouble(trade ->
                                trade.getPrice().toDouble()).min().getAsDouble(),
                        inProgressCandle.getLowPriceSoFar()));
                inProgressCandle.setVolumeSoFar(inProgressCandle.getVolumeSoFar() +
                        currentCandleTrades.stream().mapToDouble(trade -> trade.getAmount().toDouble()).sum());
                inProgressCandle.setCurrentTill(currentTill);
                inProgressCandle.setLastPrice(currentCandleTrades.get(currentCandleTrades.size() - 1)
                        .getPrice().toDouble());
                data.put(inProgressCandle.getOpenTime(), inProgressCandle.snapshot());
            }

            List<Trade> nextCandleTrades = candlePartitionedNewTrades.get(true);
            if (Instant.now().getEpochSecond() >= inProgressCandle.getOpenTime() + secondsPerCandle) {
                // Reset in-progress candle
                inProgressCandle.setOpenTime(inProgressCandle.getOpenTime() + secondsPerCandle);
                inProgressCandle.setOpenPrice(inProgressCandle.getLastPrice());

                if (!nextCandleTrades.isEmpty()) {
                    inProgressCandle.setIsPlaceholder(false);
                    inProgressCandle.setHighPriceSoFar(nextCandleTrades.stream().mapToDouble(trade ->
                            trade.getPrice().toDouble()).max().getAsDouble());
                    inProgressCandle.setLowPriceSoFar(currentCandleTrades.stream().mapToDouble(trade ->
                            trade.getPrice().toDouble()).min().getAsDouble());
                    inProgressCandle.setVolumeSoFar(nextCandleTrades.stream().mapToDouble(trade ->
                            trade.getAmount().toDouble()).sum());
                    inProgressCandle.setLastPrice(nextCandleTrades.get(0).getPrice().toDouble());
                    inProgressCandle.setCurrentTill((int) nextCandleTrades.get(0).getTimestamp().getEpochSecond());
                } else {
                    inProgressCandle.setIsPlaceholder(true);
                    inProgressCandle.setHighPriceSoFar(inProgressCandle.getLastPrice());
                    inProgressCandle.setLowPriceSoFar(inProgressCandle.getLastPrice());
                    inProgressCandle.setVolumeSoFar(0);
                }

                data.put(inProgressCandle.getOpenTime(), inProgressCandle.snapshot());
            }

            drawChartContents(true);
        }

        public void setReady(boolean ready) {
            this.ready = ready;
        }
    }

    private class MouseDraggedHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            if (paging) {
                event.consume();
                return;
            }

            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (mousePrevX == -1 && mousePrevY == -1) {
                mousePrevX = event.getScreenX();
                mousePrevY = event.getScreenY();
                return;
            }

            double dx = event.getScreenX() - mousePrevX;

            scrollDeltaXSum += dx;

            if (Math.abs(scrollDeltaXSum) >= 10) {
                int deltaX = (int) -Math.signum(scrollDeltaXSum);
                moveAlongX(deltaX, false);
                scrollDeltaXSum = 0;
            }
            mousePrevX = event.getScreenX();
            mousePrevY = event.getScreenY();
        }
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            if (paging) {
                event.consume();
                return;
            }

            boolean consume = false;
            if (event.isControlDown() && event.getCode() == KeyCode.PLUS) {
                changeZoom(ZoomDirection.IN);
                consume = true;
            } else if (event.isControlDown() && event.getCode() == KeyCode.MINUS) {
                changeZoom(ZoomDirection.OUT);
                consume = true;
            }

            int deltaX = 0;
            if (event.getCode() == KeyCode.LEFT) {
                deltaX = -1;
                consume = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                deltaX = 1;
                consume = true;
            }

            if (deltaX != 0) {
                moveAlongX(deltaX, false);
            }

            if (consume) {
                event.consume();
            }
        }
    }

    private class ScrollEventHandler implements EventHandler<ScrollEvent> {
        @Override
        public void handle(ScrollEvent event) {
            if (paging) {
                event.consume();
                return;
            }

            if (event.getDeltaY() != 0 && event.getTouchCount() == 0 && !event.isInertia()) {
                final double direction = -Math.signum(event.getDeltaY());

                if (direction == 1.0d) {
                    changeZoom(ZoomDirection.OUT);
                } else if (direction == -1.0d) {
                    changeZoom(ZoomDirection.IN);
                }
            }
            event.consume();
        }
    }

    private class SizeChangeListener extends DelayedSizeChangeListener {
        SizeChangeListener(BooleanProperty gotFirstSize, ObservableValue<Number> containerWidth,
                           ObservableValue<Number> containerHeight) {
            super(750, 300, gotFirstSize, containerWidth, containerHeight);
        }

        @Override
        public void resize() {
            chartWidth = Math.max(300, Math.floor(containerWidth.getValue().doubleValue() / candleWidth) *
                    candleWidth - 60 + ((float) candleWidth / 2));
            chartHeight = Math.max(300, containerHeight.getValue().doubleValue());
            canvas.setWidth(chartWidth);
            canvas.setHeight(chartHeight - 2);

            // Because the chart has been resized, the number of visible candles has changed and thus we must
            // recompute the sliding window extrema where the size of the sliding window is the new number of
            // visible candles.
            int newLowerBoundX = (int) (xAxis.getUpperBound() - ((int) currZoomLevel.getNumVisibleCandles() *
                    secondsPerCandle));
            if (newLowerBoundX < currZoomLevel.getMinXValue()) {
                // We need to try and request more data so that we can properly resize the chart.
                paging = true;
                progressIndicator.setVisible(true);

                CompletableFuture.supplyAsync(candleDataPager.getCandleDataSupplier()).thenAccept(
                        candleDataPager.getCandleDataPreProcessor()).whenComplete((result, throwable) -> {
                    currZoomLevel.getExtremaForCandleRangeMap().clear();
                    List<CandleData> candleData = new ArrayList<>(data.values());
                    putSlidingWindowExtrema(currZoomLevel.getExtremaForCandleRangeMap(),
                            candleData, (int) Math.round(currZoomLevel.getNumVisibleCandles()));
                    putExtremaForRemainingElements(currZoomLevel.getExtremaForCandleRangeMap(),
                            candleData.subList(candleData.size() - (int) Math.floor(
                                    currZoomLevel.getNumVisibleCandles()), candleData.size()));
                    Platform.runLater(() -> {
                        xAxis.setLowerBound(newLowerBoundX);
                        setYAndExtraAxisBounds();
                        layoutChart();
                        drawChartContents(true);
                        progressIndicator.setVisible(false);
                        paging = false;
                    });
                });
            } else {
                currZoomLevel.getExtremaForCandleRangeMap().clear();
                List<CandleData> candleData = new ArrayList<>(data.values());
                putSlidingWindowExtrema(currZoomLevel.getExtremaForCandleRangeMap(),
                        candleData, (int) Math.round(currZoomLevel.getNumVisibleCandles()));
                putExtremaForRemainingElements(currZoomLevel.getExtremaForCandleRangeMap(),
                        candleData.subList(candleData.size() - (int) Math.floor(
                                currZoomLevel.getNumVisibleCandles()), candleData.size()));
                xAxis.setLowerBound(newLowerBoundX);
                setYAndExtraAxisBounds();
                layoutChart();
                drawChartContents(true);
            }
        }
    }

    private class CandlePageConsumer implements Consumer<List<CandleData>> {
        @Override
        public void accept(List<CandleData> candleData) {
            if (Platform.isFxApplicationThread()) {
                Log.error("candle data paging must not happen on FX thread!");
                throw new IllegalStateException("candle data paging must not happen on FX thread!");
            }

            if (candleData.isEmpty()) {
                Log.warn("candleData was empty");
                return;
            }

            if (candleData.get(0).getOpenTime() >= candleData.get(1).getOpenTime()) {
                Log.error("Paged candle data must be in ascending order by x-value");
                throw new IllegalArgumentException("Paged candle data must be in ascending order by x-value");
            }

            if (data.isEmpty()) {
                if (liveSyncing) {
                    if (inProgressCandle == null) {
                        throw new RuntimeException("inProgressCandle was null in live syncing mode.");
                    }
                    // We obtained the first page of candle data which does *not* include the current in-progress
                    // candle. Since we are live-syncing we need to fetch the data for what has occurred so far in
                    // the current candle.
                    long secondsIntoCurrentCandle = (Instant.now().toEpochMilli() / 1000L) -
                            (candleData.get(candleData.size() - 1).getOpenTime() + secondsPerCandle);
                    inProgressCandle.setOpenTime(candleData.get(candleData.size() - 1).getOpenTime() +
                            secondsPerCandle);


                    CompletableFuture<Optional<InProgressCandleData>> inProgressCandleDataOptionalFuture = null;
                    inProgressCandleDataOptionalFuture = exchange
                            .fetchCandleDataForInProgressCandle(tradePair, Instant.ofEpochSecond(
                                            candleData.get(candleData.size() - 1).getOpenTime() + secondsPerCandle),
                                    secondsIntoCurrentCandle, secondsPerCandle);
                    inProgressCandleDataOptionalFuture.whenComplete((inProgressCandleDataOptional, throwable) -> {
                        if (throwable == null) {
                            if (inProgressCandleDataOptional.isPresent()) {
                                InProgressCandleData inProgressCandleData = inProgressCandleDataOptional.get();

                                int currentTill = (int) Instant.now().getEpochSecond();
                                CompletableFuture<List<Trade>> tradesFuture = exchange.fetchRecentTradesUntil(
                                        tradePair, Instant.ofEpochSecond(inProgressCandleData.currentTill()));

                                tradesFuture.whenComplete((trades, exception) -> {
                                    if (exception == null) {
                                        inProgressCandle.setOpenPrice(inProgressCandleData.openPrice());
                                        inProgressCandle.setCurrentTill(currentTill);

                                        if (trades.isEmpty()) {
                                            // No trading activity happened in addition to the sub-candles from above.
                                            inProgressCandle.setHighPriceSoFar(
                                                    inProgressCandleData.highPriceSoFar());
                                            inProgressCandle.setLowPriceSoFar(inProgressCandleData.lowPriceSoFar());
                                            inProgressCandle.setVolumeSoFar(inProgressCandleData.volumeSoFar());
                                            inProgressCandle.setLastPrice(inProgressCandleData.lastPrice());
                                        } else {
                                            // We need to factor in the trades that have happened after the
                                            // "currentTill" time of the in-progress candle.
                                            inProgressCandle.setHighPriceSoFar(Math.max(trades.stream().mapToDouble(
                                                            trade -> trade.getPrice().toDouble()).max().getAsDouble(),
                                                    inProgressCandleData.highPriceSoFar()));
                                            inProgressCandle.setLowPriceSoFar(Math.max(trades.stream().mapToDouble(
                                                            trade -> trade.getPrice().toDouble()).min().getAsDouble(),
                                                    inProgressCandleData.lowPriceSoFar()));
                                            inProgressCandle.setVolumeSoFar(inProgressCandleData.volumeSoFar() +
                                                    trades.stream().mapToDouble(
                                                            trade -> trade.getAmount().toDouble()).sum());
                                            inProgressCandle.setLastPrice(trades.get(trades.size() - 1).getPrice()
                                                    .toDouble());
                                        }
                                        Platform.runLater(() -> setInitialState(candleData));
                                    } else {
                                        Log.error("error fetching recent trades until: " +
                                                inProgressCandleData.currentTill() + "\n" + exception);
                                    }
                                });
                            } else {
                                // No trades have happened during the current candle so far.
                                inProgressCandle.setIsPlaceholder(true);
                                inProgressCandle.setVolumeSoFar(0);
                                inProgressCandle.setCurrentTill((int) (secondsIntoCurrentCandle +
                                        (candleData.get(candleData.size() - 1).getOpenTime() + secondsPerCandle)));
                                Platform.runLater(() -> setInitialState(candleData));
                            }
                        } else {
                            Log.error("error fetching in-progress candle data: " + throwable);
                        }
                    });
                } else {
                    setInitialState(candleData);
                }
            } else {
                int slidingWindowSize = (int) currZoomLevel.getNumVisibleCandles();

                // In order to compute the y-axis extrema for the new data in the page, we have to include the
                // first numVisibleCandles from the previous page (otherwise the sliding window will not be able
                // to reach all the way).
                Map<Integer, CandleData> extremaData = new TreeMap<>(data.subMap(currZoomLevel.getMinXValue(),
                        currZoomLevel.getMinXValue() + (int) (currZoomLevel.getNumVisibleCandles() *
                                secondsPerCandle)));
                List<CandleData> newDataPlusOffset = new ArrayList<>(candleData);
                newDataPlusOffset.addAll(extremaData.values());
                putSlidingWindowExtrema(currZoomLevel.getExtremaForCandleRangeMap(), newDataPlusOffset,
                        slidingWindowSize);
                data.putAll(candleData.stream().collect(Collectors.toMap(CandleData::getOpenTime,
                        Function.identity())));
                currZoomLevel.setMinXValue(candleData.get(0).getOpenTime());
            }
        }

    }
}