package org.tradeexpert.tradeexpert;

import java.util.ServiceLoader;

public abstract class ServiceProvider {
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

    public abstract String getMessage();


}