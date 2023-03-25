package cryptoinvestor.cryptoinvestor;

import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;

import java.text.ParseException;
import java.util.Date;

import static cryptoinvestor.cryptoinvestor.NewsManager.getNewsList;

public class NewsView extends TreeTableView<News> {

    public NewsView() throws ParseException {
        super();

        this.setPrefHeight(780);
        this.setPrefWidth(1530);


        setPrefSize(1530, 780);
        TreeTableColumn<News,String> titleColumn = new TreeTableColumn<>("Title");
        titleColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getTitle()));

        TreeTableColumn<News,String> dateColumn = new TreeTableColumn<>("Date");
        dateColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getDate().toString()));
        TreeTableColumn<News,String> impactColumn = new TreeTableColumn<>("Impact");
        impactColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getImpact()));
        TreeTableColumn<News,String>  forecastColumn = new TreeTableColumn<>("Forecast");
        forecastColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getForecast()));
        TreeTableColumn<News,String> previousColumn = new TreeTableColumn<>("Previous");
        previousColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getPrevious()));
        TreeTableColumn<News,String> countryColumn = new TreeTableColumn<>("Country");
        countryColumn.setCellValueFactory(param ->new ReadOnlyStringWrapper( param.getValue().getValue().getCountry()));
        TreeItem<News> root = new TreeItem<>();
        root.setExpanded(true);

root.setValue(
        new News(
                "Crypto Investor",
                "2020-01-,","",
                new Date(),
                "Crypto Investor is a cryptocurrency investment platform based on blockchain technology.",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        )
);

getColumns().addAll(
                titleColumn,
                dateColumn,
                countryColumn,
                impactColumn,
                forecastColumn,
                previousColumn
        );





    }


}
