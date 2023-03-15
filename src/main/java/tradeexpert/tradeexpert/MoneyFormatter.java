package tradeexpert.tradeexpert;


public interface MoneyFormatter<T extends Money> {
    String format(T money);
}
