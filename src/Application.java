import java.util.*;
import java.util.Date;
import java.sql.*;

public class Application {
    private String appId;
    private String projName;
    private Date startDate;
    private Date endDate;
    private Date completion;
    private String consumer;
    private String appStatus;
    private List<Service> selectedServices = new ArrayList<>();//composition

    public Application() {}

    //GETTERS AND SETTERS
    public String getAppId() { return appId; }
    public void setAppId(String id) { this.appId = id; }
    
    public String getProjName() { return projName; }
    public void setProjName(String name) { this.projName = name; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date d) { this.startDate = d; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date d) { this.endDate = d; }
    
    public Date getCompleetion() { return completion; }
    public void setCompleetion(Date c) { this.completion = c; }
    
    public String getConsumer() { return consumer; }
    public void setConsumer(String c) { this.consumer = c; }
    
    public String getAppStatus() { return appStatus; }
    public void setAppStatus(String s) { this.appStatus = s; }
    
    public List<Service> getServices() { return selectedServices; }

    //METHODS
    public void saveApplication() throws SQLException {
        String sql = "INSERT INTO Application (AppID, ProjName, StartDate, Deadline, AppStatus, Consumer) VALUES (?, ?, ?, ?, 'Pending', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, appId);
            pstmt.setString(2, projName);
            
            if (startDate != null) {
                pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }
            
            if (endDate != null) {
                pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }
            
            pstmt.setString(5, consumer);
            pstmt.executeUpdate();
        }
    }

   
    public static List<Application> getProjectList() {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT AppID, ProjName, AppStatus FROM Application WHERE AppStatus = 'Accepted'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Application app = new Application();
                app.setAppId(rs.getString("AppID"));
                app.setProjName(rs.getString("ProjName"));
                app.setAppStatus(rs.getString("AppStatus"));
                list.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void fetchProjectActivities(String projectId) {
        String sql = "SELECT s.ServID, s.ServName, s.ServCost, ps.Description " +
                     "FROM [Proj-Service] ps " +
                     "INNER JOIN Service s ON ps.ServID = s.ServID " +
                     "WHERE ps.AppID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            
            selectedServices.clear(); 
            
            while (rs.next()) {
                Service s = new Service(
                    rs.getString("ServID"), 
                    rs.getString("ServName"), 
                    rs.getDouble("ServCost")
                );
                s.setDescription(rs.getString("Description"));
                s.setProjName(this.projName);
                
                selectedServices.add(s); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Date getProjectProgress(String projectId) {
        return this.completion;
    }
}