import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List; 

public class ServDescription extends JFrame {


    private User currentUser;
    private Application currentApp;

    // Database Cache for Material Definitions
    private Map<String, Material> materialCatalog = new HashMap<>(); 

    private final Color THEME_COLOR = new Color(70, 130, 180);
    private final Color GREEN_COLOR = new Color(60, 179, 113);
    private final Color RED_COLOR = new Color(220, 20, 60);

    public ServDescription(User user, Application app) {
        this.currentUser = user;
        this.currentApp = app;

        setTitle("Material Allocation & Costing");
        setSize(1100, 700); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        loadMaterialCatalog(); 
        
        buildHeader();
        buildAllocationTable(); 
        buildFooter();

        setVisible(true);
    }

    private void loadMaterialCatalog() {
        String sql = "SELECT MatID, MatName, MatCost FROM Material";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Material m = new Material(
                    rs.getString("MatID").trim(), 
                    rs.getString("MatName"), 
                    rs.getDouble("MatCost"), 
                    0 
                );
                materialCatalog.put(m.getMatId(), m);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error Loading Materials: " + e.getMessage());
        }
    }

    private void buildHeader() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        topPanel.setBackground(THEME_COLOR);
        topPanel.setPreferredSize(new Dimension(900, 70));

        JLabel mainHeading = new JLabel("Material Allocation Summary: " + currentApp.getProjName());
        mainHeading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainHeading.setForeground(Color.WHITE);
        topPanel.add(mainHeading);

        add(topPanel, BorderLayout.NORTH);
    }

    private void buildAllocationTable() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 0, 20)); 

        JPanel headerRow = new JPanel(new GridLayout(1, 5, 10, 0)); 
        headerRow.setBackground(new Color(230, 230, 230));
        headerRow.setPreferredSize(new Dimension(0, 45)); 
        headerRow.add(new JLabel(" Service")); 
        headerRow.add(new JLabel(" User Description")); 
        headerRow.add(new JLabel(" Allocated Materials")); 
        headerRow.add(new JLabel(" Service Cost")); 
        headerRow.add(new JLabel(" Mat. Est. Cost")); 
        contentPanel.add(headerRow, BorderLayout.NORTH);

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        double grandTotal = 0;

        for (Service service : currentApp.getServices()) {
            analyzeMaterialsForService(service);
            double totalServiceCost = service.showServicesCost();
            grandTotal += totalServiceCost;
            addServiceRow(listContainer, service, totalServiceCost);
        }

        JScrollPane scrollPane = new JScrollPane(listContainer);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(createTotalRow(grandTotal), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addServiceRow(JPanel container, Service service, double totalCost) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0)); 
        row.setBackground(Color.WHITE);
        int rowHeight = Math.max(70, service.getMaterials().size() * 20 + 20);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240,240,240)));

        row.add(new JLabel(service.getServiceName()));
        row.add(new JLabel("<html><i>" + (service.getDescription() == null || service.getDescription().isEmpty() ? "None" : service.getDescription()) + "</i></html>"));

        StringBuilder matSb = new StringBuilder("<html>");
        if (service.getMaterials().isEmpty()) {
            matSb.append("<font color='red'>No Materials Found</font>");
        } else {
            for(Material m : service.getMaterials()) {
                matSb.append("• ").append(m.getMatName())
                     .append(" (x").append(m.getMatQuantity()).append(")<br>");
            }
        }
        matSb.append("</html>");
        
        row.add(new JLabel(matSb.toString()));
        row.add(new JLabel("Rs. " + service.getServiceCost())); 
        double matOnlyCost = totalCost - service.getServiceCost();
        row.add(new JLabel("Rs. " + matOnlyCost)); 
        container.add(row);
    }

    private JPanel createTotalRow(double total) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10)); 
        row.setBackground(new Color(245, 255, 245));
        JLabel lblTotal = new JLabel("TOTAL ESTIMATE: Rs. " + total);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(new Color(30, 100, 30));
        row.add(lblTotal);
        return row;
    }

    private void buildFooter() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("Back"); 
        btnBack.setBackground(RED_COLOR);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            dispose();
            new ProjectSpecs(currentUser, currentApp).setVisible(true);
        });

        JButton btnConfirm = new JButton("Confirm Application"); 
        btnConfirm.setBackground(GREEN_COLOR);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> processFinalSubmission());
        
        bottomPanel.add(btnBack); 
        bottomPanel.add(btnConfirm);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- RULES ENGINE LOGIC (Same as you requested) ---
    private void analyzeMaterialsForService(Service service) {
        String desc = service.getDescription() != null ? service.getDescription().toLowerCase().trim() : "";
        String name = service.getServiceName();
        boolean isLargeScope = containsAny(desc, "all", "every", "whole", "chairs", "tables", "walls", "doors", "floor", "rooms", "classrooms", "labs", "offices", "system", "unit", "machines", "components", "frames", "joints");
        boolean specificRuleMatched = false; 

        if (name.equalsIgnoreCase("Finishing")) {
            if (containsAny(desc, "polishing", "polish")) { addMatToService(service, "M-001", isLargeScope ? 4 : 1); specificRuleMatched = true; }
            if (containsAny(desc, "surfacing", "surface")) { addMatToService(service, "M-003", isLargeScope ? 8 : 1); specificRuleMatched = true; }
            if (containsAny(desc, "paint", "painting", "coloring", "color", "recolor")) { addMatToService(service, "M-016", isLargeScope ? 4 : 1); specificRuleMatched = true; }
            if (!specificRuleMatched) { addMatToService(service, "M-001", isLargeScope ? 4 : 1); addMatToService(service, "M-003", isLargeScope ? 8 : 1); addMatToService(service, "M-016", isLargeScope ? 4 : 1); }
        } else if (name.equalsIgnoreCase("Sheet Metal")) {
            if (containsAny(desc, "making", "constructing", "crafting")) { addMatToService(service, "M-002", isLargeScope ? 100 : 10); addMatToService(service, "M-004", isLargeScope ? 5 : 1); addMatToService(service, "M-008", isLargeScope ? 100 : 10); specificRuleMatched = true; }
            if (!specificRuleMatched) { addMatToService(service, "M-002", isLargeScope ? 100 : 10); addMatToService(service, "M-004", isLargeScope ? 5 : 1); addMatToService(service, "M-008", isLargeScope ? 100 : 10); }
        } else if (name.equalsIgnoreCase("Mechanical")) { addMatToService(service, "M-002", isLargeScope ? 100 : 20); addMatToService(service, "M-008", isLargeScope ? 100 : 20); addMatToService(service, "M-014", isLargeScope ? 2 : 1); }
        else if (name.equalsIgnoreCase("Electrical")) { addMatToService(service, "M-007", isLargeScope ? 3 : 1); addMatToService(service, "M-009", isLargeScope ? 5 : 1); addMatToService(service, "M-010", isLargeScope ? 5 : 1); }
        else if (name.equalsIgnoreCase("Machining")) { addMatToService(service, "M-004", isLargeScope ? 4 : 1); addMatToService(service, "M-013", isLargeScope ? 3 : 1); addMatToService(service, "M-014", isLargeScope ? 3 : 1); }
        else if (name.equalsIgnoreCase("Reproduction")) {
            if (containsAny(desc, "mold", "replica", "replicate")) { addMatToService(service, "M-007", isLargeScope ? 4 : 2); addMatToService(service, "M-015", isLargeScope ? 4 : 2); addMatToService(service, "M-016", isLargeScope ? 4 : 2); specificRuleMatched = true; }
            if (!specificRuleMatched) { addMatToService(service, "M-007", isLargeScope ? 4 : 1); addMatToService(service, "M-015", isLargeScope ? 4 : 1); addMatToService(service, "M-016", isLargeScope ? 4 : 1); }
        } else if (name.equalsIgnoreCase("Refrigeration")) { addMatToService(service, "M-005", isLargeScope ? 3 : 1); addMatToService(service, "M-006", isLargeScope ? 3 : 1); addMatToService(service, "M-009", isLargeScope ? 5 : 1); }
        else if (name.equalsIgnoreCase("Smithy")) { addMatToService(service, "M-004", isLargeScope ? 4 : 1); addMatToService(service, "M-014", isLargeScope ? 2 : 1); }
        else if (name.equalsIgnoreCase("Firefighting")) { addMatToService(service, "M-017", 3); }
        else if (name.equalsIgnoreCase("Instrumentation")) { addMatToService(service, "M-002", isLargeScope ? 100 : 10); addMatToService(service, "M-007", isLargeScope ? 3 : 1); addMatToService(service, "M-010", isLargeScope ? 4 : 1); }
        else if (name.equalsIgnoreCase("Welding")) { addMatToService(service, "M-004", isLargeScope ? 3 : 1); addMatToService(service, "M-011", isLargeScope ? 3 : 1); addMatToService(service, "M-012", isLargeScope ? 3 : 1); }
        else if (name.equalsIgnoreCase("Designing")) { addMatToService(service, "M-015", isLargeScope ? 4 : 1); addMatToService(service, "M-016", isLargeScope ? 4 : 1); }
        else if (name.equalsIgnoreCase("Carpentry")) { addMatToService(service, "M-002", isLargeScope ? 100 : 10); addMatToService(service, "M-008", isLargeScope ? 100 : 10); }
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isEmpty()) return false;
        for (String key : keywords) { if (text.contains(key.toLowerCase())) return true; }
        return false;
    }

    private void addMatToService(Service s, String matID, int qty) {
        Material catalogMat = materialCatalog.get(matID);
        if (catalogMat != null) {
            Material allocated = new Material(catalogMat.getMatId(), catalogMat.getMatName(), catalogMat.getMatCost(), qty);
            s.addMaterial(allocated); 
        }
    }
    private void processFinalSubmission() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Check Workers
            for (Service s : currentApp.getServices()) {
                String checkWorker = "SELECT WorkID FROM Worker WHERE ServiceIncharge = ? AND Availability = true";
                PreparedStatement pst = conn.prepareStatement(checkWorker);
                pst.setString(1, s.getServiceId());
                ResultSet rs = pst.executeQuery();
                
                // If no Worker available then reject
                if (!rs.next()) {
                    conn.rollback(); // Cancel anything pending
                    conn.setAutoCommit(true); // Switch to separate transaction for rejection
                    
                    // Insert application as rejected
                    insertApplicationRecord(conn, "Rejected");
                    
                    JOptionPane.showMessageDialog(this, 
                        "Application REJECTED: No available worker for " + s.getServiceName(), 
                        "Submission Failed", JOptionPane.ERROR_MESSAGE);
                    
                    dispose();
                    new MenuScreen(currentUser).setVisible(true);
                    return; 
                }
            }

            // Insert Application if Accepted
            insertApplicationRecord(conn, "Accepted");

            //Assign Workers & Insert Related Data
            for (Service s : currentApp.getServices()) {
                // Find and Assign Worker
                String checkWorker = "SELECT WorkID FROM Worker WHERE ServiceIncharge = ? AND Availability = true";
                PreparedStatement pst = conn.prepareStatement(checkWorker);
                pst.setString(1, s.getServiceId());
                ResultSet rs = pst.executeQuery();
                if(rs.next()) {
                    String workerID = rs.getString("WorkID");
                    String assignSQL = "UPDATE Worker SET Availability = false, AssignedProj = ? WHERE WorkID = ?";
                    PreparedStatement pstUpdate = conn.prepareStatement(assignSQL);
                    pstUpdate.setString(1, currentApp.getAppId());
                    pstUpdate.setString(2, workerID);
                    pstUpdate.executeUpdate();
                }

                // Insert Service Links
                String sqlServ = "INSERT INTO [Proj-Service] (AppID, ServID, Description) VALUES (?, ?, ?)";
                PreparedStatement pstServ = conn.prepareStatement(sqlServ);
                pstServ.setString(1, currentApp.getAppId());
                pstServ.setString(2, s.getServiceId());
                pstServ.setString(3, s.getDescription());
                pstServ.executeUpdate();

                // Insert Progress
                String sqlProg = "INSERT INTO [Proj-Service-Prog] (AppID, ServID, Start, Status) VALUES (?, ?, ?, ?)";
                PreparedStatement pstProg = conn.prepareStatement(sqlProg);
                pstProg.setString(1, currentApp.getAppId());
                pstProg.setString(2, s.getServiceId());
                if (currentApp.getStartDate() != null) {
                    pstProg.setDate(3, new java.sql.Date(currentApp.getStartDate().getTime()));
                } else {
                    pstProg.setNull(3, Types.DATE);
                }
                pstProg.setString(4, "Pending");
                pstProg.executeUpdate();

                // Insert Materials
                String sqlMat = "INSERT INTO [Proj-Service-Mat] (AppID, ServID, MatID, Quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement pstMat = conn.prepareStatement(sqlMat);
                for (Material m : s.getMaterials()) {
                    pstMat.setString(1, currentApp.getAppId());
                    pstMat.setString(2, s.getServiceId());
                    pstMat.setString(3, m.getMatId());
                    pstMat.setInt(4, m.getMatQuantity());
                    pstMat.executeUpdate();
                }
            }

            conn.commit(); // Commit the "Accepted" one

            JOptionPane.showMessageDialog(this, "Application Submitted Successfully!");
            dispose();
            new MenuScreen(currentUser).setVisible(true);

        } catch (Exception e) {
            try { if(conn != null) conn.rollback(); } catch(Exception ex){}
            JOptionPane.showMessageDialog(this, "Submission Failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if(conn != null) conn.close(); } catch(Exception ex){}
        }
    }

    // Helper to insert the Application Header
    private void insertApplicationRecord(Connection conn, String status) throws SQLException {
        String sql = "INSERT INTO Application (AppID, ProjName, StartDate, Deadline, AppStatus, Consumer) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, currentApp.getAppId());
        pstmt.setString(2, currentApp.getProjName());
        
        if (currentApp.getStartDate() != null) pstmt.setDate(3, new java.sql.Date(currentApp.getStartDate().getTime()));
        else pstmt.setNull(3, java.sql.Types.DATE);
        
        if (currentApp.getEndDate() != null) pstmt.setDate(4, new java.sql.Date(currentApp.getEndDate().getTime()));
        else pstmt.setNull(4, java.sql.Types.DATE);
        
        pstmt.setString(5, status);
        pstmt.setString(6, currentApp.getConsumer()); // Use Consumer from object
        pstmt.executeUpdate();
    }
}