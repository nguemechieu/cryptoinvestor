package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class OrderHistory extends StackPane {
    public OrderHistory(Exchange exchange) throws IOException, InterruptedException {
        super();

        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: Black;");
        stackPane.setStyle("-fx-border-color: rgb(50,234,234);");
        stackPane.setStyle("-fx-border-width: 2px;");
        stackPane.setStyle("-fx-border-radius: 5px;");
       stackPane.setPrefSize(400, 300);
   if (exchange instanceof Coinbase coinbase){

       stackPane.getChildren().add(coinbase.getAllOrders());
   }
        this.getChildren().add(stackPane);
    }
}
