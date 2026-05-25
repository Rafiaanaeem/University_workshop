import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MenuScreen extends JFrame implements ActionListener {

    private JButton btnView, btnAdd, btnLogout;
    private JLabel lblTitle;
    
    // Store the prev. User obj
    private User currentUser; 

    private Font titleFont = new Font("Segoe UI", Font.BOLD, 48); 
    private Font uiFont = new Font("Segoe UI", Font.PLAIN, 20);   
    private Color themeColor = new Color(70, 130, 180);           
    private Color backBtnColor = new Color(220, 20, 60);          

    // Constructor accepts the User object
    public MenuScreen(User user) {
        this.currentUser = user; 
        initUI();
    }

    private void initUI() {
        setTitle("University Workshop - Main Menu");
        setSize(500, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(40, 50, 40, 50));

        // getters for user persnolization
        lblTitle = new JLabel("<html><center>Welcome,<br>" + currentUser.getPF_name() + "</center></html>", SwingConstants.CENTER);
        lblTitle.setFont(titleFont);
        lblTitle.setForeground(themeColor);

        btnView = new JButton("View Projects");
        styleButton(btnView, themeColor);
        btnView.addActionListener(this);

        btnAdd = new JButton("Add Project");
        styleButton(btnAdd, themeColor);
        btnAdd.addActionListener(this);

        btnLogout = new JButton("Logout");
        styleButton(btnLogout, backBtnColor); 
        btnLogout.addActionListener(this);

        setLayout(new GridLayout(4, 1, 20, 30)); 
        getContentPane().setBackground(Color.WHITE);

        add(lblTitle);
        add(btnView);
        add(btnAdd);
        add(btnLogout);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(uiFont);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); 
        btn.setOpaque(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnView) {
            dispose(); 
            //Passing current User obj to the next screen
            new ProjectListScreen(currentUser).setVisible(true);
        }

        if (e.getSource() == btnAdd) {
            dispose(); 
            // Passing current User obj to the next screen
            new AddProjectScreen(currentUser).setVisible(true);
        }

        if (e.getSource() == btnLogout) {
            dispose(); 
            new LoginScreen(); 
        }
    }
}