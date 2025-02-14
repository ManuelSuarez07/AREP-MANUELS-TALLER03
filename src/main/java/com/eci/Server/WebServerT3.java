package com.eci.Server;

import com.eci.annotation.GetMapping;
import com.eci.annotation.PostMapping;
import com.eci.annotation.RestController;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class WebServerT3 {

    private static final Map<String, Service> services = new HashMap<>();
    private static final Map<String, MethodHandler> routes = new HashMap<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        // Registra el servicio de conversión
        services.put("/convertir", (req, resp) -> CurrencyConverter.handleCurrencyConversion(req, resp));

        // Registra controladores y rutas dinámicas
        try {
            registerControllers();
        } catch (Exception e) {
            System.err.println("Error registering controllers: " + e.getMessage());
        }

        // Inicia el servidor en el puerto 35000
        try (ServerSocket serverSocket = new ServerSocket(35000)) {
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {

                    String inputLine = in.readLine();
                    if (inputLine == null) {
                        continue;
                    }

                    String[] requestParts = inputLine.split(" ");
                    String method = requestParts[0];
                    String file = requestParts[1];
                    URI resourceURI = new URI(file);

                    String response = switch (method) {
                        case "GET" -> handleRequest("GET", resourceURI, clientSocket.getOutputStream());
                        case "POST" -> handleRequest("POST", resourceURI, clientSocket.getOutputStream());
                        default -> "HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\n";
                    };

                    out.println(response);
                } catch (Exception e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        }
    }

    private static String handleRequest(String httpMethod, URI resourceURI, OutputStream out) throws IOException {
        String path = resourceURI.getPath();

        // Si la ruta es "/", cargamos el index.html como la página principal
        if (path.equals("/") || path.isEmpty()) {
            return obtainFile("/index.html", out); // Carga el archivo index.html
        }

        // Si es un archivo estático (CSS, JS, imágenes, etc.), lo servimos
        if (path.endsWith(".css") || path.endsWith(".js") || path.matches(".*\\.(jpg|jpeg|png|gif|bmp)")) {
            return obtainFile(path, out); // Sirve archivos estáticos
        }

        // Si la ruta es un servicio
        if (services.containsKey(path)) {
            Request req = new Request(resourceURI.getQuery());
            Response resp = new Response();

            Service service = services.get(path);
            if (service != null) {
                String responseBody = service.getValue(req, resp);
                return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + responseBody;
            } else {
                return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nService not found";
            }
        }

        // Si la ruta es dinámica (controladores)
        MethodHandler handler = routes.get(httpMethod + " " + path);
        if (handler != null) {
            try {
                return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" +
                        handler.invoke(new Request(resourceURI.getQuery()));
            } catch (Exception e) {
                return "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage();
            }
        }

        // Si no corresponde a ningún archivo estático, servicio o ruta, devolvemos 404
        return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found";
    }

    private static void registerControllers() throws Exception {
        List<Class<?>> controllers = List.of(
                Class.forName("com.eci.controller.CurrencyController"),
                Class.forName("com.eci.controller.GreetingController")
        );

        for (Class<?> controllerClass : controllers) {
            if (controllerClass.isAnnotationPresent(RestController.class)) {
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping mapping = method.getAnnotation(GetMapping.class);
                        routes.put("GET " + mapping.value(), new MethodHandler(controllerInstance, method));
                        System.out.println("Registered GET route: " + mapping.value());
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        PostMapping mapping = method.getAnnotation(PostMapping.class);
                        routes.put("POST " + mapping.value(), new MethodHandler(controllerInstance, method));
                        System.out.println("Registered POST route: " + mapping.value());
                    }
                }
            }
        }
    }

    // Métodos originales para manejar archivos estáticos
    public static String obtainFile(String path, OutputStream out) throws IOException {
        String file = path.equals("/") ? "index.html" : path.substring(1);
        String extension = file.contains(".") ? file.substring(file.lastIndexOf('.') + 1) : "";
        String filePath = "src/main/resources/static/" + file;
        String responseHeader = "HTTP/1.1 200 OK\r\nContent-Type: " + obtainContentType(extension) + "\r\n\r\n";
        String notFoundResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found";

        try {
            File requestedFile = new File(filePath);
            if (requestedFile.exists()) {
                if (extension.matches("jpg|jpeg|png")) {
                    out.write(responseHeader.getBytes());
                    Files.copy(requestedFile.toPath(), out);
                    return "";
                } else {
                    return responseHeader + new String(Files.readAllBytes(requestedFile.toPath()));
                }
            } else {
                return notFoundResponse;
            }
        } catch (IOException e) {
            return "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage();
        }
    }

    public static String obtainContentType(String extension) {
        return switch (extension) {
            case "html", "css" -> "text/" + extension;
            case "js" -> "text/javascript";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "text/plain";
        };
    }

    // Clase interna para manejar métodos dinámicos
    private static class MethodHandler {
        private final Object instance;
        private final Method method;

        public MethodHandler(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }

        public String invoke(Request request) throws Exception {
            return (String) method.invoke(instance, request);
        }
    }
}
