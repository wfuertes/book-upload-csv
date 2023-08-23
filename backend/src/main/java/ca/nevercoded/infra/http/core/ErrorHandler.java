package ca.nevercoded.infra.http.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ErrorHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);

    private final HttpHandler next;

    public ErrorHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            next.handle(exchange);
        } catch (Exception err) {
            LOG.error("Error on handle request: " + err.getMessage(), err);
            final var message = err.getMessage().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, message.length);
            exchange.getResponseBody().write(message);
            exchange.getResponseBody().close();
        }
    }
}
