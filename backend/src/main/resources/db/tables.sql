-- bookstore.books definition
CREATE TABLE books (
  isbn varchar(13) NOT NULL,
  title varchar(256) NOT NULL,
  author varchar(128) NOT NULL,
  release_date datetime NOT NULL,
  PRIMARY KEY (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- bookstore.book_prices definition
CREATE TABLE book_prices (
  isbn varchar(13) NOT NULL,
  price decimal(14,2) NOT NULL,
  created_at datetime NOT NULL,
  PRIMARY KEY (isbn,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
