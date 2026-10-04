public class Main {

    public static void main(String[] args) {

        System.out.println(
                "Starting Book Store Management System..."
        );

        // Create tables if they don't already exist
        DatabaseManager.createTables();

        // Start GUI
        StoreGUI.main(args);
    }
}