import java.sql.*;

public class User {
    private String uni_id;
    private String password;
    private String pf_name;
    private String pl_name;

    public User() {}
    public User(String uniId, String password, String pfName, String plName) {
        this.uni_id = uniId; this.password = password;
        this.pf_name = pfName; this.pl_name = plName;
    }

    //Getters and Setters
    public String getUni_id() { return uni_id; }
    public void setUni_id(String uniId) { this.uni_id = uniId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPF_name() { return pf_name; } // Getter for PF name [cite: 4]
    public void setPF_name(String pfName) { this.pf_name = pfName; }
    public String getPL_name() { return pl_name; } // Getter for PL name [cite: 5]
    public void setPL_name(String plName) { this.pl_name = plName; }

    //Methods
    
    //Returns true if the object has been populated with data
    public boolean checkuser() {
        return uni_id != null && !uni_id.isEmpty();
    }

    // VerifyID logic [cite: 7]
    public boolean verifyid(String id) { 
        return this.uni_id != null && this.uni_id.equals(id); 
    }

    // Fetchuser logic: Database interaction moved to the model 
    public User fetchuser() throws SQLException {
        String sql = "SELECT * FROM User WHERE UniID = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, this.uni_id);
            pst.setString(2, this.password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("UniID"), rs.getString("Password"),
                                rs.getString("PFName"), rs.getString("PLName"));
            }
        }
        return null;
    }
}