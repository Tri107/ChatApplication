/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author DELL
 */
public class MyConnection {
   public Connection getConnection(){
       try {
           Class.forName("com.mysql.jdbc.Driver");
           String url = "jdbc:mysql://localhost/chatapp?user=root&password=";
           Connection con = DriverManager.getConnection(url);
           return con;
       } catch (Exception e) {
           JOptionPane.showMessageDialog(null, e.toString(),"Loi",JOptionPane.ERROR_MESSAGE);
           return null;
       }
   }
}
