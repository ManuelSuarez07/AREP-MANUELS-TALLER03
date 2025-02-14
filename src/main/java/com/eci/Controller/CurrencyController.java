package com.eci.controller;

import com.eci.annotation.GetMapping;
import com.eci.annotation.PostMapping;
import com.eci.annotation.RestController;
import com.eci.Server.CurrencyConverter;
import com.eci.Server.Request;

import java.util.Map;

@RestController
public class CurrencyController {

    /**
     * Agregar una nueva moneda y su tasa de cambio relativa al USD.
     * @param request Objeto con los valores enviados en la solicitud.
     * @return Respuesta en formato JSON.
     */
    @PostMapping("/api/currencies")
    public String addCurrency(Request request) {
        String currencyName = request.getValues("name").toLowerCase();
        String exchangeRateStr = request.getValues("rate");

        if (currencyName == null || exchangeRateStr == null) {
            return "{\"error\": \"Missing 'name' or 'rate' parameter\"}";
        }

        try {
            double exchangeRate = Double.parseDouble(exchangeRateStr);
            CurrencyConverter.addExchangeRate(currencyName, exchangeRate);
            return "{\"status\": \"Currency added successfully\"}";
        } catch (NumberFormatException e) {
            return "{\"error\": \"Invalid rate value\"}";
        }
    }

    /**
     * Obtener todas las monedas y sus tasas de cambio actuales.
     * @param request Objeto con los valores enviados en la solicitud.
     * @return Respuesta en formato JSON.
     */
    @GetMapping("/api/currencies")
    public String getCurrencies(Request request) {
        Map<String, Double> currencies = CurrencyConverter.getExchangeRates();
        StringBuilder jsonResponse = new StringBuilder("{\"currencies\": {");

        // Generar la lista de monedas y sus tasas en formato JSON
        currencies.forEach((currency, rate) -> {
            jsonResponse.append("\"").append(currency).append("\": ").append(rate).append(",");
        });

        // Eliminar la última coma y cerrar el JSON
        if (!currencies.isEmpty()) {
            jsonResponse.deleteCharAt(jsonResponse.length() - 1);
        }
        jsonResponse.append("}}");

        return jsonResponse.toString();
    }
}
