package cryptoinvestor.cryptoinvestor.Coinbase;

public class Fill {

    private double price;
    private int liquidity;

    public Fill() {

    }

    public Fill(int liquidity, double price) {
        this.liquidity = liquidity;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}
