package tradeexpert.tradeexpert;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TradeExpert extends Application {
    private static final Logger logger = LoggerFactory.getLogger(TradeExpert.class);
    public static void main(String[] args) {
        launch(args);
        logger.info("Application started");

    }

    public TradeExpert() {
        logger.info("TradeExpert started "+ new java.util.Date());
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("TradeExpert   ->            Best Trading Platform           Date: "+ new java.util.Date());
        try {
            primaryStage.setScene(new Scene( new TradeExpertScene(),1530,780));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.getIcons().setAll(
                new Image(Objects.requireNonNull(TradeExpert.class.getResourceAsStream("/img/icon.ico")))
        );

        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
