package org.tradeexpert.tradeexpert;


public record InProgressCandleData(int openTime, double openPrice, double highPriceSoFar, double lowPriceSoFar,
                                   int currentTill, double lastPrice, double volumeSoFar) {

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        InProgressCandleData other = (InProgressCandleData) object;

        return openTime == other.openTime &&
                openPrice == other.openPrice &&
                highPriceSoFar == other.highPriceSoFar &&
                lowPriceSoFar == other.lowPriceSoFar &&
                currentTill == other.currentTill &&
                lastPrice == other.lastPrice &&
                volumeSoFar == other.volumeSoFar;
    }

    @Override
    public String toString() {
        return String.format("InProgressCandleData [openTime = %d, openPrice = %f, highPriceSoFar = %f, " +
                        "lowPriceSoFar = %f, currentTill = %d, lastPrice = %f, volumeSoFar = %f]", openTime, openPrice,
                highPriceSoFar, lowPriceSoFar, currentTill, lastPrice, volumeSoFar);
    }

    public double getLowPriceSoFar() {
        return lowPriceSoFar;
    }

    public double getVolumeSoFar() {
        return volumeSoFar;
    }

    public double getHighPriceSoFar() {
        return highPriceSoFar;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public long getCurrentTill() {
        return currentTill;
    }
}
