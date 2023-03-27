package cryptoinvestor.cryptoinvestor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
public class CryptoInvestor extends Application {
    private static final Logger logger = LoggerFactory.getLogger(CryptoInvestor.class);

    public CryptoInvestor() {
        logger.info("CryptoInvestor started " + new java.util.Date());
    }

    public static void main(String[] args) throws BackingStoreException {

        Preferences preferences = Preferences.userNodeForPackage(
                CryptoInvestor.class
        );
        preferences.put("version", String.valueOf(0.01));
        preferences.put("last_update", String.valueOf(new java.util.Date()));
        preferences.put("username", "root");
        preferences.put("password", "root307#");
        try {
         preferences.exportNode(new FileOutputStream(System.getProperty("user.home") + "/.config/cryptoinvestor.xml"));


        } catch (BackingStoreException | IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }


        preferences.flush();

        launch(args);
        logger.info("Application started");
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> logger.error("[" + thread + "]: ", exception));


        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cryptoinvestor"))))
        ;

        TradingWindow tradingWindow;
        try {
            tradingWindow = new TradingWindow();

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setTitle("CryptoInvestor                    " + new java.util.Date() );
        primaryStage.setScene(new Scene(tradingWindow, 1530, 780));
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.getIcons().setAll(new Image(Objects.requireNonNull(CryptoInvestor.class.getResourceAsStream("/cryptoInvestor/cryptoInvestor/MediumSquareLogo.jpg"))));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
