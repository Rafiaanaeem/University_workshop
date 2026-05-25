import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnOk;

    private Font mainTitleFont = new Font("Segoe UI", Font.BOLD, 36);
    private Font labelFont = new Font("Segoe UI", Font.BOLD, 20);
    private Font inputFont = new Font("Segoe UI", Font.PLAIN, 20);
    private Color themeColor = new Color(70, 130, 180);

    private int attempts = 0;
    private boolean isBlocked = false;

    public LoginScreen() {
        setTitle("Login Screen");
        setSize(500, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); 
        getContentPane().setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(50, 20, 30, 20));
        
        JLabel lblSystemTitle = new JLabel("<html><center>University Workshop<br>Software System</center></html>", SwingConstants.CENTER);
        lblSystemTitle.setFont(mainTitleFont);
        lblSystemTitle.setForeground(themeColor);
        titlePanel.add(lblSystemTitle);
        add(titlePanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 

        JLabel lblUser = new JLabel("User ID:");
        lblUser.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblUser, gbc);

        txtUser = new JTextField(15);
        txtUser.setFont(inputFont);
        txtUser.setPreferredSize(new Dimension(250, 40)); 
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtUser, gbc);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblPass, gbc);

        txtPass = new JPasswordField(15);
        txtPass.setFont(inputFont);
        txtPass.setPreferredSize(new Dimension(250, 40)); 
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtPass, gbc);

        btnOk = new JButton("Login");
        btnOk.setFont(labelFont);
        btnOk.setBackground(themeColor);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnOk.setPreferredSize(new Dimension(150, 45)); 
        btnOk.addActionListener(this);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(30, 0, 0, 0); 
        formPanel.add(btnOk, gbc);

        add(formPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isBlocked) {
            JOptionPane.showMessageDialog(this, "Login blocked! Please wait.");
            return;
        }

        
        // 1. Create a User object instance
        User userObj = new User();
        
        userObj.setUni_id(txtUser.getText());
        userObj.setPassword(new String(txtPass.getPassword()));

        try { 
            User authenticatedUser = userObj.fetchuser();

            //verify authentication
            if (authenticatedUser != null && authenticatedUser.checkuser()) {
                
                //getter for personalize the welcome message
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + authenticatedUser.getPF_name());
                
                dispose(); 
                
                //Pass the full User obj to the next screen for Consumer attribute
                new MenuScreen(authenticatedUser).setVisible(true); 
                
            } else {
                handleFailedAttempt();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during authentication: " + ex.getMessage());
        }
    }

    private void handleFailedAttempt() {
        attempts++;
        JOptionPane.showMessageDialog(this, "Invalid User ID or Password\nAttempt: " + attempts + "/3");
        if (attempts >= 3) { blockUser(); }
    }

    void blockUser() {
        isBlocked = true;
        btnOk.setEnabled(false);
        btnOk.setBackground(Color.GRAY);
        Timer timer = new Timer(60000, e -> {
            isBlocked = false;
            attempts = 0;
            btnOk.setEnabled(true);
            btnOk.setBackground(themeColor);
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) { new LoginScreen(); }
}