package ca.nevercoded.domain.books;

import java.util.Collection;
import java.util.List;

public interface BookRepository {

    List<Book> findAll();

    void save(Collection<Book> books);
}
