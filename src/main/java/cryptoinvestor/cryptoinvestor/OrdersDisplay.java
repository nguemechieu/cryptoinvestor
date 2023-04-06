package cryptoinvestor.cryptoinvestor;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class OrdersDisplay extends VBox {
    public OrdersDisplay(@NotNull Exchange exchange) throws IOException, InterruptedException, ParseException, URISyntaxException {
        getStyleClass().add("app");
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(800, 600);
        vbox.getChildren().add(new ListView<>(FXCollections.observableArrayList(exchange.getName(), exchange.getAvailableSymbols(), exchange.getAllOrders())));

        ListView<Object> orderListView =
                new ListView<>(FXCollections.observableArrayList(exchange.getName(), exchange.getAvailableSymbols(), exchange.getAllOrders()));
        vbox.getChildren().add(orderListView);
        getChildren().addAll(vbox);

    }
}
