package org.tradeexpert.tradeexpert;


import org.tradeexpert.tradeexpert.oanda.Oanda;

import static java.lang.System.out;
import static org.tradeexpert.tradeexpert.oanda.Oanda.getApi_key;

public class Accounts {
    double balance;
    private String tags;
    private String name;
    private String currency;
    private String accountType;
    private String accountStatus;
    private String tradingTimeZone;
    private String tradingTime;
    private String tradingMode;
    private String tradingStatus;
    private String tradingSession;

    public Accounts() {
        super();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addTag(String name) {
        if (this.tags == null) {
            this.tags = "";
        } else this.tags = this.tags + "," + name;
    }

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public String getTradingTimeZone() {
        return tradingTimeZone;
    }

    public String getTradingTime() {
        return tradingTime;
    }

    public String getTradingMode() {
        return tradingMode;
    }

    public String getTradingStatus() {
        return tradingStatus;
    }

    public String getTradingSession() {
        return tradingSession;
    }

    public String getToken() throws Exception {
        Oanda oanda=new Oanda(
                getApi_key(),Oanda.accountID
        );

        out.println(oanda);
        return
                getApi_key();
    }
}
