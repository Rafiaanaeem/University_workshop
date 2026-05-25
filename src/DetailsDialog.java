import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailsDialog extends JDialog {

    private final Color THEME_COLOR = new Color(70, 130, 180); 
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    private JPanel mainContentPanel;
    private GridBagConstraints gbc = new GridBagConstraints();
    
    private Application currentApp; 
    private List<Worker> assignedWorkers = new ArrayList<>();
    private User consumer; 
    
    private double grandTotalCost = 0;

    public DetailsDialog(JFrame parent, String projectName) {
        super(parent, "Project Details: " + projectName, true); 
        setSize(600, 750); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainContentPanel.setBackground(Color.WHITE);

        JScrollPane mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);

        //Fetch Data of Project
        loadProjectObjects(projectName);

        addMainTitle("Details");
        
        if (currentApp != null) {
            buildTopInfoSection();
            addSectionSeparator();
            buildServicesTable();
            addSectionSeparator();
            buildHRTable();
            addSectionSeparator();
            buildMaterialsTable();
            addSectionSeparator();
            buildTotalFooter();
        } else {
            mainContentPanel.add(new JLabel("Error: Project Data Not Found"));
        }
    }

    private void loadProjectObjects(String projectName) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            //FetchApplica tion Info
            String sqlApp = "SELECT * FROM Application WHERE ProjName = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlApp)) {
                pst.setString(1, projectName);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    currentApp = new Application();
                    currentApp.setAppId(rs.getString("AppID"));
                    currentApp.setProjName(rs.getString("ProjName"));
                    currentApp.setStartDate(rs.getDate("StartDate"));
                    currentApp.setEndDate(rs.getDate("Deadline"));
                    currentApp.setConsumer(rs.getString("Consumer"));
                    
                    fetchConsumerDetails(currentApp.getConsumer());
                }
            }

            if (currentApp == null) return;

            //Fetch Services
            currentApp.fetchProjectActivities(currentApp.getAppId());
            
            // Fetch Materials for each Service
            for (Service s : currentApp.getServices()) {
                fetchMaterialsForService(s);
            }

            //Fetch Workers
            String sqlWorker = "SELECT * FROM Worker WHERE AssignedProj = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlWorker)) {
                pst.setString(1, currentApp.getAppId());
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    // FIX: Handle NULL Last Name for Workers
                    String fName = rs.getString("WorkFName");
                    String lName = rs.getString("WorkerLName");
                    if (lName == null) lName = ""; // Replace null with empty string

                    Worker w = new Worker(
                        rs.getString("WorkID"),
                        fName,
                        lName, 
                        rs.getString("ServiceIncharge"),
                        rs.getBoolean("Availability"),
                        rs.getString("AssignedProj")
                    );
                    assignedWorkers.add(w);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void fetchConsumerDetails(String consumerID) {
        try {
            String sql = "SELECT PFName, PLName FROM User WHERE UniID = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, consumerID);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    consumer = new User();
                    consumer.setPF_name(rs.getString("PFName"));
                    
                    String lName = rs.getString("PLName");
                    if (lName == null) lName = ""; 
                    consumer.setPL_name(lName);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fetchMaterialsForService(Service s) {
        String sql = "SELECT m.MatID, m.MatName, m.MatCost, psm.Quantity " +
                     "FROM [Proj-Service-Mat] psm " +
                     "INNER JOIN Material m ON psm.MatID = m.MatID " +
                     "WHERE psm.AppID = ? AND psm.ServID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, currentApp.getAppId());
            pst.setString(2, s.getServiceId());
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                Material m = new Material(
                    rs.getString("MatID"),
                    rs.getString("MatName"),
                    rs.getDouble("MatCost"),
                    rs.getInt("Quantity")
                );
                s.addMaterial(m); 
            }
        } catch(Exception e) { e.printStackTrace(); }
    }


    private void buildTopInfoSection() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gbc.insets = new Insets(5, 5, 5, 20); 
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String dateStr = (currentApp.getStartDate() != null) ? currentApp.getStartDate().toString() : "N/A";
        String deadStr = (currentApp.getEndDate() != null) ? currentApp.getEndDate().toString() : "N/A";
        
        String regBy = "Unknown";
        if (consumer != null) {
            regBy = consumer.getPF_name() + " " + (consumer.getPL_name() == null ? "" : consumer.getPL_name());
            regBy = regBy.trim();
        }

        addInfoRow(infoPanel, 0, "Project ID:", currentApp.getAppId());
        addInfoRow(infoPanel, 1, "Project Name:", currentApp.getProjName());
        addInfoRow(infoPanel, 2, "Start Date:", dateStr);
        addInfoRow(infoPanel, 3, "Deadline:", deadStr);
        addInfoRow(infoPanel, 4, "Registered by:", regBy);
        addInfoRow(infoPanel, 5, "Completion Date:", "Pending"); 

        mainContentPanel.add(infoPanel);
    }

    private void addInfoRow(JPanel panel, int row, String labelText, String valueText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(BOLD_FONT);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(lbl, gbc);

        JLabel val = new JLabel(valueText);
        val.setFont(MAIN_FONT);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        panel.add(val, gbc);
    }

    private void buildServicesTable() {
        addSectionTitle("Services");
        String[] columns = {"Service Name", "Base Cost"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Service s : currentApp.getServices()) {
            model.addRow(new Object[]{ s.getServiceName(), "Rs " + s.getServiceCost() });
            grandTotalCost += s.getServiceCost();
        }

        JPanel tablePanel = createStyledTablePanel(columns, model);
        mainContentPanel.add(tablePanel);
    }

    private void buildHRTable() {
        addSectionTitle("Human Resources");
        String[] columns = {"Person Name", "Service Incharge"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Worker w : assignedWorkers) {
            // Worker full name is constructed inside Worker class. 
            // Since we replaced null with "" earlier, it will appear as "First " which is clean.
            model.addRow(new Object[]{ w.getFullName(), w.getServiceIncharge() });
        }

        JPanel tablePanel = createStyledTablePanel(columns, model);
        mainContentPanel.add(tablePanel);
    }

    private void buildMaterialsTable() {
        addSectionTitle("Materials Used");
        String[] columns = {"Name", "Cost(per unit)", "Quantity", "Net Cost"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Service s : currentApp.getServices()) {
            for (Material m : s.getMaterials()) {
                double netCost = m.getMatCost() * m.getMatQuantity();
                grandTotalCost += netCost; 

                model.addRow(new Object[]{
                    m.getMatName(), 
                    "Rs " + m.getMatCost(), 
                    String.valueOf(m.getMatQuantity()), 
                    "Rs " + netCost
                });
            }
        }

        JPanel tablePanel = createStyledTablePanel(columns, model);
        mainContentPanel.add(tablePanel);
    }

    private void buildTotalFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotal = new JLabel("Total Cost of Project: Rs " + grandTotalCost);
        lblTotal.setFont(TITLE_FONT);
        lblTotal.setForeground(THEME_COLOR);

        footerPanel.add(lblTotal);
        mainContentPanel.add(footerPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }


    private void addMainTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(TITLE_FONT);
        lbl.setForeground(THEME_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContentPanel.add(lbl);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private void addSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(BOLD_FONT);
        lbl.setForeground(THEME_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, THEME_COLOR),
                new EmptyBorder(0, 0, 5, 0)
        ));
        mainContentPanel.add(lbl);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void addSectionSeparator() {
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 25)));
    }

    private JPanel createStyledTablePanel(String[] columnNames, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        int rowHeight = 25;
        int headerHeight = 30;
        int tableHeight = headerHeight + (model.getRowCount() * rowHeight) + 5;
        if (tableHeight > 150) tableHeight = 150;
        if (tableHeight < 60) tableHeight = 60;
        panel.setPreferredSize(new Dimension(500, tableHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));

        JTable table = new JTable(model);
        table.setFont(MAIN_FONT);
        table.setRowHeight(rowHeight);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(THEME_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}