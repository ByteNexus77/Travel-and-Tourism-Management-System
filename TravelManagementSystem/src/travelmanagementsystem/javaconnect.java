
package travelmanagementsystem;

import java.sql.*;
import javax.swing.JOptionPane;

public class javaconnect {
    Connection conn;
   
    public static Connection connectdb(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
           Connection  conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/THBS","root","");
           System.out.println("Connected");
           return conn;
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,e);
            return null;
        }
    }
    
   
}
