package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;


public class TradeView extends Region  {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);

    public TradeView(@NotNull Exchange exchange, String telegramToken) throws IOException, InterruptedException, ParseException, URISyntaxException {

        super();
        TabPane tradingTabPane = new TabPane();
        tradingTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tradingTabPane.setSide(Side.TOP);
        tradingTabPane.setPadding(new Insets(10));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        AnchorPane anchorPane = new AnchorPane();
        logger.debug("Creating TradeView");

        ChoiceBox<String> symbolChoicebox = new ChoiceBox<>();

        logger.info(CurrencyDataProvider.getInstance(exchange).toString());

        try {
            symbolChoicebox.getItems().addAll(exchange.getTradePair());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        final double[] price = new double[1];
        double quantity = 1000;
        long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
        double stopPrice = 100;
        double takeProfitPrice = 100;


        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(
                event -> tradingTabPane.getTabs().remove(tradingTabPane.getSelectionModel().getSelectedItem())
        );



        Button AddBtn = new Button("LOAD NEW CHART");





        AddBtn.setOnAction(
                event -> {

                    String symbol = symbolChoicebox.getSelectionModel().getSelectedItem();
                    logger.debug(symbol);
                    String sym1 = symbol.split("/")[0];
                    String sym2 = symbol.split("/")[1];

                    logger.debug(sym1 + sym2);
                    DraggableTab tradeTab2 = new DraggableTab(sym1 + "/" + sym2, "");
                    CandleStickChartContainer container2;
                    try {
                        TradePair tradePair3 = new TradePair(
                                sym1, sym2
                        );
                        Exchange.tradePair = tradePair3;
                        container2 = new CandleStickChartContainer(exchange, tradePair3, telegramToken, true
                        );

                        tradeTab2.setContent(container2);
                        tradingTabPane.getTabs().add(tradeTab2);
                        tradingTabPane.getSelectionModel().select(tradeTab2);
                    } catch (URISyntaxException | IOException | SQLException e) {
                        throw new RuntimeException(e);
                    }
                });


        Button tradingBtn = new Button("TRADING BUTTONS");
        tradingBtn.setOnAction(
                event -> {


                    GridPane gridPane = new GridPane();
                    gridPane.setHgap(20);


                    gridPane.add(
                            new Label(exchange.getName()), 0, 0
                    );
                    gridPane.setVgap(20);
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    Spinner<Double> spinner = new Spinner<>(0.01, 100000, 0);
                    spinner.setEditable(true);
                    spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 100000, 0));

                    gridPane.add(spinner, 1, 1);
                    Button btnBuy = new Button("BUY");


                    btnBuy.setOnAction(
                            event1 -> {
                                try {
                                    exchange.createOrder(
                                            new TradePair(
                                                    symbolChoicebox.getValue().split("/")[0],
                                                    symbolChoicebox.getValue().split("/")[1]
                                            ),
                                            cryptoinvestor.cryptoinvestor.Side.BUY,

                                            ENUM_ORDER_TYPE.MARKET, price[0],

                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID


                                    );
                                } catch (IOException | InterruptedException | SQLException e) {
                                    throw new RuntimeException(e);
                                }


                            }
                    );


                    gridPane.add(btnBuy, 0, 1);
                    Button btnSell = new Button("SELL");
                    btnSell.setOnAction(
                            event1 -> {
                                try {
                                    exchange.createOrder(
                                            new TradePair(
                                                    symbolChoicebox.getValue().split("/")[0],
                                                    symbolChoicebox.getValue().split("/")[1]
                                            ),
                                            cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.MARKET, price[0],
                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID


                                    );
                                } catch (IOException | InterruptedException | SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );


                    gridPane.add(btnSell, 1, 2);

                    Button closeAll = new Button("CLOSE ALL");
                    closeAll.setOnAction(event1 -> {
                        try {
                            exchange.closeAllOrders();
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    gridPane.add(closeAll, 3, 2);
                    Button trailingBuy = new Button("TRAILING BUY");
                    Button trailingSell = new Button("TRAILING SELL");
                    trailingSell.setOnAction(event2 -> {

                        try {
                            exchange.createOrder(
                                    new TradePair(
                                            symbolChoicebox.getValue().split("/")[0],
                                            symbolChoicebox.getValue().split("/")[1]


                                    ),
                                    cryptoinvestor.cryptoinvestor.Side.BUY,

                                    ENUM_ORDER_TYPE.TRAILING_STOP_SELL, price[0],

                                    quantity,
                                    new Date(), stopPrice,
                                    takeProfitPrice,
                                    orderID

                            );
                        } catch (IOException | InterruptedException | SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });


                    trailingBuy.setOnAction(event2 -> {

                        try {
                            exchange.createOrder(
                                    new TradePair(
                                            symbolChoicebox.getValue().split("/")[0],
                                            symbolChoicebox.getValue().split("/")[1]
                                    ),
                                    cryptoinvestor.cryptoinvestor.Side.BUY,

                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY, price[0],

                                    quantity,
                                    new Date(), stopPrice,
                                    takeProfitPrice,
                                    orderID


                            );
                        } catch (IOException | InterruptedException | SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });


                    gridPane.add(trailingBuy, 4, 1);
                    gridPane.add(trailingSell, 4, 2);

                    Button buyStopBtn = new Button("BUY STOP");
                    Button sellStopBtn = new Button("SELL STOP");
                    Button sellCancelBtn = new Button("SELL CANCEL");
                    Button buyCancelBtn = new Button("BUY CANCEL");
                    Button cancelAllBtn = new Button("CANCEL ALL");
                    gridPane.add(buyStopBtn, 0, 7);
                    gridPane.add(sellStopBtn, 2, 7);
                    gridPane.add(sellCancelBtn, 2, 8);
                    gridPane.add(buyCancelBtn, 0, 9);
                    gridPane.add(cancelAllBtn, 3, 9);


                    buyStopBtn.setOnAction(
                            event3 -> {
                                try {
                                    exchange.createOrder(
                                            new TradePair(
                                                    symbolChoicebox.getValue().split("/")[0],
                                                    symbolChoicebox.getValue().split("/")[1]),
                                            cryptoinvestor.cryptoinvestor.Side.BUY,

                                            ENUM_ORDER_TYPE.STOP_LOSS, price[0],

                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID
                                    );
                                } catch (IOException | InterruptedException | SQLException e) {
                                    throw new RuntimeException(e);
                                }
                                sellStopBtn.setOnAction(
                                        event4 -> {
                                            try {
                                                exchange.createOrder(
                                                        new TradePair(
                                                                symbolChoicebox.getValue().split("/")[0],
                                                                symbolChoicebox.getValue().split("/")[1]
                                                        ),
                                                        cryptoinvestor.cryptoinvestor.Side.SELL,

                                                        ENUM_ORDER_TYPE.STOP_LOSS, price[0],

                                                        quantity,
                                                        new Date(), stopPrice,
                                                        takeProfitPrice,
                                                        orderID


                                                );
                                            } catch (IOException | InterruptedException | SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                sellCancelBtn.setOnAction(
                                        event5 -> {


                                            try {
                                                exchange.cancelOrder(orderID);
                                            } catch (IOException | InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                buyCancelBtn.setOnAction(
                                        event6 -> {
                                            try {
                                                exchange.cancelOrder(orderID);
                                            } catch (IOException | InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });


                            });


                    Stage stage = new Stage();


                    stage.setTitle("Cryptoinvestor");
                    stage.setScene(new Scene(gridPane, 800, 300));
                    stage.setAlwaysOnTop(true);
                    stage.show();
                });

        Button orderViewBtn = new Button("Order View");

        orderViewBtn.setOnAction(
                event3 -> {
                    TreeTableView<Trade> orders = new TreeTableView<>();
                    orders.setEditable(true);
                    Scene scene = new Scene(orders, 1200, 300);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();
                });
        Button walletBtn = new Button(" WALLET  -- ");
        walletBtn.setOnAction(event7 -> new Wallet(exchange));

        symbolChoicebox.setValue(
                "SELECT A SYMBOL"
        );

        Button orderHistoryBtn =
                new Button("ORDERS -- ");
        orderHistoryBtn.setOnAction(
                event9 ->
                {
                    try {
                        new OrdersDisplay(exchange);
                    } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
        );


        anchorPane.setPrefSize(1230, 780);
        tabPane.setPrefSize(1530, 630);
        tabPane.setTranslateY(25);
        tabPane.setSide(Side.BOTTOM);
        tabPane.getTabs().add(new Tab("ORDERS VIEW"));
        tabPane.getTabs().get(0).setContent(new VBox(

        ));


        tabPane.getTabs().add(new Tab(exchange.getName() + " -->  WALLET"));
        tabPane.getTabs().get(1).setContent(new VBox(new Separator(Orientation.HORIZONTAL), new VBox(new ListView<>(exchange.getAccounts()))));

        tabPane.getTabs().add(new Tab("Stellar Network  Trading (XLM)"));

        tabPane.getTabs().get(2).setContent(new VBox(new Label("STELLAR LUMEN 'S "),
                new VBox(new ListView<>(exchange.getAccounts()))));

        tabPane.getTabs().add(new Tab("TRADING VIEW"));
        tabPane.getSelectionModel().select(3);
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            tabPane.getTabs().get(i).setContent(tradingTabPane);
        }
        for (int i = 0; i < tradingTabPane.getTabs().size(); i++) {
            if (exchange instanceof Oanda oanda) {

                try {
                    tradingTabPane.getTabs().get(i).setContent(
                            new CandleStickChartContainer(oanda, new TradePair(symbolChoicebox.getValue().split("/")[0],
                                    symbolChoicebox.getValue().split("/")[1]
                            ), telegramToken, true)
                    );
                } catch (URISyntaxException | IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    tradingTabPane.getTabs().get(i).setContent(new CandleStickChartContainer(exchange, new TradePair(

                            symbolChoicebox.getValue().split("/")[0],
                            symbolChoicebox.getValue().split("/")[1]
                    ), telegramToken, true));
                } catch (URISyntaxException | IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        Button connexionBtn = new Button("CONNECTION");
        connexionBtn.setOnAction(
                event8 -> new ConnectionScene(exchange));
        Button allOrdersBtn = new Button("ALL ORDERS");
        Button sendScreenShotBtn = new Button("SEND SCREENSHOT");
        sendScreenShotBtn.setOnAction(event -> {

            try {
                try {
                    TelegramClient telegramClient = new TelegramClient(telegramToken);
                    File fil = File.createTempFile(
                            exchange.getName(), "png"
                    );
                    Screenshot.capture(fil);
                    telegramClient.sendPhoto(fil);
                } catch (TelegramApiException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        allOrdersBtn.setOnAction(event -> {
            try {


                new OrdersDisplay(exchange);

            } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        Button tradeStrategyBtn = new Button("STRATEGY TESTER");
        tradeStrategyBtn.setOnAction(event -> new StrategyTester(exchange));
        Button navigationBtn = new Button("NAVIGATION");
        navigationBtn.setOnAction(event -> new Navigator(exchange));
        HBox hBox = new HBox(removeBtn, AddBtn, tradingBtn, new HBox(symbolChoicebox), orderHistoryBtn,
                connexionBtn, walletBtn,
                orderViewBtn,
                allOrdersBtn,
                sendScreenShotBtn, tradeStrategyBtn, navigationBtn);

        setPadding(new Insets(10, 10, 10, 10));

        hBox.setPrefSize(1500, 20);
        anchorPane.getChildren().addAll(hBox, tabPane);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);

        setPadding(new Insets(10, 10, 10, 10));
        getChildren().add(anchorPane);
        setPadding(new Insets(10, 10, 10, 10));


    }


}