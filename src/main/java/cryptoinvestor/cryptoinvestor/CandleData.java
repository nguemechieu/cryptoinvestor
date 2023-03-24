package cryptoinvestor.cryptoinvestor;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CandleData")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")

@Access(AccessType.FIELD)

public class CandleData extends RecursiveTreeObject<CandleData> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

public static final Logger logger = LoggerFactory.getLogger(CandleData.class);
    private final double averagePrice = -1;
    public int closeTime;
    int openTime;
    double openPrice;
    double closePrice;
    double highPrice;
    double lowPrice;
    double volume;
    private double volumeWeightedAveragePrice;
    private boolean placeHolder;
    @Id
    private Long id;

    public CandleData(double openPrice, double closePrice, double highPrice, double lowPrice, int openTime,
                      double volume) {


        if (openTime == -1 && openPrice == 1 && closePrice == -1 && highPrice == -1 && lowPrice == -1 && volume == -1) {

            throw new IllegalArgumentException("Invalid CandleData openTime: " + openTime + " closeTime: " + closePrice + " highPrice: " + highPrice + " lowPrice: " + lowPrice);

        }

        this.openTime = openTime;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;

        this.placeHolder = false;
        this.setId(Math.round(Math.random() * 1000000000));
        this.volumeWeightedAveragePrice = 0;
        this.closeTime = -1;
        logger.info("CandleData created with id: " + this.id);
    }

    public CandleData() {

    }


    public int getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(int closeTime) {
        this.closeTime = closeTime;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public int getOpenTime() {

        return openTime;
    }

    public double getVolume() {
        return volume;
    }

    public double getAveragePrice() {

        double p = openPrice + closePrice + highPrice + lowPrice;
        if (p == 0) {
            return 0;
        }
        return p / 4;


    }

    public double getVolumeWeightedAveragePrice() {
        return volumeWeightedAveragePrice;
    }

    public boolean isPlaceHolder() {
        return placeHolder;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        CandleData other = (CandleData) object;

        return openPrice == other.openPrice && closePrice == other.closePrice && highPrice == other.highPrice && lowPrice == other.lowPrice && Objects.equals(openTime, other.openTime) && volume == other.volume && volumeWeightedAveragePrice == other.volumeWeightedAveragePrice && placeHolder == other.placeHolder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(openPrice, closePrice, highPrice, lowPrice, openTime, volume, averagePrice,
                volumeWeightedAveragePrice, placeHolder);
    }

    @Override
    public String toString() {
        return "CandleData{" +
                "averagePrice=" + averagePrice +
                ", closeTime=" + closeTime +
                ", openTime=" + openTime +
                ", openPrice=" + openPrice +
                ", closePrice=" + closePrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", volume=" + volume +
                ", volumeWeightedAveragePrice=" + volumeWeightedAveragePrice +
                ", placeHolder=" + placeHolder +
                ", id=" + id +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getChangePercent() {
        return (closePrice - openPrice) / openPrice * 100;
    }
}
