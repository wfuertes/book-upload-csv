package ca.nevercoded.books;

import java.util.Collection;
import java.util.List;

public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> fetchBooks() {
        return repository.findAll();
    }

    public void storeBooks(Collection<Book> books) {
        repository.save(books);
    }
}
