package cryptoinvestor.cryptoinvestor;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class StrategyTester extends Stage {

    public StrategyTester() {
        super();
        setTitle(
                "Strategy Tester"
        );
        setResizable(false);
        setScene(new Scene(
                new StrategyTesterView()
        ));
        show();
    }
}
