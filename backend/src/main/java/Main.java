import ca.nevercoded.infra.http.books.BooksResource;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            final var server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/books", BooksResource.create());
            server.start();
            LOG.info("Server has started and is now accepting requests at port 8080");
        } catch (IOException e) {
            LOG.error("Error on start HttpServer on port 8080", e);
            throw new RuntimeException(e);
        }
    }
}
