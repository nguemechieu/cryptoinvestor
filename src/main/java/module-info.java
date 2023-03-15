
module tradeexpert.tradeexpert{

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;


    requires java.sql;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires com.jfoenix;
    requires org.jetbrains.annotations;
    requires com.fasterxml.jackson.annotation;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires Java.WebSocket;
    requires java.net.http;
    requires slf4j.api;
    requires javafx.web;
    requires jdk.jsobject;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.fasterxml.jackson.dataformat.csv;

    opens tradeexpert.tradeexpert to javafx.fxml;
    exports tradeexpert.tradeexpert;

}

