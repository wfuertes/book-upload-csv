package ca.nevercoded.books;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Book(String isbn, String title, String author, BigDecimal price, LocalDate releaseDate) {
}
