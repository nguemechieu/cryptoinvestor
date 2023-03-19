package cryptoinvestor.cryptoinvestor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CryptoInvestor extends Application {
    private static final Logger logger = LoggerFactory.getLogger(CryptoInvestor.class);
    public static void main(String[] args) {
        launch(args);
        logger.info("Application started");

    }

    public CryptoInvestor() {
        logger.info("CryptoInvestor started "+ new java.util.Date());
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("CryptoInvestor  ->            Best Investment Platform           Date: "+ new java.util.Date());
        try {
            primaryStage.setScene(new Scene( new CryptoInvestorScene(),1530,780));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.getIcons().setAll(new Image(Objects.requireNonNull(CryptoInvestor.class.getResourceAsStream("/Crypto Investor/MediumSquareLogo.jpg"))));

        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
