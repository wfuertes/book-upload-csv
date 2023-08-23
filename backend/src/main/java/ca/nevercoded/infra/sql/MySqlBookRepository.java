package ca.nevercoded.infra.sql;

import ca.nevercoded.domain.books.Book;
import ca.nevercoded.domain.books.BookRepository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MySqlBookRepository implements BookRepository {

    private final DataSource dataSource;

    public MySqlBookRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Book> findAll() {
        try (final var conn = dataSource.getConnection()) {
            try (final var record = conn.createStatement().executeQuery("""
                    SELECT b.isbn, b.title, b.author, p.price, b.release_date
                    FROM books b
                    INNER JOIN (
                        SELECT bp.isbn, bp.price
                        FROM book_prices bp
                        INNER JOIN (
                            SELECT isbn, MAX(created_at) as max_date
                            FROM book_prices
                            GROUP BY isbn
                        ) sub_query ON bp.isbn = sub_query.isbn AND bp.created_at = sub_query.max_date
                    ) p ON p.isbn = b.isbn
                    WHERE 1 = 1
                    """
            )) {
                final List<Book> books = new LinkedList<>();
                while (record.next()) {
                    final var book = new Book(record.getString("isbn"),
                            record.getString("title"),
                            record.getString("author"),
                            record.getBigDecimal("price"),
                            record.getTimestamp("release_date").toLocalDateTime().toLocalDate());
                    books.add(book);
                }
                return books;
            }
        } catch (SQLException err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public void save(Collection<Book> books) {
        try (final var conn = dataSource.getConnection()) {
            try (final var query = conn.prepareStatement("""
                    INSERT INTO books (isbn, title, author, release_date) VALUES (?,?,?,?) AS new
                    ON DUPLICATE KEY UPDATE title=new.title,author=new.author,release_date=new.release_date
                    """
            )) {
                for (final var book : books) {
                    query.setString(1, book.isbn());
                    query.setString(2, book.title());
                    query.setString(3, book.author());
                    query.setDate(4, Date.valueOf(book.releaseDate()));
                    query.addBatch();
                    query.execute();
                }
            }

            // "INSERT INTO book_prices (isbn, price, created_at) VALUES (?,?,?)"
            try (final var query = conn.prepareStatement("""
                    INSERT INTO book_prices (isbn, price, created_at)
                    SELECT ?, ?, ?
                    WHERE NOT EXISTS (
                        SELECT bp.isbn, bp.price
                        FROM book_prices bp
                        JOIN (
                            SELECT isbn, MAX(created_at) as max_date
                            FROM book_prices
                            GROUP BY isbn
                        ) sub_query ON bp.isbn = sub_query.isbn AND bp.created_at = sub_query.max_date
                        WHERE (bp.isbn, bp.price) = (?, ?)
                    )
                    """
            )) {
                for (final var book : books) {
                    query.setString(1, book.isbn());
                    query.setBigDecimal(2, book.price());
                    query.setTimestamp(3, Timestamp.from(Instant.now()));
                    // set where attributes
                    query.setString(4, book.isbn());
                    query.setBigDecimal(5, book.price());
                    query.execute();
                }
            }
        } catch (SQLException err) {
            throw new RuntimeException(err);
        }
    }
}
