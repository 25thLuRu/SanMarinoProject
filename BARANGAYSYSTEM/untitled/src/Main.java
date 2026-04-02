import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLOutput;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        try {
            // 1. Path to your database
            String url = "jdbc:ucanaccess://C:/Users/User/Documents/BARANGAYSYSTEM/SanMarinoDB.accdb";

            // 2. Connect to database
            Connection conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement();

            String sql = "SELECT * FROM RESIDENT";
            ResultSet br = stmt.executeQuery(sql);

            while (br.next()){
                int res_ID = br.getInt("Resident_ID");
                String res_fname = br.getNString("RF_Name");
                String res_mname = br.getNString("RM_Name");
                String res_lname = br.getNString("RL_Name");
                long res_contact = br.getLong("RContact_Num");
                String res_email= br.getNString("REmail");
                int res_hn= br.getInt("House_Num");
                String res_street= br.getNString("Street");
                int res_civid= br.getInt("CivilStatus_ID");
                java.sql.Date res_dob= br.getDate("Date_of_Birth");

                System.out.println(res_ID + " " + res_fname + " " + res_mname + " " + res_lname + " " + " " + res_contact + " " + res_email + " "
                + res_hn + " " + res_street + " " + res_civid + " " + res_dob);

            }
            System.out.println("✅ Connected to MS Access!");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}