package cryptoinvestor.cryptoinvestor.Discord;


import cryptoinvestor.cryptoinvestor.Chat;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.TelegramClient;
import cryptoinvestor.cryptoinvestor.oanda.OandaWebSocket;

import java.io.IOException;


public class Discord extends TelegramClient   {
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
                    this.getChatId() +
                    "&parse_mode=Markdown&text=";
    private String clientSecret = "1087210119097499751";
    private static String apiUrl;
    private static String apiVersion;
    private String accessToken;
    private String refreshToken;

    public Discord(
            String apiUrl,
            String apiVersion,
            String clientSecret,
            String clientId,
            String accessToken,
            String refreshToken) throws IOException, InterruptedException {
        super(
                apiUrl,
                apiVersion,
                clientSecret,
                clientId,
                accessToken,
                refreshToken
        );
        Discord.apiUrl = apiUrl;
        Discord.apiVersion = apiVersion;
        this.clientSecret = clientSecret;
        this.clientId = clientId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        connect();
        this.sendMessage(Chat.getWelcomeMessage());

        this.sendMessage(Coinbase.getCoinbaseMessage());
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
        post(url, url, photo);
        Thread.sleep(1000);

        System.out.println(url);

    }

    public static Object connect() {
        String url = getApiUrl() + getApiVersion() + "/oauth2/token";
        System.out.println(url);




        return "";

    }

    public String sendPhoto(String photo) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/photos";
        System.out.println(url);

        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, photo);
    }
    public String sendPhoto(String photo, String caption) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/photos";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);

        return  post(url, photo, caption);
    }
    public  String sendVideo(String video) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/videos";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, video);
    }
    public  String sendAudio(String audio) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/audio";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, audio);
    }
    public  String sendDocument(String document) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/documents";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, document);
    }
    public  String sendSticker(String sticker) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/stickers";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, sticker);
    }
    public  String sendVoice(String voice) throws IOException, InterruptedException {
        String url = getApiUrl() + getApiVersion() + "/voice";
        System.out.println(url);
        Thread.sleep(1000);

        System.out.println(url);
        return  post(url, url, voice);
    }


}