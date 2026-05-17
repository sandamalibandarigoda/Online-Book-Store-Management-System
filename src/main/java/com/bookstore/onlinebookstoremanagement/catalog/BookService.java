package com.bookstore.onlinebookstoremanagement.catalog;

import com.bookstore.onlinebookstoremanagement.models.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BookService {

    @Autowired
    private BookFileManager bookFileManager;

    public void init() {
        bookFileManager.init();
    }

    // Get all books
    public List<Book> getAllBooks() {
        return bookFileManager.loadAllBooks();
    }

    // Get book by ID
    public Book getBookById(String bookId) {
        for (Book b : bookFileManager.loadAllBooks()) {
            if (b.getBookId().equals(bookId)) return b;
        }
        return null;
    }

    // Search by title (partial, case-insensitive)
    public List<Book> searchByTitle(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : bookFileManager.loadAllBooks()) {
            if (b.getTitle().toLowerCase()
                    .contains(keyword.toLowerCase()))
                result.add(b);
        }
        return result;
    }

    // Search by author (partial, case-insensitive)
    public List<Book> searchByAuthor(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : bookFileManager.loadAllBooks()) {
            if (b.getAuthor().toLowerCase()
                    .contains(keyword.toLowerCase()))
                result.add(b);
        }
        return result;
    }

    // Filter by category
    public List<Book> filterByCategory(String category) {
        List<Book> result = new ArrayList<>();
        for (Book b : bookFileManager.loadAllBooks()) {
            if (b.getCategory().equalsIgnoreCase(category))
                result.add(b);
        }
        return result;
    }

    // Filter by max price
    public List<Book> filterByMaxPrice(double maxPrice) {
        List<Book> result = new ArrayList<>();
        for (Book b : bookFileManager.loadAllBooks()) {
            if (b.getPrice() <= maxPrice) result.add(b);
        }
        return result;
    }

    // Get all unique categories
    public List<String> getAllCategories() {
        Set<String> cats = new LinkedHashSet<>();
        for (Book b : bookFileManager.loadAllBooks())
            cats.add(b.getCategory());
        return new ArrayList<>(cats);
    }

    // Sort by price low to high
    public List<Book> sortByPriceLowToHigh() {
        List<Book> books = bookFileManager.loadAllBooks();
        books.sort(Comparator.comparingDouble(Book::getPrice));
        return books;
    }

    // Sort by price high to low
    public List<Book> sortByPriceHighToLow() {
        List<Book> books = bookFileManager.loadAllBooks();
        books.sort(Comparator.comparingDouble(
                Book::getPrice).reversed());
        return books;
    }

    // Sort by rating
    public List<Book> sortByRating() {
        List<Book> books = bookFileManager.loadAllBooks();
        books.sort(Comparator.comparingDouble(
                Book::getRating).reversed());
        return books;
    }

    // Add new book (admin)
    public String addBook(Book book) {
        if (book.getTitle() == null
                || book.getTitle().trim().isEmpty())
            return "ERROR: Title cannot be empty.";
        if (book.getAuthor() == null
                || book.getAuthor().trim().isEmpty())
            return "ERROR: Author cannot be empty.";
        if (book.getPrice() <= 0)
            return "ERROR: Price must be greater than 0.";
        if (book.getStock() < 0)
            return "ERROR: Stock cannot be negative.";

        String bookId = "B" + UUID.randomUUID()
                .toString().substring(0, 6).toUpperCase();
        book.setBookId(bookId);
        bookFileManager.appendBook(book);
        return "SUCCESS: Book added with ID " + bookId;
    }

    // Edit book (admin)
    public String editBook(String bookId, Book updated) {
        List<Book> books = bookFileManager.loadAllBooks();
        for (Book b : books) {
            if (b.getBookId().equals(bookId)) {
                if (updated.getTitle() != null
                        && !updated.getTitle().isEmpty())
                    b.setTitle(updated.getTitle());
                if (updated.getAuthor() != null
                        && !updated.getAuthor().isEmpty())
                    b.setAuthor(updated.getAuthor());
                if (updated.getCategory() != null
                        && !updated.getCategory().isEmpty())
                    b.setCategory(updated.getCategory());
                if (updated.getPrice() > 0)
                    b.setPrice(updated.getPrice());
                if (updated.getStock() >= 0)
                    b.setStock(updated.getStock());
                if (updated.getDescription() != null
                        && !updated.getDescription().isEmpty())
                    b.setDescription(updated.getDescription());
                bookFileManager.saveAllBooks(books);
                return "SUCCESS: Book " + bookId + " updated.";
            }
        }
        return "ERROR: Book not found.";
    }

    // Delete book (admin)
    public String deleteBook(String bookId) {
        List<Book> books = bookFileManager.loadAllBooks();
        boolean removed = books.removeIf(
                b -> b.getBookId().equals(bookId));
        if (!removed) return "ERROR: Book not found.";
        bookFileManager.saveAllBooks(books);
        return "SUCCESS: Book " + bookId + " deleted.";
    }

    // Restock book (admin)
    public String restockBook(String bookId, int qty) {
        if (qty <= 0)
            return "ERROR: Quantity must be greater than 0.";
        List<Book> books = bookFileManager.loadAllBooks();
        for (Book b : books) {
            if (b.getBookId().equals(bookId)) {
                b.setStock(b.getStock() + qty);
                bookFileManager.saveAllBooks(books);
                return "SUCCESS: Restocked. New stock: "
                        + b.getStock();
            }
        }
        return "ERROR: Book not found.";
    }

    // Update book rating (called by review module)
    public void updateBookRating(String bookId,
                                 double avgRating) {
        List<Book> books = bookFileManager.loadAllBooks();
        for (Book b : books) {
            if (b.getBookId().equals(bookId)) {
                b.setRating(Math.round(avgRating * 10.0)
                        / 10.0);
                bookFileManager.saveAllBooks(books);
                return;
            }
        }
    }
    // Save all books — used by order service
// after stock deduction
    public void saveAllBooks(List<Book> books) {
        bookFileManager.saveAllBooks(books);
    }
}
