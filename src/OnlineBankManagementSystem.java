import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OnlineBankManagementSystem extends JFrame {

    private String currentUserName = "";
    private String fullName = "";
    private String currentPassword = "";
    private double balance = 0.0;
    private String[] transactionHistory = new String[100]; // Fixed size array for transactions
	private int transactionCount = 0;
	private Color primaryColor = new Color(30, 144, 255);     
	private Color secondaryColor = new Color(245, 250, 255);  
	private Color panelBorderColor = new Color(220, 220, 220); 

    private Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font buttonFont = new Font("Segoe UI", Font.PLAIN, 14);
    
    // Database connection variables
    private Connection connection;
    private final String DB_URL = "jdbc:mysql://localhost:3306/online_bank";
    private final String DB_USER = "root";
    private final String DB_PASS = "W7301@jqir#";

    public OnlineBankManagementSystem() {
        initializeDatabaseConnection();
        initializeWelcomeScreen();
    }

    private void initializeDatabaseConnection() {
        try {
           Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            createTablesIfNotExist();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(100) NOT NULL, " +
            "full_name VARCHAR(100) NOT NULL, " +
            "balance DOUBLE NOT NULL DEFAULT 0.0, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "user_id INT NOT NULL, " +
            "amount DOUBLE NOT NULL, " +
            "type VARCHAR(20) NOT NULL, " +
            "description VARCHAR(255), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id))";
            
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTransactionsTable);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(buttonFont);
        button.setBackground(primaryColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 70, 140)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(0, 80, 160));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });
        
        return button;
    }

    private JPanel createBorderedPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(secondaryColor);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(panelBorderColor),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            buttonFont,
            primaryColor
        ));
        return panel;
    }

    private void initializeWelcomeScreen() {
        setTitle("Welcome to SecureBank");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(secondaryColor);
        JLabel welcomeLabel = new JLabel("SECURE BANK");
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(primaryColor);
        headerPanel.add(welcomeLabel);

        // Button Panel
        JPanel buttonPanel = createBorderedPanel("Account Options");
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton createAccountButton = createStyledButton("Create Account");
        createAccountButton.addActionListener(e -> openCreateAccountScreen());
        
        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> openLoginScreen());

        buttonPanel.add(createAccountButton);
        buttonPanel.add(loginButton);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(secondaryColor);
        JLabel footerLabel = new JLabel("© 2025 SecureBank - All Rights Reserved");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerPanel.add(footerLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void openCreateAccountScreen() {
        JFrame accountFrame = new JFrame("Create Account");
        accountFrame.setSize(450, 350);
        accountFrame.setLocationRelativeTo(this);
        accountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        JPanel formPanel = createBorderedPanel("Account Information");
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JLabel nameLabel = new JLabel("Full Name:");
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel confirmLabel = new JLabel("Confirm Password:");

        formPanel.add(nameLabel); formPanel.add(nameField);
        formPanel.add(usernameLabel); formPanel.add(usernameField);
        formPanel.add(passwordLabel); formPanel.add(passwordField);
        formPanel.add(confirmLabel); formPanel.add(confirmPasswordField);

        JButton submitButton = createStyledButton("Create Account");
        submitButton.addActionListener(e -> {
            fullName = nameField.getText().trim();
            currentUserName = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            String confirm = new String(confirmPasswordField.getPassword()).trim();

            if (fullName.isEmpty() || currentUserName.isEmpty() || pass.isEmpty()) {
                showMessage("All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!pass.equals(confirm)) {
                showMessage("Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (pass.length() < 6) {
                showMessage("Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    String checkUser = "SELECT username FROM users WHERE username = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(checkUser)) {
                        pstmt.setString(1, currentUserName);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            showMessage("Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    String insertUser = "INSERT INTO users (username, password, full_name, balance) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, currentUserName);
                        pstmt.setString(2, pass);
                        pstmt.setString(3, fullName);
                        pstmt.setDouble(4, 0.0);
                        pstmt.executeUpdate();

                        String insertTransaction = "INSERT INTO transactions (user_id, amount, type, description) " +
                            "VALUES ((SELECT id FROM users WHERE username = ?), 0, 'ACCOUNT', 'Account created')";
                        try (PreparedStatement transStmt = connection.prepareStatement(insertTransaction)) {
                            transStmt.setString(1, currentUserName);
                            transStmt.executeUpdate();
                        }

                        currentPassword = pass;
                        balance = 0.0;
                        addTransaction(getCurrentDateTime() + " - Account created");
                        showMessage("Account created successfully for " + fullName, "Success", JOptionPane.INFORMATION_MESSAGE);
                        accountFrame.dispose();
                    }
                } catch (SQLException ex) {
                    showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton backButton = createStyledButton("Back");
        backButton.setBackground(new Color(102, 102, 102));
        backButton.addActionListener(e -> accountFrame.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(secondaryColor);
        buttonPanel.add(backButton);
        buttonPanel.add(submitButton);

        mainPanel.add(new JLabel("Create New Account", JLabel.CENTER), BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        accountFrame.add(mainPanel);
        accountFrame.setVisible(true);
    }

    private void openLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 250);
        loginFrame.setLocationRelativeTo(this);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        JPanel formPanel = createBorderedPanel("Login Credentials");
        formPanel.setLayout(new GridLayout(3, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showMessage("Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                        pstmt.setString(1, user);
                        pstmt.setString(2, pass);
                        ResultSet rs = pstmt.executeQuery();
                        
                        if (rs.next()) {
                            currentUserName = user;
                            currentPassword = pass;
                            fullName = rs.getString("full_name");
                            balance = rs.getDouble("balance");
                            
                            loadUserTransactions();
                            
                            showMessage("Login successful! Welcome " + fullName, "Success", JOptionPane.INFORMATION_MESSAGE);
                            loginFrame.dispose();
                            openDashboard();
                        } else {
                            showMessage("Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (SQLException ex) {
                    showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton backButton = createStyledButton("Back");
        backButton.setBackground(new Color(102, 102, 102));
        backButton.addActionListener(e -> loginFrame.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(secondaryColor);
        buttonPanel.add(backButton);
        buttonPanel.add(loginButton);

        mainPanel.add(new JLabel("Login to Your Account", JLabel.CENTER), BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginFrame.add(mainPanel);
        loginFrame.setVisible(true);
    }

    private void loadUserTransactions() {
        transactionCount = 0;
        try {
            String sql = "SELECT t.created_at, t.amount, t.type, t.description " +
                         "FROM transactions t JOIN users u ON t.user_id = u.id " +
                         "WHERE u.username = ? ORDER BY t.created_at DESC LIMIT 100";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, currentUserName);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next() && transactionCount < transactionHistory.length) {
                    String dateTime = rs.getTimestamp("created_at").toLocalDateTime()
                                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("type");
                    String desc = rs.getString("description");
                    
                    String transaction = dateTime + " - " + type + ": $" + 
                                        String.format("%.2f", amount) + 
                                        (desc != null ? " (" + desc + ")" : "");
                    transactionHistory[transactionCount++] = transaction;
                }
            }
        } catch (SQLException ex) {
            showMessage("Error loading transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTransaction(String transaction) {
        if (transactionCount < transactionHistory.length) {
            transactionHistory[transactionCount++] = transaction;
        } else {
            for (int i = 0; i < transactionHistory.length - 1; i++) {
                transactionHistory[i] = transactionHistory[i + 1];
            }
            transactionHistory[transactionHistory.length - 1] = transaction;
        }
    }

    private void openDashboard() {
        JFrame dashboard = new JFrame("Dashboard");
        dashboard.setSize(600, 500);
        dashboard.setLocationRelativeTo(this);
        dashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(secondaryColor);
        JLabel welcomeLabel = new JLabel("Welcome, " + fullName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(primaryColor);
        headerPanel.add(welcomeLabel);

        JPanel balancePanel = createBorderedPanel("Account Balance");
         balancePanel.setLayout(new BorderLayout());
        JLabel balanceLabel = new JLabel("Current Balance: $" + String.format("%.2f", balance));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balancePanel.add(balanceLabel);

        JPanel buttonPanel = createBorderedPanel("Banking Options");
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JButton depositButton = createStyledButton("Deposit");
        depositButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(dashboard, "Enter amount to deposit:", "Deposit", JOptionPane.PLAIN_MESSAGE);
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        showMessage("Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try {
                            String updateBalance = "UPDATE users SET balance = balance + ? WHERE username = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(updateBalance)) {
                                pstmt.setDouble(1, amount);
                                pstmt.setString(2, currentUserName);
                                pstmt.executeUpdate();
                            }
                            
                            String insertTransaction = "INSERT INTO transactions (user_id, amount, type, description) " +
                                "VALUES ((SELECT id FROM users WHERE username = ?), ?, 'DEPOSIT', 'Cash deposit')";
                            try (PreparedStatement transStmt = connection.prepareStatement(insertTransaction)) {
                                transStmt.setString(1, currentUserName);
                                transStmt.setDouble(2, amount);
                                transStmt.executeUpdate();
                            }
                            
                            balance += amount;
                            String transaction = getCurrentDateTime() + " - Deposited: $" + String.format("%.2f", amount);
                            addTransaction(transaction);
                            balanceLabel.setText("Current Balance: $" + String.format("%.2f", balance));
                            showMessage("Deposit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (SQLException ex) {
                            showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid amount entered", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton withdrawButton = createStyledButton("Withdraw");
        withdrawButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(dashboard, "Enter amount to withdraw:", "Withdraw", JOptionPane.PLAIN_MESSAGE);
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        showMessage("Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (amount > balance) {
                        showMessage("Insufficient funds", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try {
                            String updateBalance = "UPDATE users SET balance = balance - ? WHERE username = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(updateBalance)) {
                                pstmt.setDouble(1, amount);
                                pstmt.setString(2, currentUserName);
                                pstmt.executeUpdate();
                            }
                            
                            String insertTransaction = "INSERT INTO transactions (user_id, amount, type, description) " +
                                "VALUES ((SELECT id FROM users WHERE username = ?), ?, 'WITHDRAWAL', 'Cash withdrawal')";
                            try (PreparedStatement transStmt = connection.prepareStatement(insertTransaction)) {
                                transStmt.setString(1, currentUserName);
                                transStmt.setDouble(2, amount);
                                transStmt.executeUpdate();
                            }
                            
                            balance -= amount;
                            String transaction = getCurrentDateTime() + " - Withdrew: $" + String.format("%.2f", amount);
                            addTransaction(transaction);
                            balanceLabel.setText("Current Balance: $" + String.format("%.2f", balance));
                            showMessage("Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (SQLException ex) {
                            showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid amount entered", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton historyButton = createStyledButton("View Transaction History");
        historyButton.addActionListener(e -> showTransactionHistory());

        JButton detailsButton = createStyledButton("View Account Details");
        detailsButton.addActionListener(e -> showAccountDetails());

        JButton changePassButton = createStyledButton("Change Password");
        changePassButton.addActionListener(e -> openChangePasswordScreen());

        JButton logoutButton = createStyledButton("Logout");
        logoutButton.setBackground(new Color(204, 0, 0));
        logoutButton.addActionListener(e -> {
            dashboard.dispose();
            setVisible(true);
        });

        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(changePassButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(balancePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dashboard.add(mainPanel);
        dashboard.setVisible(true);
        this.setVisible(false);
    }

    private void openChangePasswordScreen() {
        JFrame passFrame = new JFrame("Change Password");
        passFrame.setSize(400, 250);
        passFrame.setLocationRelativeTo(this);
        passFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        JPanel formPanel = createBorderedPanel("Password Change");
        formPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        formPanel.add(new JLabel("Current Password:"));
        formPanel.add(oldPass);
        formPanel.add(new JLabel("New Password:"));
        formPanel.add(newPass);
        formPanel.add(new JLabel("Confirm New Password:"));
        formPanel.add(confirmPass);

        JButton updateBtn = createStyledButton("Update Password");
        updateBtn.addActionListener(e -> {
            String old = new String(oldPass.getPassword()).trim();
            String newP = new String(newPass.getPassword()).trim();
            String confirm = new String(confirmPass.getPassword()).trim();

            if (old.isEmpty() || newP.isEmpty() || confirm.isEmpty()) {
                showMessage("All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!old.equals(currentPassword)) {
                showMessage("Current password is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!newP.equals(confirm)) {
                showMessage("New passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (newP.length() < 6) {
                showMessage("Password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    String updatePass = "UPDATE users SET password = ? WHERE username = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(updatePass)) {
                        pstmt.setString(1, newP);
                        pstmt.setString(2, currentUserName);
                        pstmt.executeUpdate();
                        
                        String insertTransaction = "INSERT INTO transactions (user_id, amount, type, description) " +
                            "VALUES ((SELECT id FROM users WHERE username = ?), 0, 'ACCOUNT', 'Password changed')";
                        try (PreparedStatement transStmt = connection.prepareStatement(insertTransaction)) {
                            transStmt.setString(1, currentUserName);
                            transStmt.executeUpdate();
                        }
                        
                        currentPassword = newP;
                        addTransaction(getCurrentDateTime() + " - Password changed");
                        showMessage("Password updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        passFrame.dispose();
                    }
                } catch (SQLException ex) {
                    showMessage("Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton cancelBtn = createStyledButton("Cancel");
        cancelBtn.setBackground(new Color(102, 102, 102));
        cancelBtn.addActionListener(e -> passFrame.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(secondaryColor);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(updateBtn);

        mainPanel.add(new JLabel("Change Password", JLabel.CENTER), BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        passFrame.add(mainPanel);
        passFrame.setVisible(true);
    }

    private void showTransactionHistory() {
        JFrame historyFrame = new JFrame("Transaction History");
        historyFrame.setSize(600, 400);
        historyFrame.setLocationRelativeTo(this);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(secondaryColor);

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBackground(Color.WHITE);

        if (transactionCount == 0) {
            historyArea.setText("No transactions yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < transactionCount; i++) {
                sb.append(transactionHistory[i]).append("\n");
            }
            historyArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(historyArea);
        JPanel scrollPanel = createBorderedPanel("Transaction History");
        scrollPanel.setLayout(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(scrollPanel, BorderLayout.CENTER);

        JButton closeButton = createStyledButton("Close");
        closeButton.addActionListener(e -> historyFrame.dispose());
        mainPanel.add(closeButton, BorderLayout.SOUTH);

        historyFrame.add(mainPanel);
        historyFrame.setVisible(true);
    }

    private void showAccountDetails() {
        JFrame detailsFrame = new JFrame("Account Details");
        detailsFrame.setSize(400, 300);
        detailsFrame.setLocationRelativeTo(this);
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(secondaryColor);

        JPanel detailsPanel = createBorderedPanel("Account Information");
        detailsPanel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel nameLabel = new JLabel("Full Name: " + fullName);
        JLabel userLabel = new JLabel("Username: " + currentUserName);
        JLabel balanceLabel = new JLabel("Current Balance: $" + String.format("%.2f", balance));
        
        String createdDate = "Unknown";
        try {
            String sql = "SELECT created_at FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, currentUserName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    createdDate = rs.getTimestamp("created_at").toLocalDateTime()
                                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        JLabel sinceLabel = new JLabel("Member Since: " + createdDate);

        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sinceLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        detailsPanel.add(nameLabel);
        detailsPanel.add(userLabel);
        detailsPanel.add(balanceLabel);
        detailsPanel.add(sinceLabel);

        JButton closeButton = createStyledButton("Close");
        closeButton.addActionListener(e -> detailsFrame.dispose());

        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(closeButton, BorderLayout.SOUTH);

        detailsFrame.add(mainPanel);
        detailsFrame.setVisible(true);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new OnlineBankManagementSystem();
        });
    }
}