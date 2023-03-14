package org.tradeexpert.tradeexpert;

import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.tradeexpert.tradeexpert.BinanceUs.BinanceUs;
import org.tradeexpert.tradeexpert.Coinbase.Coinbase;
import org.tradeexpert.tradeexpert.oanda.OANDA_ACCESS_TOKEN;
import org.tradeexpert.tradeexpert.oanda.Oanda;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static java.lang.System.out;

public class TradeExpertScene extends AnchorPane {

    private static final String BINANCE_ACCESS_TOKEN = "";
    private static final String BINANCE_ACCESS_SECRET = "";
    private static final String BINANCE_ACCESS_PASSWORD = "";
 private static final TabPane  tabPane = new TabPane();
    public TradeExpertScene() throws Exception {

        VBox ordersBox = new VBox(listOrders());
        VBox navigator = new VBox(listNavigator());
        navigator.setPrefHeight(700);
        navigator.setPrefWidth(160);
        navigator.setAlignment(Pos.CENTER);
        navigator.setSpacing(5);
        navigator.setPadding(new Insets(5));
        navigator.setTranslateY(25);
        navigator.setTranslateX(0);
        ordersBox.setTranslateX(150);
        ordersBox.setTranslateY(650);
        ordersBox.setPrefSize(1300, 650);



        Oanda oanda = new Oanda("77be89b17b7fe4c04affd4200454827c-dea60a746483dc7702878bdfa372bb99", OANDA_ACCESS_TOKEN.ACCOUNT_ID.toString());
        BinanceUs binance = new BinanceUs(BINANCE_ACCESS_TOKEN, BINANCE_ACCESS_SECRET, BINANCE_ACCESS_PASSWORD);
        TradePair tradePair=TradePair.of("BCH","USD");

        out.println("Binance tradePair "+tradePair.toString('-'));

        TradePair tradePair1=TradePair.of("BTC","USD");

        out.println("Coinbase us tradePair "+tradePair1.toString('-'));

        TradePair tradePair2=TradePair.of("AUD","USD");

        out.println("Oanda us tradePair "+tradePair2.toString('_'));
        CandleStickChartContainer binanceCandleStickChartContainer = new CandleStickChartContainer(binance, tradePair, true);


        Coinbase coinbase = new Coinbase(tradePair);
        CandleStickChartContainer coinbaseCandleStickChartContainer = new CandleStickChartContainer(coinbase, tradePair1, true);
        CandleStickChartContainer oandaCandleStickChartContainer = new CandleStickChartContainer(oanda, tradePair2, true);
        DraggableTab oandaTab = new DraggableTab("Oanda.Com ");
        oandaTab.setContent(new VBox(oandaCandleStickChartContainer));
        DraggableTab binanceTab = new DraggableTab("Binance Us ");
        binanceTab.setContent(new VBox(binanceCandleStickChartContainer));
        DraggableTab coinbaseTab = new DraggableTab("Coinbase Pro ");

        coinbaseTab.setContent(new VBox(coinbaseCandleStickChartContainer));

        DraggableTab newsTab = new DraggableTab("Market News ");
        newsTab.setContent(new VBox(getNews()));
        DraggableTab PortFolioTab = new DraggableTab("PortFolio ");
        PortFolioTab.setContent(new VBox(getPortFolio()));
        DraggableTab MarketAnalysisTab = new DraggableTab("Market Analysis ");
        MarketAnalysisTab.setContent(new VBox(getMarketAnalysis()));
        DraggableTab tradeSignalTab = new DraggableTab("Trade Signal ");
        tradeSignalTab.setContent(new VBox(getTradeSignal()));
        DraggableTab tradeHistoryTab = new DraggableTab("Trade History ");
        tradeHistoryTab.setContent(new VBox(getTradeHistory()));
        DraggableTab miniBrowserTab = new DraggableTab("Mini Browser ");
        miniBrowserTab.setContent(new Browser().start());



        tabPane.getTabs().addAll(oandaTab, binanceTab, coinbaseTab,
                newsTab, PortFolioTab, MarketAnalysisTab, tradeSignalTab, tradeHistoryTab, miniBrowserTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //candlestickChartContainer,new Separator(Orientation.VERTICAL),navigator,ordersBox, connex
        tabPane.setTranslateY(25);
        tabPane.setCursor(Cursor.DEFAULT);
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.setPrefSize(1350,700);

        tabPane.setDepthTest(DepthTest.ENABLE);
        tabPane.setTranslateX(150);

        setStyle(
                "-fx-background-color: rgb(45, 25, 144, 1);" +
                        "-fx-border-color: rgb(45, 25, 144, 1);" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-insets: 5;" +
                        "-fx-background-insets: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-insets: 5;"
        );

        getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());

        getChildren().addAll(getMenuBar(), tabPane,ordersBox,navigator);



    }

