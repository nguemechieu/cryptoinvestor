package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;


public class TradeView extends Region  {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);

    public TradeView(@NotNull Exchange exchange, String telegramToken) throws IOException, InterruptedException {

        super();
        TabPane tradingTabPane = new TabPane();
        tradingTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tradingTabPane.setSide(Side.TOP);

//
//        28,326.39 USD
//        Last trade price
//                +4.06%
//                24h price
//        26,047 BTC
//        24h volume
//        Discord = new Discord(
//
//                "https://discordapp.com/api/oauth2/authorize?client_id=80000000000000000&permissions=8&scope=bot",
//        );
        //  Discord discord = new Discord("MTA4NzIxMDExOTA5NzQ5OTc1MQ.GTPtoi.IoKi82j9vnTZAe1VG8LHO60aFUGJzgfYG5blYo");


        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        AnchorPane anchorPane = new AnchorPane();
        logger.debug("Creating TradeView");
        ChoiceBox<String> symbolChoicebox = new ChoiceBox<>();
        ChoiceBox<String> counterChoicebox = new ChoiceBox<>();
        try {
//
            if (exchange instanceof Oanda) {
                symbolChoicebox.getItems().addAll(exchange.getAvailableSymbols().stream().map(Currency::getCode).toList());
            } else {
                symbolChoicebox.getItems().addAll(CurrencyDataProvider.getInstance().stream().map(Currency::getCode).toList());
                counterChoicebox.getItems().addAll("USD", "BTC", "USDT", "CAD");
                counterChoicebox.setValue("USD");

            }


            logger.debug("SYMBOLS--> " + symbolChoicebox.getItems().toString());
        } catch (Exception e) {
            e.printStackTrace();
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

        counterChoicebox.getItems().addAll("USD", "BTC", "USDT", "CAD");

        Button AddBtn = new Button("LOAD NEW CHART");

        String data1 = "EUR_USD";
symbolChoicebox.setValue(data1);

        final String[][] dat = new String[1][1];


        AddBtn.setOnAction(
                event -> {
                    if (exchange instanceof Oanda) {
                        price[0] = 0.3;
                        dat[0] =symbolChoicebox.getValue().split("_");

                    } else {
                        dat[0] = "EUR_USD".split("_");
                        price[0] = 0;
                        dat[0][0] = symbolChoicebox.getValue();
                        dat[0][1] = counterChoicebox.getValue();

                    }
                    DraggableTab tradeTab2 = new DraggableTab(dat[0][0] + " / " + dat[0][1], "");
                    CandleStickChartContainer container2;
                    try {
                        TradePair tradePair3 = new TradePair(dat[0][0], dat[0][1]);
                        exchange.tradePair = tradePair3;
                        container2 = new CandleStickChartContainer(exchange, tradePair3, telegramToken, true
                        );

                        tradeTab2.setContent(container2);
                        tradingTabPane.getTabs().add(tradeTab2);
                        tradingTabPane.getSelectionModel().select(tradeTab2);
                    } catch (URISyntaxException | IOException e) {
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
                                            new TradePair(dat[0][0], dat[0][1]
                                            ),
                                            cryptoinvestor.cryptoinvestor.Side.BUY,

                                            ENUM_ORDER_TYPE.MARKET, price[0],

                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID


                                    );
                                } catch (IOException | InterruptedException e) {
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
                                            new TradePair(dat[0][0], dat[0][1]
                                            ),
                                            cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.MARKET, price[0],
                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID


                                    );
                                } catch (IOException | InterruptedException  e) {
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
                                    new TradePair(dat[0][0], dat[0][1]
                                    ),
                                    cryptoinvestor.cryptoinvestor.Side.BUY,

                                    ENUM_ORDER_TYPE.TRAILING_STOP_SELL, price[0],

                                    quantity,
                                    new Date(), stopPrice,
                                    takeProfitPrice,
                                    orderID

                            );
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });


                    trailingBuy.setOnAction(event2 -> {

                        try {
                            exchange.createOrder(
                                    new TradePair(dat[0][0], dat[0][1]
                                    ),
                                    cryptoinvestor.cryptoinvestor.Side.BUY,

                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY, price[0],

                                    quantity,
                                    new Date(), stopPrice,
                                    takeProfitPrice,
                                    orderID


                            );
                        } catch (IOException | InterruptedException e) {
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
                                            new TradePair(dat[0][0], dat[0][1]
                                            ),
                                            cryptoinvestor.cryptoinvestor.Side.BUY,

                                            ENUM_ORDER_TYPE.STOP_LOSS, price[0],

                                            quantity,
                                            new Date(), stopPrice,
                                            takeProfitPrice,
                                            orderID
                                    );
                                } catch (IOException | InterruptedException  e) {
                                    throw new RuntimeException(e);
                                }
                                sellStopBtn.setOnAction(
                                        event4 -> {
                                            try {
                                                exchange.createOrder(
                                                        new TradePair(dat[0][0], dat[0][1]
                                                        ),
                                                        cryptoinvestor.cryptoinvestor.Side.SELL,

                                                        ENUM_ORDER_TYPE.STOP_LOSS, price[0],

                                                        quantity,
                                                        new Date(), stopPrice,
                                                        takeProfitPrice,
                                                        orderID


                                                );
                                            } catch (IOException | InterruptedException e) {
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

                    gridPane.setStyle("-fx-background-color: #000000;");

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
        walletBtn.setOnAction(
                event7 -> {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Wallet(exchange)));
                    stage.show();
                });

        symbolChoicebox.setValue(
                "SELECT A SYMBOL");

        Button orderHistoryBtn =
                new Button("ORDERS -- ");
        orderHistoryBtn.setOnAction(
                event9 -> {
                    Stage stage = new Stage();
                    try {
                        Scene scene = new Scene(new OrdersDisplay(exchange));
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }

                });


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
                            new CandleStickChartContainer(oanda, new TradePair(dat[0][0], dat[0][1]), telegramToken, true)
                    );
                } catch (URISyntaxException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    tradingTabPane.getTabs().get(i).setContent(new CandleStickChartContainer(exchange, new TradePair(
                            dat[0][0], dat[0][1]
                    ), telegramToken, true));
                } catch (URISyntaxException | IOException e) {
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
                exchange.getAllOrders();

                Stage stage = new Stage();
                stage.setScene(new Scene(new OrdersDisplay(exchange)));
                stage.show();
            } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        HBox hBox = new HBox(removeBtn, AddBtn, tradingBtn, new HBox(symbolChoicebox, counterChoicebox), orderHistoryBtn,
                connexionBtn, walletBtn,
                orderViewBtn,
                allOrdersBtn,
                sendScreenShotBtn);

        setPadding(new Insets(10, 10, 10, 10));

        hBox.setPrefSize(1500, 20);
        anchorPane.getChildren().addAll(hBox, tabPane);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);

        setPadding(new Insets(10, 10, 10, 10));
        getChildren().add(anchorPane);
        setPadding(new Insets(10, 10, 10, 10));

        new StrategyTester();

    }


}