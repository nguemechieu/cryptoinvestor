package cryptoinvestor.cryptoinvestor.Discord;


import cryptoinvestor.cryptoinvestor.Chat;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.TelegramClient;

import java.io.IOException;


public class Discord extends TelegramClient {
    String url = "https://discord.com/api/oauth2/authorize?client_id=1087210119097499751&permissions=0&scope=bot";

    String discordToken = "MTA4NzIxMDExOTA5NzQ5OTc1MQ.GTPtoi.IoKi82j9vnTZAe1VG8LHO60aFUGJzgfYG5blYo";

    private String clientId = "72421114444444444";
    String host =
            "https://api.discord.org/bot" +
                    this.getClientId() +
                    "/sendMessage?chat_id=" +
                    this.getChatId() +
                    "&parse_mode=Markdown&text=";
    private String clientSecret = "1087210119097499751";
    private String apiUrl;
    private String apiVersion;
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
        this.apiUrl = apiUrl;
        this.apiVersion = apiVersion;
        this.clientSecret = clientSecret;
        this.clientId = clientId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        connect();
        this.sendMessage(Chat.getWelcomeMessage());

        this.sendMessage(Coinbase.getCoinbaseMessage());
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}