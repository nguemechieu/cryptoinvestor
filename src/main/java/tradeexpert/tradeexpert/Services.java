package tradeexpert.tradeexpert;

import java.util.ServiceLoader;

public class Services {

    ServiceProvider serviceProvider;
    ServiceLoader <Trade> serviceLoader;
    public static void main(String[] args) {
        Services services = new Services();
        services.serviceProvider = new ServiceProvider() {
            @Override
            public String getMessage() {
                return "Hello";
            }
        };
        services.serviceLoader = ServiceLoader.load(Trade.class);
        services.serviceLoader.iterator().forEachRemaining(System.out::println);
        System.out.println(services.serviceProvider.getMessage());

        System.out.println(services.serviceLoader.toString());



    }





}