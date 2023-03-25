package cryptoinvestor.cryptoinvestor;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;

public class Browser extends Region {
    static final String[] urls = new String[]{
            "https://www.google.com/search?q",
            "https://www.ebay.com",
            "https://www.amazon.com",
            "https://www.facebook.com"
    };

    static String[] imageFiles = new String[]{
           "7123025_logo_google_g_icon.png",
            "8377-ebay_102466 (1).png",
            "amazon.png",
            "Facebook-PNG-Photos (1).png"};




    static ImageView selectedImage = new ImageView();

    static
    WebView webView = new WebView();


    WebEngine webEngine = webView.getEngine();
    String[] captions = new String[]{
            "Google",
            "Ebay",
            "Amazon",
            "Facebook"
    };
    final Hyperlink[] pls
            = new Hyperlink[captions.length];
    HBox toolBar;
    ComboBox<String> comboBox = new ComboBox<>();
    boolean needDocumentationButton = false;
    TabPane tabPane = new TabPane();


    public Browser() {
        //apply the styles'

        getStyleClass().add("browser");
        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        //process history
        WebHistory history = webEngine.getHistory();
        history.getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
            c.next();
            for (WebHistory.Entry e : c.getRemoved()) {
                comboBox.getItems().remove(e.getUrl());
            }
            for (WebHistory.Entry e : c.getAddedSubList()) {
                comboBox.getItems().add(e.getUrl());
            }
        });

        //set the behavior for the history combobox
        comboBox.setOnAction(ev -> {
            int offset = comboBox.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
            history.go(offset);
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                history.go(history.getCurrentIndex() + 1);
            }
        });
        // process page loading


        // load the home pag

        DraggableTab tab = new DraggableTab(new Date().toString(), "");
        WebView webView1 = new WebView();
        webView1.getEngine().load("https://www.google.com");
        tab.setContent(webView1);
        tabPane.getTabs().add(tab);

         tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        Button addButton = new Button("+");
        tabPane.setTranslateY(25);

        tabPane.setPrefSize(1530, 780);
        VBox  h1box=new VBox();
        h1box.setAlignment(Pos.CENTER);
        h1box.setSpacing(5);

        h1box.getChildren().addAll(new HBox(removeButton(),addButton,refreshButton(),screenShotButton(),goBack(),goForward(),downloadButton()),tabPane);


        TextField label=new TextField();

        label.setBackground(
               Background.fill(
                       Paint.valueOf(
                               "rgba(25, 189, 189, 1)"

                       )
               )
        );
        addButton.setOnAction(e ->{   DraggableTab tab1 = new DraggableTab(captions[0], "");
            label.setPromptText("  Enter a keyword to search for");
            label.setOnAction(ev -> {
                String keyword = label.getText();
                if (keyword!= null &&!keyword.isEmpty()) {
                    WebView webView3 = new WebView();
                    webView3.getEngine().load("https://www.google.com/search?q=" + keyword);

                    tabPane.getSelectionModel().getSelectedItem().setContent(webView3);
                }
            });

            WebView webView4 = new WebView();
            webView4.getEngine().load("https://www.google.com");
            tab1.setContent(webView4);
            tabPane.getTabs().add(tab1);
        });

        label.setPrefWidth(800);
        label.setMaxHeight(100);
        label.setPromptText("Enter a keyword to search for");
        HBox searchBox = new HBox(comboBox,label);
        getChildren().addAll(new VBox(hbox,new Separator(), searchBox, h1box));
        label.setOnAction(ev -> {
            String keyword = label.getText();
            if (keyword!= null &&!keyword.isEmpty()) {
                WebView webView0= new WebView();
                webView0.getEngine().load("https://www.google.com/search?q=" + keyword);

                tabPane.getSelectionModel().getSelectedItem().setContent(webView0);
                label.setText("");
                tabPane.getTabs().add(
                        new DraggableTab(captions[0], "")
                );
            }
        });




    }




private @NotNull Button downloadButton(){
        Button button = new Button("Download");
        button.setOnAction(e -> webEngine.executeScript("window.print()"));
        return button;

}

    private @NotNull Button removeButton() {
        Button button = new Button("-");
        button.setOnAction(e -> tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex()));

        return button;
    }

    private @NotNull Button refreshButton() {
        Button button = new Button("Refresh");
        button.setOnAction(e -> webEngine.reload());
        return button;
    }

    private @NotNull Node createImage(String url) {
        Image image = new Image(url);
        selectedImage.setImage(image);
        return selectedImage;
    }
    private @NotNull Node createLink(String url) {
        Hyperlink link = new Hyperlink(url);
        link.setOnAction(e -> webEngine.load(url));
        return link;
    }
    private @NotNull  Button goBack () {
        Button button = new Button("Go Back");
        button.setOnAction(e -> webEngine.getHistory().go(-1));
        return button;
    }
    private @NotNull  Button goForward () {
        Button button = new Button("Go Forward");
        button.setOnAction(e -> webEngine.getHistory().go(1));
        return button;
    }

    private @NotNull Button screenShotButton() {
        Button button = new Button("Screen Shot");
        button.setOnAction(e -> {
            webEngine.executeScript("window.print()");
            webEngine.getDocument().createElement("img");
            Screenshot.capture(new File("screenshot.png"));

        });
        return button;
    }
    private @NotNull Node createSpacer() {
        Region region = new Region();
        region.getStyleClass().add("app");
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    @Override
    public Node getStyleableNode() {
        return super.getStyleableNode();
    }

    // JavaScript interface object
    public static class JavaApp {
        @Override
        public String toString() {
            return "JavaScript App";
        }
    }


}