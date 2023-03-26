package cryptoinvestor.cryptoinvestor;

public enum CurrencyType {
    FIAT,
    CRYPTO,
    NULL;

    public short getIsoCode() {
        return (short) ordinal();
    }
}
