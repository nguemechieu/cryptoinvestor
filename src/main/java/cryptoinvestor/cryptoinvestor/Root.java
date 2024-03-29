package cryptoinvestor.cryptoinvestor;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class Root extends VBox {
    public String lastTransactionID;
    public Account account = new Account();
    public String lastTransactionTime;

    public Root() {

        getStyleClass().add("root");
    }

    public String getLastTransactionID() {
        return lastTransactionID;
    }

    public void setLastTransactionID(String lastTransactionID) {
        this.lastTransactionID = lastTransactionID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Root{" +
                "lastTransactionID='" + lastTransactionID + '\'' +
                ", account=" + account +
                ", lastTransactionTime='" + lastTransactionTime + '\'' +
                '}';
    }

    public String getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(String lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }


    @Override
    public Node getStyleableNode() {
        return super.getStyleableNode();
    }
}
