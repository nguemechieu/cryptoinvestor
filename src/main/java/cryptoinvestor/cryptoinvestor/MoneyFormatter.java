package cryptoinvestor.cryptoinvestor;


public interface MoneyFormatter<T extends Money> {
    String format(T money);
}
