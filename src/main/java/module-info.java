module cryptoinvestor.cryptoinvestor {

    requires javafx.controls;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires org.jetbrains.annotations;
    requires com.fasterxml.jackson.annotation;
    requires com.jfoenix;
    requires Java.WebSocket;
    requires javafx.web;
    requires jdk.jsobject;
    requires java.desktop;
    requires org.apache.tomcat.embed.websocket;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.csv;
    requires java.prefs;
    requires java.scripting;
    requires org.slf4j;
    requires mysql.connector.j;
    requires org.testng;
    requires org.junit.jupiter.api;


    exports cryptoinvestor.cryptoinvestor;
    exports cryptoinvestor.cryptoinvestor.JsonToCsv;
}