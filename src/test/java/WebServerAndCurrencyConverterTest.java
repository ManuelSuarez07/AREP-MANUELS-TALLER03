import com.eci.Server.CurrencyConverter;
import com.eci.Server.WebServerT3;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

public class WebServerAndCurrencyConverterTest {

    // Pruebas para CurrencyConverter

    @Test
    public void shouldConvertValidCurrencies() {
        double amount = 100.0;
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        double result = CurrencyConverter.convert(amount, fromCurrency, toCurrency);
        assertEquals("La conversión de 100 USD a EUR no es correcta", 85.0, result, 0.01);
    }

    @Test
    public void shouldThrowExceptionForInvalidFromCurrency() {
        double amount = 100.0;
        String fromCurrency = "XYZ"; // Moneda no soportada
        String toCurrency = "EUR";
        try {
            CurrencyConverter.convert(amount, fromCurrency, toCurrency);
            fail("Se esperaba una excepción por moneda no soportada");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // Excepción esperada
        }
    }

    @Test
    public void shouldThrowExceptionForInvalidToCurrency() {
        double amount = 100.0;
        String fromCurrency = "USD";
        String toCurrency = "XYZ"; // Moneda no soportada
        try {
            CurrencyConverter.convert(amount, fromCurrency, toCurrency);
            fail("Se esperaba una excepción por moneda no soportada");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // Excepción esperada
        }
    }

    @Test
    public void shouldConvertIdenticalCurrencies() {
        double amount = 100.0;
        String fromCurrency = "USD";
        String toCurrency = "USD"; // La misma moneda
        double result = CurrencyConverter.convert(amount, fromCurrency, toCurrency);
        assertEquals("La conversión entre la misma moneda debe devolver el mismo valor", 100.0, result, 0.01);
    }

    @Test
    public void shouldConvertNegativeAmount() {
        double amount = -100.0;
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        double result = CurrencyConverter.convert(amount, fromCurrency, toCurrency);
        assertEquals("La conversión de una cantidad negativa debe devolver el valor negativo correspondiente", -85.0, result, 0.01);
    }

    @Test
    public void shouldConvertZeroAmount() {
        double amount = 0.0;
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        double result = CurrencyConverter.convert(amount, fromCurrency, toCurrency);
        assertEquals("La conversión de 0 USD debe devolver 0 EUR", 0.0, result, 0.01);
    }

    // Pruebas para WebServerT3
    @Test
    public void shouldReturnContentTypeForHtml() {
        String extension = WebServerT3.obtainContentType("html");
        assertEquals("El tipo de contenido para HTML no es correcto", "text/html", extension);
    }

    @Test
    public void shouldReturnContentTypeForCss() {
        String extension = WebServerT3.obtainContentType("css");
        assertEquals("El tipo de contenido para CSS no es correcto", "text/css", extension);
    }

    @Test
    public void shouldReturnContentTypeForJs() {
        String extension = WebServerT3.obtainContentType("js");
        assertEquals("El tipo de contenido para JS no es correcto", "text/javascript", extension);
    }

    @Test
    public void shouldReturnContentTypeForJpg() {
        String extension = WebServerT3.obtainContentType("jpg");
        assertEquals("El tipo de contenido para JPG no es correcto", "image/jpeg", extension);
    }

    @Test
    public void shouldReturnContentTypeForPng() {
        String extension = WebServerT3.obtainContentType("png");
        assertEquals("El tipo de contenido para PNG no es correcto", "image/png", extension);
    }
}
