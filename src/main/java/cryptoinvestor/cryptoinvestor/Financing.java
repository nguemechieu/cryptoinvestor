package cryptoinvestor.cryptoinvestor;

import java.util.ArrayList;

public class Financing {
    public String shortRate;
    public String longRate;
    public ArrayList<FinancingDaysOfWeek> financingDaysOfWeek;

    public Financing() {
        financingDaysOfWeek = new ArrayList<>();
    }

    public String getShortRate() {
        return shortRate;
    }

    public String getLongRate() {
        return longRate;
    }

    public ArrayList<FinancingDaysOfWeek> getFinancingDaysOfWeek() {
        return financingDaysOfWeek;
    }
}
