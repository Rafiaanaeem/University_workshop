import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class ProgressDialog extends JDialog {

    private final Color THEME_COLOR = new Color(70, 130, 180); 
    private final Color ALERT_COLOR = new Color(220, 20, 60);  
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font SUMMARY_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private JTable progressTable;
    private DefaultTableModel tableModel;
    
    private JLabel lblDeadline;
    private JLabel lblExtra;

    private DateTimeFormatter[] formatters = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH)
    };

    public ProgressDialog(JFrame parent, String projectName) {
        super(parent, "Progress: " + projectName, true);
        setSize(700, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        buildHeader();
        buildTable();
        buildSummaryPanel();

        // Load project data
        loadDataFromDB(projectName);
    }

    private void buildHeader() {
        JLabel lblTitle = new JLabel("Activity Progress Log", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(THEME_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);
    }

    private void buildTable() {
        String[] columnNames = {"Activity", "Start Date", "Days Spent", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        
        progressTable = new JTable(tableModel);
        progressTable.setFont(MAIN_FONT);
        progressTable.setRowHeight(25);
        progressTable.setFillsViewportHeight(true);
        progressTable.setEnabled(false); 
        
        JTableHeader header = progressTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(THEME_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(progressTable);
        scrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(0, 1, 5, 5)); 
        summaryPanel.setBorder(new EmptyBorder(10, 20, 20, 20)); 
        summaryPanel.setBackground(Color.WHITE);

        lblDeadline = new JLabel("Days till deadline: Calculating...");
        lblDeadline.setFont(SUMMARY_FONT);
        lblDeadline.setForeground(Color.BLACK); 

        lblExtra = new JLabel("Extra days being consumed: 0 days");
        lblExtra.setFont(SUMMARY_FONT);
        lblExtra.setForeground(ALERT_COLOR); 

        JButton btnBack = new JButton("Back");
        btnBack.setBackground(ALERT_COLOR);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(HEADER_FONT);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> dispose());
        
        summaryPanel.add(lblDeadline);
        summaryPanel.add(lblExtra);
        summaryPanel.add(Box.createVerticalStrut(10));
        summaryPanel.add(btnBack);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void loadDataFromDB(String projectName) {
        String appID = null;
        String deadlineStr = null;
        String projStartStr = null;

        //Fetch AppID, Deadline, and Project Start Date from Application table
        String sqlApp = "SELECT AppID, StartDate, Deadline FROM Application WHERE ProjName = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlApp)) {
            
            pst.setString(1, projectName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                appID = rs.getString("AppID");
                projStartStr = rs.getString("StartDate");
                deadlineStr = rs.getString("Deadline");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching project info: " + e.getMessage());
            return;
        }

        if (appID == null) return;

        //Fetch Activity Data & Calculate Days Spent
        long totalDaysSpentSum = 0;

        String sqlProg = "SELECT s.ServName, p.Start, p.End, p.Status " +
                         "FROM [Proj-Service-Prog] p " +
                         "INNER JOIN Service s ON p.ServID = s.ServID " +
                         "WHERE p.AppID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlProg)) {
            
            pst.setString(1, appID);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String activity = rs.getString("ServName");
                String startStr = rs.getString("Start");
                String endStr = rs.getString("End");
                String status = rs.getString("Status");


                long daysSpent = calculateDaysDifference(startStr, endStr);
                
                tableModel.addRow(new Object[]{activity, startStr, daysSpent + " days", status});
                
                //Extra Days logic
                totalDaysSpentSum += daysSpent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateSummaries(projStartStr, deadlineStr, totalDaysSpentSum);
    }

    private long calculateDaysDifference(String startStr, String endStr) {
        try {
            LocalDate startDate = parseDate(startStr);
            LocalDate endDate;

            //If End is blank use current system date
            if (endStr == null || endStr.trim().isEmpty()) {
                endDate = LocalDate.now(); 
            } else {
                endDate = parseDate(endStr);
            }

            if (startDate != null && endDate != null) {
                long diff = ChronoUnit.DAYS.between(startDate, endDate);
                // If negative num then display 0
                return (diff < 0) ? 0 : diff;
            }
        } catch (Exception e) { }
        return 0;
    }

    private void updateSummaries(String projStartStr, String deadlineStr, long sumOfDaysSpent) {
        try {
            LocalDate deadline = parseDate(deadlineStr);
            LocalDate projectStart = parseDate(projStartStr);
            LocalDate today = LocalDate.now();

            if (deadline == null || projectStart == null) {
                lblDeadline.setText("Days till deadline: N/A");
                return;
            }

            // Deadline date - current date 
            //if negative display 0
            long daysTillDeadline = ChronoUnit.DAYS.between(today, deadline);
            if (daysTillDeadline < 0) daysTillDeadline = 0;
            
            lblDeadline.setText("Days till deadline: " + daysTillDeadline + " days");

            // Difference of (Start date + Sum of all days spent) with the deadline date
            LocalDate projectedFinishDate = projectStart.plusDays(sumOfDaysSpent);
            
            long extraDays = ChronoUnit.DAYS.between(deadline, projectedFinishDate);
            if (extraDays < 0) extraDays = 0; // for -ve values again simply display 0

            lblExtra.setText("Extra days being consumed: " + extraDays + " days");
            
            if (extraDays == 0) {
                lblExtra.setForeground(new Color(34, 139, 34));
            } else {
                lblExtra.setForeground(ALERT_COLOR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblDeadline.setText("Days till deadline: Error");
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        //remove time part like "00:00:00.0"
        if (dateStr.contains(" ")) {
            dateStr = dateStr.split(" ")[0]; 
        }

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try another format
            }
        }
        return null;
    }
}