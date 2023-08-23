package ca.nevercoded.infra.http.books;

import ca.nevercoded.domain.books.BookService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GetBooksHandler implements HttpHandler {

    private final HttpHandler next;
    private final Gson gson;
    private final BookService service;

    GetBooksHandler(HttpHandler next, Gson gson, BookService service) {
        this.next = next;
        this.gson = gson;
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            next.handle(exchange);
            return;
        }

        final var books = service.fetchBooks();
        byte[] response = gson.toJson(books).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
}
