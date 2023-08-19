-- List books with current price
SELECT b.isbn, b.title, b.author, p.price, b.release_date
FROM books b
INNER JOIN (
    SELECT bp.isbn, bp.price
    FROM book_prices bp
    JOIN (
        SELECT isbn, MAX(created_at) as max_date
        FROM book_prices
        GROUP BY isbn
    ) subquery ON bp.isbn = subquery.isbn AND bp.created_at = subquery.max_date
) p ON p.isbn = b.isbn;