package com.bookstore.onlinebookstoremanagement.catalog;

import com.bookstore.onlinebookstoremanagement.models.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StubBookService bookService;

    @BeforeEach
    void setUp() {
        BookController controller = new BookController();
        bookService = new StubBookService();
        ReflectionTestUtils.setField(controller, "bookService", bookService);
        controller.init();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllBooksReturnsBooks() throws Exception {
        bookService.allBooks = List.of(book("B001", "Clean Code"));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B001"))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void getBookByIdReturnsBook() throws Exception {
        bookService.booksById.put("B001", book("B001", "Clean Code"));

        mockMvc.perform(get("/api/books/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("B001"))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getBookByIdReturnsBadRequestWhenMissing() throws Exception {
        mockMvc.perform(get("/api/books/MISSING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Book not found."));
    }

    @Test
    void searchByTitleReturnsBooks() throws Exception {
        bookService.titleSearchResults.put("clean", List.of(book("B001", "Clean Code")));

        mockMvc.perform(get("/api/books/search/title").param("keyword", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void searchByAuthorReturnsBooks() throws Exception {
        bookService.authorSearchResults.put("martin", List.of(book("B001", "Clean Code")));

        mockMvc.perform(get("/api/books/search/author").param("keyword", "martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"));
    }

    @Test
    void filterByCategoryReturnsBooks() throws Exception {
        bookService.categoryResults.put("Technology", List.of(book("B001", "Clean Code")));

        mockMvc.perform(get("/api/books/category").param("name", "Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Technology"));
    }

    @Test
    void filterByMaxPriceReturnsBooks() throws Exception {
        bookService.priceResults.put(20.0, List.of(book("B002", "Harry Potter")));

        mockMvc.perform(get("/api/books/price").param("max", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B002"));
    }

    @Test
    void getAllCategoriesReturnsCategories() throws Exception {
        bookService.categories = List.of("Technology", "Fiction");

        mockMvc.perform(get("/api/books/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Technology"))
                .andExpect(jsonPath("$[1]").value("Fiction"));
    }

    @Test
    void sortByPriceLowToHighReturnsBooks() throws Exception {
        bookService.priceAscBooks = List.of(book("B002", "Harry Potter"), book("B001", "Clean Code"));

        mockMvc.perform(get("/api/books/sort/price-asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B002"));
    }

    @Test
    void sortByPriceHighToLowReturnsBooks() throws Exception {
        bookService.priceDescBooks = List.of(book("B001", "Clean Code"), book("B002", "Harry Potter"));

        mockMvc.perform(get("/api/books/sort/price-desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B001"));
    }

    @Test
    void sortByRatingReturnsBooks() throws Exception {
        Book topRated = book("B001", "Clean Code");
        topRated.setRating(4.8);
        bookService.ratingBooks = List.of(topRated);

        mockMvc.perform(get("/api/books/sort/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(4.8));
    }

    @Test
    void addBookReturnsSuccess() throws Exception {
        bookService.addResult = "SUCCESS: Book added with ID B777";

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(book(null, "New Book"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS: Book added with ID B777"));
    }

    @Test
    void addBookReturnsBadRequestForInvalidBook() throws Exception {
        bookService.addResult = "ERROR: Title cannot be empty.";

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(book(null, ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERROR: Title cannot be empty."));
    }

    @Test
    void editBookReturnsSuccess() throws Exception {
        bookService.editResults.put("B001", "SUCCESS: Book B001 updated.");

        mockMvc.perform(put("/api/books/B001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(book(null, "Updated Book"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS: Book B001 updated."));
    }

    @Test
    void editBookReturnsBadRequestWhenMissing() throws Exception {
        bookService.editResults.put("MISSING", "ERROR: Book not found.");

        mockMvc.perform(put("/api/books/MISSING")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(book(null, "Updated Book"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERROR: Book not found."));
    }

    @Test
    void deleteBookReturnsSuccess() throws Exception {
        bookService.deleteResults.put("B001", "SUCCESS: Book B001 deleted.");

        mockMvc.perform(delete("/api/books/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS: Book B001 deleted."));
    }

    @Test
    void deleteBookReturnsBadRequestWhenMissing() throws Exception {
        bookService.deleteResults.put("MISSING", "ERROR: Book not found.");

        mockMvc.perform(delete("/api/books/MISSING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERROR: Book not found."));
    }

    @Test
    void restockBookReturnsSuccess() throws Exception {
        bookService.restockResults.put("B001:5", "SUCCESS: Restocked. New stock: 15");

        mockMvc.perform(put("/api/books/B001/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("quantity", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS: Restocked. New stock: 15"));
    }

    @Test
    void restockBookReturnsBadRequestForInvalidQuantity() throws Exception {
        bookService.restockResults.put("B001:0", "ERROR: Quantity must be greater than 0.");

        mockMvc.perform(put("/api/books/B001/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("quantity", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ERROR: Quantity must be greater than 0."));
    }

    private Book book(String bookId, String title) {
        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle(title);
        book.setAuthor("Robert C. Martin");
        book.setCategory("Technology");
        book.setPrice(35.99);
        book.setStock(10);
        book.setDescription("Sample");
        book.setImageUrl("");
        book.setRating(4.5);
        return book;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private static class StubBookService extends BookService {
        private List<Book> allBooks = List.of();
        private final Map<String, Book> booksById = new HashMap<>();
        private final Map<String, List<Book>> titleSearchResults = new HashMap<>();
        private final Map<String, List<Book>> authorSearchResults = new HashMap<>();
        private final Map<String, List<Book>> categoryResults = new HashMap<>();
        private final Map<Double, List<Book>> priceResults = new HashMap<>();
        private List<String> categories = List.of();
        private List<Book> priceAscBooks = List.of();
        private List<Book> priceDescBooks = List.of();
        private List<Book> ratingBooks = List.of();
        private String addResult = "SUCCESS";
        private final Map<String, String> editResults = new HashMap<>();
        private final Map<String, String> deleteResults = new HashMap<>();
        private final Map<String, String> restockResults = new HashMap<>();

        @Override
        public void init() {
        }

        @Override
        public List<Book> getAllBooks() {
            return allBooks;
        }

        @Override
        public Book getBookById(String bookId) {
            return booksById.get(bookId);
        }

        @Override
        public List<Book> searchByTitle(String keyword) {
            return titleSearchResults.getOrDefault(keyword, List.of());
        }

        @Override
        public List<Book> searchByAuthor(String keyword) {
            return authorSearchResults.getOrDefault(keyword, List.of());
        }

        @Override
        public List<Book> filterByCategory(String category) {
            return categoryResults.getOrDefault(category, List.of());
        }

        @Override
        public List<Book> filterByMaxPrice(double maxPrice) {
            return priceResults.getOrDefault(maxPrice, List.of());
        }

        @Override
        public List<String> getAllCategories() {
            return categories;
        }

        @Override
        public List<Book> sortByPriceLowToHigh() {
            return priceAscBooks;
        }

        @Override
        public List<Book> sortByPriceHighToLow() {
            return priceDescBooks;
        }

        @Override
        public List<Book> sortByRating() {
            return ratingBooks;
        }

        @Override
        public String addBook(Book book) {
            return addResult;
        }

        @Override
        public String editBook(String bookId, Book updated) {
            return editResults.getOrDefault(bookId, "ERROR: Book not found.");
        }

        @Override
        public String deleteBook(String bookId) {
            return deleteResults.getOrDefault(bookId, "ERROR: Book not found.");
        }

        @Override
        public String restockBook(String bookId, int qty) {
            return restockResults.getOrDefault(bookId + ":" + qty, "ERROR: Book not found.");
        }
    }
}
