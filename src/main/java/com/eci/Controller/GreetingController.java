package com.eci.controller;

import com.eci.annotation.GetMapping;
import com.eci.annotation.RestController;
import com.eci.Server.Request;

@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public String getGreeting(Request request) {
        // Ejemplo de uso de query params (si los hay)
        String name = request.getValues("name");
        if (name != null) {
            return "{\"message\": \"Hello, " + name + "! Welcome to CurrencyConverter!\"}";
        }
        return "{\"message\": \"Hello! Welcome to CurrencyConverter!\"}";
    }
}
