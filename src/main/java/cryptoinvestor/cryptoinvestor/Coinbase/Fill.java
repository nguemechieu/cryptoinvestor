package cryptoinvestor.cryptoinvestor.Coinbase;

public class Fill {

    private double price;

    public Fill() {

    }

    public Fill(int liquidity, double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}
