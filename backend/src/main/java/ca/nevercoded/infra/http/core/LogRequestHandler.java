package ca.nevercoded.infra.http.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LogRequestHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogRequestHandler.class);

    private final HttpHandler next;

    public LogRequestHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOG.info("%s %s".formatted(exchange.getRequestMethod(), exchange.getRequestURI().toString()));
        next.handle(exchange);
    }
}
