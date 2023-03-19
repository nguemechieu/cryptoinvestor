//package cryptoinvestor.cryptoinvestor;
//
//import com.jfoenix.controls.RecursiveTreeItem;
//import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
//import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
//import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
//import cryptoinvestor.cryptoinvestor.oanda.OANDA_ACCESS_TOKEN;
//import cryptoinvestor.cryptoinvestor.oanda.Oanda;
//import javafx.beans.property.ReadOnlyStringWrapper;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.*;
//import javafx.geometry.Side;
//import javafx.scene.DepthTest;
//import javafx.scene.Node;
//import javafx.scene.control.*;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Text;
//import javafx.util.Callback;
//import org.jetbrains.annotations.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//import static cryptoinvestor.cryptoinvestor.CryptoInvestorScene.BINANCE_ACCESS_TOKEN;
//
//public class TradingViewPane extends Region{
//
//    private static final Logger logger = LoggerFactory.getLogger(TradingViewPane.class);
//   protected static TabPane tabPane= new TabPane();
//   ENUM_EXCHANGE_LIST mode;
//    Exchange exchange;
//    private static final String API_URL = "https://api.binance.us/api/v3/exchangeInfo";
//
//    public TradingViewPane(String api_key, String accountId, ENUM_EXCHANGE_LIST mode, String telegramToken, String baseCode, String counterCode) throws Exception, TelegramApiException {
//        super();
//
//
//
//
//
//        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());
//
//
//        TabPane mainTabPane=new TabPane();
//        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
//
//        TabPane orderTabPane=new TabPane();
//        VBox vbox1=new VBox(mainTabPane,splitPane,new Label("Trading View"),orderTabPane);
//        vbox1.setSpacing(10);
//        vbox1.setTranslateY(25);
//
//        getChildren().add(vbox1);
//
//
//
//
//      List<Color> colors = new ArrayList<>(
//                List.of(Color.rgb(
//                        (int) (Math.random() * 2),
//                        (int) (Math.random() * 2),
//                                (int) (Math.random() * 2),1),
//                        Color.rgb(
//                                (int) (Math.random() * 255),
//                                (int) (Math.random() * 255),
//                                (int) (Math.random() * 255),1)
//
//      ));
//
//
//
//        int ik = (int) (Math.random() * colors.size()) ;
//
//        ArrayList  <TradePair> tradePairs=new ArrayList<>();
//
//        for (Currency currency : CurrencyDataProvider.getInstance()) {
//
//            Currency currency2 = null;
//
//            int count = 0;
//            while (currency2==currency) {
//                count++;
//                currency2 = CurrencyDataProvider.getInstance().get(count);
//                assert false;
//                if (currency.currencyType.equals(CurrencyType.FIAT)) {
//                    logger.info("Forex Trades Pairs " + currency.code);
//                }
//
//            }
//            tradePairs.add(new TradePair(currency.code, "USD"));
//
//
//
//        }
//
//        //Create random tab colors
//
//        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
//
//       //tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
//        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
//        tabPane.setSide(Side.BOTTOM);
//
//        DraggableTab tab = new DraggableTab(tradePairs.get(ik).getBaseCurrency().code + "  -  " + "USD");
//        String oandaApiKey;
//        String oandaAccountID;
//        if (mode == ENUM_EXCHANGE_LIST.OANDA) {
//
//            oandaApiKey =api_key;
//            oandaAccountID =accountId;
//
//            this.exchange = new Oanda(oandaApiKey, oandaAccountID,telegramToken);
//        }else if (mode == ENUM_EXCHANGE_LIST.BINANCE_US) {
//
//            this.exchange = new BinanceUs(telegramToken);
//        }else  if (mode == ENUM_EXCHANGE_LIST.COINBASE_PRO) {
//
//            this.exchange = new Coinbase(telegramToken);
//        }else {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText(null);
//            alert.setContentText("Unsupported Exchange!");
//            alert.showAndWait();
//            return;
//        }
//        tab.setGraphic(new ImageView(new Image(new URL(tradePairs.get(ik).getImage()).openStream())));
//
//
//        Button addView =new Button("+");
//        Button removeView=new Button("-");
//
//        removeView.setBackground(Background.fill(Color.rgb(255, 0, 0,1
//        )));
//        removeView.setBorder(Border.stroke(Color.BLUE));
//        addView.setBackground(Background.fill(Color.rgb(0, 255, 0,1)));
//        HBox hb=new HBox(removeView,new Separator(Orientation.VERTICAL),addView);
//
//        removeView.setOnAction(event -> {
//                    if(tabPane.getTabs().size()>1) {
//                        tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex());
//
//                    }else {
//                        logger.info("No more tabs");
//                        tabPane.getTabs().clear();
//                        tabPane.getTabs().add(tab);
//                    }
//                }
//        );
//
//        removeView.setBorder(Border.stroke(Color.BLUE));
//        removeView.defaultButtonProperty().set(true);
//              tabPane.getTabs().add(tab);
//              tabPane.getTabs().get(0).setContent(new VBox(new Label(baseCode + " -  " + counterCode)));
//              tab.setGraphic(new ImageView(new Image(tradePairs.get(0).getImage())));
////                 if (
////                         tradePairs.get(i).getBaseCurrency().getImage() != null &&
////                                 !tradePairs.get(i).getCounterCurrency().getImage().isEmpty()
////                 ) {
////                 tabPane.getTabs().get(i).setGraphic(new ImageView(new Image(
////                         tradePairs.get(i).getBaseCurrency().getImage())));
////
////                 }
////          if (tradePairs.get(i).getCounterCurrency().getImage() != null) {
////              tabPane.getTabs().get(i).setStyle("-fx-background-image: url(" + tradePairs.get(i).getBaseCurrency().getImage() + ");");
////               setStyle(    "-fx-background-image: url(" + tradePairs.get(i).getBaseCurrency().getImage() + ";");
////
////          }
//
//
//
//
//
//
//
//
//
//
//        TradePair tradePair=new TradePair(baseCode , counterCode);
//        CandleStickChartContainer candleStickContainer = new CandleStickChartContainer(exchange, tradePair, true);
//              tabPane.getTabs().get(0).setContent(new VBox(new Label(tradePairs.get(0).getBaseCurrency().code + " -  " + tradePair.getCounterCurrency().code), candleStickContainer));
//
//              tabPane.getTabs().add(tab);
//
//
//          tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
//        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
//        tabPane.setSide(Side.BOTTOM);
//        hb.setSpacing(10);
//        hb.setAlignment(Pos.CENTER);
//        hb.setSpacing(10);
//        hb.setTranslateX(-getWidth()+20);
//
//        splitPane.setOrientation(Orientation.VERTICAL);
//        splitPane.setDividerPositions(0.5);
//        splitPane.setDividerPositions(5);
//        splitPane.setDividerPositions(10);
//        splitPane.getItems().add(tabPane);
//        splitPane.getItems().add(hb);
//
//        DraggableTab newsTab = new DraggableTab("Market News ");
//        newsTab.setContent(new VBox(getNews()));
//        DraggableTab PortFolioTab = new DraggableTab("PortFolio ");
//        PortFolioTab.setContent(new VBox(getPortFolio()));
//        DraggableTab MarketAnalysisTab = new DraggableTab("Market Analysis ");
//        MarketAnalysisTab.setContent(new VBox(exchange.account.getMarketAnalysis()));
//        DraggableTab tradeSignalTab = new DraggableTab("Trade Signal ");
//        tradeSignalTab.setContent(new VBox(exchange.account.getTradeSignal()));
//        DraggableTab tradeHistoryTab = new DraggableTab("Trade History ");
//        tradeHistoryTab.setContent(new VBox(exchange.account.getTradeHistory()));
//        DraggableTab miniBrowserTab = new DraggableTab("Mini Browser ");
//        miniBrowserTab.setContent(new Browser().start());
//
//orderTabPane.getTabs().addAll(
//        newsTab,
//        PortFolioTab,
//        MarketAnalysisTab,
//        tradeSignalTab,
//        tradeHistoryTab,
//        miniBrowserTab
//        );
//        orderTabPane.setSide(Side.BOTTOM);
//        orderTabPane.setTranslateX(-getWidth()+20);
//        orderTabPane.setTranslateY(-getHeight()+20);
//        orderTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
//        orderTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
//       DraggableTab  ordersTab=new DraggableTab("Orders ");
//       ordersTab.setContent(new VBox(new Text(exchange.account.getOrders().toString())));
//        orderTabPane.getTabs().addAll(ordersTab,
//                PortFolioTab,
//                MarketAnalysisTab,
//                tradeSignalTab,
//                tradeHistoryTab
//        );
//        orderTabPane.setTranslateX(-getWidth()+20);
//        orderTabPane.setTranslateY(-getHeight()+20);
//
//        TabPane tabPane = new TabPane();
//
//
//        DraggableTab tradePanelTab=new DraggableTab("Trade Panel");
//        CandleStickChartContainer tradePanel=new CandleStickChartContainer(exchange,tradePair, true);
//        tradePanel.setTranslateY(50);
//        tradePanel.setTranslateX(-getWidth()+20);
//        tradePanelTab.setContent(new VBox(new Label(tradePairs.get(tradePane.getSelectedIndex()+1).getBaseCurrency().code + " -  " + tradePairs.get(getSelectionModel().getSelectedIndex()+1).getCounterCurrency().code),tradePanel,tabPane));
//        //tradePanelTab.setGraphic(new ImageView(new Image(tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().getImage())));
//        tabPane.getTabs().addAll(
//                newsTab,
//                PortFolioTab,
//                MarketAnalysisTab,
//                tradeSignalTab,
//                tradeHistoryTab,
//                miniBrowserTab
//        );
//
//
//        for (int i = 0; i < tabPane.getTabs().size(); i++){
//
//
//
//           if (!tradePairs.get(i).getBaseCurrency().getImage().isEmpty() ){
//            tabPane.getTabs().get(i).setGraphic(new ImageView(new Image(tradePairs.get(i).getBaseCurrency().getImage())));
//            tabPane.getTabs().get(i).setStyle("-fx-background-image: url(" + tradePairs.get(i).getBaseCurrency().getImage() + ");");}
//        }
//        tabPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
//        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
//        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
//        tabPane.setSide(Side.BOTTOM);
//
//        addView.defaultButtonProperty().set(false);
//        DraggableTab tab0=new DraggableTab(tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().code + "  - " + tradePairs.get(getSelectionModel().getSelectedIndex()+1).getCounterCurrency().code);
//        addView.setOnAction(event -> tabPane.getTabs().add(tab0));
//        tab0.setContent(new VBox(new Label(tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().code + " -  " + tradePairs.get(getSelectionModel().getSelectedIndex()+1).getCounterCurrency().code)));
//        tab0.setGraphic(new ImageView(new Image(tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().getImage())));
//        tab0.setStyle("-fx-background-image: url(" + tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().getImage() + ");");
//        tabPane.getTabs().add(tab0);
//
//
//        VBox vBox=new VBox(tabPane);
//        vBox.setSpacing(10);
//        vBox.setAlignment(Pos.CENTER);
//        vBox.setFillWidth(true);
//for (int i = 0; i < getTabs().size(); i++){
//    if (!tradePairs.get(i).getBaseCurrency().getImage().isEmpty() ){
//        getTabs().get(i).setGraphic(new ImageView(new Image(tradePairs.get(i).getBaseCurrency().getImage())));
//        getTabs().get(i).setStyle("-fx-background-image: url(" + tradePairs.get(i).getBaseCurrency().getImage() + ");");}
//    getTabs().get(i).setContent(new VBox(new Label(
//            tradePairs.get(getSelectionModel().getSelectedIndex()+1).getBaseCurrency().code + " -  " + tradePairs.get(getSelectionModel().getSelectedIndex()+1).getCounterCurrency().code
//    ),new Separator(Orientation.VERTICAL),vBox
//            ));
//}
//    }
//
//
//
//    SplitPane splitPane = new SplitPane();
//
//    private @NotNull HBox getPortFolio() {
//        HBox portFolio = new HBox();
//
//        portFolio.setPadding(new Insets(10));
//        portFolio.setSpacing(10);
//
//        portFolio.setDepthTest(DepthTest.ENABLE);
//        portFolio.setTranslateX(150);
//        portFolio.setPrefSize(1200, 550);
//        portFolio.setAlignment(Pos.CENTER);
//        portFolio.getChildren().add(new Label("PortFolio"));
//        GridPane gridPortFolio = new GridPane();
//        gridPortFolio.setHgap(10);
//        gridPortFolio.setVgap(10);
//        gridPortFolio.setPadding(new Insets(10));
//
//        gridPortFolio.setDepthTest(DepthTest.ENABLE);
//        gridPortFolio.setTranslateX(0);
//
//
//        gridPortFolio.setAlignment(Pos.CENTER);
//        gridPortFolio.add(new Text("Account ID "), 0, 0);
//        gridPortFolio.add(new Label(OANDA_ACCESS_TOKEN.ACCOUNT_ID.name()), 1, 0);
//        gridPortFolio.add(new Label("Balance  "), 0, 1);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getBalance())), 1, 1);
//        gridPortFolio.add(new Label("Margin %  "), 3, 0);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getMarginPercent())), 4, 0);
//        gridPortFolio.add(new Label("Previous Urge loss  "), 0, 2);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getPreviousUrgeLoss())), 1, 2);
//        gridPortFolio.add(new Label("Free Margin  "), 0, 6);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getFreeMargin())), 1, 6);
//        gridPortFolio.add(new Label("Profit  "), 0, 7);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getProfit())), 1, 7);
//        gridPortFolio.add(new Label("PNL   "), 0, 8);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getPNL())), 1, 8);
//        gridPortFolio.add(new Label("Reset  "), 0, 9);
//        gridPortFolio.add(new Label(String.valueOf(exchange.account.getReset())), 1, 9);
//        portFolio.getChildren().add(gridPortFolio);
//
//        gridPortFolio.setBackground(
//                new Background(new BackgroundFill(Color.rgb(255, 255, 255, 1), CornerRadii.EMPTY, Insets.EMPTY))
//        );
//        return portFolio;
//    }
//
//
//    TradePair binanceUsTradePair=TradePair.of("BTC","USD");
//    TradePair tradePair=TradePair.of("LTC","USD");
//
//    TradePair oandaTradePair=TradePair.of("USD","CAD");
//
//    BinanceUs binance = new BinanceUs(BINANCE_ACCESS_TOKEN );
//    CandleStickChartContainer binanceCandleStickChartContainer = new CandleStickChartContainer(binance, tradePair,true);
//
//    String coinbaseApi="2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo";
//
//    String oandaApi="2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo";
//
//       private @NotNull TreeTableView <News>getNews() throws ParseException, TelegramApiException, IOException, InterruptedException, URISyntaxException {
//
//
//
//
//        TreeTableView<News> treeTableNews = new TreeTableView<>();
//        TreeTableColumn<News, String> columnNewsDate = new TreeTableColumn<>();
//        columnNewsDate.setText("Date");
//        TreeTableColumn<News, String> columnNewsTitle = new TreeTableColumn<>();
//        columnNewsTitle.setText("Title");
//        TreeTableColumn<News, String> columnNewsCountry = new TreeTableColumn<>();
//        columnNewsCountry.setText("Country");
//        TreeTableColumn<News, String> columnNewsImpact = new TreeTableColumn<>();
//        columnNewsImpact.setText("Impact");
//        TreeTableColumn<News, String> columnNewsForecast = new TreeTableColumn<>();
//        columnNewsForecast.setText("Forecast");
//        TreeTableColumn<News, String> columnNewsPrevious = new TreeTableColumn<>();
//        columnNewsPrevious.setText("Previous");
//
//
//        //Loading News from Forex factory url:https://nfs.faireconomy.media/ff_calendar_thisweek.json?version=1bed8a31256f1525dbb0b6daf6898823
//        ObservableList<News> dat = FXCollections.observableArrayList();
//        ArrayList<News> news0 = NewsManager.getNewsList();
//        dat.addAll(news0);
//
//        Callback<RecursiveTreeObject<News>, ObservableList<News>> callback
//                = RecursiveTreeObject::getChildren;
//        RecursiveTreeItem<News> root = new RecursiveTreeItem<>(dat, callback);
//        root.setExpanded(true);
//        root.setValue(dat.get(0));
//
//        treeTableNews.setRoot(root);
//
//
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsDateValue = param -> new SimpleObjectProperty<>(
//                param.getValue().getValue().getDate()).asString();
//        columnNewsDate.setCellValueFactory(columnNewsDateValue);
//
//
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsTitleValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getTitle());
//        columnNewsTitle.setCellValueFactory(columnNewsTitleValue);
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsCountryValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getCountry());
//        columnNewsCountry.setCellValueFactory(columnNewsCountryValue);
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsImpactValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getImpact());
//        columnNewsImpact.setCellValueFactory(columnNewsImpactValue);
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsForecastValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getForecast());
//        columnNewsForecast.setCellValueFactory(columnNewsForecastValue);
//        Callback<TreeTableColumn.CellDataFeatures<News, String>, ObservableValue<String>> columnNewsPreviousValue = param -> new ReadOnlyStringWrapper(param.getValue().getValue().getPrevious());
//        columnNewsPrevious.setCellValueFactory(columnNewsPreviousValue);
//        root.setExpanded(true);
//        treeTableNews.setRoot(root);
//        treeTableNews.setPrefSize(1000,650);
//        treeTableNews.getColumns().add(columnNewsDate);//, columnNewsTitle, columnNewsCountry, columnNewsImpact, columnNewsForecast, columnNewsPrevious);
//        treeTableNews.getColumns().add(columnNewsTitle);//, columnNewsCountry, columnNewsImpact, columnNews
//        treeTableNews.getColumns().add(columnNewsCountry);//, columnNewsImpact, columnNewsForecast, columnNewsPrevious);
//        treeTableNews.getColumns().add(columnNewsImpact);//, columnNewsForecast, columnNewsPrevious);
//        treeTableNews.getColumns().add(columnNewsForecast);//, columnNewsPrevious);
//        treeTableNews.getColumns().add(columnNewsPrevious);//, columnNewsForecast, columnNewsPrevious);
//
//
//
//        return treeTableNews;
//
//
//    }
//}
//
