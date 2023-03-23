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
import java.text.ParseException;
import java.util.List;

public class TradeView extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(TradeView.class);
    String accountID;

    private TradePair tradePair;
    private Exchange exchange;

    public TradeView(Exchange exchange) throws URISyntaxException, IOException, TelegramApiException, ParseException, InterruptedException {

        super();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        AnchorPane anchorPane = new AnchorPane();
        logger.debug(
                "Creating TradeView"
        );


        ChoiceBox<String> symbolChoicebox =
                new ChoiceBox<>();
        TradePair tradePair;

        for (Currency symbol : CurrencyDataProvider.getInstance()) {
            symbolChoicebox.getItems().addAll(
                    symbol.code
            );
        }
        symbolChoicebox.setValue("SELECT A BASE CURRENCY");


        ChoiceBox<String> counterChoicebox = new ChoiceBox<>();
        counterChoicebox.getItems().addAll("USD", "CAD", "BTC");
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
        Button tradingBtn = new Button("Trading Button");
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
                    stackPane.setPrefSize(1500, 630);
                    GridPane gridPane = new GridPane();
                    gridPane.setPrefSize(1500, 630);
                    gridPane.setHgap(10);
                    gridPane.setVgap(10);
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    Spinner<Double> spinner = new Spinner<>(0.01, 1000000, 0);
                    gridPane.add(spinner, 1, 0);
                    Button btnBuy = new Button("Buy");
                    gridPane.add(btnBuy, 0, 1);
                    Button btnSell = new Button("Sell");
                    gridPane.add(btnSell, 1, 1);
                    stackPane.getChildren().add(gridPane);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(stackPane));
                    stage.show();

                });
        HBox hBox = new HBox(symbolChoicebox, counterChoicebox, removeBtn, AddBtn, tradingBtn);
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

    private @NotNull TreeTableView<Object> listNavigator() {
        TreeTableView<Object> orders = new TreeTableView<>();
        orders.setPrefHeight(300);
        orders.setPrefWidth(200);
        return orders;
    }

    private @NotNull ListView<Order> listOrders() {
        ListView<Order> orders = new ListView<>();
        orders.setPrefHeight(100);
        orders.setPrefWidth(100);
        orders.setCellFactory(param -> new OrderCell());
        orders.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Order order = orders.getSelectionModel().getSelectedItem();
                if (order != null) {
                    order.showOrderDetails();
                }
            }
        });
        orders.getItems().addAll(Trade.getOrders());
        return orders;
    }

    public TradePair getTradePair() {
        return tradePair;
    }

    public void setTradePair(TradePair tradePair) {
        this.tradePair = tradePair;
    }

    @NotNull List<Currency> getTradePairs() {
        return CurrencyDataProvider.getInstance();
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
