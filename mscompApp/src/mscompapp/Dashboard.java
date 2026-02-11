/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mscompapp;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
/**
 *
 * @author ASUS
 */

public class Dashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    private String userRole;
    private int currentPanelIndex = 0;

    public Dashboard(String username, String role) {
        this.userRole = role;
        Login.namaUser = username;        
        initComponents();
        lblWelcome.setText("Selamat Datang, " + username);
        
        pSide.setLayout(new java.awt.BorderLayout());
        
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.setMaximizedBounds(env.getMaximumWindowBounds());
        this.setExtendedState(this.getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setResizable(true); 

        initSidebar();
        
        // Load Halaman Awal Berdasarkan Role
        if ("admin".equalsIgnoreCase(userRole)) {
            switchPanel(new Beranda());
        } else {
            switchPanel(new Teknisi()); // Halaman awal teknisi
        }
        
        initKeyShortcuts();
        
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss");
                lblTanggal.setText(sdf.format(new Date()));
            }
        });
        timer.start();
    }
    
    // --- 1. INISIALISASI SIDEBAR BERDASARKAN ROLE ---
    private void initSidebar() {
        pSide.removeAll();
        if ("admin".equalsIgnoreCase(userRole)) {
            pSide.add(new sidebar_admin(this)); 
        } else {
            // Memuat Sidebar Teknisi
            pSide.add(new sidebar_teknisi(this));
        }
        pSide.revalidate();
        pSide.repaint();
    }
    
    // --- 2. INISIALISASI SHORTCUT KEYBOARD ---
    private void initKeyShortcuts() {
        InputMap im = pMain.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = pMain.getActionMap();

        // Loop Alt+1 sampai Alt+8
        for (int i = 1; i <= 8; i++) {
            String key = "alt " + i;
            final int index = i - 1; // Index panel dimulai dari 0
            
            im.put(KeyStroke.getKeyStroke(key), key);
            am.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jumpToPanel(index);
                }
            });
        }
    }

    // --- 3. LOGIKA PINDAH HALAMAN (Admin vs Teknisi) ---
    private void jumpToPanel(int index) {
        // Cek Role dulu, baru tentukan halaman mana yang dibuka
        if ("admin".equalsIgnoreCase(userRole)) {
            // --- MENU ADMIN ---
            // Batasi index agar tidak error jika tekan Alt+9
            if (index > 7) return; 
            
            setPanelIndex(index);
            switch (index) {
                case 0: switchPanel(new Beranda()); break;      // Alt + 1
                case 1: switchPanel(new PKelLaporan()); break;  // Alt + 2
                case 2: switchPanel(new PKelService()); break;  // Alt + 3
                case 3: switchPanel(new PKelBarang()); break;   // Alt + 4
                case 4: switchPanel(new PKatService()); break;  // Alt + 5
                case 5: switchPanel(new PKatBarang()); break;   // Alt + 6
                case 6: switchPanel(new PKelStok()); break;     // Alt + 7
                case 7: switchPanel(new PKelUser()); break;     // Alt + 8
            }
        } else {
            // --- MENU TEKNISI ---
            // Teknisi cuma punya 2 menu saat ini, batasi index
            if (index > 1) return;
            
            setPanelIndex(index);
            switch (index) {
                case 0: switchPanel(new Teknisi()); break;       // Alt + 1 (Daftar Servis Masuk)
                case 1: switchPanel(new DafServisAnda()); break; // Alt + 2 (Tugas Saya)
            }
        }
    }

    public void setPanelIndex(int index) {
        this.currentPanelIndex = index;
    }

    public void switchPanel(javax.swing.JPanel panel) {
        pMain.removeAll();
        pMain.add(panel);
        pMain.repaint();
        pMain.revalidate();
    }
    
    public void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Login().setVisible(true);
            this.dispose();
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

        pNav = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblTanggal = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pSide = new javax.swing.JPanel();
        pMain = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1920, 1080));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pNav.setBackground(new java.awt.Color(4, 102, 200));
        pNav.setForeground(new java.awt.Color(0, 24, 69));
        pNav.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(255, 255, 255));
        lblWelcome.setText("Welcome");
        pNav.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(1510, 30, -1, -1));

        lblTanggal.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        lblTanggal.setForeground(new java.awt.Color(255, 255, 255));
        lblTanggal.setText("Tanggal");
        pNav.add(lblTanggal, new org.netbeans.lib.awtextra.AbsoluteConstraints(1510, 60, -1, -1));

        jLabel2.setForeground(new java.awt.Color(0, 24, 69));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ant.png"))); // NOI18N
        pNav.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 396, 120));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 48)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        pNav.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(639, 31, 42, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Logotim.png"))); // NOI18N
        pNav.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 210, 120));

        getContentPane().add(pNav, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 120));

        pSide.setBackground(new java.awt.Color(204, 204, 204));
        pSide.setForeground(new java.awt.Color(204, 204, 204));
        pSide.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(pSide, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, 200, 960));

        pMain.setLayout(new java.awt.BorderLayout());
        getContentPane().add(pMain, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 120, 1720, 960));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
   public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        // PERBAIKAN ERROR: Menambahkan parameter kedua pada inisialisasi Dashboard
        java.awt.EventQueue.invokeLater(() -> new Dashboard("", "").setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pMain;
    private javax.swing.JPanel pNav;
    private javax.swing.JPanel pSide;
    // End of variables declaration//GEN-END:variables
}
