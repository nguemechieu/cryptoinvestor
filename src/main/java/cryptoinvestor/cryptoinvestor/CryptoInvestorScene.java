package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Objects;

public class CryptoInvestorScene extends AnchorPane {

    private static final Logger logger = LoggerFactory.getLogger(CryptoInvestorScene.class);
    static final String BINANCE_ACCESS_TOKEN = "";
     BinanceUs binanceUs = new BinanceUs(BINANCE_ACCESS_TOKEN);
     Coinbase coinbase = new Coinbase(BINANCE_ACCESS_TOKEN);


    Oanda oanda = new Oanda("77be89b17b7fe4c04affd4200454827c-dea60a746483dc7702878bdfa372bb99", "001-001-2783446-002","2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo");

    public CryptoInvestorScene() throws TelegramApiException, IOException, ParseException, InterruptedException {


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

   setPrefSize(1530, 780);
   setBorder(Border.stroke(
           Color.rgb(
                    (int)(Math.random()*255),
                    (int)(Math.random()*255),
                    (int)(Math.random()*255),1
            )
   ));
        getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());

        ExchangeViewPane exchangeViewPane = new ExchangeViewPane();

        for (int i =0;i<exchangeViewPane.tabPane.getTabs().size();i++) {
            TradeViewImpl tradeView;
            try {
                tradeView = new TradeViewImpl(null, null, null,null);
            } catch (TelegramApiException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

            SplitPane splitPane = new SplitPane();
            splitPane.getItems().add(tradeView);

            navigator.setBackground(
                    Background.fill(
                            Color.rgb(
                                    (int)(Math.random()*255),
                                    (int)(Math.random()*255)
                                    ,
                                    (int)(Math.random()*255),1
                            )
                    )
            );
            splitPane.getItems().add(navigator);


            OrdersDisplay ordersDisplay = new OrdersDisplay();
            ordersDisplay.setBackground(
                    Background.fill(
                            Color.rgb(
                                    (int)(Math.random()*255),
                                    (int)(Math.random()*255),1)));

            splitPane.getItems().add(ordersDisplay);

            exchangeViewPane.tabPane.getTabs().get(i).setContent(new VBox(new Label("Start"
            ),splitPane));

        }
exchangeViewPane.setTranslateY(25);
        getChildren().addAll(getMenuBar(),new Separator(), exchangeViewPane);


            logger.info("CryptoInvestor created");



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

