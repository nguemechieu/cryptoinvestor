package cryptoinvestor.cryptoinvestor;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

public class TradeView extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);


    private TradePair tradePair=
            new TradePair(
                    "ETH",
                    "BTC"
            );

    private Exchange exchange;

    public TradeView(Exchange exchange) throws URISyntaxException, IOException {

        super();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        AnchorPane anchorPane = new AnchorPane();
        logger.debug(
                "Creating TradeView"
        );


        ChoiceBox<String> symbolChoicebox =
                new ChoiceBox<>();
        TradePair tradePair = new TradePair("USD","CAD");
        ChoiceBox<String> counterChoicebox = new ChoiceBox<>();
        for (Currency symbol : CurrencyDataProvider.getInstance()) {

            if (exchange.getName().equals("OANDA")) {
                symbolChoicebox.getItems().addAll(
                        "USD",
                        "EUR",
                        "GBP",
                        "CAD",
                        "AUD",
                        "NZD",
                        "CHF",
                        "JPY",
                        "CNY",
                        "SGD",
                        "HKD",
                        "INR",
                        "IDR",
                        "MYR",
                        "THB",
                        "TRY",
                        "DKK",
                        "SEK",
                        "NOK",
                        "ZAR",
                        "MXN"
                );
                symbolChoicebox.setValue("USD");
                counterChoicebox.getItems().addAll(
                        "USD",
                        "EUR",
                        "GBP",
                        "CAD",
                        "AUD",
                        "NZD",
                        "CHF",
                        "JPY",
                        "CNY",
                        "SGD",
                        "HKD",
                        "INR",
                        "IDR",
                        "MYR",
                        "THB",
                        "TRY",
                        "DKK",
                        "SEK",
                        "NOK",
                        "ZAR",
                        "MXN"
                );

break;

            } else {

                if (symbol.currencyType.equals(CurrencyType.CRYPTO)) {
                symbolChoicebox.getItems().add(
                        symbol.code
                );
                }
            }


        }
        symbolChoicebox.setValue("SELECT A BASE CURRENCY");
        counterChoicebox.setValue("USD");

        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(
                event -> tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem())
        );

        Button AddBtn = new Button("Load new chart");
        AddBtn.setOnAction(
                event -> {



                    String baseCurrency = symbolChoicebox.getValue();
                    String counterCurrency = counterChoicebox.getValue();
                    if (baseCurrency.equals("SELECT A BASE CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a base currency");
                        alert.showAndWait();
                        return;
                    }
                    if (counterCurrency.equals("SELECT A COUNTER CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a counter currency");
                        alert.showAndWait();

                        return;
                    }

                    DraggableTab tradeTab2 = new DraggableTab(
                            baseCurrency + " / " + counterCurrency
                            , "");
                    CandleStickChartContainer container2;
                    try {
                        TradePair tradePair3 = new TradePair(baseCurrency, counterCurrency);
                        container2 = new CandleStickChartContainer(exchange, tradePair3, true
                        );
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    tradeTab2.setContent(
                            container2
                    );
                    tabPane.getTabs().add(tradeTab2);
                    tabPane.getSelectionModel().select(tradeTab2);

                });
        Button tradingBtn = new Button("Trade Buttons");
        TradePair finalTradePair2 = tradePair;
        tradingBtn.setOnAction(
                event -> {
                    String baseCurrency = symbolChoicebox.getValue();
                    String counterCurrency = counterChoicebox.getValue();
                    if (baseCurrency.equals("SELECT A BASE CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a base currency");
                        alert.showAndWait();
                        return;
                    }
                    if (counterCurrency.equals("SELECT A COUNTER CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a counter currency");
                        alert.showAndWait();

                        return;
                    }
                    StackPane stackPane = new StackPane();
                    stackPane.setPrefSize(350, 230);
                    GridPane gridPane = new GridPane();
                    gridPane.setPrefSize(350, 230);
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    Spinner<Double> spinner = new Spinner<>(0.01, 100000, 0);
                    gridPane.add(spinner, 1, 0);
                    Button btnBuy = new Button("Buy");
                    gridPane.add(btnBuy, 0, 1);
                    Button btnSell = new Button("Sell");
                    gridPane.add(btnSell, 1, 1);

                    Button closeAll = new Button("Close All");
                    closeAll.setOnAction(event1 -> exchange.CloseAll());
                    gridPane.add(
                            closeAll ,3,0

                    );
                    Button trailingBuy=new Button("Trailing Buy");
                    Button trailingSell=new Button("Trailing Sell");
                    trailingSell.setOnAction(
                            event2 ->

                                {
                                    double quantity = spinner.getValue();
                                    long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                    double stopPrice = 100;
                                    double takeProfitPrice = 100;
                                    @NotNull Instant timestamp=
                                            Instant.now();
                                    double price=0;
                                    try {
                                        exchange.createOrder(
                                                finalTradePair2, cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice

                                        );
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        exchange.createOrder(
                                                finalTradePair2, cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                });
                    gridPane.add(trailingBuy, 4,0);
                    gridPane.add(trailingSell, 4,1);
                    stackPane.getChildren().add(gridPane);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(stackPane));
                    stage.show();

                });
        Button orderViewBtn=new Button("Order View");

        orderViewBtn.setOnAction(
                event -> {
                    String baseCurrency = symbolChoicebox.getValue();
                    String counterCurrency = counterChoicebox.getValue();
                    if (baseCurrency.equals("SELECT A BASE CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a base currency");
                        alert.showAndWait();
                        return;
                    }
                    if (counterCurrency.equals("SELECT A COUNTER CURRENCY")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");

                        alert.setHeaderText(null);
                        alert.setContentText("Please select a counter currency");
                        alert.showAndWait();

                        return;
                    }
                    TreeTableView<Trade> orders = new TreeTableView<>();
                    TreeItem<Trade> root = new TreeItem<>();
                    root.setExpanded(true);

                    root.setValue(
                            new Trade(

                            )
                    );
                    root.getChildren().add(exchange.getTradesList());
                    orders.setRoot(root);

                    TreeTableColumn <Trade, String> symbolColumn = new TreeTableColumn<>("Symbol");
//                    symbolColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(finalTradePair1.getCounterCurrency().code) //new ReadOnlyStringWrapper(param.getValue().getValue().order.symbol)
//                    );
                    TreeTableColumn <Trade, String> priceColumn = new TreeTableColumn<>("Price");
//                    priceColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getPrice())));
//
                    TreeTableColumn <Trade, String> amountColumn = new TreeTableColumn<>("Amount");
//                    amountColumn.setCellValueFactory(
//                            param ->new ReadOnlyStringWrapper( String.valueOf(
//                                    param.getValue().getValue().order.getLotSize()))
//                    );
                    TreeTableColumn <Trade, String> sideColumn = new TreeTableColumn<>("Side");
//                    sideColumn.setCellValueFactory(
//                            param ->   new ReadOnlyStringWrapper(String.valueOf(
//                                    param.getValue().getValue().order.getSide()))
//                    );
                    TreeTableColumn <Trade, String> typeColumn = new TreeTableColumn<>("Type");
//                    typeColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(String.valueOf(
//                                    param.getValue().getValue().order.getType())));

                    TreeTableColumn <Trade, String> timeColumn = new TreeTableColumn<>("Time");
//                    timeColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().order.getTime().toString())
//                    );
                    TreeTableColumn <Trade, String> idColumn = new TreeTableColumn<>("ID");
                    /*idColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().order.getId())
                    );*/
                    TreeTableColumn <Trade, String> orderIdColumn = new TreeTableColumn<>("Order ID");
//                    orderIdColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().order.getOrderId())
//                    );

                    TreeTableColumn <Trade, String> statusColumn = new TreeTableColumn<>("Status");
//                    statusColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().order.getStatus())
//                    );


                    TreeTableColumn <Trade, String> filledColumn = new TreeTableColumn<>("Filled");
//                    filledColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(String.valueOf(
//                                    param.getValue().getValue().order.getFilled()))
//                    );


                    TreeTableColumn <Trade, String> remainingColumn = new TreeTableColumn<>("Remaining");
//                    remainingColumn.setCellValueFactory(
//                            param -> new ReadOnlyStringWrapper(String.valueOf(
//                                    param.getValue().getValue().getRemaining()))
//                    );



                    orders.getColumns().addAll(symbolColumn, priceColumn, amountColumn, sideColumn,
                    typeColumn, timeColumn, idColumn, orderIdColumn, statusColumn, filledColumn, remainingColumn);
                    Scene scene = new Scene(orders,1000,200);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();
                });
        HBox hBox = new HBox(symbolChoicebox, counterChoicebox, removeBtn, AddBtn, tradingBtn, orderViewBtn);
        setPadding(new Insets(10, 10, 10, 10));


        anchorPane.setPrefSize(1230, 630);
        tabPane.setPrefSize(1530, 630);
        tabPane.setTranslateY(25);
        tabPane.setSide(Side.BOTTOM);
        DraggableTab tradeTab = new DraggableTab(
                symbolChoicebox.getValue() + " " + counterChoicebox.getValue()
                , "");
        tradePair = new TradePair("BTC", "USD");
        CandleStickChartContainer container =
                new CandleStickChartContainer(exchange, tradePair, true
                );

        anchorPane.setPadding(new Insets(10, 10, 10, 10));
        container.setPrefSize(1000, 600);
        tradeTab.setContent(container);

        tabPane.getTabs().add(tradeTab);
        anchorPane.getChildren().addAll(hBox, tabPane);


        getChildren().add(anchorPane);

    }



    public TradePair getTradePair() {
        return tradePair;
    }

    public void setTradePair(TradePair tradePair) {
        this.tradePair = tradePair;
    }



    public double getPrice() {

        return 0;
    }

    public double getVolume() {
        return 0;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }
    // Constructor


}
