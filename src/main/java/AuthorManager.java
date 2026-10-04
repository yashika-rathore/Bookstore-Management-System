import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthorManager {

    // ==========================================
    // INSERT - Add author
    // ==========================================
    public static void addAuthor(String name, String country) {

        String sql =
                "INSERT INTO AUTHOR (author_name, country) VALUES (?, ?)";

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, name);
            statement.setString(2, country);

            statement.executeUpdate();

            System.out.println("Author added successfully.");

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println("Error while adding author.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // SELECT - Display all authors
    // ==========================================
    public static void displayAuthors() {

        String sql = "SELECT * FROM AUTHOR";

        try {
            Connection connection = DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

            System.out.println();
            System.out.println("==============================================");
            System.out.println("                AUTHOR LIST");
            System.out.println("==============================================");

            while (result.next()) {

                int id = result.getInt("author_id");
                String name = result.getString("author_name");
                String country = result.getString("country");

                System.out.println(
                        "ID: " + id +
                                " | Name: " + name +
                                " | Country: " + country
                );
            }

            System.out.println("==============================================");

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println("Error while displaying authors.");
            e.printStackTrace();
        }
    }


    // ==========================================
    // DELETE - Delete author
    // ==========================================
    public static String deleteAuthor(int authorId) {

        String checkSql = """
                SELECT COUNT(*)
                FROM BOOK
                WHERE author_id = ?
                """;

        String deleteSql = """
                DELETE FROM AUTHOR
                WHERE author_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();

            // ------------------------------------------
            // Check whether author has books
            // ------------------------------------------
            PreparedStatement checkStatement =
                    connection.prepareStatement(checkSql);

            checkStatement.setInt(1, authorId);

            ResultSet result = checkStatement.executeQuery();

            if (result.next() && result.getInt(1) > 0) {

                result.close();
                checkStatement.close();
                connection.close();

                return "Cannot delete this author because books are associated with this author.";
            }

            result.close();
            checkStatement.close();


            // ------------------------------------------
            // Delete author
            // ------------------------------------------
            PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql);

            deleteStatement.setInt(1, authorId);

            int rowsDeleted = deleteStatement.executeUpdate();

            deleteStatement.close();
            connection.close();

            if (rowsDeleted > 0) {
                return "Author deleted successfully.";
            } else {
                return "Author ID not found.";
            }

        } catch (Exception e) {

            e.printStackTrace();

            return "Error while deleting author: " + e.getMessage();
        }
    }
}