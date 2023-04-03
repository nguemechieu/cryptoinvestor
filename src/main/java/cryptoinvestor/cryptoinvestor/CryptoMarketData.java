package cryptoinvestor.cryptoinvestor;

public class CryptoMarketData {

    public Double last_close;
    String id;
    String symbol;
    String name;
    String image;
    String current_price;
    String market_cap;
    String market_cap_rank;
    String fully_diluted_valuation;
    String total_volume;
    String high_24h;
    String low_24h;
    String price_change_24h;
    String price_change_percentage_24h;
    String market_cap_change_24h;
    String market_cap_change_percentage_24h;
    String circulating_supply;
    String total_supply;
    String max_supply;
    String ath;
    String ath_change_percentage;
    String ath_date;
    String atl_change_percentage;
    String atl_date;
    Roi roi;
    private String athChangePercentage;
    private String lastUpdated;
    private String athDate;
    private String atl;
    private String atlChangePercentage;
    private String atlDate;
    private String currency;
    private String times;
    private String percentage;
    private String total_volume_24h;
    private String status;
    private String high24h;
    private String low24h;
    private String open24h;
    private String close24h;
    private String volume24h;

    public CryptoMarketData(String id, String symbol, String name, String image, String currentPrice, String marketCap, String marketCapRank, String fullyDilutedValuation, String totalVolume, String high24h, String low24h, String priceChange24h, String priceChangePercentage24h, String marketCapChange24h, String marketCapChangePercentage24h, String circulatingSupply, String totalSupply, String maxSupply, String ath, String athChangePercentage, String athDate, String atl, String atlChangePercentage, String atlDate, String currency, String times, String percentage, String lastUpdated) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.current_price = currentPrice;
        this.market_cap = marketCap;
        this.market_cap_rank = marketCapRank;
        this.fully_diluted_valuation = fullyDilutedValuation;
        this.total_volume = totalVolume;
        this.high_24h = high24h;
        this.low_24h = low24h;
        this.price_change_24h = priceChange24h;
        this.price_change_percentage_24h = priceChangePercentage24h;
        this.market_cap_change_24h = marketCapChange24h;
        this.market_cap_change_percentage_24h = marketCapChangePercentage24h;
        this.circulating_supply = circulatingSupply;
        this.total_supply = totalSupply;
        this.max_supply = maxSupply;
        this.ath = ath;
        this.athChangePercentage = athChangePercentage;
        this.athDate = athDate;
        this.atl = atl;
        this.atlChangePercentage = atlChangePercentage;
        this.atlDate = atlDate;
        this.currency = currency;
        this.times = times;
        this.percentage = percentage;
        this.lastUpdated = lastUpdated;

    }

    public CryptoMarketData(String id, String symbol, String name, String image, String current_price, String market_cap, String market_cap_rank, String fully_diluted_valuation, String total_volume, String high_24h, String low_24h, String price_change_24h, String price_change_percentage_24h, String market_cap_change_24h, String market_cap_change_percentage_24h, String circulating_supply, String total_supply, String max_supply, String ath, String ath_change_percentage, String ath_date, String atl, String atl_change_percentage, String atl_date, Roi roi) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.current_price = current_price;
        this.market_cap = market_cap;
        this.market_cap_rank = market_cap_rank;
        this.fully_diluted_valuation = fully_diluted_valuation;
        this.total_volume = total_volume;
        this.high_24h = high_24h;
        this.low_24h = low_24h;
        this.price_change_24h = price_change_24h;
        this.price_change_percentage_24h = price_change_percentage_24h;
        this.market_cap_change_24h = market_cap_change_24h;
        this.market_cap_change_percentage_24h = market_cap_change_percentage_24h;
        this.circulating_supply = circulating_supply;
        this.total_supply = total_supply;
        this.max_supply = max_supply;
        this.ath = ath;
        this.ath_change_percentage = ath_change_percentage;
        this.ath_date = ath_date;
        this.atl = atl;
        this.atl_change_percentage = atl_change_percentage;
        this.atl_date = atl_date;
        this.roi = roi;
    }

    public CryptoMarketData() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCurrent_price() {
        return current_price;
    }

    public void setCurrent_price(String current_price) {
        this.current_price = current_price;
    }

    public String getMarket_cap() {
        return market_cap;
    }

    public void setMarket_cap(String market_cap) {
        this.market_cap = market_cap;
    }

    public String getMarket_cap_rank() {
        return market_cap_rank;
    }

    public void setMarket_cap_rank(String market_cap_rank) {
        this.market_cap_rank = market_cap_rank;
    }

    public String getFully_diluted_valuation() {
        return fully_diluted_valuation;
    }

    public void setFully_diluted_valuation(String fully_diluted_valuation) {
        this.fully_diluted_valuation = fully_diluted_valuation;
    }

    public String getTotal_volume() {
        return total_volume;
    }

    public void setTotal_volume(String total_volume) {
        this.total_volume = total_volume;
    }

    public String getHigh_24h() {
        return high_24h;
    }

    public void setHigh_24h(String high_24h) {
        this.high_24h = high_24h;
    }

    public String getLow_24h() {
        return low_24h;
    }

    public void setLow_24h(String low_24h) {
        this.low_24h = low_24h;
    }

    public String getPrice_change_24h() {
        return price_change_24h;
    }

    public void setPrice_change_24h(String price_change_24h) {
        this.price_change_24h = price_change_24h;
    }

    public String getPrice_change_percentage_24h() {
        return price_change_percentage_24h;
    }

    public void setPrice_change_percentage_24h(String price_change_percentage_24h) {
        this.price_change_percentage_24h = price_change_percentage_24h;
    }

    public String getMarket_cap_change_24h() {
        return market_cap_change_24h;
    }

    public void setMarket_cap_change_24h(String market_cap_change_24h) {
        this.market_cap_change_24h = market_cap_change_24h;
    }

    public String getMarket_cap_change_percentage_24h() {
        return market_cap_change_percentage_24h;
    }

    public void setMarket_cap_change_percentage_24h(String market_cap_change_percentage_24h) {
        this.market_cap_change_percentage_24h = market_cap_change_percentage_24h;
    }

    public String getCirculating_supply() {
        return circulating_supply;
    }

    public void setCirculating_supply(String circulating_supply) {
        this.circulating_supply = circulating_supply;
    }

    public String getTotal_supply() {
        return total_supply;
    }

    public void setTotal_supply(String total_supply) {
        this.total_supply = total_supply;
    }

    public String getMax_supply() {
        return max_supply;
    }

    public void setMax_supply(String max_supply) {
        this.max_supply = max_supply;
    }

    public String getAth() {
        return ath;
    }

    public void setAth(String ath) {
        this.ath = ath;
    }

    public String getAth_change_percentage() {
        return ath_change_percentage;
    }

    public void setAth_change_percentage(String ath_change_percentage) {
        this.ath_change_percentage = ath_change_percentage;
    }

    public String getAth_date() {
        return ath_date;
    }

    public void setAth_date(String ath_date) {
        this.ath_date = ath_date;
    }

    public String getAtl() {
        return atl;
    }

    public void setAtl(String atl) {
        this.atl = atl;
    }

    public String getAtl_change_percentage() {
        return atl_change_percentage;
    }

    public void setAtl_change_percentage(String atl_change_percentage) {
        this.atl_change_percentage = atl_change_percentage;
    }

    public String getAtl_date() {
        return atl_date;
    }

    public void setAtl_date(String atl_date) {
        this.atl_date = atl_date;
    }

    public Roi getRoi() {
        return roi;
    }

    public void setRoi(Roi roi) {
        this.roi = roi;
    }

    public String getAthChangePercentage() {
        return athChangePercentage;
    }

    public void setAthChangePercentage(String athChangePercentage) {
        this.athChangePercentage = athChangePercentage;
    }

    @Override
    public String toString() {
        return "CryptoMarketData{" +
                "athChangePercentage='" + athChangePercentage + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", athDate='" + athDate + '\'' +
                ", atl='" + atl + '\'' +
                ", atlChangePercentage='" + atlChangePercentage + '\'' +
                ", atlDate='" + atlDate + '\'' +
                ", currency='" + currency + '\'' +
                ", times='" + times + '\'' +
                ", percentage='" + percentage + '\'' +
                ", id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", current_price='" + current_price + '\'' +
                ", market_cap='" + market_cap + '\'' +
                ", market_cap_rank='" + market_cap_rank + '\'' +
                ", fully_diluted_valuation='" + fully_diluted_valuation + '\'' +
                ", total_volume='" + total_volume + '\'' +
                ", high_24h='" + high_24h + '\'' +
                ", low_24h='" + low_24h + '\'' +
                ", price_change_24h='" + price_change_24h + '\'' +
                ", price_change_percentage_24h='" + price_change_percentage_24h + '\'' +
                ", market_cap_change_24h='" + market_cap_change_24h + '\'' +
                ", market_cap_change_percentage_24h='" + market_cap_change_percentage_24h + '\'' +
                ", circulating_supply='" + circulating_supply + '\'' +
                ", total_supply='" + total_supply + '\'' +
                ", max_supply='" + max_supply + '\'' +
                ", ath='" + ath + '\'' +
                ", ath_change_percentage='" + ath_change_percentage + '\'' +
                ", ath_date='" + ath_date + '\'' +
                ", atl_change_percentage='" + atl_change_percentage + '\'' +
                ", atl_date='" + atl_date + '\'' +
                ", roi=" + roi +
                '}';
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getAthDate() {
        return athDate;
    }

    public void setAthDate(String athDate) {
        this.athDate = athDate;
    }

    public String getAtlChangePercentage() {
        return atlChangePercentage;
    }

    public void setAtlChangePercentage(String atlChangePercentage) {
        this.atlChangePercentage = atlChangePercentage;
    }

    public String getAtlDate() {
        return atlDate;
    }

    public void setAtlDate(String atlDate) {
        this.atlDate = atlDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }


    public String getTotal_volume_24h() {
        return total_volume_24h;
    }

    public String getStatus() {
        return status;
    }

    public String getHigh24h() {
        return high24h;
    }

    public String getLow24h() {
        return low24h;
    }

    public String getOpen24h() {
        return open24h;
    }

    public String getClose24h() {
        return close24h;
    }

    public String getVolume24h() {
        return volume24h;
    }
}
