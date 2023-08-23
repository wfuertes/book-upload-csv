package ca.nevercoded.infra.http.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NotFoundHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        throw new IllegalArgumentException("No handler could handle the request");
    }
}
