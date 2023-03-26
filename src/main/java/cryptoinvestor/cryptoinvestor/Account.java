package cryptoinvestor.cryptoinvestor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.Date;


@Entity

public class Account implements Serializable {

String          uuid;
//    string
//    Unique identifier for account.
String           name;
    private double available;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Object getAvailable_balance() {
        return available_balance;
    }

    public void setAvailable_balance(Object available_balance) {
        this.available_balance = available_balance;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public Date getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Date deleted_at) {
        this.deleted_at = deleted_at;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPossible_values() {
        return Possible_values;
    }

    public void setPossible_values(Object possible_values) {
        Possible_values = possible_values;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public Object getHold() {
        return hold;
    }

    public void setHold(Object hold) {
        this.hold = hold;
    }

    public Object getExchange() {
        return exchange;
    }

    public void setExchange(Object exchange) {
        this.exchange = exchange;
    }

    //            string
//    Name for the account.
String   currency;
//            string
   String symbol;// for the account.
Object    available_balance;
//            object
//    required
Object          value;
//    string
//    Amount of currency that this object represents.
//    currency
//            string
//    Denomination of the currency.
//    default
//    boolean
//    Whether or not this account is the user's primary account
boolean   active;
//    boolean
//    Whether or not this account is active and okay to use.
Date           created_at;
//   date_time;
//    Time at which this account was created.
Date updated_at;
//    date-time
//    Time at which this account was updated.
Date            deleted_at;
//    date-time
//    Time at which this account was deleted.
String            type;
//            string
  Object Possible_values;//: //[ACCOUNT_TYPE_UNSPECIFIED, ACCOUNT_TYPE_CRYPTO, ACCOUNT_TYPE_FIAT, ACCOUNT_TYPE_VAULT]
  boolean  ready;
//    boolean
//    Whether or not this account is ready to trade.
Object           hold;
//            object
//    required

//    string
//    Amount of currency that this object represents.
//            string
//    Denomination of the currency.
    @JsonProperty("createdByUserID")
    public int createdByUserID;
    @JsonProperty("NAV")
    public String nAV;
    public String marginCloseoutUnrealizedPL;
    public String marginCallMarginUsed;
    public int openPositionCount;
    public String withdrawalLimit;
    public String positionValue;
    public double marginRate;
    public String marginCallPercent;
    public double balance;
    public String lastTransactionID;
    public double resettablePL;
    public double financing;
    public String createdTime;
    public String alias;

    public double commission;
    public double marginCloseoutPercent;
    @Id
    public String id;
    public int openTradeCount;
    public int pendingOrderCount;
    public boolean hedgingEnabled;
    public String resettablePLTime;

    public String marginAvailable;
    public String dividendAdjustment;
    public String marginCloseoutPositionValue;
    public String marginCloseoutMarginUsed;
    public String unrealizedPL;
    public String marginCloseoutNAV;
    public String guaranteedStopLossOrderMode;
    public String marginUsed;
    public String guaranteedExecutionFees;


    public String pl;
    public Object exchange;

    public Account() {
    }

    @Override
    public String toString() {
        return "Account{" +
                "createdByUserID=" + createdByUserID +
                ", nAV='" + nAV + '\'' +
                ", marginCloseoutUnrealizedPL='" + marginCloseoutUnrealizedPL + '\'' +
                ", marginCallMarginUsed='" + marginCallMarginUsed + '\'' +
                ", openPositionCount=" + openPositionCount +
                ", withdrawalLimit='" + withdrawalLimit + '\'' +
                ", positionValue='" + positionValue + '\'' +
                ", marginRate=" + marginRate +
                ", marginCallPercent='" + marginCallPercent + '\'' +
                ", balance=" + balance +
                ", lastTransactionID='" + lastTransactionID + '\'' +
                ", resettablePL=" + resettablePL +
                ", financing=" + financing +
                ", createdTime='" + createdTime + '\'' +
                ", alias='" + alias + '\'' +
                ", currency='" + currency + '\'' +
                ", commission=" + commission +
                ", marginCloseoutPercent=" + marginCloseoutPercent +
                ", id='" + id + '\'' +
                ", openTradeCount=" + openTradeCount +
                ", pendingOrderCount=" + pendingOrderCount +
                ", hedgingEnabled=" + hedgingEnabled +
                ", resettablePLTime='" + resettablePLTime + '\'' +

                ", marginAvailable='" + marginAvailable + '\'' +
                ", dividendAdjustment='" + dividendAdjustment + '\'' +
                ", marginCloseoutPositionValue='" + marginCloseoutPositionValue + '\'' +
                ", marginCloseoutMarginUsed='" + marginCloseoutMarginUsed + '\'' +
                ", unrealizedPL='" + unrealizedPL + '\'' +
                ", marginCloseoutNAV='" + marginCloseoutNAV + '\'' +
                ", guaranteedStopLossOrderMode='" + guaranteedStopLossOrderMode + '\'' +
                ", marginUsed='" + marginUsed + '\'' +
                ", guaranteedExecutionFees='" + guaranteedExecutionFees + '\'' +

                ", pl='" + pl + '\'' +
                '}';
    }

    public int getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(int createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    public String getnAV() {
        return nAV;
    }

    public void setnAV(String nAV) {
        this.nAV = nAV;
    }

    public String getMarginCloseoutUnrealizedPL() {
        return marginCloseoutUnrealizedPL;
    }

    public void setMarginCloseoutUnrealizedPL(String marginCloseoutUnrealizedPL) {
        this.marginCloseoutUnrealizedPL = marginCloseoutUnrealizedPL;
    }

    public String getMarginCallMarginUsed() {
        return marginCallMarginUsed;
    }

    public void setMarginCallMarginUsed(String marginCallMarginUsed) {
        this.marginCallMarginUsed = marginCallMarginUsed;
    }

    public int getOpenPositionCount() {
        return openPositionCount;
    }

    public void setOpenPositionCount(int openPositionCount) {
        this.openPositionCount = openPositionCount;
    }

    public String getWithdrawalLimit() {
        return withdrawalLimit;
    }

    public void setWithdrawalLimit(String withdrawalLimit) {
        this.withdrawalLimit = withdrawalLimit;
    }

    public String getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(String positionValue) {
        this.positionValue = positionValue;
    }

    public double getMarginRate() {
        return marginRate;
    }

    public void setMarginRate(double marginRate) {
        this.marginRate = marginRate;
    }

    public String getMarginCallPercent() {
        return marginCallPercent;
    }

    public void setMarginCallPercent(String marginCallPercent) {
        this.marginCallPercent = marginCallPercent;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getLastTransactionID() {
        return lastTransactionID;
    }

    public void setLastTransactionID(String lastTransactionID) {
        this.lastTransactionID = lastTransactionID;
    }

    public double getResettablePL() {
        return resettablePL;
    }

    public void setResettablePL(double resettablePL) {
        this.resettablePL = resettablePL;
    }

    public double getFinancing() {
        return financing;
    }

    public void setFinancing(double financing) {
        this.financing = financing;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getMarginCloseoutPercent() {
        return marginCloseoutPercent;
    }

    public void setMarginCloseoutPercent(double marginCloseoutPercent) {
        this.marginCloseoutPercent = marginCloseoutPercent;
    }

    public int getOpenTradeCount() {
        return openTradeCount;
    }

    public void setOpenTradeCount(int openTradeCount) {
        this.openTradeCount = openTradeCount;
    }

    public int getPendingOrderCount() {
        return pendingOrderCount;
    }

    public void setPendingOrderCount(int pendingOrderCount) {
        this.pendingOrderCount = pendingOrderCount;
    }

    public boolean isHedgingEnabled() {
        return hedgingEnabled;
    }

    public void setHedgingEnabled(boolean hedgingEnabled) {
        this.hedgingEnabled = hedgingEnabled;
    }

    public String getResettablePLTime() {
        return resettablePLTime;
    }

    public void setResettablePLTime(String resettablePLTime) {
        this.resettablePLTime = resettablePLTime;
    }

    public String getMarginAvailable() {
        return marginAvailable;
    }

    public void setMarginAvailable(String marginAvailable) {
        this.marginAvailable = marginAvailable;
    }

    public String getDividendAdjustment() {
        return dividendAdjustment;
    }

    public void setDividendAdjustment(String dividendAdjustment) {
        this.dividendAdjustment = dividendAdjustment;
    }

    public String getMarginCloseoutPositionValue() {
        return marginCloseoutPositionValue;
    }

    public void setMarginCloseoutPositionValue(String marginCloseoutPositionValue) {
        this.marginCloseoutPositionValue = marginCloseoutPositionValue;
    }

    public String getMarginCloseoutMarginUsed() {
        return marginCloseoutMarginUsed;
    }

    public void setMarginCloseoutMarginUsed(String marginCloseoutMarginUsed) {
        this.marginCloseoutMarginUsed = marginCloseoutMarginUsed;
    }

    public String getUnrealizedPL() {
        return unrealizedPL;
    }

    public void setUnrealizedPL(String unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    public String getMarginCloseoutNAV() {
        return marginCloseoutNAV;
    }

    public void setMarginCloseoutNAV(String marginCloseoutNAV) {
        this.marginCloseoutNAV = marginCloseoutNAV;
    }

    public String getGuaranteedStopLossOrderMode() {
        return guaranteedStopLossOrderMode;
    }

    public void setGuaranteedStopLossOrderMode(String guaranteedStopLossOrderMode) {
        this.guaranteedStopLossOrderMode = guaranteedStopLossOrderMode;
    }

    public String getMarginUsed() {
        return marginUsed;
    }

    public void setMarginUsed(String marginUsed) {
        this.marginUsed = marginUsed;
    }

    public String getGuaranteedExecutionFees() {
        return guaranteedExecutionFees;
    }

    public void setGuaranteedExecutionFees(String guaranteedExecutionFees) {
        this.guaranteedExecutionFees = guaranteedExecutionFees;
    }

    public String getPl() {
        return pl;
    }

    public void setPl(String pl) {
        this.pl = pl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = String.valueOf(id);
    }

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
    }
}
