package ca.nevercoded.infra.http.books;

import ca.nevercoded.domain.books.BookRepository;
import ca.nevercoded.domain.books.BookService;
import ca.nevercoded.infra.http.core.*;
import ca.nevercoded.infra.sql.DataSourceFactory;
import ca.nevercoded.infra.json.GsonFactory;
import ca.nevercoded.infra.sql.MySqlBookRepository;
import ca.nevercoded.infra.keycloack.KeycloakGateway;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;

public class BooksResource {

    public static HttpHandler create() {
        BookRepository bookRepository = new MySqlBookRepository(DataSourceFactory.create());
        BookService bookService = new BookService(bookRepository);
        Gson gson = GsonFactory.create();
        final var getBooks = new GetBooksHandler(new NotFoundHandler(), gson, bookService);
        final var postBooks = new PostBooksHandler(getBooks, bookService);
        final var tokenValidation = new TokenValidationHandler(postBooks, new KeycloakGateway(gson));
        final var options = new OptionsHandler(tokenValidation);
        final var cors = new CorsHandler(options);
        final var logs = new LogRequestHandler(cors);
        final var errorHandler = new ErrorHandler(logs);
        return errorHandler;
    }
}
