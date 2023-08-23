package ca.nevercoded.infra.http.books;

import ca.nevercoded.domain.books.Book;
import ca.nevercoded.domain.books.BookService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class PostBooksHandler implements HttpHandler {
    private final HttpHandler next;
    private final BookService service;

    PostBooksHandler(HttpHandler next, BookService service) {
        this.next = next;
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final boolean isTextCsv = Optional.ofNullable(exchange.getRequestHeaders())
                .stream()
                .map(headers -> headers.get("Content-Type"))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(String::toLowerCase)
                .anyMatch(value -> value.contains("text/csv"));

        final boolean isPostAndTextCsv = exchange.getRequestMethod().equalsIgnoreCase("POST") && isTextCsv;

        if (!isPostAndTextCsv) {
            next.handle(exchange);
            return;
        }

        try (final var reader = NamedCsvReader.builder().build(new InputStreamReader(exchange.getRequestBody()))) {
            final var books = reader.stream().map(PostBooksHandler::deserialize).toList();
            service.storeBooks(books);
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }

    private static Book deserialize(NamedCsvRow row) {
        return new Book(row.getField("isbn"),
                row.getField("title"),
                row.getField("author"),
                new BigDecimal(row.getField("price").replace("$", "")),
                LocalDate.parse(row.getField("release_date")));
    }
}
