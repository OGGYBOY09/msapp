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
        
        String url = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/ms_db");
        String user = prop.getProperty("db.user", "root");
        String pass = prop.getProperty("db.pass", "");

        // SET TIMEOUT: Biar tidak freeze kelamaan (contoh: 5 detik)
        DriverManager.setLoginTimeout(5); 
        
        DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        mysqlconfig = DriverManager.getConnection(url, user, pass);            
    } catch (IOException | SQLException e) {
        // Jika gagal koneksi dari file, lempar error agar ditangkap Login.java
        throw new SQLException("Pastikan IP/Konfigurasi Database benar: " + e.getMessage());
    }
    return mysqlconfig;
    }
}