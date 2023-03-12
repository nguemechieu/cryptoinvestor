package org.tradeexpert.tradeexpert;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.tradeexpert.tradeexpert.oanda.OandaException;


import java.util.Objects;

public class TradeExpert extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        primaryStage.setTitle("TradeExpert   ");
        primaryStage.setScene(new Scene( new TradeExpertScene(),1530,780));
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.getIcons().setAll(new Image(Objects.requireNonNull(TradeExpert.class.getResourceAsStream("/img/symbol.png"))));
        primaryStage.centerOnScreen();
        primaryStage.show();

    }
}
