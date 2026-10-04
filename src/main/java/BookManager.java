import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookManager {

    // ==========================================
    // INSERT - Add a new book
    // ==========================================
    public static void addBook(
            String title,
            int authorId,
            String category,
            double price,
            int stock) {

        String sql = """
                INSERT INTO BOOK
                (title, author_id, category, price, stock)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, title);
            statement.setInt(2, authorId);
            statement.setString(3, category);
            statement.setDouble(4, price);
            statement.setInt(5, stock);

            statement.executeUpdate();

            System.out.println("Book added successfully.");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while adding book.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // SELECT - Display all books
    // ==========================================
    public static void displayBooks() {

        String sql = """
                SELECT
                    b.book_id,
                    b.title,
                    a.author_name,
                    b.category,
                    b.price,
                    b.stock
                FROM BOOK b
                LEFT JOIN AUTHOR a
                    ON b.author_id = a.author_id
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

            System.out.println();
            System.out.println("======================================================================");
            System.out.println("                         BOOK LIST");
            System.out.println("======================================================================");

            while (result.next()) {

                int id = result.getInt("book_id");
                String title = result.getString("title");
                String author = result.getString("author_name");
                String category = result.getString("category");
                double price = result.getDouble("price");
                int stock = result.getInt("stock");

                System.out.println(
                        "ID: " + id +
                                " | Title: " + title +
                                " | Author: " + author +
                                " | Category: " + category +
                                " | Price: ₹" + price +
                                " | Stock: " + stock
                );
            }

            System.out.println("======================================================================");

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while displaying books.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // UPDATE - Update book price
    // ==========================================
    public static void updateBookPrice(int bookId, double newPrice) {

        String sql = """
                UPDATE BOOK
                SET price = ?
                WHERE book_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setDouble(1, newPrice);
            statement.setInt(2, bookId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book price updated successfully.");
            } else {
                System.out.println("Book ID not found.");
            }

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while updating book price.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // UPDATE - Update book stock
    // ==========================================
    public static void updateBookStock(int bookId, int newStock) {

        String sql = """
                UPDATE BOOK
                SET stock = ?
                WHERE book_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, newStock);
            statement.setInt(2, bookId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book stock updated successfully.");
            } else {
                System.out.println("Book ID not found.");
            }

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while updating book stock.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // DELETE - Delete a book
    // ==========================================
    public static String deleteBook(int bookId) {

        String checkSql = """
                SELECT COUNT(*)
                FROM ORDER_ITEM
                WHERE book_id = ?
                """;

        String deleteSql = """
                DELETE FROM BOOK
                WHERE book_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            // ------------------------------------------
            // First check whether the book is used
            // in an existing order
            // ------------------------------------------
            PreparedStatement checkStatement =
                    connection.prepareStatement(checkSql);

            checkStatement.setInt(1, bookId);

            ResultSet result = checkStatement.executeQuery();

            if (result.next() && result.getInt(1) > 0) {

                result.close();
                checkStatement.close();
                connection.close();

                return "Cannot delete this book because it is part of an existing order.";
            }

            result.close();
            checkStatement.close();


            // ------------------------------------------
            // Delete the book
            // ------------------------------------------
            PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql);

            deleteStatement.setInt(1, bookId);

            int rowsDeleted = deleteStatement.executeUpdate();

            deleteStatement.close();
            connection.close();

            if (rowsDeleted > 0) {
                return "Book deleted successfully.";
            } else {
                return "Book ID not found.";
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Error while deleting book: " + e.getMessage();
        }
    }
}