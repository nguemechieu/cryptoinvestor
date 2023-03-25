package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import cryptoinvestor.cryptoinvestor.oanda.POSITION_FILL;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;


public class TradeView extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);
     double price;


    private TradePair tradePair;

    private Exchange exchange;

    public TradeView(Exchange exchange) throws URISyntaxException, IOException, ParseException, InterruptedException {

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


        tradePair = new TradePair("BTC", "USD");

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


        for (Tab  container : tabPane.getTabs()) {
            Label lab = new Label(tradePair.getBaseCurrency().code + " - " + "USD");
            if (tradePair.getImage() != null && !tradePair.getImage().isEmpty()) {
                container.setStyle("-fx-background-image: url(" + tradePair.getImage() + ");");
                container.setStyle("-fx-background-repeat: no-repeat;");
                container.setStyle("-fx-background-position: center center;");
                container.setStyle("-fx-background-size: 100% 100%;");
                lab.setStyle("-fx-font-weight: bold;");
                lab.setPadding(new Insets(10, 10, 10, 10));


                lab.setGraphic(new ImageView(tradePair.getBaseCurrency().getImage()));
                container.setGraphic(lab);
                container.setGraphic(new ImageView(tradePair.getBaseCurrency().getImage()));
                container.setStyle("-fx-background-image: url(" + tradePair.getBaseCurrency().getImage() + ");");
            } else {

                anchorPane.setStyle("-fx-background-size: 100% 100%;");
                anchorPane.setStyle("-fx-background-color: #000000;");
                lab.setStyle("-fx-font-weight: bold;");
                setTradePair(tradePair);
                container.setGraphic(lab);
                setBorder(Border.stroke(Color.web("#000000")));
            }
            logger.info(tradePair.getBaseCurrency().code + " - " + "USD");
        }Button AddBtn = new Button("Load new chart");

        String telegramToken="9q3vfhm7l33rus21toc8fndupq76itje";
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
                        container2 = new CandleStickChartContainer(exchange, tradePair3, telegramToken, true
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


                                double price = Double.parseDouble(baseCurrency + counterCurrency);
                                if (exchange instanceof Oanda oanda){

                                  oanda.createOrder(
                                          new TradePair(baseCurrency,counterCurrency),
                                          POSITION_FILL.DEFAULT_FILL,
                                          price,
                                          ENUM_ORDER_TYPE.MARKET
                                          , cryptoinvestor.cryptoinvestor.Side.BUY,
                                          quantity,
                                          stopPrice,
                                          takeProfitPrice

                                  );}else  if (exchange instanceof Bitfinex coinbase){

                                    coinbase.createOrder(
                                            new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                            ENUM_ORDER_TYPE.MARKET,
                                            quantity,
                                            0,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice

                                    );

                                }else if (exchange instanceof BinanceUs binanceUs){
                                    binanceUs.createOrder(
                                            new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                            ENUM_ORDER_TYPE.MARKET,
                                            quantity,
                                            0,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice

                                    );
                                }

                            }
                    );


                    gridPane.add(btnBuy, 0, 1);
                    Button btnSell = new Button("Sell");
                    btnSell.setOnAction(
                            event1 -> {
                                double quantity = spinner.getValue();
                                long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                double stopPrice = 100;
                                double takeProfitPrice = 100;
                                @NotNull Instant timestamp=
                                        Instant.now();

                                if (exchange instanceof Oanda oanda){


                                    oanda.createOrder(
                                            new TradePair(baseCurrency,counterCurrency),
                                            POSITION_FILL.DEFAULT_FILL,
                                            price,
                                            ENUM_ORDER_TYPE.MARKET
                                           , cryptoinvestor.cryptoinvestor.Side.SELL,
                                            quantity,
                                            stopPrice,
                                            takeProfitPrice



                                    );
                                }else if (exchange instanceof Coinbase coinbase){
                                    try {

                                        coinbase.createOrder(

                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                0,
                                                0,
                                                timestamp, stopPrice,
                                                takeProfitPrice,

                                                takeProfitPrice);
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                }else if (exchange instanceof Bittrex bittrex){
                                        bittrex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice


                                        );


                                }else if (exchange instanceof Kraken kraken)  {
                                    kraken.createOrder(
                                            new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.MARKET,
                                            quantity,
                                            0,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice

                                    );
                                }else if (exchange instanceof Poloniex poloniex){

                                    try {
                                        poloniex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice

                                        );
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                }else if (exchange instanceof Bitfinex bitfinex){


                                        bitfinex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice


                                        );

                                }else if (exchange instanceof Bitstamp bitstamp){

                                    try {
                                        bitstamp.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                                ENUM_ORDER_TYPE.MARKET,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice

                                        );
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                }else if (exchange instanceof BinanceUs binanceUs){


                                    binanceUs.createOrder(
                                            new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.MARKET,
                                            quantity,
                                            0,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice

                                    );
                                }
                            });
                    gridPane.add(btnSell, 1, 1);

                    Button closeAll = new Button("Close All");
                    closeAll.setOnAction(event1 -> {
                    if (exchange instanceof Bittrex bittrex){
                           bittrex.closeAll();
                       }else if (exchange instanceof Kraken kraken){
                           kraken.closeAll();
                       }else if (exchange instanceof Poloniex poloniex){
                           poloniex.closeAll();
                       }else if (exchange instanceof Bitfinex bitfinex){
                           bitfinex.closeAll();
                       }else if (exchange instanceof Bitstamp bitstamp){
                           bitstamp.closeAll();

                       }else if (exchange instanceof BinanceUs binanceUs){
                           binanceUs.closeAll();
                       }else if (exchange instanceof Coinbase coinbase){
                           try {
                               coinbase.CloseAllOrders();
                           } catch (IOException | InterruptedException e) {
                               throw new RuntimeException(e);
                           }

                       }else if (exchange instanceof Oanda oanda){
                        oanda.closeAll();
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

                                    if (exchange instanceof Bittrex bittrex){
                                        bittrex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Kraken kraken){
                                        kraken.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Poloniex poloniex){
                                        try {
                                            poloniex.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                        } catch (IOException | InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }else if (exchange instanceof Bitfinex bitfinex){
                                        bitfinex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Bitstamp bitstamp){
                                        try {
                                            bitstamp.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                        } catch (IOException | InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }

                                    }else if (exchange instanceof BinanceUs binanceUs){
                                        binanceUs.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                quantity,
                                                0,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Coinbase coinbase){
                                        try {
                                            coinbase.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                        } catch (IOException | InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }else if (exchange instanceof Oanda oanda){
                                        oanda.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()),
                                            POSITION_FILL.DEFAULT_FILL,price,
                                                ENUM_ORDER_TYPE.TRAILING_STOP_SELL,
                                                cryptoinvestor.cryptoinvestor.Side.SELL,
                                                quantity,
                                                stopPrice,
                                                takeProfitPrice
                                        );

                                    }
                                });


                        trailingBuy.setOnAction(event2 ->{
                                    try {
                                        @NotNull Instant timestamp=
                                                Instant.now();
                                        double price=0;
                                        Double quantity = spinner.getValue();
                                        long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                        double stopPrice = 100;
                                        double takeProfitPrice = 100;
                                        switch (exchange) {
                                            case Bittrex bittrex -> bittrex.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    price,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case Kraken kraken -> kraken.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    price,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case Poloniex poloniex -> poloniex.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    price,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case Bitfinex bitfinex -> bitfinex.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case Bitstamp bitstamp -> bitstamp.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case Kucoin kucoin -> kucoin.createOrder(
                                                    new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                    ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                    quantity,
                                                    0,
                                                    timestamp,
                                                    orderID,
                                                    stopPrice,
                                                    takeProfitPrice
                                            );
                                            case null, default -> {
                                               if (exchange instanceof BinanceUs binanceUs) {
                                                    binanceUs.createOrder(
                                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                            ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                            quantity,
                                                            price,
                                                            timestamp,
                                                            orderID,
                                                            stopPrice,
                                                            takeProfitPrice
                                                    );
                                                } else if (exchange instanceof Coinbase coinbase) {
                                                    coinbase.createOrder(
                                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                            ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                            quantity,
                                                            price,
                                                            timestamp,
                                                            orderID,
                                                            stopPrice,
                                                            takeProfitPrice
                                                    );
                                                } else if (exchange instanceof Oanda oanda) {
                                                   oanda.createOrder(
                                                           new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()),
                                                           POSITION_FILL.DEFAULT_FILL,price,
                                                           ENUM_ORDER_TYPE.TRAILING_STOP_BUY,
                                                           cryptoinvestor.cryptoinvestor.Side.SELL,
                                                           quantity,
                                                           stopPrice,
                                                           takeProfitPrice
                                                   );

                                                }
                                            }
                                        }
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                });
                    gridPane.add(trailingBuy, 4,0);
                    gridPane.add(trailingSell, 4,1);

                    Button buyStopBtn = new Button("Buy Stop");
                    Button sellStopBtn = new Button("Sell Stop");
                    Button sellCancelBtn = new Button("Sell Cancel");
                    Button buyCancelBtn = new Button("Buy Cancel");
                    Button cancelAllBtn = new Button("Cancel All");
                    gridPane.add(buyStopBtn, 0, 6);
                    gridPane.add(sellStopBtn, 2, 6);
                    gridPane.add(sellCancelBtn, 2, 7);
                    gridPane.add(buyCancelBtn, 0, 8);
                    gridPane.add(cancelAllBtn, 3, 8);
                    buyStopBtn.setOnAction(
                            event3 -> {
                                try {
                                    @NotNull Instant timestamp=
                                            Instant.now();
                                    double price=0;
                                    Double quantity = spinner.getValue();
                                    long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                    double stopPrice = 100;
                                    double takeProfitPrice = 100;

                                    if (exchange instanceof Oanda oanda){
                                        oanda.createOrder(
                                                new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()),
                                                POSITION_FILL.DEFAULT_FILL,price,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                cryptoinvestor.cryptoinvestor.Side.SELL,
                                                quantity,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Coinbase coinbase){
                                        coinbase.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Bittrex bittrex){
                                        bittrex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Poloniex poloniex){
                                        poloniex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Bitfinex bitfinex){
                                        bitfinex.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof Kraken kraken){
                                        kraken.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }else if (exchange instanceof BinanceUs binanceUs){
                                        binanceUs.createOrder(
                                                new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.BUY,
                                                ENUM_ORDER_TYPE.STOP_LOSS,
                                                quantity,
                                                price,
                                                timestamp,
                                                orderID,
                                                stopPrice,
                                                takeProfitPrice
                                        );
                                    }

                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                    );
                    sellStopBtn.setOnAction(
                            event4 -> {
                                try {
                                    @NotNull Instant timestamp=
                                            Instant.now();
                                    double price=0;
                                    Double quantity = spinner.getValue();
                                    long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                    double stopPrice = 100;
                                    double takeProfitPrice = 100;
                                if (exchange instanceof Coinbase coinbase){
                                    coinbase.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }else if (exchange instanceof Bittrex bittrex){
                                    bittrex.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }else if (exchange instanceof Poloniex poloniex){
                                    poloniex.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }else if (exchange instanceof Bitfinex bitfinex){
                                    bitfinex.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }else if (exchange instanceof Kraken kraken){

                                    kraken.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                            );
                                }else if (exchange instanceof BinanceUs binanceUs){
                                    binanceUs.createOrder(
                                            new TradePair(symbolChoicebox.getValue(), counterChoicebox.getValue()), cryptoinvestor.cryptoinvestor.Side.SELL,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            quantity,
                                            price,
                                            timestamp,
                                            orderID,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }else if (exchange instanceof Oanda oanda){
                                    oanda.createOrder(
                                            new TradePair(symbolChoicebox.getValue(),counterChoicebox.getValue()),
                                            POSITION_FILL.DEFAULT_FILL,price,
                                            ENUM_ORDER_TYPE.STOP_LOSS,
                                            cryptoinvestor.cryptoinvestor.Side.SELL,
                                            quantity,
                                            stopPrice,
                                            takeProfitPrice
                                    );
                                }
                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    sellCancelBtn.setOnAction(
                            event5 -> {
                                try {

                                    long orderID=
                                            Math.round(Instant.now().getEpochSecond() * 1000000);
                                    if (exchange instanceof Coinbase coinbase){
                                        coinbase.cancelOrder(orderID);
                                    }else if (exchange instanceof Bittrex bittrex){
                                        bittrex.CancelOrder(orderID);
                                    }
                                    else if (exchange instanceof Poloniex poloniex){
                                        poloniex.CancelOrder(orderID);
                                    }
                                    else if (exchange instanceof Bitfinex bitfinex){
                                        bitfinex.CancelOrder(orderID);
                                    }
                                    else if (exchange instanceof Kraken kraken){
                                        kraken.CancelOrder(orderID);
                                    }
                                    else if (exchange instanceof BinanceUs binanceUs){
                                        binanceUs.CancelOrder(orderID);
                                    }else if (exchange instanceof Oanda oanda){
                                        oanda.CancelOrder(orderID);
                                    }



                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    buyCancelBtn.setOnAction(
                            event6 -> {
                                try {
                                    @NotNull Instant timestamp=
                                            Instant.now();
                                    double price=0;
                                    Double quantity = spinner.getValue();
                                    long orderID = Math.round(Instant.now().getEpochSecond() * 1000000);
                                    double stopPrice = 100;
                                    double takeProfitPrice = 100;

                                    if (exchange instanceof Oanda oanda){
                                        oanda.CancelOrder(orderID);
                                    }else if (exchange instanceof Bitfinex bitfinex){
                                        bitfinex.CancelOrder(orderID);
                                    }else if (exchange instanceof Bitstamp bitstamp){
                                        bitstamp.CancelOrder(orderID);
                                    }else if (exchange instanceof Coinbase coinbase){
                                        coinbase.cancelOrder(orderID);
                                    }else if (exchange instanceof BinanceUs binanceUs){
                                        binanceUs.CancelOrder(orderID);
                                    }else if (exchange instanceof Kucoin kucoin){
                                        kucoin.CancelOrder(orderID);
                                    }else if (exchange instanceof Bittrex bittrex){
                                        bittrex.CancelOrder(orderID);
                                    }
                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });

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
                    TreeItem<Trade> tradeItem = new TreeItem<>();

                    Trade order9=new Trade();
                    order9.order.setSide(cryptoinvestor.cryptoinvestor.Side.BUY);
                    order9.order.setSymbol(baseCurrency);

                    tradeItem.getChildren().add(
                            new TreeItem<>(order9)
                    );

                    tradeItem.setExpanded(true);
                    root.getChildren().add(tradeItem);

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

        CandleStickChartContainer container =
                new CandleStickChartContainer(exchange, new TradePair("BTC","USD"), telegramToken, true
                );

        anchorPane.setPadding(new Insets(10, 10, 10, 10));
        container.setPrefSize(1000, 600);
        String baseCurrency="AUD";
        String counterCurrency="USD";

        if (exchange instanceof Oanda) {baseCurrency = "EUR"; counterCurrency="USD";}
        DraggableTab tradeTab = new DraggableTab(
                baseCurrency + "/" + counterCurrency
                , "");


        tradeTab.setContent(new
                CandleStickChartContainer(exchange, new TradePair(baseCurrency,counterCurrency), telegramToken, true));

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
