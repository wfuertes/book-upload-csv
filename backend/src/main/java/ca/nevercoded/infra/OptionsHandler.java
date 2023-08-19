package ca.nevercoded.infra;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class OptionsHandler implements HttpHandler {

    private final HttpHandler next;

    public OptionsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            next.handle(exchange);
            return;
        }
        exchange.sendResponseHeaders(204, -1);
        exchange.getResponseBody().close();
    }
}
