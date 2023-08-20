package ca.nevercoded.infra;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class CorsHandler implements HttpHandler {

    private final HttpHandler next;

    CorsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
        next.handle(exchange);
    }
}
