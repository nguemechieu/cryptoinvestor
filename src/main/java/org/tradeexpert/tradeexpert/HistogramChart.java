package org.tradeexpert.tradeexpert;

import javafx.scene.Node;
import javafx.scene.chart.Chart;

public class HistogramChart<T, T1> extends Chart {

    private StableTicksAxis xAxis;

    {
        new StableTicksAxis();
    }

    private StableTicksAxis yAxis;

    {
        new StableTicksAxis();
    }


    public HistogramChart() {
        this(new StableTicksAxis(), new StableTicksAxis());
    }

    public HistogramChart(StableTicksAxis xAxis, StableTicksAxis yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;


        setLegendVisible(false);
        setLegendSide(Side.BOTTOM);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {

    }

    @Override
    public Node getStyleableNode() {
        return super.getStyleableNode();
    }

    public StableTicksAxis getxAxis() {
        return xAxis;
    }

    public void setxAxis(StableTicksAxis xAxis) {
        this.xAxis = xAxis;
    }

    public StableTicksAxis getyAxis() {
        return yAxis;
    }

    public void setyAxis(StableTicksAxis yAxis) {
        this.yAxis = yAxis;
    }
}
