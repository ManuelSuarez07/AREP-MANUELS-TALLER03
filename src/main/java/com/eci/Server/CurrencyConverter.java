package com.eci.Server;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    private static final Map<String, Double> exchangeRates = new HashMap<>();

    static {
        exchangeRates.put("usd", 1.0);  // Cambiar a minúsculas
        exchangeRates.put("eur", 0.85);
        exchangeRates.put("gbp", 0.75);
        exchangeRates.put("jpy", 130.0);
        exchangeRates.put("aud", 1.5);
        exchangeRates.put("cad", 1.35);
        exchangeRates.put("chf", 0.92);
        exchangeRates.put("cny", 6.5);
        exchangeRates.put("inr", 83.0);
        exchangeRates.put("brl", 5.3);
        exchangeRates.put("mxn", 18.5);
        exchangeRates.put("ars", 350.0);
        exchangeRates.put("krw", 1200.0);
        exchangeRates.put("zar", 18.0);
        exchangeRates.put("cop", 4000.0);
        exchangeRates.put("sek", 10.5);
        exchangeRates.put("try", 19.0);
        exchangeRates.put("rub", 85.0);
        exchangeRates.put("sgd", 1.35);
        exchangeRates.put("nzd", 1.6);
        exchangeRates.put("myr", 4.5);
        exchangeRates.put("idr", 15000.0);
        exchangeRates.put("sar", 3.75);
    }

    public static double convert(double amount, String fromCurrency, String toCurrency) {
        // Convertir las monedas a minúsculas para hacer que la comparación sea insensible a mayúsculas
        fromCurrency = fromCurrency.toLowerCase();
        toCurrency = toCurrency.toLowerCase();

        if (exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)) {
            double fromRate = exchangeRates.get(fromCurrency);
            double toRate = exchangeRates.get(toCurrency);
            return amount * (toRate / fromRate);
        }
        throw new IllegalArgumentException("Moneda no soportada");
    }

    // Método para manejar la conversión directamente como servicio
    public static String handleCurrencyConversion(Request req, Response resp) {
        String fromCurrency = req.getValues("fromCurrency").toLowerCase();  // Convertir a minúsculas
        String toCurrency = req.getValues("toCurrency").toLowerCase();      // Convertir a minúsculas
        String amountStr = req.getValues("amount");

        if (fromCurrency != null && toCurrency != null && amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                double result = convert(amount, fromCurrency, toCurrency);

                // Crear la respuesta en formato JSON
                resp.setBody("{\"amount\":" + amount + ", \"fromCurrency\":\"" + fromCurrency + "\", \"toCurrency\":\"" + toCurrency + "\", \"converted\":" + result + "}");
                return resp.getBody();
            } catch (NumberFormatException e) {
                resp.setBody("Error: Parámetros inválidos para la cantidad.");
                return resp.getBody();
            } catch (IllegalArgumentException e) {
                resp.setBody("Error: " + e.getMessage());
                return resp.getBody();
            }
        } else {
            resp.setBody("Error: Parámetros faltantes. Asegúrese de incluir 'fromCurrency', 'toCurrency' y 'amount'.");
            return resp.getBody();
        }
    }

    public static Map<String, Double> getExchangeRates() {
        return new HashMap<>(exchangeRates);
    }

    public static void addExchangeRate(String currencyName, double rate) {
        exchangeRates.put(currencyName.toLowerCase(), rate);
    }
}
