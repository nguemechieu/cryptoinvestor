package cryptoinvestor.cryptoinvestor;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.*;

public class Navigator extends Region {
    public Navigator() {
        super();
        getStyleClass().add("navigator");

        setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        VBox vBox = new VBox();
        HBox hBox = new HBox(new Region(), new Region(), new Region());



        vBox.getChildren().addAll(hBox);


        getChildren().add(vBox);

    }

    private class HomeButton extends Button {
        public HomeButton() {
            super();
            getStyleClass().add("home-button");
            setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }
    }

    private class AboutButton extends Button {
        public AboutButton() {
            super();
            getStyleClass().add("about-button");
            setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }
    }

    private class ExitButton extends Button {
        public ExitButton() {
            super();
            getStyleClass().add("exit-button");
            setPrefSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }
    }
}
