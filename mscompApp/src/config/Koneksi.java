/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection mysqlconfig;
    private static final String CONFIG_FILE = "db_config.properties";

    public static Connection configDB() throws SQLException {
        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            prop.load(fis);
            fis.close();
            
            // Mengambil data dari file properties
            String url = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/ms_db");
            String user = prop.getProperty("db.user", "root");
            String pass = prop.getProperty("db.pass", "");

            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            mysqlconfig = DriverManager.getConnection(url, user, pass);            
        } catch (IOException e) {
            // Jika file tidak ada, gunakan default hardcoded (hanya untuk pertama kali)
            String url = "jdbc:mysql://localhost:3306/ms_db";
            String user = "root";
            String pass = "";
            mysqlconfig = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            System.err.println("Koneksi gagal: " + e.getMessage());
        }
        return mysqlconfig;
    }
}