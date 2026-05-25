import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class AddProjectScreen extends JFrame {

    private final Color THEME_COLOR = new Color(70, 130, 180); 
    private final Color BACK_BTN_COLOR = new Color(220, 20, 60); 
    private final Color PROCEED_BTN_COLOR = new Color(40, 167, 69); 
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 18); 
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32); 

    private JTextField txtProjectName, txtStartDate, txtDeadline;
    
    private User currentUser;

    public AddProjectScreen(User user) {
        this.currentUser = user; 

        setTitle("University Workshop - Add Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        buildUI();
    }

    private void buildUI() {
        JLabel lblTitle = new JLabel("Add Project", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(THEME_COLOR);
        lblTitle.setBorder(new EmptyBorder(40, 0, 30, 0)); 
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createLabel("Project Name:"), gbc);
        gbc.gridx = 1; txtProjectName = createTextField(); formPanel.add(txtProjectName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(createLabel("Start Date (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; txtStartDate = createTextField(); formPanel.add(txtStartDate, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(createLabel("Deadline (DD/MM/YYYY):"), gbc);
        gbc.gridx = 1; txtDeadline = createTextField(); formPanel.add(txtDeadline, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 40)); 
        buttonPanel.setBackground(Color.WHITE);
        JButton btnBack = new JButton("Back"); styleButton(btnBack, BACK_BTN_COLOR);
        JButton btnProceed = new JButton("Proceed"); styleButton(btnProceed, PROCEED_BTN_COLOR);

        btnBack.addActionListener(e -> { 
            dispose(); 
            new MenuScreen(currentUser).setVisible(true); 
        });

        btnProceed.addActionListener(e -> handleProceed());

        buttonPanel.add(btnBack);
        buttonPanel.add(btnProceed);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleProceed() {
        String name = txtProjectName.getText().trim();
        String startStr = txtStartDate.getText().trim();
        String deadStr = txtDeadline.getText().trim();

        if (startStr.isEmpty() || deadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Start Date and Deadline!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Create Application obj
        Application newApp = new Application();
        if (name.isEmpty()) name = "Default Project " + System.currentTimeMillis();
        
        newApp.setProjName(name);
        newApp.setAppId("APP-" + (100 + new Random().nextInt(900)));
        newApp.setStartDate(parseDate(startStr));
        newApp.setEndDate(parseDate(deadStr));
        newApp.setConsumer(currentUser.getUni_id()); 

        // 2. if navigation then no DB saving
        dispose(); 
        new ProjectSpecs(currentUser, newApp).setVisible(true); 
    }

    private Date parseDate(String dateStr) {
        try {
            dateStr = dateStr.replace("-", "/");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date utilDate = sdf.parse(dateStr);
            return new java.sql.Date(utilDate.getTime());
        } catch (Exception e) { return null; }
    }

    private JLabel createLabel(String text) { JLabel l = new JLabel(text); l.setFont(MAIN_FONT); return l; }
    private JTextField createTextField() { JTextField t = new JTextField(15); t.setFont(MAIN_FONT); return t; }
    private void styleButton(JButton btn, Color bg) { btn.setFont(MAIN_FONT); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setPreferredSize(new Dimension(140, 50)); }
}