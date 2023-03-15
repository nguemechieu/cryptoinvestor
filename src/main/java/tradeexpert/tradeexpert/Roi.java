package tradeexpert.tradeexpert;

public class Roi {
    public String times;
    public String currency;
    public String percentage;

    @Override
    public String toString() {
        return "Roi{" +
                "times='" + times + '\'' +
                ", currency='" + currency + '\'' +
                ", percentage='" + percentage + '\'' +
                '}';
    }

    public Roi(String times, String currency, String percentage) {
        this.times = times;
        this.currency = currency;
        this.percentage = percentage;
    }
}
