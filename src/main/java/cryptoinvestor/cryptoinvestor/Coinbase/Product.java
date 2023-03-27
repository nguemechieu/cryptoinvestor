package cryptoinvestor.cryptoinvestor.Coinbase;

public class Product {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String name;
    public String description;
    public String url;
    public String category;
    public String currency;

    public Product(String name, String description, String url, String category, String currency) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.category = category;
        this.currency = currency;
    }
    public Product(){
        this.name = "";
        this.description = "";
        this.url = "";
        this.category = "";
        this.currency = "";
    }
}
