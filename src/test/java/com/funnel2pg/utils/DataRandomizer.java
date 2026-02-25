package com.funnel2pg.utils;

import java.util.Random;

public class DataRandomizer {

    private static Random random;
    private static boolean randomizeEnabled;

    static {
        try {
            // Read from checkout_data.json
            randomizeEnabled = TestDataReader.isRandomizeEnabled();
            long seed = TestDataReader.getRandomizeSeed();
            random = new Random(seed);
        } catch (Exception e) {
            randomizeEnabled = false;
            random = new Random();
        }
    }

    /**
     * Get randomized or overridden customer data if enabled
     * Otherwise return default data
     */
    public static String getCustomerField(String field) {
        if (!randomizeEnabled) {
            return TestDataReader.getCustomer(field);
        }

        // Check if override value exists
        String overrideValue = TestDataReader.getCustomerOverride(field);
        if (overrideValue != null && !overrideValue.isEmpty()) {
            return overrideValue;
        }

        // Generate random data
        return generateRandomData(field);
    }

    private static String generateRandomData(String field) {
        switch (field.toLowerCase()) {
            case "firstname":
                return "User" + randomInt(1000, 9999);
            case "lastname":
                return "Test" + randomInt(1000, 9999);
            case "email":
                return "testuser" + randomInt(10000, 99999) + "@mailinator.com";
            case "phone":
                return String.format("555%07d", randomInt(0, 9999999));
            case "address":
                return randomInt(100, 999) + " " + randomStreet() + " " + randomCity() + " TX";
            case "city":
                return randomCity();
            case "state":
                return "Texas";
            case "zipcode":
                return String.format("%05d", randomInt(10000, 99999));
            case "country":
                return "United States";
            default:
                return "";
        }
    }

    private static int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static String randomStreet() {
        String[] streets = {"Oak", "Elm", "Maple", "Pine", "Cedar", "Birch", "Ash", "Willow"};
        String[] types = {"Street", "Avenue", "Road", "Boulevard", "Lane", "Drive"};
        return streets[random.nextInt(streets.length)] + " " + types[random.nextInt(types.length)];
    }

    private static String randomCity() {
        String[] cities = {"Austin", "Dallas", "Houston", "San Antonio", "Austin", "Tyler", "Amarillo"};
        return cities[random.nextInt(cities.length)];
    }

    public static boolean isEnabled() {
        return randomizeEnabled;
    }
}

