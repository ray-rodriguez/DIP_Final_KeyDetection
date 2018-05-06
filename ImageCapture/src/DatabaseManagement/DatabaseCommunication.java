/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseManagement;
import java.sql.*;
/**
 *
 * @author Ray-GRod
 */
public class DatabaseCommunication {
    Connection myConn = null;
    Statement myStmt = null;
    ResultSet myRs = null;

    public void DBConnect() throws SQLException
    {
        try 
        {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgprocessing?autoReconnect=true&useSSL=false", "ray.grod" , "utrgv123");

            // 2. Create a statement
            myStmt = myConn.createStatement();

            // 3. Execute SQL query
            myRs = myStmt.executeQuery("select * from `keys`");

            // 4. Process the result set
            while (myRs.next()) 
            {
                System.out.println(myRs.getString("id") + ", " + myRs.getString("name")+ ", " + myRs.getString("building")+ ", " + myRs.getString("office")+ "\n\n\n" + myRs.getString("features")+ "\n\n\n" + myRs.getString("image"));
            }
        }
        catch (Exception exc) 
        {
            exc.printStackTrace();
        }
        finally 
        {
            if (myRs != null) {
                    myRs.close();
            }

            if (myStmt != null) {
                    myStmt.close();
            }

            if (myConn != null) {
                    myConn.close();
            }
        }
    }
}
