package cryptoinvestor.cryptoinvestor.oanda;

public record OandaTransaction() {
    private static String transactionId ;

    public static String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String toString) {
        transactionId = toString;
    }
}
