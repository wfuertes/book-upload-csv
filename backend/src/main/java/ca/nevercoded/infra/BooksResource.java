package ca.nevercoded.infra;

import ca.nevercoded.books.BookRepository;
import ca.nevercoded.books.BookService;
import ca.nevercoded.infra.config.DataSourceFactory;
import ca.nevercoded.infra.config.GsonFactory;
import ca.nevercoded.infra.data.MySqlBookRepository;
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
