package com.eci.webservert2;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class WebServerT2 {

    private static final Map<String, Service> services = new HashMap<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        // Registra el servicio de conversión
        services.put("/convertir", (req, resp) -> CurrencyConverter.handleCurrencyConversion(req, resp));

        // Inicia el servidor en el puerto 35000
        try (ServerSocket serverSocket = new ServerSocket(35000)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept(); BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String inputLine = in.readLine();
                    if (inputLine == null) {
                        continue;
                    }

                    String[] requestParts = inputLine.split(" ");
                    String method = requestParts[0];
                    String file = requestParts[1];
                    URI resourceURI = new URI(file);

                    String response = switch (method) {
                        case "GET" ->
                            handleRequest(resourceURI, clientSocket.getOutputStream());
                        default ->
                            "HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\n";
                    };

                    out.println(response);
                } catch (Exception e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        }
    }

private static String handleRequest(URI resourceURI, OutputStream out) throws IOException {
    String path = resourceURI.getPath();

    // Si la ruta es "/", cargamos el index.html como la página principal
    if (path.equals("/") || path.isEmpty()) {
        return obtainFile("/index.html", out); // Carga el archivo index.html
    }

    // Si es un archivo estático (CSS, JS, imágenes, etc.), lo servimos
    if (path.endsWith(".css") || path.endsWith(".js") || path.matches(".*\\.(jpg|jpeg|png|gif|bmp)")) {
        return obtainFile(path, out); // Sirve archivos estáticos
    }

    // Si no es un archivo estático ni la raíz, se maneja como servicio (ej. "/convertir")
    if (path.startsWith("/convertir")) {
        String query = resourceURI.getQuery();
        if (query == null) {
            return "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nMissing parameters";
        }

        // Crear un objeto Request a partir de la query
        Request req = new Request(query);
        Response resp = new Response();

        Service service = services.get(path);
        if (service != null) {
            // Llamamos al servicio que maneja la conversión de divisas
            String responseBody = service.getValue(req, resp);

            // Aquí establecemos el encabezado de la respuesta a 'application/json'
            return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + responseBody;
        } else {
            return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nService not found";
        }
    }

    // Si no corresponde a ningún archivo estático o servicio conocido, devolvemos 404
    return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found";
}


    // Método para registrar los servicios
    private static void get(String url, Service service) {
        services.put(url, service);
        System.out.println("Service registered at: " + url);
    }

    // Método para servir archivos estáticos
    public static String obtainFile(String path, OutputStream out) throws IOException {
        // Determina el nombre del archivo a servir
        String file = path.equals("/") ? "index.html" : path.substring(1); // Elimina la barra inicial

        // Determina la extensión del archivo solicitado
        String extension = file.contains(".") ? file.substring(file.lastIndexOf('.') + 1) : "";
        String filePath = "src/main/resources/static/" + file; // Ruta al archivo estático en recursos
        String responseHeader = "HTTP/1.1 200 OK\r\nContent-Type: " + obtainContentType(extension) + "\r\n\r\n";
        String notFoundResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found";

        // Sirve los archivos estáticos si la extensión es válida
        try {
            // Si el archivo es HTML, CSS, JS o imagen
            if (extension.matches("html|css|js|jpg|jpeg|png")) {
                File requestedFile = new File(filePath);
                if (requestedFile.exists()) {
                    if (extension.matches("jpg|jpeg|png")) {
                        // Para imágenes, enviamos el contenido directamente
                        out.write(responseHeader.getBytes());
                        Files.copy(requestedFile.toPath(), out);
                        return ""; // No es necesario retornar el cuerpo completo para imágenes
                    } else {
                        // Para otros archivos, leemos y enviamos el contenido como texto
                        return responseHeader + new String(Files.readAllBytes(requestedFile.toPath()));
                    }
                } else {
                    return notFoundResponse; // Si el archivo no existe
                }
            }
        } catch (IOException e) {
            return "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage();
        }

        return notFoundResponse; // En caso de no encontrar el archivo
    }

    public static String obtainContentType(String extension) {
        switch (extension) {
            case "html", "css" -> {
                return "text/" + extension;
            }
            case "js" -> {
                return "text/javascript";
            }
            case "jpg", "jpeg" -> {
                return "image/jpeg";
            }
            case "png" -> {
                return "image/png";
            }
            default -> {
            }
        }
        return "text/plain";
    }
}
