package org.tradeexpert.tradeexpert;

import java.util.ServiceLoader;

import static org.tradeexpert.tradeexpert.TradePair.logger;

public abstract class ServiceProvider {
    public ServiceProvider() {
        super();
        logger.info("ServiceProvider created");
    }

    public static ServiceProvider getDefault() {

        // load our plugin
        ServiceLoader<ServiceProvider> serviceLoader =
                ServiceLoader.load(ServiceProvider.class);

        //checking if load was successful
        for (ServiceProvider provider : serviceLoader) {
            return provider;
        }
        throw new Error("Something is wrong with registering the addon");
    }

    public static ServiceProvider getInstance() {
        return getDefault();
    }

    public abstract String getMessage();



}