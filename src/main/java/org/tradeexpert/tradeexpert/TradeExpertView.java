package org.tradeexpert.tradeexpert;

import javafx.scene.Parent;

public class TradeExpertView extends Parent {

    public TradeExpertView() {
        super();

        viewController = new ViewController(this);

    }

    ViewController viewController;

}
