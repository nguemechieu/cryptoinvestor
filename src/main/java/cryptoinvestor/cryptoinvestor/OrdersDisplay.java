package cryptoinvestor.cryptoinvestor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class OrdersDisplay extends VBox {
    public OrdersDisplay() {
        super();

        BorderPane borderPane = new BorderPane();

        BorderPane.setMargin(this, new Insets(10, 10, 10,12));
        setPadding( new Insets(10, 10, 10,12));
        setSpacing( 10);
        setPrefSize( 1300, 200);
        setAlignment(Pos.CENTER);

        borderPane.setCenter(this);
        ListView<Order> orderListView = new ListView<>();
        borderPane.setBottom(orderListView);

        //orderListView.getItems().addAll(exchange.getOrdersList());

      //  orderListView.setItems(exchange.getOrdersList());



    }
}
