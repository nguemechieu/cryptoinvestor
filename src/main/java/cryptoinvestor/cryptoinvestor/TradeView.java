package cryptoinvestor.cryptoinvestor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;


public class TradeView extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);


    private TradePair tradePair;

    private Exchange exchange;

    public TradeView(Exchange exchange) throws URISyntaxException, IOException, InterruptedException {

        super();
//
//        28,326.39 USD
//        Last trade price
//                +4.06%
//                24h price
//        26,047 BTC
//        24h volume
//        Discord discord = new Discord(
//
//                "https://discordapp.com/api/oauth2/authorize?client_id=80000000000000000&permissions=8&scope=bot",
//        );


        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        AnchorPane anchorPane = new AnchorPane();
        logger.debug(
                "Creating TradeView"
        );


        ChoiceBox<String> symbolChoicebox =
                new ChoiceBox<>();


        tradePair = new TradePair("EUR", "GBP");

        ChoiceBox<String> counterChoicebox = new ChoiceBox<>();


        symbolChoicebox.getItems().addAll(
                "USD",
                "EUR",
                "GBP","AUD",
                "CAD",
                "CHF",
                "CNY",
                "DKK",
                "EUR",
                "HKD",
                "HUF",
                "IDR",
                "ILS",
                "INR",
                "JPY",
                "KRW",
                "MXN",
                "MYR",
                "NOK",
                "NZD",
                "PHP",
                "PLN",
                "RUB",
                "SEK",
                "SGD",
                "THB",
                "TRY",
                "USD",
                "ZAR"


        );
        symbolChoicebox.setValue("USD");
        counterChoicebox.getItems().addAll(
                "USD",
                "EUR",
                "GBP","AUD",
                "CAD",
                "CHF",
                "CNY",
                "DKK",
                "EUR",
                "HKD",
                "HUF",
                "IDR",
                "ILS",
                "INR",
                "JPY",
                "KRW",
                "MXN",
                "MYR",
                "NOK",
                "NZD",
                "PHP",
                "PLN",
                "RUB",
                "SEK",
                "SGD",
                "THB",
                "TRY",
                "USD",
                "ZAR"


        );

                for (Currency symbol : CurrencyDataProvider.getInstance()) {




                    if (symbol.currencyType.equals(CurrencyType.CRYPTO)) {




                        symbolChoicebox.getItems().addAll(
                                symbol.code
                        );

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
                    stackPane.setPrefSize(390, 230);
                    GridPane gridPane = new GridPane();
                    gridPane.setPrefSize(350, 230);
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    Spinner<Double> spinner = new Spinner<>(0.01, 100000, 0);
                    gridPane.add(spinner, 1, 0);
                    Button btnBuy = new Button("Buy");
                    btnBuy.setOnAction(
                            event1 -> {
                                double quantity = spinner.getValue();
                                long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                double stopPrice = 100;
                                double takeProfitPrice = 100;
                                @NotNull Instant timestamp=
                                        Instant.now();
                                double price= 0;
                                try {
                                    price = exchange.getPrice(tradePair);
                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    exchange.createOrder(
                                            finalTradePair2, cryptoinvestor.cryptoinvestor.Side.BUY,
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

                            }
                    );


                    gridPane.add(btnBuy, 0, 1);
                    Button btnSell = new Button("Sell");
                    gridPane.add(btnSell, 1, 1);

                    Button closeAll = new Button("Close All");
                    closeAll.setOnAction(event1 -> {
                        try {
                            exchange.CloseAll();
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    gridPane.add(closeAll ,3,0);
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
                   symbolColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(tradePair.getCounterCurrency().code) //new ReadOnlyStringWrapper(param.getValue().getValue().order.symbol)
                    );
                    TreeTableColumn <Trade, String> priceColumn = new TreeTableColumn<>("Price");
                  //  priceColumn.setCellValueFactory(
                       //     param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getPrice())));

                    TreeTableColumn <Trade, String> amountColumn = new TreeTableColumn<>("Amount");
                   amountColumn.setCellValueFactory(
                            param ->new ReadOnlyStringWrapper( String.valueOf(
                                    param.getValue().getValue().size))
                    );
                    TreeTableColumn <Trade, String> sideColumn = new TreeTableColumn<>("Side");
                  sideColumn.setCellValueFactory(
                            param ->   new ReadOnlyStringWrapper(String.valueOf(
                                   param.getValue().getValue().side))
                    );
                    TreeTableColumn <Trade, String> typeColumn = new TreeTableColumn<>("Type");
                   typeColumn.setCellValueFactory(
                           param -> new ReadOnlyStringWrapper(String.valueOf(
                                    param.getValue().getValue().order_type)));

                    TreeTableColumn <Trade, String> timeColumn = new TreeTableColumn<>("Time");
                  timeColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().timestamp.toString())
                    );
                    TreeTableColumn <Trade, String> idColumn = new TreeTableColumn<>("ID");
                    idColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getId()))
                    );
                    TreeTableColumn <Trade, String> orderIdColumn = new TreeTableColumn<>("Order ID");
                 orderIdColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().getId().toString())
                    );

                    TreeTableColumn <Trade, String> statusColumn = new TreeTableColumn<>("Status");
                   statusColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString())
                    );


                    TreeTableColumn <Trade, String> filledColumn = new TreeTableColumn<>("Filled");
                   filledColumn.setCellValueFactory(
                            param -> new ReadOnlyStringWrapper(String.valueOf(
                                    param.getValue().getValue().state))
                    );


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
        Button walletBtn = new Button(" Wallet  -- ");
        walletBtn.setOnAction(
                event -> {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Wallet(exchange)));
                    stage.show();
                });


        HBox hBox = new HBox(symbolChoicebox, counterChoicebox, removeBtn, AddBtn, tradingBtn, orderViewBtn,walletBtn);
        setPadding(new Insets(10, 10, 10, 10));

        hBox.setPrefSize(1500,20);

        anchorPane.setPrefSize(1230, 630);
        tabPane.setPrefSize(1530, 630);
        tabPane.setTranslateY(25);
        tabPane.setSide(Side.BOTTOM);


        tradePair = new TradePair("BTC", "USD");
        CandleStickChartContainer container =
                new CandleStickChartContainer(exchange, tradePair, true
                );

        anchorPane.setPadding(new Insets(10, 10, 10, 10));
        container.setPrefSize(1000, 600);
        String baseCurrency="BTC";
        String counterCurrency="USD";

        if (exchange instanceof Oanda) {baseCurrency = "EUR"; counterCurrency="USD";}
        DraggableTab tradeTab = new DraggableTab(
                baseCurrency + "/" + counterCurrency
                , "");


        tradeTab.setContent(new
                CandleStickChartContainer(exchange, new TradePair(baseCurrency,counterCurrency), true));

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
