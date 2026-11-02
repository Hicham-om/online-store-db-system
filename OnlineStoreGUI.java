import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

/**
 * Online Store Database System -- Java Swing GUI
 * Student: Hicham Ouikrim  |  ID: 312024704044
 *
 * Requires: JDK 11+  and  mysql-connector-j-8.x.x.jar on the classpath
 * Compile : javac -cp .;mysql-connector-j-8.x.x.jar OnlineStoreGUI.java
 * Run     : java  -cp .;mysql-connector-j-8.x.x.jar OnlineStoreGUI
 */
public class OnlineStoreGUI extends JFrame {

    // -- DB config -------------------------------------------------------------
    // Credentials are read from environment variables so no secret is committed.
    // Set DB_URL / DB_USER / DB_PASS in your environment, or edit the defaults
    // below for local use. Never hard-code a real password here.
    private static final String DB_URL  = env("DB_URL",  "jdbc:mysql://localhost:3306/online_store_044");
    private static final String DB_USER = env("DB_USER", "root");
    private static final String DB_PASS = env("DB_PASS", "");

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    // -- Colours ---------------------------------------------------------------
    private static final Color  C_DARK   = new Color(0x1F, 0x4E, 0x79);
    private static final Color  C_MID    = new Color(0x2E, 0x75, 0xB6);
    private static final Color  C_LIGHT  = new Color(0xD6, 0xE4, 0xF0);
    private static final Color  C_WHITE  = Color.WHITE;
    private static final Color  C_BG     = new Color(0xF5, 0xF7, 0xFA);
    private static final Color  C_RED    = new Color(0xC0, 0x39, 0x2B);
    private static final Color  C_GREEN  = new Color(0x1A, 0x7A, 0x4A);
    private static final Font   F_TITLE  = new Font("Arial", Font.BOLD,  22);
    private static final Font   F_LABEL  = new Font("Arial", Font.BOLD,  13);
    private static final Font   F_BODY   = new Font("Arial", Font.PLAIN, 13);
    private static final Font   F_MONO   = new Font("Monospaced", Font.PLAIN, 12);

