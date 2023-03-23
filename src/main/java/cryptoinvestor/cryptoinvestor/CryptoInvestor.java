package cryptoinvestor.cryptoinvestor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CryptoInvestor extends Application {
    private static final Logger logger = LoggerFactory.getLogger(CryptoInvestor.class);

    public CryptoInvestor() {
        logger.info("CryptoInvestor started " + new java.util.Date());
    }

    public static void main(String[] args) {
        launch(args);
        logger.info("Application started");
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {


        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cryptoinvestor"))))
        ;

        TradingWindow tradingWindow;
        try {
            tradingWindow = new TradingWindow();

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setTitle("CryptoInvestor  ->             Best Investment Software Date: " + new java.util.Date() + "         version :" + 0.01);
        primaryStage.setScene(new Scene(tradingWindow, 1530, 780));
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.getIcons().setAll(new Image(Objects.requireNonNull(CryptoInvestor.class.getResourceAsStream("/cryptoInvestor/cryptoInvestor/MediumSquareLogo.jpg"))));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
