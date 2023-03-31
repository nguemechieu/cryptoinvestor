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

        VBox vbox = new VBox();
        vbox.setStyle("-fx-background-color: black;");
        vbox.setSpacing(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(1000, 600);
        vbox.getChildren().add(new ListView<>(FXCollections.observableArrayList(exchange.getName(), exchange.getAvailableSymbols(), exchange.getAllOrders())));
        //orderListView.getItems().addAll(exchange.getOrdersList());
        //  orderListView.setItems(exchange.getOrdersList());
        getChildren().addAll(vbox);

    }
}
