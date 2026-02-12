/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ASUS
 */
public class Login extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Login.class.getName());
    public static String namaUser;
    public static String role;
    public static String idUser;
    
    private final String CONFIG_FILE = "db_config.properties"; // File target konfigurasi
    
    public Login() {
        initComponents();
        this.namaUser = Session.namaUser;
        this.role = Session.level;
        this.idUser = Session.idUser;
        this.getRootPane().setDefaultButton(btLogin);
        
        // 1. Setting Tampilan Layar Penuh
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.setMaximizedBounds(env.getMaximumWindowBounds());
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setResizable(true); 

        // 2. Setting Label Config agar terlihat seperti tombol
        btn_config.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_config.setToolTipText("Klik untuk mengubah konfigurasi database");
        
        // 3. Listener Klik pada btn_config
        btn_config.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bukaPopupConfig();
            }
        });
    }
    
    
    private void restartApplication() {
    try {
        // Mendapatkan lokasi file jar yang sedang berjalan
        String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
        java.io.File currentJar = new java.io.File(Login.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        // Jika yang dijalankan adalah file JAR
        if (currentJar.getName().endsWith(".jar")) {
            new ProcessBuilder(javaBin, "-jar", currentJar.getPath()).start();
        } else {
            // Jika dijalankan dari IDE (NetBeans/IntelliJ)
            // Ganti 'mscompapp.Login' dengan nama package.MainClass kamu
            new ProcessBuilder(javaBin, "-cp", System.getProperty("java.class.path"), "mscompapp.Login").start();
        }

        // Tutup aplikasi saat ini
        System.exit(0);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal restart otomatis: " + e.getMessage());
    }
}
    
    private void bukaPopupConfig() {
        // Ambil data lama dari file properties
        Properties prop = new Properties();
        String currentUrl = "jdbc:mysql://localhost:3306/ms_db";
        String currentUser = "root";
        String currentPass = "";

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            prop.load(fis);
            currentUrl = prop.getProperty("db.url", currentUrl);
            currentUser = prop.getProperty("db.user", currentUser);
            currentPass = prop.getProperty("db.pass", currentPass);
        } catch (IOException e) {
            // Gunakan default jika file tidak ditemukan
        }

        // Buat Form di dalam Panel untuk Pop-up
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField tfUrl = new JTextField(currentUrl);
        JTextField tfUser = new JTextField(currentUser);
        JTextField tfPassField = new JTextField(currentPass);

        panel.add(new JLabel("Database URL:"));
        panel.add(tfUrl);
        panel.add(new JLabel("Username Database:"));
        panel.add(tfUser);
        panel.add(new JLabel("Password Database:"));
        panel.add(tfPassField);

        // Tampilkan Dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Pengaturan Koneksi Database",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
    try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
        prop.setProperty("db.url", tfUrl.getText().trim());
        prop.setProperty("db.user", tfUser.getText().trim());
        prop.setProperty("db.pass", tfPassField.getText());
        prop.store(fos, "Updated from Login");

        JOptionPane.showMessageDialog(this, "Konfigurasi diperbarui. Merestart aplikasi...");
        
        // Panggil restart (pastikan method restartApplication sudah dicopy ke Login.java juga)
        restartApplication();
        
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage());
    }
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btLogin = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tPass = new javax.swing.JPasswordField();
        tUsn = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btn_config = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(6);
        setMinimumSize(new java.awt.Dimension(1920, 1080));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel3.setText("Password :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 420, 120, 40));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel2.setText("Username :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(108, 295, 150, 60));

        btLogin.setBackground(new java.awt.Color(204, 204, 204));
        btLogin.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btLogin.setText("LOGIN");
        btLogin.setFocusPainted(false);
        btLogin.addActionListener(this::btLoginActionPerformed);
        jPanel1.add(btLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 630, 200, 60));

        jLabel1.setFont(new java.awt.Font("Swis721 Hv BT", 1, 35)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("LOGIN");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 90, 150, 60));

        tPass.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tPass.addActionListener(this::tPassActionPerformed);
        jPanel1.add(tPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 470, 340, 50));

        tUsn.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tUsn.addActionListener(this::tUsnActionPerformed);
        jPanel1.add(tUsn, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 360, 340, 50));

        jLabel4.setFont(new java.awt.Font("Swis721 LtEx BT", 1, 25)); // NOI18N
        jLabel4.setText("username dan password");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 210, 340, 40));

        jLabel6.setFont(new java.awt.Font("Swis721 LtEx BT", 1, 25)); // NOI18N
        jLabel6.setText("Silahkan masukkan");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 270, 40));

        btn_config.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 14)); // NOI18N
        btn_config.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        btn_config.setText("Ubah Koneksi!");
        jPanel1.add(btn_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 120, 50));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 610, 1030));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/backlogin.png"))); // NOI18N
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 0, 1310, 1020));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoginActionPerformed
        // TODO add your handling code here
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

    // Gunakan Thread agar UI tidak freeze saat mengecek IP/Koneksi
    new Thread(() -> {
        try {
            java.sql.Connection conn = config.Koneksi.configDB();
            
            // Validasi: Jika koneksi null, berarti ada yang salah dengan konfigurasi
            if (conn == null || conn.isClosed()) {
                throw new java.sql.SQLException("Koneksi tidak dapat dibangun. Periksa IP Address Anda.");
            }

            String sql = "SELECT * FROM tbl_user WHERE username=? AND password=?";
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tUsn.getText());
            ps.setString(2, new String(tPass.getPassword()));
            java.sql.ResultSet res = ps.executeQuery();

            if (res.next()) {
                // ... (Logika session Anda tetap sama)
                String id = res.getString("id_user");
                String nama = res.getString("nama");
                String hakAkses = res.getString("role");

                Session.idUser = id;
                Session.namaUser = nama;
                Session.level = hakAkses;

                java.awt.EventQueue.invokeLater(() -> {
                    Dashboard dash = new Dashboard(nama, hakAkses);
                    dash.setVisible(true);
                    this.dispose();
                });
            } else {
                java.awt.EventQueue.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Username atau Password Salah");
                });
            }
        } catch (Exception e) {
            // Tampilkan pesan error jika IP salah atau koneksi gagal
            java.awt.EventQueue.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "GAGAL TERHUBUNG KE DATABASE!\n" +
                    "Penyebab: IP Address salah atau server mati.\n\n" +
                    "Error: " + e.getMessage(), 
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            });
        } finally {
            // Kembalikan kursor ke normal
            java.awt.EventQueue.invokeLater(() -> {
                setCursor(java.awt.Cursor.getDefaultCursor());
            });
        }
    }).start();
    }//GEN-LAST:event_btLoginActionPerformed

    private void tPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPassActionPerformed
        // TODO add your handling code here:
        btLogin.doClick();
    }//GEN-LAST:event_tPassActionPerformed

    private void tUsnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tUsnActionPerformed
        // TODO add your handling code here:
        tPass.requestFocus();
    }//GEN-LAST:event_tUsnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btLogin;
    private javax.swing.JLabel btn_config;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField tPass;
    private javax.swing.JTextField tUsn;
    // End of variables declaration//GEN-END:variables
}
