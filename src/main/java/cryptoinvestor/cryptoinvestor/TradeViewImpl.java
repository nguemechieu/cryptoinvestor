package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

public class TradeViewImpl extends Region {
    private static final Logger logger = LoggerFactory.getLogger(TradeViewImpl.class);
    private TradePair tradePair;
    private Exchange exchange;

    public void setTradePair(TradePair tradePair) {
        this.tradePair = tradePair;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    private  String apiKey;
    private  String apiSecret;

    public  TradeViewImpl(TradePair tradePair,  Exchange exchange, String apiKey, String apiSecret) throws URISyntaxException, IOException, TelegramApiException, ParseException, InterruptedException, NoSuchAlgorithmException {

        super();
        this.tradePair = tradePair;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.exchange = exchange;

  setPadding(new Insets(10, 10, 10, 10));

  setTradePair(tradePair);

  setApiKey(apiKey);
  setApiSecret(apiSecret);
        TabPane anchorPane = new TabPane();
        anchorPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        anchorPane.setTranslateY(25);
        anchorPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        anchorPane.setSide(Side.BOTTOM);
        for (Currency tradePair1 : getTradePairs()) {
            int index = getTradePairs().indexOf(tradePair1);
            DraggableTab tradeTab = new DraggableTab(tradePair1.code + " - " + "USD","");
            VBox container=new VBox();
            container.setStyle("-fx-background-size: 100% 100%;");
            container.setPrefSize(1500,500
            );
            Coinbase coinbase = new Coinbase("2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo","2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo","2032573404:AAE3yV0yFvtO8irplRnj2YK59dOXUITC1Eo");
if (index>2){
    break;
}
            tradePair= new TradePair("BTC", "USD");
            CandleStickChartContainer candleStickChartContainer = new CandleStickChartContainer(coinbase,tradePair,true);
            candleStickChartContainer.setPrefSize(1000,400);
            candleStickChartContainer.setBorder(Border.stroke(Color.rgb(25, 195, 255, 0.5)));

            container.getChildren().add(candleStickChartContainer);
            tradeTab.setContent(new VBox(new Label(tradePair1.code + " - " + "USD"),new Separator(), container));
            anchorPane.getTabs().add(tradeTab);

            Label lab=new Label(tradePair1.code + " - " + "USD");
            if (tradePair1.getImage()!= null &&!tradePair1.getImage().isEmpty()) {
                container.setStyle("-fx-background-image: url("+tradePair1.getImage()+");");
                container.setStyle("-fx-background-repeat: no-repeat;");
                container.setStyle("-fx-background-position: center center;");
                container.setStyle("-fx-background-size: 100% 100%;");
                lab.setStyle("-fx-font-weight: bold;");
                lab.setPadding(new Insets(10, 10, 10, 10));
                tradeTab.setDetachable(true);

               //lab.setGraphic(new ImageView( tradePair1.getImage()));
                anchorPane.getTabs().get(index).setGraphic(lab);
                tradeTab.setGraphic(new ImageView( tradePair1.getImage()));
                tradeTab.setStyle("-fx-background-image: url(" + tradePair1.getImage() + ");");
            }
            else {
                tradeTab.setDetachable(true);
               anchorPane. setStyle("-fx-background-size: 100% 100%;");
                anchorPane.setStyle("-fx-background-color: #000000;");
                lab.setStyle("-fx-font-weight: bold;");
                setTradePair(tradePair);
                anchorPane.getTabs().get(index).setGraphic(lab);
                setBorder( Border.stroke(Color.web("#000000")));
            }
            logger.info(tradePair1.code + " - " + "USD");
        }
        anchorPane.setSide(Side.BOTTOM);
        getChildren().add(anchorPane);setPrefSize(1300, 600);
    }
    public  TradePair  getTradePair() {
        return tradePair;
    }
    @NotNull List<Currency> getTradePairs() {
        return CurrencyDataProvider.getInstance();
    }
    public  double getPrice() {
        return 0;
    }

    public  double getVolume() {
        return 0;
    }
    public  Exchange getExchange() {
        return exchange;
    }
    public  String getApiKey() {
        return null;
    }
    public  String getApiSecret() {
        return null;
    }
    public  String getApiKeySecret() {
        return null;
    }

    // Constructor




















}
