package com.bookstore.onlinebookstoremanagement.catalog;

import com.bookstore.onlinebookstoremanagement.models.Book;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookFileManager {

    private static final String FILE_PATH = "data/books.txt";

    public void init() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
                seedSampleBooks();
            }
        } catch (IOException e) {
            System.out.println("Error creating books file: "
                    + e.getMessage());
        }
    }

    public List<Book> loadAllBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty())
                    books.add(Book.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("Error reading books: "
                    + e.getMessage());
        }
        return books;
    }

    public void saveAllBooks(List<Book> books) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH))) {
            for (Book book : books) {
                bw.write(book.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving books: "
                    + e.getMessage());
        }
    }

    public void appendBook(Book book) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            bw.write(book.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending book: "
                    + e.getMessage());
        }
    }

    public boolean bookIdExists(String bookId) {
        for (Book b : loadAllBooks()) {
            if (b.getBookId().equals(bookId)) return true;
        }
        return false;
    }

    // Auto-seed 6 sample books on first run
    private void seedSampleBooks() {
        List<Book> samples = new ArrayList<>();
        samples.add(new Book("B001", "Clean Code",
                "Robert C. Martin", "Technology",
                35.99, 10,
                "A handbook of agile software craftsmanship"));
        samples.add(new Book("B002", "Harry Potter",
                "J.K. Rowling", "Fiction",
                19.99, 25,
                "A young wizard's journey at Hogwarts"));
        samples.add(new Book("B003", "Atomic Habits",
                "James Clear", "Self-Help",
                22.50, 15,
                "Tiny changes, remarkable results"));
        samples.add(new Book("B004", "The Alchemist",
                "Paulo Coelho", "Fiction",
                14.99, 20,
                "A journey of self-discovery"));
        samples.add(new Book("B005", "Deep Work",
                "Cal Newport", "Self-Help",
                18.00, 8,
                "Rules for focused success"));
        samples.add(new Book("B006", "Design Patterns",
                "Gang of Four", "Technology",
                45.00, 5,
                "Elements of reusable OOP software"));
        saveAllBooks(samples);
    }
}