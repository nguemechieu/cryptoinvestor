package cryptoinvestor.cryptoinvestor;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExchangeViewPane extends AnchorPane {
    TabPane tabPane = new TabPane();
    List<ENUM_EXCHANGE_LIST> exChangeList = new ArrayList<>(

    );
    private final Logger logger = LoggerFactory.getLogger(ExchangeViewPane.class);

    public ExchangeViewPane() {

        super();


        exChangeList.add(ENUM_EXCHANGE_LIST.BINANCE_US);
        exChangeList.add(ENUM_EXCHANGE_LIST.COINBASE_PRO);
        exChangeList.add(ENUM_EXCHANGE_LIST.OANDA);
        exChangeList.add(ENUM_EXCHANGE_LIST.KRAKEN);
        exChangeList.add(ENUM_EXCHANGE_LIST.KUCOIN);


        for (ENUM_EXCHANGE_LIST enumExchangeList : exChangeList) {
            Tab tab = new Tab();
            tab.setText(enumExchangeList.name());

            Label lab = new Label(enumExchangeList.name());
            getChildren().add(lab);

            tabPane.getTabs().add(tab);
        }


        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        tabPane.setPrefSize(1530, 730);
        getChildren().add(
                tabPane
        );
    }







}
