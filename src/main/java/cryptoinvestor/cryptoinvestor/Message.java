package cryptoinvestor.cryptoinvestor;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Message extends Stage {
    public Message(@NotNull MessageType title, @NotNull Object message) {
        super();
        setTitle(
                title.name()
        );
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(new VBox(new Label(message.toString())));
        dialogPane.setHeaderText(null);
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());
        dialogPane.setMaxWidth(Double.MAX_VALUE);
        dialogPane.setMaxHeight(Double.MAX_VALUE);
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(600);
        setScene(new Scene(dialogPane));
        setResizable(true);
        setAlwaysOnTop(true);
        show();

    }

    public enum MessageType {
        INFO,
        WARNING,
        ERROR
    }
}