    // -- State -----------------------------------------------------------------
    private Connection conn;
    private JTabbedPane tabs;
    private JLabel statusBar;

    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new OnlineStoreGUI().setVisible(true);
        });
    }

    // -------------------------------------------------------------------------
    public OnlineStoreGUI() {
        setTitle("Online Store Database System  |  Hicham Ouikrim  |  ID: 312024704044");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setBackground(C_BG);

        buildUI();
        connect();
    }

    // -- UI skeleton -----------------------------------------------------------
    private void buildUI() {
        setLayout(new BorderLayout());

        // Header banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("Online Store Database System");
        title.setFont(F_TITLE);
        title.setForeground(C_WHITE);
        JLabel sub = new JLabel("Student: Hicham Ouikrim  |  ID: 312024704044  |  Suffix: _044");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(0xBBDDFF));
        header.add(title, BorderLayout.WEST);
        header.add(sub,   BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Status bar built FIRST so tab listeners never see a null statusBar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusPanel.setBackground(C_DARK);
        statusBar = new JLabel("Connecting to database...");
        statusBar.setFont(new Font("Arial", Font.PLAIN, 12));
        statusBar.setForeground(C_WHITE);
        statusPanel.add(statusBar);
        add(statusPanel, BorderLayout.SOUTH);

        // Tabs (built after statusBar exists)
        tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(F_LABEL);
        tabs.setBackground(C_BG);
        tabs.addTab("[Products]  Products",   buildProductsTab());
        tabs.addTab("[Customers]  Customers", buildCustomersTab());
        tabs.addTab("[Orders]  Orders",       buildOrdersTab());
        tabs.addTab("[Payments]  Payments",   buildPaymentsTab());
        tabs.addTab("[Inventory]  Inventory", buildInventoryTab());
        tabs.addTab("[Reports]  Reports",     buildReportsTab());
        tabs.addTab("[SQL]  Custom SQL",      buildSQLTab());
        add(tabs, BorderLayout.CENTER);
    }

    // -- DB connection ---------------------------------------------------------
    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            setStatus("OK  Connected to online_store_044 @ localhost", C_GREEN);
        } catch (Exception e) {
            e.printStackTrace();   // prints full error to CMD window
            setStatus("ERROR  " + e.getMessage(), C_RED);
            JOptionPane.showMessageDialog(this,
                "Connection failed:\n\n" + e.getMessage() +
                "\n\nFull error printed in CMD window.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setStatus(String msg, Color colour) {
        statusBar.setText(msg);
        statusBar.setForeground(colour != null ? colour : C_WHITE);
    }

    // =========================================================================
    // TAB 1 -- PRODUCTS
    // =========================================================================
    private JPanel buildProductsTab() {
        JPanel root = panel(new BorderLayout(0, 0));

        // Form panel
        JPanel form = formPanel("Add / Edit Product");
        String[] labels = {"Product Name", "Category", "Purchase Price", "Selling Price",
                           "Member Discount (%)", "Status", "Supplier ID"};
        JTextField fName     = field(); JTextField fCat    = field();
        JTextField fBuy      = field(); JTextField fSell   = field();
        JTextField fDisc     = field("0"); JTextField fSupp = field("1");
        String[]   statuses  = {"Available", "Unavailable", "Discontinued"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        styleCombo(cbStatus);

        addFormRow(form, "Product Name",       fName);
        addFormRow(form, "Category",           fCat);
        addFormRow(form, "Purchase Price ($)", fBuy);
        addFormRow(form, "Selling Price ($)",  fSell);
        addFormRow(form, "Member Discount (%)",fDisc);
        addFormRow(form, "Status",             cbStatus);
        addFormRow(form, "Supplier ID",        fSupp);

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Buttons
        JButton btnLoad   = btn("Refresh  Load All",   C_MID);
        JButton btnAdd    = btn("Add  Add Product", C_GREEN);
        JButton btnDelete = btn("Delete  Delete",      C_RED);

        btnLoad.addActionListener(e -> loadTable(table,
            "SELECT product_id, product_name, category, purchase_price, selling_price, " +
            "member_discount, status, supplier_id FROM products_044 ORDER BY product_id"));

        btnAdd.addActionListener(e -> {
            String sql = "INSERT INTO products_044 " +
                "(product_name,category,purchase_price,selling_price,member_discount,status,supplier_id) " +
                "VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fName.getText().trim());
                ps.setString(2, fCat.getText().trim());
                ps.setDouble(3, Double.parseDouble(fBuy.getText().trim()));
                ps.setDouble(4, Double.parseDouble(fSell.getText().trim()));
                ps.setDouble(5, Double.parseDouble(fDisc.getText().trim()));
                ps.setString(6, (String) cbStatus.getSelectedItem());
                ps.setInt(7,    Integer.parseInt(fSupp.getText().trim()));
                ps.executeUpdate();
                setStatus("OK  Product added.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            Object val = table.getValueAt(row, 0);
            if (val == null) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            int id = Integer.parseInt(val.toString());
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete product ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM products_044 WHERE product_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                setStatus("OK  Product " + id + " deleted.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        JPanel btns = buttonRow(btnLoad, btnAdd, btnDelete);
        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(btns,   BorderLayout.SOUTH);

        // Auto-load on tab switch
        tabs.addChangeListener(ev -> { if (tabs.getSelectedComponent() == root) btnLoad.doClick(); });
        return root;
    }

    // =========================================================================
    // TAB 2 -- CUSTOMERS
    // =========================================================================
    private JPanel buildCustomersTab() {
        JPanel root = panel(new BorderLayout(0, 0));

        JPanel form = formPanel("Add Customer");
        JTextField fFirst = field(), fLast = field(), fEmail = field(),
                   fPhone = field(), fAddr  = field();
        JComboBox<String> cbMem = new JComboBox<>(
            new String[]{"Regular","Silver","Gold","Platinum"});
        styleCombo(cbMem);

        addFormRow(form, "First Name",       fFirst);
        addFormRow(form, "Last Name",        fLast);
        addFormRow(form, "Email",            fEmail);
        addFormRow(form, "Phone",            fPhone);
        addFormRow(form, "Address",          fAddr);
        addFormRow(form, "Membership Type",  cbMem);

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JButton btnLoad   = btn("Refresh  Load All",    C_MID);
        JButton btnAdd    = btn("Add  Add Customer", C_GREEN);
        JButton btnDelete = btn("Delete  Delete",       C_RED);

        btnLoad.addActionListener(e -> loadTable(table,
            "SELECT customer_id, first_name, last_name, email, phone, " +
            "membership_type, total_points FROM customers_044 ORDER BY customer_id"));

        btnAdd.addActionListener(e -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customers_044 " +
                    "(first_name,last_name,email,phone,address,membership_type) VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, fFirst.getText().trim());
                ps.setString(2, fLast.getText().trim());
                ps.setString(3, fEmail.getText().trim());
                ps.setString(4, fPhone.getText().trim());
                ps.setString(5, fAddr.getText().trim());
                ps.setString(6, (String) cbMem.getSelectedItem());
                ps.executeUpdate();
                setStatus("OK  Customer added.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            Object val = table.getValueAt(row, 0);
            if (val == null) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            int id = Integer.parseInt(val.toString());
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM customers_044 WHERE customer_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                setStatus("OK  Customer " + id + " deleted.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttonRow(btnLoad, btnAdd, btnDelete), BorderLayout.SOUTH);

        tabs.addChangeListener(ev -> { if (tabs.getSelectedComponent() == root) btnLoad.doClick(); });
        return root;
    }

    // =========================================================================
    // TAB 3 -- ORDERS
    // =========================================================================
    private JPanel buildOrdersTab() {
        JPanel root = panel(new BorderLayout(0, 0));

        JPanel form = formPanel("Place New Order / Update Status");
        JTextField fCustId = field(), fAddr = field(), fNotes = field();
        JComboBox<String> cbStatus = new JComboBox<>(
            new String[]{"Pending","Processing","Shipped","Completed","Cancelled"});
        JTextField fOrderId = field("(select row to update)");
        styleCombo(cbStatus);

        addFormRow(form, "Customer ID",       fCustId);
        addFormRow(form, "Shipping Address",  fAddr);
        addFormRow(form, "Notes",             fNotes);
        addFormRow(form, "---- Update Existing Order ----", new JLabel(""));
        addFormRow(form, "Order ID (to update)",  fOrderId);
        addFormRow(form, "New Status",            cbStatus);

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Clicking a row pre-fills the order ID field
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fOrderId.setText(String.valueOf(table.getValueAt(table.getSelectedRow(), 0)));
            }
        });

        JButton btnLoad   = btn("Refresh  Load All",       C_MID);
        JButton btnNew    = btn("Add  New Order",       C_GREEN);
        JButton btnUpdate = btn("Update  Update Status",  C_MID);

        btnLoad.addActionListener(e -> loadTable(table,
            "SELECT o.order_id, CONCAT(c.first_name,' ',c.last_name) AS customer, " +
            "o.order_date, o.status, o.total_amount, o.points_awarded " +
            "FROM orders_044 o JOIN customers_044 c ON o.customer_id=c.customer_id " +
            "ORDER BY o.order_id DESC"));

        btnNew.addActionListener(e -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO orders_044 (customer_id,shipping_address,notes) VALUES (?,?,?)")) {
                ps.setInt(1,    Integer.parseInt(fCustId.getText().trim()));
                ps.setString(2, fAddr.getText().trim());
                ps.setString(3, fNotes.getText().trim());
                ps.executeUpdate();
                setStatus("OK  Order created.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int oid = Integer.parseInt(fOrderId.getText().trim());
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders_044 SET status=? WHERE order_id=?")) {
                    ps.setString(1, (String) cbStatus.getSelectedItem());
                    ps.setInt(2, oid);
                    ps.executeUpdate();
                    setStatus("OK  Order " + oid + " -> " + cbStatus.getSelectedItem() +
                              "  (points trigger auto-fires if Completed)", C_GREEN);
                    btnLoad.doClick();
                }
            } catch (Exception ex) { showError(ex); }
        });

        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttonRow(btnLoad, btnNew, btnUpdate), BorderLayout.SOUTH);

        tabs.addChangeListener(ev -> { if (tabs.getSelectedComponent() == root) btnLoad.doClick(); });
        return root;
    }

    // =========================================================================
    // TAB 4 -- PAYMENTS
    // =========================================================================
    private JPanel buildPaymentsTab() {
        JPanel root = panel(new BorderLayout(0, 0));

        JPanel form = formPanel("Record Payment");
        JTextField fOrderId = field(), fAmount = field(), fRef = field();
        JComboBox<String> cbMethod = new JComboBox<>(
            new String[]{"Cash","Credit Card","Debit Card","Bank Transfer","Digital Wallet"});
        JComboBox<String> cbStatus = new JComboBox<>(
            new String[]{"Pending","Completed","Failed","Refunded"});
        styleCombo(cbMethod); styleCombo(cbStatus);

        addFormRow(form, "Order ID",        fOrderId);
        addFormRow(form, "Amount ($)",      fAmount);
        addFormRow(form, "Payment Method",  cbMethod);
        addFormRow(form, "Payment Status",  cbStatus);
        addFormRow(form, "Transaction Ref", fRef);

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JButton btnLoad = btn("Refresh  Load All",     C_MID);
        JButton btnAdd  = btn("Add  Add Payment",  C_GREEN);

        btnLoad.addActionListener(e -> loadTable(table,
            "SELECT p.payment_id, p.order_id, p.payment_date, p.amount, " +
            "p.payment_method, p.payment_status, p.transaction_ref " +
            "FROM payments_044 p ORDER BY p.payment_id DESC"));

        btnAdd.addActionListener(e -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO payments_044 " +
                    "(order_id,amount,payment_method,payment_status,transaction_ref) VALUES (?,?,?,?,?)")) {
                ps.setInt(1,    Integer.parseInt(fOrderId.getText().trim()));
                ps.setDouble(2, Double.parseDouble(fAmount.getText().trim()));
                ps.setString(3, (String) cbMethod.getSelectedItem());
                ps.setString(4, (String) cbStatus.getSelectedItem());
                ps.setString(5, fRef.getText().trim().isEmpty() ? null : fRef.getText().trim());
                ps.executeUpdate();
                setStatus("OK  Payment recorded.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttonRow(btnLoad, btnAdd), BorderLayout.SOUTH);

        tabs.addChangeListener(ev -> { if (tabs.getSelectedComponent() == root) btnLoad.doClick(); });
        return root;
    }

    // =========================================================================
    // TAB 5 -- INVENTORY
    // =========================================================================
    private JPanel buildInventoryTab() {
        JPanel root = panel(new BorderLayout(0, 0));

        JPanel form = formPanel("Update Stock Level");
        JTextField fProductId = field(), fQty = field(), fThresh = field();

        addFormRow(form, "Product ID",          fProductId);
        addFormRow(form, "New Quantity",         fQty);
        addFormRow(form, "Reorder Threshold",   fThresh);

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);

        // Clicking row pre-fills fields
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                fProductId.setText(String.valueOf(table.getValueAt(table.getSelectedRow(), 0)));
                fQty.setText(String.valueOf(table.getValueAt(table.getSelectedRow(), 2)));
                fThresh.setText(String.valueOf(table.getValueAt(table.getSelectedRow(), 3)));
            }
        });

        JButton btnLoad   = btn("Refresh  Load All",      C_MID);
        JButton btnUpdate = btn("Edit  Update Stock",  C_GREEN);
        JButton btnAlerts = btn("WARNING  Low Stock Alerts", new Color(0xE67E22));

        btnLoad.addActionListener(e -> loadTable(table,
            "SELECT i.product_id, p.product_name, i.quantity_in_stock, " +
            "i.reorder_threshold, i.last_updated " +
            "FROM inventory_044 i JOIN products_044 p ON i.product_id=p.product_id " +
            "ORDER BY i.quantity_in_stock ASC"));

        btnUpdate.addActionListener(e -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory_044 SET quantity_in_stock=?, reorder_threshold=? WHERE product_id=?")) {
                ps.setInt(1, Integer.parseInt(fQty.getText().trim()));
                ps.setInt(2, Integer.parseInt(fThresh.getText().trim()));
                ps.setInt(3, Integer.parseInt(fProductId.getText().trim()));
                int rows = ps.executeUpdate();
                setStatus(rows > 0 ? "OK  Inventory updated." : "WARNING  Product ID not found.", C_GREEN);
                btnLoad.doClick();
            } catch (Exception ex) { showError(ex); }
        });

        btnAlerts.addActionListener(e -> loadTable(table,
            "SELECT w.warning_id, p.product_name, w.remaining_stock, w.threshold, w.warned_at " +
            "FROM low_stock_warnings_044 w " +
            "JOIN products_044 p ON w.product_id=p.product_id " +
            "ORDER BY w.warned_at DESC"));

        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttonRow(btnLoad, btnUpdate, btnAlerts), BorderLayout.SOUTH);

        tabs.addChangeListener(ev -> { if (tabs.getSelectedComponent() == root) btnLoad.doClick(); });
        return root;
    }

    // =========================================================================
    // TAB 6 -- REPORTS  (Views + Multi-table queries)
    // =========================================================================
    private JPanel buildReportsTab() {
        JPanel root = panel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = styledTable();
        JScrollPane scroll = new JScrollPane(table);

        // Report buttons
        String[][] reports = {
            {"?  Cashier View",           "SELECT * FROM vw_cashier_products_044"},
            {"?  Management Sales",        "SELECT * FROM vw_management_sales_stats_044"},
            {"Discounts  Member Discounts",        "SELECT * FROM vw_member_discount_products_044"},
            {"Linked  Orders + Payments",
                "SELECT o.order_id, CONCAT(c.first_name,' ',c.last_name) AS customer, " +
                "c.membership_type, o.status, o.total_amount, p.payment_method, p.payment_status " +
                "FROM orders_044 o JOIN customers_044 c ON o.customer_id=c.customer_id " +
                "LEFT JOIN payments_044 p ON o.order_id=p.order_id ORDER BY o.order_id DESC"},
            {"Revenue  Revenue by Product",
                "SELECT pr.product_name, s.supplier_name, " +
                "SUM(oi.quantity) AS units_sold, SUM(oi.line_total) AS revenue " +
                "FROM order_items_044 oi JOIN products_044 pr ON oi.product_id=pr.product_id " +
                "JOIN suppliers_044 s ON pr.supplier_id=s.supplier_id " +
                "JOIN orders_044 o ON oi.order_id=o.order_id WHERE o.status='Completed' " +
                "GROUP BY pr.product_id, pr.product_name, s.supplier_name ORDER BY revenue DESC"},
            {"Update  Repeat Customers",
                "SELECT c.customer_id, CONCAT(c.first_name,' ',c.last_name) AS full_name, " +
                "c.membership_type, sub.num_orders, sub.total_spent " +
                "FROM customers_044 c JOIN " +
                "(SELECT customer_id, COUNT(*) AS num_orders, SUM(total_amount) AS total_spent " +
                "FROM orders_044 GROUP BY customer_id HAVING COUNT(*) > 1) AS sub " +
                "ON c.customer_id=sub.customer_id ORDER BY sub.total_spent DESC"},
            {"WARNING  Low Stock + Supplier",
                "SELECT pr.product_name, pr.category, i.quantity_in_stock, i.reorder_threshold, " +
                "s.supplier_name, s.contact_person, s.email " +
                "FROM inventory_044 i JOIN products_044 pr ON i.product_id=pr.product_id " +
                "LEFT JOIN suppliers_044 s ON pr.supplier_id=s.supplier_id " +
                "WHERE i.quantity_in_stock <= i.reorder_threshold ORDER BY i.quantity_in_stock"},
            {"Top  Top Customers by Points",
                "SELECT customer_id, CONCAT(first_name,' ',last_name) AS name, " +
                "membership_type, total_points FROM customers_044 " +
                "ORDER BY total_points DESC LIMIT 10"},
            {"Monthly  Monthly Sales Summary",
                "SELECT DATE_FORMAT(o.order_date,'%Y-%m') AS month, " +
                "COUNT(DISTINCT o.order_id) AS orders, " +
                "COUNT(DISTINCT o.customer_id) AS customers, " +
                "SUM(oi.line_total) AS revenue " +
                "FROM orders_044 o JOIN order_items_044 oi ON o.order_id=oi.order_id " +
                "WHERE o.status IN ('Completed','Shipped','Processing') " +
                "GROUP BY DATE_FORMAT(o.order_date,'%Y-%m') ORDER BY month DESC"},
            {"Points  Points Records",
                "SELECT pr.record_id, CONCAT(c.first_name,' ',c.last_name) AS customer, " +
                "pr.order_id, pr.points_earned, pr.awarded_at " +
                "FROM points_records_044 pr JOIN customers_044 c ON pr.customer_id=c.customer_id " +
                "ORDER BY pr.awarded_at DESC"},
        };

        JPanel btnGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        btnGrid.setBackground(C_BG);
        for (String[] r : reports) {
            JButton b = btn(r[0], C_MID);
            final String sql = r[1];
            b.addActionListener(e -> loadTable(table, sql));
            btnGrid.add(b);
        }

        root.add(btnGrid, BorderLayout.NORTH);
        root.add(scroll,  BorderLayout.CENTER);
        return root;
    }

    // =========================================================================
    // TAB 7 -- CUSTOM SQL
    // =========================================================================
    private JPanel buildSQLTab() {
        JPanel root = panel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea sqlArea = new JTextArea(6, 80);
        sqlArea.setFont(F_MONO);
        sqlArea.setBackground(new Color(0x1E, 0x1E, 0x1E));
        sqlArea.setForeground(new Color(0xD4, 0xD4, 0xD4));
        sqlArea.setCaretColor(Color.WHITE);
        sqlArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sqlArea.setText("-- Type any SQL query here\nSELECT * FROM products_044 LIMIT 10;");

        JScrollPane sqlScroll = new JScrollPane(sqlArea);
        sqlScroll.setBorder(BorderFactory.createLineBorder(C_MID, 2));

        JTable table = styledTable();
        JTextArea resultInfo = new JTextArea(2, 80);
        resultInfo.setFont(F_MONO);
        resultInfo.setEditable(false);
        resultInfo.setBackground(new Color(0x28, 0x28, 0x28));
        resultInfo.setForeground(new Color(0x6A, 0xFF, 0x6A));

        JButton btnRun   = btn("Run  Run Query",  C_GREEN);
        JButton btnClear = btn("Delete  Clear",      C_RED);

        btnRun.addActionListener(e -> {
            String sql = sqlArea.getText().trim();
            if (sql.isEmpty()) return;
            String sqlLower = sql.toLowerCase().trim();
            if (sqlLower.startsWith("select") || sqlLower.startsWith("show") || sqlLower.startsWith("describe") || sqlLower.startsWith("explain")) {
                loadTable(table, sql);
                resultInfo.setText("Query executed successfully.");
            } else {
                try (Statement st = conn.createStatement()) {
                    int rows = st.executeUpdate(sql);
                    resultInfo.setText("OK  " + rows + " row(s) affected.");
                    setStatus("OK  Statement executed: " + rows + " row(s) affected.", C_GREEN);
                } catch (Exception ex) {
                    resultInfo.setText("ERROR  " + ex.getMessage());
                    showError(ex);
                }
            }
        });

        btnClear.addActionListener(e -> { sqlArea.setText(""); resultInfo.setText(""); });

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(C_BG);
        top.add(sqlScroll,  BorderLayout.CENTER);
        top.add(buttonRow(btnRun, btnClear), BorderLayout.SOUTH);

        root.add(top,        BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(new JScrollPane(resultInfo), BorderLayout.SOUTH);
        return root;
    }

    // =========================================================================
    // UTILITIES
    // =========================================================================
    private void loadTable(JTable table, String sql) {
        if (conn == null) {
            setStatus("ERROR  Not connected to database.", C_RED);
            return;
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            String[] colNames = new String[cols];
            for (int i = 0; i < cols; i++) colNames[i] = meta.getColumnLabel(i + 1) + "_044";

            Vector<String[]> rows = new Vector<>();
            while (rs.next()) {
                String[] row = new String[cols];
                for (int i = 0; i < cols; i++) {
                    Object v = rs.getObject(i + 1);
                    row[i] = v == null ? "NULL" : v.toString();
                }
                rows.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(colNames, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (String[] row : rows) model.addRow(row);
            table.setModel(model);

            // Stripe rows
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                    setFont(F_BODY);
                    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    if (sel) {
                        setBackground(C_MID); setForeground(C_WHITE);
                    } else {
                        setBackground(r % 2 == 0 ? C_WHITE : C_LIGHT);
                        setForeground(Color.BLACK);
                    }
                    return this;
                }
            });

            setStatus("OK  " + rows.size() + " row(s) returned.", C_GREEN);
        } catch (Exception e) { showError(e); }
    }

    private void showError(Exception e) {
        setStatus("ERROR  " + e.getMessage(), C_RED);
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // -- Swing helpers ---------------------------------------------------------
    private JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(C_BG);
        return p;
    }

    private JPanel formPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, C_MID),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                F_LABEL, C_DARK)
        ));
        return p;
    }

    private final Map<JPanel, Integer> formRows = new HashMap<>();

    private void addFormRow(JPanel form, String labelText, JComponent field) {
        int row = formRows.getOrDefault(form, 0);
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = row; lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 8, 4, 12);
        JLabel label = new JLabel(labelText + ":");
        label.setFont(F_LABEL); label.setForeground(C_DARK);
        form.add(label, lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = row; fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1; fc.insets = new Insets(4, 0, 4, 8);
        if (field instanceof JTextField) {
            field.setFont(F_BODY);
            field.setPreferredSize(new Dimension(260, 28));
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        }
        form.add(field, fc);
        formRows.put(form, row + 1);
    }

    private JTextField field()           { return new JTextField(); }
    private JTextField field(String def) { return new JTextField(def); }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(F_BODY);
        cb.setBackground(C_WHITE);
        cb.setPreferredSize(new Dimension(260, 28));
    }

    private JTable styledTable() {
        JTable t = new JTable();
        t.setFont(F_BODY);
        t.setRowHeight(26);
        t.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        t.setSelectionBackground(C_MID);
        t.setSelectionForeground(C_WHITE);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.getTableHeader().setFont(F_LABEL);
        t.getTableHeader().setBackground(C_DARK);
        t.getTableHeader().setForeground(C_WHITE);
        t.getTableHeader().setOpaque(true);
        t.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setBackground(C_DARK);
                setForeground(C_WHITE);
                setFont(F_LABEL);
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,0,1,new Color(0x4A,0x7E,0xB5)),
                    BorderFactory.createEmptyBorder(6,8,6,8)));
                setOpaque(true);
                return this;
            }
        });
        return t;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(bg);
        b.setForeground(C_WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    private JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(C_BG);
        for (JButton b : buttons) p.add(b);
        return p;
    }
}