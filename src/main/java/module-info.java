module org.tradeexpert.tradeexpert {
    uses org.tradeexpert.tradeexpert.ServiceProvider;
    uses org.tradeexpert.tradeexpert.CurrencyDataProvider;
    uses org.tradeexpert.tradeexpert.Trade;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;
    requires com.jfoenix;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires java.logging;
    requires org.slf4j;
    requires com.fasterxml.jackson.dataformat.csv;
    requires java.net.http;
    requires jdk.jsobject;
    requires javafx.web;
    requires org.json;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.persistence;
    requires java.sql;
    requires java.websocket;
    requires jdk.hotspot.agent;


    opens org.tradeexpert.tradeexpert to javafx.fxml;
    exports org.tradeexpert.tradeexpert;
}