    private double getTradeHistory() {
        return 0;
    }

    private double getTradeSignal() {
        return 0;
    }

    private double getMarketAnalysis() {
        return 0;
    }

    private @NotNull HBox getPortFolio() {
        HBox portFolio = new HBox();

        portFolio.setPadding(new Insets(10));
        portFolio.setSpacing(10);
        portFolio.setStyle("-fx-background-color: rgb(45, 25, 144, 1);");
        portFolio.setDepthTest(DepthTest.ENABLE);
        portFolio.setTranslateX(150);
        portFolio.setPrefSize(1200, 550);
        portFolio.setAlignment(Pos.CENTER);
        portFolio.getChildren().add(new Label("PortFolio"));
        GridPane gridPortFolio=new GridPane();
        gridPortFolio.setHgap(10);
        gridPortFolio.setVgap(10);
        gridPortFolio.setPadding(new Insets(10));
        gridPortFolio.setStyle("-fx-background-color: rgb(45, 25, 144, 1);");
        gridPortFolio.setDepthTest(DepthTest.ENABLE);
        gridPortFolio.setTranslateX(0);

        gridPortFolio.setAlignment(Pos.CENTER);
        gridPortFolio.add(new Label("Account ID "), 0, 0);
        gridPortFolio.add(new Label(OANDA_ACCESS_TOKEN.ACCOUNT_ID.toString()), 1, 0);
        gridPortFolio.add(new Label("Balance  "), 0, 1);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getBalance())), 1, 1);
        gridPortFolio.add(new Label("Margin %  "), 0,1);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getMarginPercent())), 1, 1);
        gridPortFolio.add(new Label("Open  "), 0, 2);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getOpen())), 1, 2);
        gridPortFolio.add(new Label("High  "), 0, 3);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getHigh())), 1, 3);
        gridPortFolio.add(new Label("Low  "), 0, 4);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getLow())), 1, 4);
        gridPortFolio.add(new Label("Close  "), 0, 5);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getClose())), 1, 5);
        gridPortFolio.add(new Label("Volume  "), 0, 6);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getVolume())), 1, 6);
        gridPortFolio.add(new Label("Market Cap  "), 0, 7);
        gridPortFolio.add(new Label(String.valueOf(Oanda.getMarketCap())), 1, 7);
        portFolio.getChildren().add(gridPortFolio);
        return portFolio;
    }

    private @NotNull TreeTableView <News>getNews() throws ParseException {


        TreeTableView<News> treeTableNews = new TreeTableView<>();
        treeTableNews.setStyle("-fx-background-color: rgb(45, 25, 144, 1);");
        TreeTableColumn<News, String> columnNewsDate = new TreeTableColumn<>();
        columnNewsDate.setText("Date");
        TreeTableColumn<News, String> columnNewsTitle = new TreeTableColumn<>();
        columnNewsTitle.setText("Title");
        TreeTableColumn<News, String> columnNewsCountry = new TreeTableColumn<>();
        columnNewsCountry.setText("Country");
        TreeTableColumn<News, String> columnNewsImpact = new TreeTableColumn<>();
        columnNewsImpact.setText("Impact");
        TreeTableColumn<News, String> columnNewsForecast = new TreeTableColumn<>();
        columnNewsForecast.setText("Forecast");
        TreeTableColumn<News, String> columnNewsPrevious = new TreeTableColumn<>();
        columnNewsPrevious.setText("Previous");


        //Loading News from Forex factory url:https://nfs.faireconomy.media/ff_calendar_thisweek.json?version=1bed8a31256f1525dbb0b6daf6898823
        ObservableList<News> dat = FXCollections.observableArrayList();
        ArrayList<News> news0 = NewsManager.getNewsList();
        dat.addAll(news0);
        Callback<RecursiveTreeObject<News>, ObservableList<News>> callback
                = RecursiveTreeObject::getChildren;
        RecursiveTreeItem<News> root = new RecursiveTreeItem<>(dat, callback);

        for (News news1 : news0) {
            if (news1.getDate().getTime()>= new Date().getTime()) {
                root.setValue(news1);
            }
            if (Objects.equals(news1.getImpact(), "High")) {
                columnNewsImpact.setStyle("-fx-background-color: RED;");
            }else if (Objects.equals(news1.getImpact(), "Medium")) {
                columnNewsImpact.setStyle("-fx-background-color: ORANGE;");
            }else
            if (Objects.equals(news1.getImpact(), "Low")) {
                columnNewsImpact.setStyle("-fx-background-color: GREEN;");
            }     //treeItemDate.getChildren().setAll(root);


        }

        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsDateValue = param -> new SimpleObjectProperty<>(
                param.getValue().getValue().getDate()).asString();
        columnNewsDate.setCellValueFactory(columnNewsDateValue);


        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsTitleValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getTitle());
        columnNewsTitle.setCellValueFactory(columnNewsTitleValue);
        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsCountryValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getCountry());
        columnNewsCountry.setCellValueFactory(columnNewsCountryValue);
        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsImpactValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getImpact());
        columnNewsImpact.setCellValueFactory(columnNewsImpactValue);
        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsForecastValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getForecast());
        columnNewsForecast.setCellValueFactory(columnNewsForecastValue);
        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsPreviousValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPrevious());
        columnNewsPrevious.setCellValueFactory(columnNewsPreviousValue);
        root.setExpanded(true);
        treeTableNews.setRoot(root);
        treeTableNews.setPrefSize(1000,650);
        treeTableNews.getColumns().addAll(columnNewsDate, columnNewsTitle, columnNewsCountry, columnNewsImpact, columnNewsForecast, columnNewsPrevious);
        return treeTableNews;


    }

    private @NotNull HBox connexion() {
        HBox connexionInfo = new HBox();
        connexionInfo.setPrefHeight(100);
        connexionInfo.setPrefWidth(100);
        connexionInfo.setAlignment(Pos.CENTER);
        connexionInfo.setSpacing(10);
        connexionInfo.setPadding(new Insets(10));
        connexionInfo.setTranslateY(100);
        connexionInfo.setTranslateX(200);
        connexionInfo.setPrefSize(1200, 550);



        Label label = new Label("Connexion");
        label.setPrefHeight(20);
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 20px;");
        label.setPrefHeight(20);
        label.setPrefWidth(100);

        connexionInfo.getChildren().addAll(label);
        Label label2 = new Label("Connexion In progress");
        label2.setPrefHeight(20);
        if (Trade.getConnexionInfo()== 200) {
            label2=new Label("Connected...");
            label2.setStyle("-fx-font-size: 15px;");
            label2.setStyle("-fx-font-weight: bold;");
            label2.setStyle("-fx-font-style: italic;");
            label2.setPrefHeight(20);
            label2.setPrefWidth(100);


        }else
        if (Trade.getConnexionInfo()== 400) {
            label2=new Label(
                    "Connection refused"
            );
            label2.setStyle("-fx-font-size: 15px;");
            label2.setStyle("-fx-font-weight: bold;");
            label2.setStyle("-fx-font-style: italic;");
            label2.setPrefHeight(20);
            label2.setPrefWidth(100);

        }else if (
                Trade.getConnexionInfo() ==
                        401
        ){
            label2=new Label("Unauthorized");
            label2.setStyle("-fx-font-size: 15px;");
            label2.setStyle("-fx-font-weight: bold;");
            label2.setStyle("-fx-font-style: italic;");
            label2.setPrefHeight(20);
            label2.setPrefWidth(100);

        }else if (
                Trade.getConnexionInfo() ==
                        402
        ){
            label2=new Label("Bad credentials");
            label2.setStyle("-fx-font-size: 15px;");
            label2.setStyle("-fx-font-weight: bold;");
            label2.setStyle("-fx-font-style: italic;");
            label2.setPrefHeight(20);
            label2.setPrefWidth(100);

        }
        else if (
                Trade.getConnexionInfo() ==500
        ){
            label2=new Label("Internal server error");
            label2.setStyle("-fx-font-size: 15px;");
            label2.setStyle("-fx-font-weight: bold;");
            label2.setStyle("-fx-font-style: italic;");
            label2.setPrefHeight(20);
            label2.setPrefWidth(100);

        }

        label2.setPrefHeight(20);
        label2.setPrefWidth(100);
        label2.setAlignment(Pos.CENTER);
        connexionInfo.setTranslateY(730);
        connexionInfo.setTranslateX(0);
        connexionInfo.setPrefSize(1500, 30);

        connexionInfo.getChildren().addAll(new Separator(Orientation.VERTICAL),label2);


        return connexionInfo;
    }

    private @NotNull TreeTableView<Objects> listNavigator() {
        TreeTableView<Objects> orders = new TreeTableView<>();
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
                if (order!= null) {
                    order.showOrderDetails();
                }
            }
        });
        orders.getItems().addAll(Trade.getOrders());
        return orders;
    }


    MenuBar menuBar = new MenuBar();
    public Node getMenuBar() {
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(
                new MenuItem("New")
        );
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(
                new MenuItem("Open")
        );
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(
                new MenuItem("Save")
        );
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(
                new MenuItem("Save As")
        );
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(
                new MenuItem("Print")
        );
        fileMenu.getItems().add(
                new MenuItem("Exit")
        );


        Menu editMenu = new Menu("Edit");
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(
                new MenuItem("Cut")
        );
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(
                new MenuItem("Copy")
        );
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(
                new MenuItem("Paste")
        );
        editMenu.getItems().add(
                new MenuItem("Delete")
        );

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().add(new MenuItem("Zoom In"));
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(new MenuItem("Zoom Out"));
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(new MenuItem("Reset Zoom"));
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(new MenuItem("Reset"));
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().add(new MenuItem("Minimize"));
        windowMenu.getItems().add(new MenuItem("Maximize"));
        windowMenu.getItems().add(new MenuItem("Close"));

        Menu chartMenu = new Menu("Charts");
        chartMenu.getItems().add(new MenuItem("Bar Chart"));
        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Line Chart"));
        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Pie Chart"));
        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Scatter Chart"));

        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Radar Chart"));
        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Bubble Chart"));
        chartMenu.getItems().add(new SeparatorMenuItem());
        chartMenu.getItems().add(new MenuItem("Candle Stick Chart"));

        Menu insertMenu = new Menu("Insert");
        insertMenu.getItems().add(new Menu("Indicators"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        Menu lines=new Menu("Lines");

        lines.getItems().add(new MenuItem("Trend Line By Angle"));
        lines.getItems().add(new SeparatorMenuItem());
        lines.getItems().add(new MenuItem("Vertical Line"));
        lines.getItems().add(new SeparatorMenuItem());
        lines.getItems().add(new MenuItem("Horizontal Line"));
        lines.getItems().add(new SeparatorMenuItem());
        lines.getItems().add(new MenuItem("Trend Line"));
        lines.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(lines);

        insertMenu.getItems().add(new Menu("Areas"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Channels"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Markers"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Gann"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Fibonacci"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Arrows"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Shapes"));
        insertMenu.getItems().add(new SeparatorMenuItem());
        insertMenu.getItems().add(new Menu("Objects"));



        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(new MenuItem("About"));
        helpMenu.getItems().add(new SeparatorMenuItem());
        helpMenu.getItems().add(new MenuItem("Help"));



        menuBar.getMenus().addAll(
                fileMenu,
                editMenu,
                viewMenu,
                windowMenu,
                chartMenu,
                insertMenu,
                helpMenu);

        Menu toolsMenu = new Menu("Tools");
        toolsMenu.getItems().add(new MenuItem("New Orders"));
        toolsMenu.getItems().add(new SeparatorMenuItem());
        toolsMenu.getItems().add(new MenuItem("History Center"));
        toolsMenu.getItems().add(new SeparatorMenuItem());
        toolsMenu.getItems().add(new MenuItem("Balance Sheet"));
        return menuBar;
    }



}
