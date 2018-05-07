/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseManagement;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import javax.sql.rowset.serial.SerialBlob;
/**
 *
 * @author Ray-GRod
 */
public class DatabaseCommunication {
    ArrayList<DBQueryObject> queryObjs = new ArrayList<DBQueryObject>();
    Connection myConn = null;
    Statement myStmt = null;
    PreparedStatement myPStmt = null;
    ResultSet myRs = null;
    
    public DBQueryObject[] DBQueryDatabase()
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
                Blob featBlob = myRs.getBlob("features");
                int blobLength = (int)featBlob.length();
                byte[] featBlobAsBytes = featBlob.getBytes(1, blobLength);
                ByteBuffer byteBuffer = ByteBuffer.wrap(featBlobAsBytes);
                double[] doubles = new double[blobLength/8];
                for (int i = 0; i < doubles.length; i++) {
                    doubles[i] = byteBuffer.getDouble();
                }
                
                Blob imgBlob = myRs.getBlob("image");
                blobLength = (int)imgBlob.length();
                byte[] imgBlobBytes = imgBlob.getBytes(1, blobLength);
                
                DBQueryObject temp = new DBQueryObject(myRs.getInt("id"), myRs.getString("name"), myRs.getString("building"), myRs.getString("office"), doubles, imgBlobBytes);
                queryObjs.add(temp);
            }
        }
        catch (Exception exc) 
        {
            exc.printStackTrace();
        }
        finally {
            try { myRs.close(); } catch (Exception e) { /* ignored */ }
            try { myStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myPStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myConn.close(); } catch (Exception e) { /* ignored */ }
        }
        return queryObjs.toArray(new DBQueryObject[queryObjs.size()]);
    }
    
    public void DBAddRecord(String name, String building, String office, double[] features, byte[] image) 
    {
        try 
        {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgprocessing?autoReconnect=true&useSSL=false", "ray.grod" , "utrgv123");
            String sql = "INSERT INTO `keys`"
                    + "(name, building, office, features, image) VALUES"
                    + "(?, ?, ?, ?, ?)";
            myPStmt = myConn.prepareStatement(sql);
            
           Blob blob = new SerialBlob(features.toString().getBytes());
           Blob blob2 = new SerialBlob(image);
           
            myPStmt.setString(1, name);
            myPStmt.setString(2, building);
            myPStmt.setString(3, office);
            myPStmt.setBlob(4, blob);
            myPStmt.setBlob(5, blob2);
            
            myPStmt.executeUpdate();
        }
        catch (Exception exc) 
        {
            exc.printStackTrace();
        }
        finally {
            try { myRs.close(); } catch (Exception e) { /* ignored */ }
            try { myStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myPStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myConn.close(); } catch (Exception e) { /* ignored */ }
        }
    }
    
    public void UpdateRecordFeatureVector(String name, double[] features) 
    {
        try 
        {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgprocessing?autoReconnect=true&useSSL=false", "ray.grod" , "utrgv123");
            String sql = "UPDATE `keys`"
                    + " SET `features` = ? WHERE `name` = '"+name+"'";
            myPStmt = myConn.prepareStatement(sql);
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(features.length*8);
            for(double d : features)
            {
                byteBuffer.putDouble(d);
            }
            
            Blob blob = new SerialBlob(byteBuffer.array());
            myPStmt.setBlob(1, blob);

            myPStmt.executeUpdate();
        }
        catch (SQLException exc) 
        {
            exc.printStackTrace();
        }
        finally {
            try { myRs.close(); } catch (Exception e) { /* ignored */ }
            try { myStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myPStmt.close(); } catch (Exception e) { /* ignored */ }
            try { myConn.close(); } catch (Exception e) { /* ignored */ }
        }
    }
}
