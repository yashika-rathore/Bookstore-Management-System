import java.sql.Connection;
import java.sql.Statement;

public class DatabaseManager {

    public static void createTables() {

        try {
            Connection connection = DBConnection.getConnection();
            Statement statement = connection.createStatement();

            // 1. AUTHOR TABLE
            String createAuthorTable = """
                    CREATE TABLE IF NOT EXISTS AUTHOR (
                        author_id INT PRIMARY KEY AUTO_INCREMENT,
                        author_name VARCHAR(100) NOT NULL,
                        country VARCHAR(100)
                    )
                    """;

            statement.executeUpdate(createAuthorTable);
            System.out.println("AUTHOR table created successfully.");


            // 2. BOOK TABLE
            String createBookTable = """
                    CREATE TABLE IF NOT EXISTS BOOK (
                        book_id INT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(200) NOT NULL,
                        author_id INT,
                        category VARCHAR(100),
                        price DECIMAL(10,2) NOT NULL,
                        stock INT NOT NULL,
                        FOREIGN KEY (author_id) REFERENCES AUTHOR(author_id)
                    )
                    """;

            statement.executeUpdate(createBookTable);
            System.out.println("BOOK table created successfully.");


            // 3. CUSTOMER TABLE
            String createCustomerTable = """
                    CREATE TABLE IF NOT EXISTS CUSTOMER (
                        customer_id INT PRIMARY KEY AUTO_INCREMENT,
                        customer_name VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(15)
                    )
                    """;

            statement.executeUpdate(createCustomerTable);
            System.out.println("CUSTOMER table created successfully.");


            // 4. ORDERS TABLE
            String createOrdersTable = """
                    CREATE TABLE IF NOT EXISTS ORDERS (
                        order_id INT PRIMARY KEY AUTO_INCREMENT,
                        customer_id INT,
                        order_date DATE NOT NULL,
                        total_amount DECIMAL(10,2),
                        FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
                    )
                    """;

            statement.executeUpdate(createOrdersTable);
            System.out.println("ORDERS table created successfully.");


            // 5. ORDER_ITEM TABLE
            String createOrderItemTable = """
                    CREATE TABLE IF NOT EXISTS ORDER_ITEM (
                        order_item_id INT PRIMARY KEY AUTO_INCREMENT,
                        order_id INT,
                        book_id INT,
                        quantity INT NOT NULL,
                        price DECIMAL(10,2) NOT NULL,
                        FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
                        FOREIGN KEY (book_id) REFERENCES BOOK(book_id)
                    )
                    """;

            statement.executeUpdate(createOrderItemTable);
            System.out.println("ORDER_ITEM table created successfully.");


            statement.close();
            connection.close();

            System.out.println("----------------------------------");
            System.out.println("All tables created successfully!");
            System.out.println("----------------------------------");

        } catch (Exception e) {
            System.out.println("Error while creating tables.");
            e.printStackTrace();
        }
    }
}