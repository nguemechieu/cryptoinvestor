package cryptoinvestor.cryptoinvestor.Coinbase;

import cryptoinvestor.cryptoinvestor.TradePair;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TestCoinbase {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {

        Coinbase coinbase = new Coinbase("gUl2gfk/zu9o6rqicLtBokMupgGG3j8AqC1kQvZfOj8qDUQdPT0dhDiK0NIOkFPLsGNQ9MjfYtIBHKSieQaJDw==",
                "zdkva105scm");

        //coinbase.getAvailableSymbols();
        //Thread.sleep(10000);
        coinbase.getAvailableSymbols();

        coinbase.getOrderBook(new TradePair("BTC", "USD"));
    }
}
