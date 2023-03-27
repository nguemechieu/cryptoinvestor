package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class OrderHistory extends Parent {
    public OrderHistory(Exchange exchange) throws IOException, InterruptedException {
        super();

        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: #000000");
        stackPane.setPrefSize(800, 600);

   if (exchange instanceof Coinbase coinbase){
       stackPane.getChildren().add(coinbase.getAllOrders());

   }


        this.getChildren().add(stackPane);
    }
}
