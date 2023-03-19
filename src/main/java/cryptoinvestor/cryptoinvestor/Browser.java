package cryptoinvestor.cryptoinvestor;

import jakarta.persistence.Table;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


import java.util.Date;
import java.util.Objects;

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
    static WebView smallView = new WebView();
    static
    WebView webView = new WebView();
     WebEngine webEngine = webView.getEngine();
    final Image[] images = new Image[imageFiles.length];
    final Button showPrevDoc = new Button("Toggle Previous Docs");
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

     private final DraggableTab tab=new DraggableTab(new Date().toString());

     public Browser() {
        //apply the styles'
        //pane.getStyleClass().add("app.css");
        for (int i = 0; i < captions.length; i++) {
            // create hyperlinks
            Hyperlink hpl = pls[i] = new Hyperlink(captions[i]);
//             Image image = images[i] = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imageFiles[i])));
//             hpl.setGraphic(new ImageView(image));
//              String url = urls[i];


            boolean addButton = (hpl.getText().equals("Documentation"));
            // process event
            hpl.setOnAction(e -> {
                needDocumentationButton = addButton;
                webEngine.load(Objects.requireNonNull(getClass().getResourceAsStream(imageFiles[1])).toString());
            });
        }
        // create the toolbar
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().add(comboBox);
        toolBar.getChildren().addAll(pls);
        toolBar.getChildren().add(createSpacer());

        //set action for the button
        showPrevDoc.setOnAction(t -> webEngine.executeScript("toggleDisplay('PrevRel')"));

        smallView.setPrefSize(120, 80);
        //handle popup windows
        webEngine.setCreatePopupHandler(
                config -> {
                    smallView.setFontScale(0.8);
                    if (!toolBar.getChildren().contains(smallView)) {
                        toolBar.getChildren().add(smallView);
                    }
                    return smallView.getEngine();
                }
        );

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
        webEngine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    toolBar.getChildren().remove(showPrevDoc);
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("app", new JavaApp());
                        win.setMember("showPrevDoc", showPrevDoc);
                        win.setMember("selectedImage", selectedImage);
                        if (needDocumentationButton) {
                            toolBar.getChildren().add(showPrevDoc);
                        }
                    }
                });

        // load the home pag
        webEngine.load("https://www.google.com");


        getChildren().add(toolBar);
        getChildren().add(smallView);
        getChildren().add(tabPane);

    }


    @Contract(" -> new")
    public @NotNull Group start() throws Exception {

        DraggableTab tab1 = new DraggableTab("Google");
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(tab1);


        tab1.setClosable(true);
        WebView webWiew1 = new WebView();
        webWiew1.getEngine().load("https://www.google.com/search?q");
        tab1.setContent(webWiew1);
        tab1.setGraphic(new ImageView(selectedImage.getImage()));
        webWiew1.setPrefSize(1500, 780);


        DraggableTab tab2 = new DraggableTab("Amazon");
        tab2.setClosable(true);
        WebView webWiew2 = new WebView();
        webWiew2.getEngine().load("https://www.amazon.com");
        webWiew2.setPrefSize(1500, 780);



        tab.setGraphic(new ImageView(selectedImage.getImage()));
        WebView web3 = new WebView();
        web3.setPrefSize(1500, 780);


        tabPane.getTabs().addAll(tab1);
        Pane pane = new Pane();
        HBox btn = new HBox(removeButton(), addButton());
        btn.setAlignment(Pos.CENTER);
        btn.setTranslateX(
                (tabPane.getTabs().size() - 1) * 100 + 100
        );
        pane.getChildren().addAll(smallView,btn,tabPane);
        pane.setPrefSize(1530, 780);

        return new Group(pane);
    }
TabPane tabPane=new TabPane();
     private @NotNull Button addButton() {
         Button button = new Button("+");
         button.setOnAction(e -> tabPane.getTabs().add(tab));
         return button;
     }

     private @NotNull Button removeButton() {
        Button button = new Button("-");
        button.setOnAction(e -> tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex()));

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