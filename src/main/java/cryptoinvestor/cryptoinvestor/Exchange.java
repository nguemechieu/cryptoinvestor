package cryptoinvestor.cryptoinvestor;


import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import cryptoinvestor.cryptoinvestor.oanda.Oanda;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Exchange {

    private static final Logger logger = LoggerFactory.getLogger(Exchange.class);

    public TelegramClient telegram;

    public String accountId;
    protected String phraseSecret1;
    protected String apiSecret;
    protected String apiKey;
    protected static TradePair tradePair;
    Accounts account;
    private String url;
    private SocketFactory socket;
    private boolean isOpen;
    protected ArrayList<Trade> trades = new ArrayList<>();
    private double price;


    public Exchange( String token) throws TelegramApiException, IOException {

        this.telegram = new TelegramClient(token);
        this.socket = SocketFactory.getDefault();
        this.isOpen = true;
        this.account = new Accounts();

        this.accountId = account.getAccountID();

        logger.info("Connected to " + url);
    }


    public Exchange(String coinbaseApiKey, String coinbaseSecret, String telegramToken) throws TelegramApiException, IOException {
        this.apiKey = coinbaseApiKey;
        this.phraseSecret1 = coinbaseSecret;
        this.telegram = new TelegramClient(telegramToken);
        this.socket = SocketFactory.getDefault();
        this.isOpen = true;
        this.account = new Accounts();
        TelegramClient.connect();

        logger.info("Connected to " + url);


    }


    public Exchange(String apiKey, String telegramToken) throws TelegramApiException, IOException {

        this.socket = SocketFactory.getDefault();
        this.isOpen = true;
        this.account = new Accounts();
        this.accountId = account.getAccountID();

        this.apiKey = apiKey;
        this.accountId = account.getAccountID();
        logger.info("Connected to " + url);

        this.socket = SocketFactory.getDefault();
        this.isOpen = true;
        if (telegramToken != null) {
            this.telegram = new TelegramClient(telegramToken);
        } else {
            logger.error("KuCoin Telegram token is null");
        }
    }

    public abstract String getName();


    public ExchangeWebSocketClient getWebsocketClient() {

        switch (this) {
            case Bitfinex bitfinex -> url = "wss://api.bitfinex.com/ws/2";
            case Coinbase coinbase -> url = "wss://api.coinbase.com/v2/exchange";
            case BinanceUs binanceUs -> url = "wss://stream.binance.us:9443/ws";
            case Oanda oanda -> url = "wss://api-fxtrade.oanda.com:443/ws";
            case Bitstamp bitstamp -> url = "wss://stream.bitstamp.net:9443/ws";
            case Bittrex bittrex -> url = "wss://stream.bittrex.com:443/ws";
            case Kucoin kucoin -> url = "wss://stream.kucoin.com:9443/ws";
            case Kraken kraken -> url = "wss://stream.kraken.com:9443/ws";
            case Poloniex poloniex -> url = "wss://stream.poloniex.com:9443/ws";
          //  case Binance binance -> url = "wss://stream.binance.com:9443/ws";
            case default -> {
            }
        }


        return new ExchangeWebSocketClient(
                URI.create(url),
                new Draft_6455()
        ) {
            @Override
            public long getDefaultAsyncSendTimeout() {
                return 0;
            }

            @Override
            public void setAsyncSendTimeout(long l) {


            }

            @Override
            public Session connectToServer(Object o, URI uri) {

                return new Session() {

                    @Override
                    public WebSocketContainer getContainer() {
                        return null;
                    }

                    @Override
                    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {

                        logger.info("Adding message handler ");


                    }

                    @Override
                    public Set<MessageHandler> getMessageHandlers() {
                        return null;
                    }

                    @Override
                    public void removeMessageHandler(MessageHandler listener) {
                        logger.info("Removing message handler ");

                    }

                    @Override
                    public String getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public String getNegotiatedSubprotocol() {
                        return null;
                    }

                    @Override
                    public List<Extension> getNegotiatedExtensions() {
                        return null;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public boolean isOpen() {
                        return false;
                    }

                    @Override
                    public long getMaxIdleTimeout() {
                        return
                                0;
                    }

                    @Override
                    public void setMaxIdleTimeout(long timeout) {

                    }

                    @Override
                    public int getMaxBinaryMessageBufferSize() {
                        return 0;
                    }

                    @Override
                    public void setMaxBinaryMessageBufferSize(int max) {
                        logger.info("Setting max binary message buffer size to " + max);

                    }

                    @Override
                    public int getMaxTextMessageBufferSize() {
                        return 0;
                    }

                    @Override
                    public void setMaxTextMessageBufferSize(int max) {

                    }

                    @Override
                    public RemoteEndpoint.Async getAsyncRemote() {
                        return null;
                    }

                    @Override
                    public RemoteEndpoint.Basic getBasicRemote() {
                        logger.info("Getting basic remote endpoint");

                        return null;
                    }

                    @Override
                    public String getId() {
                        return null;
                    }

                    @Override
                    public void close() {
                        Exchange.this.close();
                        logger.info("Connection closed");

                    }

                    @Override
                    public void close(CloseReason closeReason) {

                    }

                    @Override
                    public URI getRequestURI() {
                        return null;
                    }

                    @Override
                    public Map<String, List<String>> getRequestParameterMap() {
                        return null;
                    }

                    @Override
                    public String getQueryString() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getPathParameters() {
                        return null;
                    }

                    @Override
                    public Map<String, Object> getUserProperties() {
                        return null;
                    }

                    @Override
                    public Principal getUserPrincipal() {
                        return null;
                    }

                    @Override
                    public Set<Session> getOpenSessions() {
                        return null;
                    }

                    @Override
                    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) throws IllegalStateException {

                    }

                    @Override
                    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) throws IllegalStateException {

                    }
                };
            }

            @Override
            public Session connectToServer(Class<?> aClass, URI uri) {
                return null;
            }

            @Override
            public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfig, URI uri) {
                return null;
            }

            @Override
            public Session connectToServer(Class<? extends Endpoint> endpoint, ClientEndpointConfig clientEndpointConfiguration, URI path) throws DeploymentException, IOException {
                return null;
            }


            @Override
            public long getDefaultMaxSessionIdleTimeout() {
                return 0;
            }

            @Override
            public void setDefaultMaxSessionIdleTimeout(long l) {

            }

            @Override
            public int getDefaultMaxBinaryMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxBinaryMessageBufferSize(int i) {

            }

            @Override
            public int getDefaultMaxTextMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxTextMessageBufferSize(int i) {

            }

            @Override
            public Set<Extension> getInstalledExtensions() {
                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {


                Log.info(
                        String.format("Exchange %s sent text message %s",
                                getExchangeName(), data), "text" + data

                );


                return null;


            }

            @Override
            public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {


                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {


                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {


                logger.info(
                        String.format("Exchange %s closed connection with status code %d and reason %s",
                                getExchangeName(), statusCode, reason), "status" + statusCode
                );


                return null;
            }

            @Override
            public String getSubprotocol() {
                return null;
            }

            @Override
            public boolean isOutputClosed() {
                return false;
            }

            @Override
            public boolean isInputClosed() {
                return false;
            }

            @Override
            public void abort() {

            }
        };
    }

    private void close() {
        this.isOpen = false;
    }


    private @NotNull @Unmodifiable Object getExchangeName() {
        return this.getClass().getSimpleName();
    }


    public abstract CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair);

    public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);

    public abstract CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
            TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

    public abstract void onOpen(ServerHandshake handshake);

    public abstract void onMessage(String message);

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);


    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String name() {
        return this.getClass().getSimpleName();
    }


    public void deposit(Double value) {
    }

    public void withdraw(Double value) {
    }

    public String getWithdraw() {
        return "No withdraw";
    }

    public String getDeposit() {
        return "No deposit";
    }

    public String getTotal() {
        return "No total";
    }

    public String getFee() {
        return "No fee";
    }

    public String getPending() {
        return "No pending";
    }

    public String getAvailable() {
        return "No available";
    }

    public String getBalance() {
        return "No balance";
    }

    public double getPrice(TradePair tradePair) throws IOException, InterruptedException {
        if (this instanceof Coinbase coinbase) {
            return coinbase.getPrice(tradePair);
        } else if (this instanceof BinanceUs binanceUs) {
            return binanceUs.getPrice(tradePair);
        } else if (this instanceof Bittrex bittrex) {
            return bittrex.getPrice(tradePair);
        } else if (this instanceof Bitfinex bitfinex) {
            return bitfinex.getPrice(tradePair);
        } else if (this instanceof Bitstamp bitstamp) {
            return bitstamp.getPrice(tradePair);
        } else if (this instanceof Oanda oanda) {
            return oanda.getPrice(tradePair);
        }
        return 0;


    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

}

