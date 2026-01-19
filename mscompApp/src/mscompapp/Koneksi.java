/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mscompapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection mysqlconfig;
    
    public static Connection configDB() throws SQLException {
        try {
            // URL koneksi: localhost, port 3306, nama database db_latihan
            String url = "jdbc:mysql://localhost:3306/ms_db"; 
            String user = "root"; // user default XAMPP
            String pass = "";     // password default XAMPP (kosong)
            
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            mysqlconfig = DriverManager.getConnection(url, user, pass);            
        } catch (Exception e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
        }
        return mysqlconfig;
    }
}