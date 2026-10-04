import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerManager {

    // ==========================================
    // INSERT - Add a new customer
    // ==========================================
    public static void addCustomer(
            String name,
            String email,
            String phone) {

        String sql = """
                INSERT INTO CUSTOMER
                (customer_name, email, phone)
                VALUES (?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);

            statement.executeUpdate();

            System.out.println("Customer added successfully.");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while adding customer.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // SELECT - Display all customers
    // ==========================================
    public static void displayCustomers() {

        String sql = "SELECT * FROM CUSTOMER";

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                      CUSTOMER LIST");
            System.out.println("==============================================================");

            while (result.next()) {

                int id = result.getInt("customer_id");
                String name = result.getString("customer_name");
                String email = result.getString("email");
                String phone = result.getString("phone");

                System.out.println(
                        "ID: " + id +
                                " | Name: " + name +
                                " | Email: " + email +
                                " | Phone: " + phone
                );
            }

            System.out.println("==============================================================");

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while displaying customers.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // UPDATE - Update customer email
    // ==========================================
    public static void updateCustomerEmail(
            int customerId,
            String newEmail) {

        String sql = """
                UPDATE CUSTOMER
                SET email = ?
                WHERE customer_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, newEmail);
            statement.setInt(2, customerId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Customer email updated successfully.");
            } else {
                System.out.println("Customer ID not found.");
            }

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error while updating customer.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // DELETE - Delete a customer
    // ==========================================
    public static String deleteCustomer(int customerId) {

        String checkSql = """
                SELECT COUNT(*)
                FROM ORDERS
                WHERE customer_id = ?
                """;

        String deleteSql = """
                DELETE FROM CUSTOMER
                WHERE customer_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            // ------------------------------------------
            // Check whether customer has orders
            // ------------------------------------------
            PreparedStatement checkStatement =
                    connection.prepareStatement(checkSql);

            checkStatement.setInt(1, customerId);

            ResultSet result = checkStatement.executeQuery();

            if (result.next() && result.getInt(1) > 0) {

                result.close();
                checkStatement.close();
                connection.close();

                return "Cannot delete this customer because they have existing orders.";
            }

            result.close();
            checkStatement.close();


            // ------------------------------------------
            // Delete customer
            // ------------------------------------------
            PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql);

            deleteStatement.setInt(1, customerId);

            int rowsDeleted = deleteStatement.executeUpdate();

            deleteStatement.close();
            connection.close();

            if (rowsDeleted > 0) {
                return "Customer deleted successfully.";
            } else {
                return "Customer ID not found.";
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Error while deleting customer: " + e.getMessage();
        }
    }
}