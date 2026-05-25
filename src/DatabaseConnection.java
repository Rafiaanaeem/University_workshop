import java.sql.*;

public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            String url =
                "jdbc:ucanaccess://database/UniWorkshop_final.accdb";

            return DriverManager.getConnection(url);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}