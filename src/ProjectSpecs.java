import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class ProjectSpecs extends JFrame {

    private User currentUser; 
    private Application currentApp; 
    
    private JPanel listPanel;
    private JButton submitButton;

    private Map<String, Service> availableServicesMap = new LinkedHashMap<>();
    private Map<String, JCheckBox> checkBoxes = new HashMap<>();
    private Map<String, String> userRequirements = new HashMap<>();

    private final Color THEME_COLOR = new Color(70, 130, 180);
    private final Color GREEN_COLOR = new Color(60, 179, 113);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    public ProjectSpecs(User user, Application app) {
        this.currentUser = user; 
        this.currentApp = app;

        setTitle("Select Services ");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        loadServiceData(); 
        
        buildHeader();
        buildServiceList();
        buildFooter();

        setVisible(true);
    }

    private void loadServiceData() {
        String sql = "SELECT ServID, ServName, ServCost FROM Service";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Service service = new Service(
                    rs.getString("ServID"), 
                    rs.getString("ServName"), 
                    rs.getDouble("ServCost")
                );
                
                availableServicesMap.put(service.getServiceName(), service);
                userRequirements.put(service.getServiceName(), ""); 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading services: " + e.getMessage());
        }
    }

    private void buildHeader() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topPanel.setBackground(THEME_COLOR);
        
        JLabel lblTitle = new JLabel("Project: " + currentApp.getProjName() + " (" + currentApp.getAppId() + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        topPanel.add(lblTitle);
        add(topPanel, BorderLayout.NORTH);
    }

    private void buildServiceList() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        for (Service s : availableServicesMap.values()) {
            addServiceRow(s);
        }
        
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void addServiceRow(Service s) {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(10, 10, 10, 10));

        JCheckBox chk = new JCheckBox(s.getServiceName());
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chk.setBackground(Color.WHITE);
        
        chk.addActionListener(e -> {
            if (chk.isSelected()) showRequirementsDialog(s.getServiceName());
            else userRequirements.put(s.getServiceName(), "");
            updateSubmitButton();
        });
        checkBoxes.put(s.getServiceName(), chk);

        row.add(chk);
        row.add(new JLabel("Service provided by University Workshop"));
        row.add(new JLabel("Base Cost: Rs. " + s.getServiceCost()));
        listPanel.add(row);
    }

    private void showRequirementsDialog(String serviceName) {
        String input = JOptionPane.showInputDialog(this, 
                "Enter Requirements for " + serviceName, 
                userRequirements.get(serviceName));
        if (input != null) userRequirements.put(serviceName, input);
    }

    private void updateSubmitButton() {
        boolean any = checkBoxes.values().stream().anyMatch(AbstractButton::isSelected);
        submitButton.setEnabled(any);
        submitButton.setBackground(any ? GREEN_COLOR : Color.GRAY);
    }

    private void buildFooter() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        
        JButton back = new JButton("Back");
        back.addActionListener(e -> { 
            dispose(); 
            new AddProjectScreen(currentUser).setVisible(true); 
        });
        
        submitButton = new JButton("Submit Services");
        submitButton.setEnabled(false);
        submitButton.addActionListener(e -> handleSubmit());

        bottom.add(back);
        bottom.add(submitButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private void handleSubmit() {
        // Clear list to avoid duplicates
        currentApp.getServices().clear(); 

        for (String name : checkBoxes.keySet()) {
            if (checkBoxes.get(name).isSelected()) {
                Service selectedService = availableServicesMap.get(name);
                
                // Link Project Name
                selectedService.setProjName(currentApp.getProjName()); 
                
                // Store Requirements
                String requirements = userRequirements.get(name);
                selectedService.setDescription(requirements);
                
                // Add to Application because of Composition
                currentApp.getServices().add(selectedService);
            }
        }

        dispose();
        new ServDescription(currentUser, currentApp).setVisible(true);
    }
}