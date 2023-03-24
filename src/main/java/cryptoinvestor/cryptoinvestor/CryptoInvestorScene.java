package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Objects;

import static javafx.scene.layout.Border.stroke;

public class CryptoInvestorScene extends TabPane {

    private static final Logger logger = LoggerFactory.getLogger(CryptoInvestorScene.class);

    public CryptoInvestorScene() throws TelegramApiException, IOException, URISyntaxException, NoSuchAlgorithmException, InterruptedException {
        VBox ordersBox = new VBox(listOrders());
        VBox navigator = new VBox(listNavigator());
        ordersBox.setTranslateX(150);
        ordersBox.setTranslateY(350);
        ordersBox.setPrefSize(1300, 350);
        navigator.setPrefSize(1300, 350);
        setPrefSize(1530, 780);
        Coinbase exchange = new Coinbase("23456jhg", "erythematic90", "erythematic90");
        setBorder(stroke(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 1)));
        getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());
        setPadding(new Insets(20));

        String accountId = "";

        for (int i = 0; i < getTabs().size(); i++) {
            TradeView tradeView = new TradeView(exchange);
            setTranslateX(10);

            tradeView.setPrefSize(1530, 780);

            getTabs().get(i).setContent(
                    tradeView
            );
            getTabs().get(i).setOnCloseRequest(event -> logger.info("Tab closed"));
            logger.info("CryptoInvestor created");
            getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());
            setTranslateX(0);
            setTranslateY(0);
            setTranslateZ(0);
        }
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
                if (order != null) {
                    order.showOrderDetails();
                }
            }
        });
        orders.getItems().addAll(Trade.getOrders());
        return orders;
    }


    public void setExchange(Node newValue) {
        for (int i = 0; i < getTabs().size(); i++) {
            getTabs().get(i).setContent(newValue);
        }
    }
}

