import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreGUI extends JFrame {

    // ---------- Theme ----------
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color PRIMARY_DARK = new Color(67, 56, 202);
    private static final Color ACCENT = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color BG = new Color(238, 242, 255);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(203, 213, 225);

    // ---------- Order controls ----------
    private JComboBox<OrderManager.Customer> customerBox;
    private JComboBox<OrderManager.Book> bookBox;
    private JSpinner quantitySpinner;
    private JLabel bookInfo, cartInfo, totalLabel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private final List<OrderManager.CartItem> cart = new ArrayList<>();

    public StoreGUI() {
        setTitle("BookHub - Book Store Management System");
        setSize(1050, 720);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(0, 0, 10, 0));

        root.add(header(), BorderLayout.NORTH);
        root.add(tabs(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PRIMARY_DARK);
        p.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel title = label("📚 BookHub", 27, Color.WHITE, true);
        JLabel sub = label("Book Store Management System", 13,
                new Color(224, 231, 255), false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        JLabel status = label("● DATABASE CONNECTED", 12,
                new Color(167, 243, 208), true);
        p.add(left, BorderLayout.WEST);
        p.add(status, BorderLayout.EAST);
        return p;
    }

    private JTabbedPane tabs() {
        JTabbedPane t = new JTabbedPane();
        t.setFont(new Font("SansSerif", Font.BOLD, 13));
        t.addTab("✍ Authors", authorsTab());
        t.addTab("📚 Books", booksTab());
        t.addTab("👥 Customers", customersTab());
        t.addTab("🛒 New Order", orderTab());
        return t;
    }

    // =========================================================
    // AUTHORS
    // =========================================================

    private JPanel authorsTab() {
        JTextField name = field();
        JTextField country = field();
        JTextArea output = outputArea();

        JPanel form = card(new GridBagLayout());
        GridBagConstraints g = formGbc();

        addFormField(form, g, 0, "Author Name", name);
        addFormField(form, g, 1, "Country", country);

        JPanel buttons = buttonRow(
                primary("＋ Add Author"),
                secondary("View Authors"),
                danger("Delete Author"),
                secondary("↻ Refresh")
        );

        JButton add = (JButton) buttons.getComponent(0);
        JButton view = (JButton) buttons.getComponent(1);
        JButton del = (JButton) buttons.getComponent(2);
        JButton refresh = (JButton) buttons.getComponent(3);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.weightx = 1;
        form.add(buttons, g);

        add.addActionListener(e -> {
            String n = name.getText().trim();
            String c = country.getText().trim();

            if (n.isEmpty() || c.isEmpty()) {
                warn("Please enter author name and country.");
                return;
            }

            AuthorManager.addAuthor(n, c);
            name.setText("");
            country.setText("");
            info("Author added successfully.");
            showAuthors(output);
        });

        view.addActionListener(e -> showAuthorsTable());
        refresh.addActionListener(e -> showAuthors(output));
        del.addActionListener(e -> deleteAuthor(output));

        JPanel page = page();
        page.add(sectionBanner("✍  AUTHORS",
                "Add and manage authors.", PRIMARY), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(scroll(output), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    // =========================================================
    // BOOKS
    // =========================================================

    private JPanel booksTab() {
        JTextField title = field();
        JTextField authorId = field();
        JTextField category = field();
        JTextField price = field();
        JTextField stock = field();
        JTextArea output = outputArea();

        JPanel form = card(new GridBagLayout());
        GridBagConstraints g = formGbc();

        addFormField(form, g, 0, "Book Title", title);
        addFormField(form, g, 1, "Author ID", authorId);
        addFormField(form, g, 2, "Category", category);
        addFormField(form, g, 3, "Price (₹)", price);
        addFormField(form, g, 4, "Stock", stock);

        JPanel buttons = buttonRow(
                primary("＋ Add Book"),
                secondary("View Books"),
                danger("Delete Book"),
                secondary("↻ Refresh")
        );

        JButton add = (JButton) buttons.getComponent(0);
        JButton view = (JButton) buttons.getComponent(1);
        JButton del = (JButton) buttons.getComponent(2);
        JButton refresh = (JButton) buttons.getComponent(3);

        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        form.add(buttons, g);

        add.addActionListener(e -> {
            try {
                String t = title.getText().trim();
                String a = authorId.getText().trim();
                String cat = category.getText().trim();

                if (t.isEmpty() || a.isEmpty() || cat.isEmpty()
                        || price.getText().trim().isEmpty()
                        || stock.getText().trim().isEmpty()) {
                    warn("Please fill all book details.");
                    return;
                }

                double p = Double.parseDouble(price.getText().trim());
                int st = Integer.parseInt(stock.getText().trim());
                int aid = Integer.parseInt(a);

                BookManager.addBook(t, aid, cat, p, st);

                title.setText("");
                authorId.setText("");
                category.setText("");
                price.setText("");
                stock.setText("");

                info("Book added successfully.");
                showBooks(output);
                loadBooks();
            } catch (NumberFormatException ex) {
                warn("Author ID, price and stock must be valid numbers.");
            }
        });

        view.addActionListener(e -> showBooksTable());
        refresh.addActionListener(e -> {
            showBooks(output);
            loadBooks();
        });
        del.addActionListener(e -> {
            deleteBook(output);
            loadBooks();
        });

        JPanel page = page();
        page.add(sectionBanner("📚  BOOK INVENTORY",
                        "Add and manage books, prices and stock.", new Color(124, 58, 237)),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(scroll(output), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    // =========================================================
    // CUSTOMERS
    // =========================================================

    private JPanel customersTab() {
        JTextField name = field();
        JTextField email = field();
        JTextField phone = field();
        JTextArea output = outputArea();

        JPanel form = card(new GridBagLayout());
        GridBagConstraints g = formGbc();

        addFormField(form, g, 0, "Customer Name", name);
        addFormField(form, g, 1, "Email", email);
        addFormField(form, g, 2, "Phone", phone);

        JPanel buttons = buttonRow(
                primary("＋ Add Customer"),
                secondary("View Customers"),
                danger("Delete Customer"),
                secondary("↻ Refresh")
        );

        JButton add = (JButton) buttons.getComponent(0);
        JButton view = (JButton) buttons.getComponent(1);
        JButton del = (JButton) buttons.getComponent(2);
        JButton refresh = (JButton) buttons.getComponent(3);

        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        form.add(buttons, g);

        add.addActionListener(e -> {
            String n = name.getText().trim();
            String em = email.getText().trim();
            String ph = phone.getText().trim();

            if (n.isEmpty() || em.isEmpty() || ph.isEmpty()) {
                warn("Please fill all customer details.");
                return;
            }

            CustomerManager.addCustomer(n, em, ph);
            name.setText("");
            email.setText("");
            phone.setText("");

            info("Customer added successfully.");
            showCustomers(output);
            loadCustomers();
        });

        view.addActionListener(e -> showCustomers(output));
        refresh.addActionListener(e -> {
            showCustomers(output);
            loadCustomers();
        });
        del.addActionListener(e -> {
            deleteCustomer(output);
            loadCustomers();
        });

        JPanel page = page();
        page.add(sectionBanner("👥  CUSTOMERS",
                        "Add and manage customer records.", new Color(8, 145, 178)),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(scroll(output), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    // =========================================================
    // ORDERS
    // =========================================================

    private JPanel orderTab() {
        customerBox = new JComboBox<>();
        bookBox = new JComboBox<>();
        customerBox.setFont(font(13, false));
        bookBox.setFont(font(13, false));

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        bookInfo = label("Please select a book.", 12, MUTED, false);
        setupOrderComboBoxes();

        JPanel input = card(new GridBagLayout());
        GridBagConstraints g = gbc();

        addRow(input, g, 0, "Customer", customerBox);
        addRow(input, g, 1, "Book", bookBox);
        addRow(input, g, 2, "Book Info", bookInfo);
        addRow(input, g, 3, "Quantity", quantitySpinner);

        JButton add = primary("＋ Add to Cart");
        JButton refresh = secondary("↻ Refresh");
        g.gridx = 1; g.gridy = 4; g.gridwidth = 1;
        input.add(add, g);
        g.gridx = 2;
        input.add(refresh, g);

        cartModel = new DefaultTableModel(
                new Object[]{"Book", "Quantity", "Price", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(27);
        cartTable.setFont(font(13, false));
        cartTable.getTableHeader().setFont(font(13, true));

        JPanel cartCard = card(new BorderLayout(10, 10));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label("🛒 Cart", 18, TEXT, true), BorderLayout.WEST);
        cartInfo = label("0 books", 12, MUTED, false);
        top.add(cartInfo, BorderLayout.EAST);
        cartCard.add(top, BorderLayout.NORTH);
        cartCard.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        JButton remove = secondary("Remove Selected");
        JButton clear = danger("Clear Cart");
        JButton previous = secondary("Previous Orders");
        JButton place = success("Place Order");
        totalLabel = label("Total: ₹0.00", 20, PRIMARY, true);
        bottom.add(remove);
        bottom.add(clear);
        bottom.add(previous);
        bottom.add(totalLabel);
        bottom.add(place);
        cartCard.add(bottom, BorderLayout.SOUTH);

        add.addActionListener(e -> addToCart());
        refresh.addActionListener(e -> {
            loadCustomers();
            loadBooks();
        });
        bookBox.addActionListener(e -> updateBookInfo());
        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> clearCart());
        previous.addActionListener(e -> showPreviousOrders());
        place.addActionListener(e -> placeOrder());

        loadCustomers();
        loadBooks();

        customerBox.setSelectedIndex(-1);
        bookBox.setSelectedIndex(-1);
        bookInfo.setText("Please select a book.");
        quantitySpinner.setValue(1);

        JPanel page = page();
        JPanel orderBody = new JPanel(new BorderLayout(12, 12));
        orderBody.setOpaque(false);
        orderBody.add(input, BorderLayout.NORTH);
        orderBody.add(cartCard, BorderLayout.CENTER);
        page.add(sectionBanner("🛒  NEW ORDER", "Create an order with multiple books.", ACCENT), BorderLayout.NORTH);
        page.add(orderBody, BorderLayout.CENTER);
        return page;
    }

    private void setupOrderComboBoxes() {
        customerBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String text = value == null ? "Select customer..." : value.toString();
                return super.getListCellRendererComponent(
                        list, text, index, isSelected, cellHasFocus);
            }
        });

        bookBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String text = value == null ? "Select book..." : value.toString();
                return super.getListCellRendererComponent(
                        list, text, index, isSelected, cellHasFocus);
            }
        });
    }

    private void loadCustomers() {
        if (customerBox == null) return;

        customerBox.removeAllItems();

        for (OrderManager.Customer c : OrderManager.getCustomers()) {
            customerBox.addItem(c);
        }

        customerBox.setSelectedIndex(-1);
    }

    private void loadBooks() {
        if (bookBox == null) return;

        bookBox.removeAllItems();

        for (OrderManager.Book b : OrderManager.getBooks()) {
            bookBox.addItem(b);
        }

        bookBox.setSelectedIndex(-1);

        if (bookInfo != null) {
            bookInfo.setText("Please select a book.");
        }
    }

    private void updateBookInfo() {
        if (bookInfo == null) return;
        OrderManager.Book b = (OrderManager.Book) bookBox.getSelectedItem();
        if (b == null) {
            bookInfo.setText("No book available.");
            return;
        }
        bookInfo.setText(String.format(
                "Price: ₹%.2f    |    Available Stock: %d",
                b.price, b.stock));
    }

    private void addToCart() {
        OrderManager.Book b = (OrderManager.Book) bookBox.getSelectedItem();
        if (b == null) {
            warn("Please select a book.");
            return;
        }

        int qty = (Integer) quantitySpinner.getValue();

        for (OrderManager.CartItem item : cart) {
            if (item.bookId == b.id) {
                if (item.quantity + qty > b.stock) {
                    warn("Only " + b.stock + " copies are available.");
                    return;
                }
                item.quantity += qty;
                refreshCart();
                return;
            }
        }

        if (qty > b.stock) {
            warn("Only " + b.stock + " copies are available.");
            return;
        }

        cart.add(new OrderManager.CartItem(
                b.id, b.title, b.price, qty, b.stock));
        quantitySpinner.setValue(1);
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0;
        int copies = 0;

        for (OrderManager.CartItem item : cart) {
            double itemTotal = item.getTotal();
            cartModel.addRow(new Object[]{
                    item.title, item.quantity,
                    String.format("₹%.2f", item.price),
                    String.format("₹%.2f", itemTotal)
            });
            total += itemTotal;
            copies += item.quantity;
        }

        cartInfo.setText(cart.size() + " book(s) | " + copies + " copy/copies");
        totalLabel.setText(String.format("Total: ₹%.2f", total));
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            warn("Select an item from the cart.");
            return;
        }
        cart.remove(row);
        refreshCart();
    }

    private void clearCart() {
        if (cart.isEmpty()) {
            warn("Cart is already empty.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Clear all items from the cart?", "Clear Cart",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCart();
        }
    }

    private void placeOrder() {
        OrderManager.Customer c =
                (OrderManager.Customer) customerBox.getSelectedItem();

        if (c == null) {
            warn("Please select a customer.");
            return;
        }
        if (cart.isEmpty()) {
            warn("Please add at least one book.");
            return;
        }

        double total = cart.stream().mapToDouble(OrderManager.CartItem::getTotal).sum();
        int ok = JOptionPane.showConfirmDialog(this,
                String.format("Customer: %s%nItems: %d%nTotal: ₹%.2f%n%nPlace order?",
                        c.name, cart.size(), total),
                "Confirm Order", JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        int orderId = OrderManager.createOrder(c.id, cart);
        if (orderId == -1) {
            error("Order could not be created. Check stock and database connection.");
            loadBooks();
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Order #" + orderId + " placed successfully!",
                "Order Successful", JOptionPane.INFORMATION_MESSAGE);

        cart.clear();
        refreshCart();
        loadBooks();
    }

    private void showPreviousOrders() {
        JTextArea area = outputArea();
        area.setText(OrderManager.getOrderDetails());
        area.setCaretPosition(0);

        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(780, 500));

        JOptionPane.showMessageDialog(this, pane,
                "Previous Orders", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // DATABASE DISPLAY
    // =========================================================

    private void showAuthors(JTextArea area) {
        String sql = "SELECT author_id, author_name, country FROM AUTHOR ORDER BY author_id";
        StringBuilder out = new StringBuilder("AUTHOR LIST\n\n");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                out.append(String.format("%-6d %-28s %-18s%n",
                        r.getInt("author_id"),
                        r.getString("author_name"),
                        r.getString("country")));
            }
            area.setText(out.toString());
        } catch (Exception e) {
            error("Error loading authors:\n" + e.getMessage());
        }
    }

    private void showBooks(JTextArea area) {
        String sql = """
                SELECT b.book_id, b.title, a.author_name,
                       b.category, b.price, b.stock
                FROM BOOK b
                JOIN AUTHOR a ON b.author_id = a.author_id
                ORDER BY b.book_id
                """;
        StringBuilder out = new StringBuilder("BOOK INVENTORY\n\n");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                out.append(String.format(
                        "ID: %-5d  %-28s  Author: %-20s%n",
                        r.getInt("book_id"),
                        r.getString("title"),
                        r.getString("author_name")));
                out.append(String.format(
                        "Category: %-15s  Price: ₹%-8.2f  Stock: %d%n%n",
                        r.getString("category"),
                        r.getDouble("price"),
                        r.getInt("stock")));
            }
            area.setText(out.toString());
        } catch (Exception e) {
            error("Error loading books:\n" + e.getMessage());
        }
    }

    private void showCustomers(JTextArea area) {
        String sql = """
                SELECT customer_id, customer_name, email, phone
                FROM CUSTOMER ORDER BY customer_id
                """;
        StringBuilder out = new StringBuilder("CUSTOMER LIST\n\n");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                out.append(String.format(
                        "ID: %-5d  Name: %-25s%nEmail: %-28s Phone: %s%n%n",
                        r.getInt("customer_id"),
                        r.getString("customer_name"),
                        r.getString("email"),
                        r.getString("phone")));
            }
            area.setText(out.toString());
        } catch (Exception e) {
            error("Error loading customers:\n" + e.getMessage());
        }
    }


    // =========================================================
    // TABLE VIEWS
    // =========================================================

    private void showAuthorsTable() {
        String sql = "SELECT author_id, author_name, country FROM AUTHOR ORDER BY author_id";
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Author Name", "Country"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getInt("author_id"),
                        r.getString("author_name"),
                        r.getString("country")
                });
            }

            showDataTable("✍ Authors", model, 600, 380);

        } catch (Exception e) {
            error("Unable to load authors.\n\n" + e.getMessage());
        }
    }

    private void showBooksTable() {
        String sql = """
                SELECT b.book_id, b.title, a.author_name,
                       b.category, b.price, b.stock
                FROM BOOK b
                JOIN AUTHOR a ON b.author_id = a.author_id
                ORDER BY b.book_id
                """;

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Category", "Price", "Stock"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getInt("book_id"),
                        r.getString("title"),
                        r.getString("author_name"),
                        r.getString("category"),
                        String.format("₹%.2f", r.getDouble("price")),
                        r.getInt("stock")
                });
            }

            showDataTable("📚 Book Inventory", model, 900, 430);

        } catch (Exception e) {
            error("Unable to load books.\n\n" + e.getMessage());
        }
    }

    private void showDataTable(String title, DefaultTableModel model,
                               int width, int height) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(font(13, false));
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT);

        table.getTableHeader().setFont(font(13, true));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel heading = label(title, 20, PRIMARY, true);
        wrapper.add(heading, BorderLayout.NORTH);
        wrapper.add(pane, BorderLayout.CENTER);

        JOptionPane option = new JOptionPane(
                wrapper,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{"Close"},
                "Close"
        );

        JDialog dialog = option.createDialog(this, title);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =========================================================
    // DELETE OPERATIONS
    // =========================================================

    private void deleteAuthor(JTextArea area) {
        Integer id = askId("Delete Author", "Enter Author ID:");
        if (id == null) return;
        if (!confirm("Delete Author ID " + id + "?")) return;

        String result = AuthorManager.deleteAuthor(id);
        messageResult(result);
        showAuthors(area);
    }

    private void deleteBook(JTextArea area) {
        Integer id = askId("Delete Book", "Enter Book ID:");
        if (id == null) return;
        if (!confirm("Delete Book ID " + id + "?")) return;

        String result = BookManager.deleteBook(id);
        messageResult(result);
        showBooks(area);
        loadBooks();
    }

    private void deleteCustomer(JTextArea area) {
        Integer id = askId("Delete Customer", "Enter Customer ID:");
        if (id == null) return;
        if (!confirm("Delete Customer ID " + id + "?")) return;

        String result = CustomerManager.deleteCustomer(id);
        messageResult(result);
        showCustomers(area);
        loadCustomers();
    }

    private Integer askId(String title, String prompt) {
        String s = JOptionPane.showInputDialog(this, prompt, title,
                JOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            warn("Please enter a valid numeric ID.");
            return null;
        }
    }

    private boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(this, text,
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void messageResult(String result) {
        boolean success = result != null &&
                result.toLowerCase().contains("successfully");
        JOptionPane.showMessageDialog(this, result,
                success ? "Success" : "Cannot Delete",
                success ? JOptionPane.INFORMATION_MESSAGE :
                        JOptionPane.WARNING_MESSAGE);
    }

    // =========================================================
    // SMALL UI HELPERS
    // =========================================================

    private JPanel sectionBanner(String title, String subtitle, Color color) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setBackground(color);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 20, Color.WHITE, true));
        text.add(Box.createVerticalStrut(2));
        text.add(label(subtitle, 12, new Color(238, 242, 255), false));

        JLabel dot = label("●", 24, Color.WHITE, true);
        p.add(text, BorderLayout.WEST);
        p.add(dot, BorderLayout.EAST);
        return p;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        g.weightx = 1;
        return g;
    }

    private void addFormField(JPanel panel, GridBagConstraints g,
                              int row, String text, JComponent component) {
        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 0.25;
        panel.add(label(text, 13, TEXT, true), g);

        g.gridx = 1;
        g.gridwidth = 1;
        g.weightx = 0.75;
        panel.add(component, g);
    }

    private JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new GridLayout(1, buttons.length, 8, 0));
        p.setOpaque(false);
        for (JButton b : buttons) {
            p.add(b);
        }
        return p;
    }

    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(15, 15, 5, 15));
        return p;
    }

    private JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(new Color(255, 255, 255));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 14, 14, 14)));
        return p;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(font(13, false));
        f.setPreferredSize(new Dimension(260, 34));
        f.setMinimumSize(new Dimension(120, 34));
        return f;
    }

    private JTextArea outputArea() {
        JTextArea a = new JTextArea();
        a.setFont(new Font("Monospaced", Font.PLAIN, 13));
        a.setEditable(false);
        a.setLineWrap(false);
        a.setBackground(Color.WHITE);
        a.setForeground(TEXT);
        a.setText("Click a View button to display records here.");
        a.setBorder(new EmptyBorder(12, 12, 12, 12));
        return a;
    }

    private JScrollPane scroll(JTextArea a) {
        JScrollPane s = new JScrollPane(a);
        s.setBorder(BorderFactory.createLineBorder(BORDER));
        return s;
    }

    private JButton primary(String text) {
        return button(text, PRIMARY, Color.WHITE);
    }

    private JButton success(String text) {
        return button(text, ACCENT, Color.WHITE);
    }

    private JButton danger(String text) {
        return button(text, DANGER, Color.WHITE);
    }

    private JButton secondary(String text) {
        return button(text, Color.WHITE, TEXT);
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(font(12, true));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        bg == Color.WHITE ? BORDER : bg),
                new EmptyBorder(8, 12, 8, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel label(String text, int size, Color color, boolean bold) {
        return new JLabel(text) {{
            setFont(font(size, bold));
            setForeground(color);
        }};
    }

    private Font font(int size, boolean bold) {
        return new Font("SansSerif",
                bold ? Font.BOLD : Font.PLAIN, size);
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        return g;
    }

    private void addRow(JPanel p, GridBagConstraints g,
                        int row, String text, Component component) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        g.weightx = 0;
        p.add(label(text, 13, TEXT, true), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        p.add(component, g);
    }

    private void info(String text) {
        JOptionPane.showMessageDialog(this, text,
                "BookHub", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String text) {
        JOptionPane.showMessageDialog(this, text,
                "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void error(String text) {
        JOptionPane.showMessageDialog(this, text,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoreGUI().setVisible(true));
    }
}
