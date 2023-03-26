package cryptoinvestor.cryptoinvestor.Discord;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;


public class Discord  {
    private String url = "https://discord.com/api/oauth2/authorize?client_id=1087210119097499751&permissions=0&scope=bot";

   private String discordToken = "MTA4NzIxMDExOTA5NzQ5OTc1MQ.GTPtoi.IoKi82j9vnTZAe1VG8LHO60aFUGJzgfYG5blYo";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public void setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    private String clientId = "72421114444444444";
    String host =
            "https://api.discord.org/bot" +
                    this.getClientId() +
                    "/sendMessage?chat_id=" +

                    "&parse_mode=Markdown&text=";
    private String clientSecret = "1087210119097499751";
    private static String apiUrl;
    private static String apiVersion;
    private String accessToken;
    private String refreshToken;
    HttpRequest.Builder requestBuilder;
    HttpClient client;

    public Discord(

            String accessToken) throws IOException, InterruptedException {

        this.accessToken = accessToken;
        requestBuilder = HttpRequest.newBuilder();
        requestBuilder.header("Client-ID", clientId);

        requestBuilder.header("Authorization", "Bearer " + accessToken);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
      requestBuilder.header("Cache-Control", "no-cache");


       this. client = HttpClient.newHttpClient();

       sendMessage(

                "Hello, I am Cryptoinvestor. I am a bot that allows you to buy cryptocurrencies from the Cryptocurrency Marketplace." +
                        " I can buy cryptocurrencies from the Cryptocurrency Marketplace, sell cryptocurrencies from the Cryptocurrency Marketplace," +
                        " and get information about cryptocurrencies from the Cryptocurrency Marketplace."
       );


    }

    public static String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        Discord.apiUrl = apiUrl;
    }

    public static String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        Discord.apiVersion = apiVersion;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public  void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void sendMessage(String message) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + message;
        System.out.println(url);
        String photo="";

        Thread.sleep(1000);

        System.out.println(url);

    }

    public static @NotNull Object connect() {
        String url = getApiUrl() + getApiVersion() + "/oauth2/token";
        System.out.println(url);
        return "";

    }

    public String sendPhoto(String photo) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/photos";
        System.out.println(url);

        Thread.sleep(1000);

        System.out.println(url);
        return  null;
    }
    public String sendPhoto(String photo, String caption) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/photos";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);

        return null;
    }
    public  String sendVideo(String video) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/videos";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return null;
    }
    public  String sendAudio(String audio) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/audio";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  null;
    }
    public  String sendDocument(String document) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/documents";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return null;
    }
    public  String sendSticker(String sticker) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/stickers";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  null;
    }
    public  String sendVoice(String voice) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/voice";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return null;
    }


}