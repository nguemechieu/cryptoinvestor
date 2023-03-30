package cryptoinvestor.cryptoinvestor;

public class CurrencyPair {
    public String base;
    public String quote;

    @Override
    public String toString() {
        return "CurrencyPair{" +
                "base='" + base + '\'' +
                ", quote='" + quote + '\'' +
                '}';
    }

    public CurrencyPair(String btc, String usd) {
        this.base = btc;
        this.quote = usd;
    }
}
