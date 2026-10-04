import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    // =========================================================
    // CUSTOMER MODEL
    // =========================================================

    public static class Customer {

        int id;
        String name;

        public Customer(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }


    // =========================================================
    // BOOK MODEL
    // =========================================================

    public static class Book {

        int id;
        String title;
        double price;
        int stock;

        public Book(int id, String title, double price, int stock) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return title +
                    "  |  ₹" +
                    String.format("%.2f", price) +
                    "  |  Stock: " +
                    stock;
        }
    }


    // =========================================================
    // CART ITEM
    // =========================================================

    public static class CartItem {

        int bookId;
        String title;
        double price;
        int quantity;
        int stock;

        public CartItem(
                int bookId,
                String title,
                double price,
                int quantity,
                int stock) {

            this.bookId = bookId;
            this.title = title;
            this.price = price;
            this.quantity = quantity;
            this.stock = stock;
        }

        public double getTotal() {
            return price * quantity;
        }
    }


    // =========================================================
    // GET CUSTOMERS
    // =========================================================

    public static List<Customer> getCustomers() {

        List<Customer> customers = new ArrayList<>();

        String sql =
                "SELECT customer_id, customer_name " +
                        "FROM CUSTOMER " +
                        "ORDER BY customer_id";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                customers.add(
                        new Customer(
                                result.getInt("customer_id"),
                                result.getString("customer_name")
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return customers;
    }


    // =========================================================
    // GET AVAILABLE BOOKS
    // =========================================================

    public static List<Book> getBooks() {

        List<Book> books = new ArrayList<>();

        String sql =
                "SELECT book_id, title, price, stock " +
                        "FROM BOOK " +
                        "WHERE stock > 0 " +
                        "ORDER BY title";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                books.add(
                        new Book(
                                result.getInt("book_id"),
                                result.getString("title"),
                                result.getDouble("price"),
                                result.getInt("stock")
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return books;
    }


    // =========================================================
    // CREATE MULTI-BOOK ORDER
    // =========================================================

    public static int createOrder(
            int customerId,
            List<CartItem> cartItems) {

        if (cartItems == null || cartItems.isEmpty()) {

            System.out.println("Cart is empty.");
            return -1;
        }

        Connection connection = null;

        try {

            connection = DBConnection.getConnection();

            /*
             * IMPORTANT:
             * All database operations for one order are treated
             * as one transaction.
             */

            connection.setAutoCommit(false);


            // -------------------------------------------------
            // STEP 1: CHECK STOCK AND CALCULATE TOTAL
            // -------------------------------------------------

            double totalAmount = 0;

            for (CartItem item : cartItems) {

                String stockQuery =
                        "SELECT price, stock " +
                                "FROM BOOK " +
                                "WHERE book_id = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(stockQuery)) {

                    statement.setInt(1, item.bookId);

                    try (ResultSet result =
                                 statement.executeQuery()) {

                        if (!result.next()) {

                            throw new SQLException(
                                    "Book not found: " +
                                            item.title
                            );
                        }

                        double currentPrice =
                                result.getDouble("price");

                        int currentStock =
                                result.getInt("stock");


                        if (item.quantity <= 0) {

                            throw new SQLException(
                                    "Invalid quantity for " +
                                            item.title
                            );
                        }


                        if (currentStock < item.quantity) {

                            throw new SQLException(
                                    "Not enough stock for " +
                                            item.title +
                                            ". Available: " +
                                            currentStock
                            );
                        }


                        /*
                         * Use the current database price.
                         */

                        item.price = currentPrice;
                        item.stock = currentStock;

                        totalAmount +=
                                currentPrice *
                                        item.quantity;
                    }
                }
            }


            // -------------------------------------------------
            // STEP 2: INSERT INTO ORDERS
            // -------------------------------------------------

            String orderQuery =
                    "INSERT INTO ORDERS " +
                            "(customer_id, order_date, total_amount) " +
                            "VALUES (?, ?, ?)";

            int orderId;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 orderQuery,
                                 Statement.RETURN_GENERATED_KEYS
                         )) {

                statement.setInt(1, customerId);

                statement.setDate(
                        2,
                        Date.valueOf(LocalDate.now())
                );

                statement.setDouble(
                        3,
                        totalAmount
                );

                statement.executeUpdate();


                try (ResultSet keys =
                             statement.getGeneratedKeys()) {

                    if (!keys.next()) {

                        throw new SQLException(
                                "Could not generate order ID."
                        );
                    }

                    orderId = keys.getInt(1);
                }
            }


            // -------------------------------------------------
            // STEP 3: INSERT ORDER ITEMS
            // -------------------------------------------------

            String itemQuery =
                    "INSERT INTO ORDER_ITEM " +
                            "(order_id, book_id, quantity, price) " +
                            "VALUES (?, ?, ?, ?)";


            try (PreparedStatement statement =
                         connection.prepareStatement(itemQuery)) {

                for (CartItem item : cartItems) {

                    statement.setInt(
                            1,
                            orderId
                    );

                    statement.setInt(
                            2,
                            item.bookId
                    );

                    statement.setInt(
                            3,
                            item.quantity
                    );

                    statement.setDouble(
                            4,
                            item.price
                    );

                    statement.addBatch();
                }

                statement.executeBatch();
            }


            // -------------------------------------------------
            // STEP 4: UPDATE BOOK STOCK
            // -------------------------------------------------

            String stockUpdate =
                    "UPDATE BOOK " +
                            "SET stock = stock - ? " +
                            "WHERE book_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(stockUpdate)) {

                for (CartItem item : cartItems) {

                    statement.setInt(
                            1,
                            item.quantity
                    );

                    statement.setInt(
                            2,
                            item.bookId
                    );

                    statement.addBatch();
                }

                statement.executeBatch();
            }


            // -------------------------------------------------
            // STEP 5: COMMIT EVERYTHING
            // -------------------------------------------------

            connection.commit();

            return orderId;


        } catch (Exception e) {

            /*
             * If anything fails, undo the complete order.
             */

            if (connection != null) {

                try {
                    connection.rollback();

                } catch (SQLException rollbackError) {

                    rollbackError.printStackTrace();
                }
            }

            System.out.println(
                    "Order creation failed: " +
                            e.getMessage()
            );

            return -1;


        } finally {

            if (connection != null) {

                try {

                    connection.setAutoCommit(true);
                    connection.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }


    // =========================================================
    // GET ORDER DETAILS
    // =========================================================

    public static String getOrderDetails() {

        String sql = """
                SELECT
                    o.order_id,
                    c.customer_name,
                    o.order_date,
                    b.title,
                    oi.quantity,
                    oi.price,
                    (oi.quantity * oi.price) AS item_total,
                    o.total_amount
                FROM ORDERS o
                JOIN CUSTOMER c
                    ON o.customer_id = c.customer_id
                JOIN ORDER_ITEM oi
                    ON o.order_id = oi.order_id
                JOIN BOOK b
                    ON oi.book_id = b.book_id
                ORDER BY o.order_id DESC
                """;


        StringBuilder output =
                new StringBuilder();


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            output.append(
                    "\n"
            );

            output.append(
                    "══════════════════════════════════════════════════════════════════════════════\n"
            );

            output.append(
                    "                         ORDER DETAILS\n"
            );

            output.append(
                    "══════════════════════════════════════════════════════════════════════════════\n\n"
            );


            boolean found = false;


            while (result.next()) {

                found = true;


                output.append(
                        "Order ID : "
                );

                output.append(
                        result.getInt("order_id")
                );

                output.append("\n");


                output.append(
                        "Customer : "
                );

                output.append(
                        result.getString("customer_name")
                );

                output.append("\n");


                output.append(
                        "Date     : "
                );

                output.append(
                        result.getDate("order_date")
                );

                output.append("\n");


                output.append(
                        "Book     : "
                );

                output.append(
                        result.getString("title")
                );

                output.append("\n");


                output.append(
                        "Quantity : "
                );

                output.append(
                        result.getInt("quantity")
                );

                output.append("\n");


                output.append(
                        "Price    : ₹"
                );

                output.append(
                        String.format(
                                "%.2f",
                                result.getDouble("price")
                        )
                );

                output.append("\n");


                output.append(
                        "Item Total : ₹"
                );

                output.append(
                        String.format(
                                "%.2f",
                                result.getDouble("item_total")
                        )
                );

                output.append("\n");


                output.append(
                        "Order Total : ₹"
                );

                output.append(
                        String.format(
                                "%.2f",
                                result.getDouble("total_amount")
                        )
                );

                output.append("\n");


                output.append(
                        "──────────────────────────────────────────────────────────────────────────────\n"
                );
            }


            if (!found) {

                output.append(
                        "No orders found.\n"
                );
            }


        } catch (Exception e) {

            output.append(
                    "\nError loading orders: "
            );

            output.append(
                    e.getMessage()
            );
        }


        return output.toString();
    }
}