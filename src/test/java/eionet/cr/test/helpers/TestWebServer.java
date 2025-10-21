package eionet.cr.test.helpers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import eionet.cr.config.GeneralConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Simple HTTP server used during integration tests for serving classpath resources.
 */
public final class TestWebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestWebServer.class);

    private static volatile HttpServer server;
    private static volatile String contextPath;
    private static volatile boolean externalServerDetected;

    private TestWebServer() {
        // Utility class
    }

    /**
     * Ensure that the HTTP server used for the integration tests is running.
     */
    public static void ensureRunning() {
        if (server != null || externalServerDetected) {
            return;
        }
        synchronized (TestWebServer.class) {
            if (server != null || externalServerDetected) {
                return;
            }

            String baseUrl = GeneralConfig.getProperty("test.httpd.url");
            if (StringUtils.isBlank(baseUrl)) {
                LOGGER.warn("Property test.httpd.url is not configured; HTTP test server will not start.");
                return;
            }
            URI baseUri = URI.create(baseUrl);
            try {
                String host = baseUri.getHost() == null ? "127.0.0.1" : baseUri.getHost();
                int port = baseUri.getPort() == -1 ? 80 : baseUri.getPort();
                String path = baseUri.getPath();
                contextPath = StringUtils.isBlank(path) ? "/" : path;

                if (isExternalServerResponding(baseUri)) {
                    externalServerDetected = true;
                    LOGGER.info("Detected external test web server at {}; embedded server will not be started.", baseUrl);
                    return;
                }

                HttpServer httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
                httpServer.createContext(contextPath, new ClasspathResourceHandler());
                httpServer.setExecutor(Executors.newCachedThreadPool());
                httpServer.start();
                server = httpServer;
                LOGGER.info("Started test web server on {}:{} serving path {}", host, port, contextPath);
            } catch (IOException e) {
                if (isExternalServerResponding(baseUri)) {
                    externalServerDetected = true;
                    LOGGER.info("Assuming external test web server at {} after bind failure; embedded server suppressed.", baseUrl);
                    return;
                }
                throw new IllegalStateException("Failed to start HTTP server for tests", e);
            }
        }
    }

    private static boolean isExternalServerResponding(URI baseUri) {
        try {
            URL url = baseUri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestMethod("GET");
            connection.connect();
            int status = connection.getResponseCode();
            connection.disconnect();
            return status >= 100;
        } catch (IOException ex) {
            return false;
        }
    }

    private static class ClasspathResourceHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String requestedPath = exchange.getRequestURI().getPath();
            String relativePath = requestedPath;
            if (StringUtils.isNotEmpty(contextPath) && !"/".equals(contextPath)) {
                relativePath = StringUtils.removeStart(requestedPath, contextPath);
            }
            relativePath = StringUtils.removeStart(relativePath, "/");

            if (StringUtils.isBlank(relativePath)) {
                sendResponse(exchange, 404, "Resource not found");
                return;
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = classLoader.getResourceAsStream(relativePath)) {
                if (inputStream == null) {
                    sendResponse(exchange, 404, "Resource not found: " + relativePath);
                    return;
                }

                byte[] data = toByteArray(inputStream);
                String contentType = URLConnection.guessContentTypeFromName(relativePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }

        private byte[] toByteArray(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}
