/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mscompapp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
/**
 *
 * @author ASUS
 */

public class Dashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    private String userRole;
    private int currentPanelIndex = 0;
    private static final double BASE_WIDTH = 1366.0;
    private static final double BASE_HEIGHT = 768.0;
    private double currentScale = 1.0;

    public Dashboard(String username, String role) {
        this.userRole = role;
        Login.namaUser = username;        
        initComponents();
        lblWelcome.setText("Selamat Datang, " + username);
        calculateScale();
        applyScaling();
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
    
    private void calculateScale() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    double scaleX = screen.getWidth() / BASE_WIDTH;
    double scaleY = screen.getHeight() / BASE_HEIGHT;

    currentScale = Math.min(scaleX, scaleY);
    currentScale = Math.max(1.0, Math.min(currentScale, 1.6));
}
    
    
    private void applyScaling() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    double scaleX = screen.getWidth() / BASE_WIDTH;
    double scaleY = screen.getHeight() / BASE_HEIGHT;
    double scale = Math.min(scaleX, scaleY);

    scale = Math.max(1.0, Math.min(scale, 1.6));

    scaleFont(getContentPane(), scale);
    scaleTables(getContentPane(), scale);
}
    
    private void scaleTables(Component comp, double scale) {
    if (comp instanceof JTable table) {
        table.setRowHeight((int)(table.getRowHeight() * scale));
        table.getTableHeader().setFont(
            table.getTableHeader().getFont().deriveFont(
                (float)(table.getTableHeader().getFont().getSize() * scale)
            )
        );
    }

    if (comp instanceof Container container) {
        for (Component child : container.getComponents()) {
            scaleTables(child, scale);
        }
    }
}
    
    private void scaleFont(Component comp, double scale) {
    if (comp.getFont() != null) {
        Font oldFont = comp.getFont();
        float newSize = (float) (oldFont.getSize() * scale);
        comp.setFont(oldFont.deriveFont(newSize));
    }

    if (comp instanceof Container) {
        for (Component child : ((Container) comp).getComponents()) {
            scaleFont(child, scale);
        }
    }
}
    
    private void adjustResponsiveLayout() {
    this.getContentPane().setLayout(new java.awt.BorderLayout());

    // Berikan lebar tetap (misal 200), tapi tinggi biarkan 0 (akan diatur BorderLayout)
    pSide.setPreferredSize(new java.awt.Dimension(200, 0)); 
    
    // Pastikan pSide menggunakan layout yang mendukung komponen di dalamnya memenuhi ruang
    pSide.setLayout(new java.awt.BorderLayout()); 

    this.getContentPane().add(pNav, java.awt.BorderLayout.NORTH);
    this.getContentPane().add(pSide, java.awt.BorderLayout.WEST);
    this.getContentPane().add(pMain, java.awt.BorderLayout.CENTER);

    pMain.setLayout(new java.awt.BorderLayout());
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
        for (int i = 1; i <= 10; i++) {
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
            if (index > 9) return; 
            
            setPanelIndex(index);
            switch (index) {
                case 0: switchPanel(new Beranda()); break;      // Alt + 1
                case 1: switchPanel(new PKelService()); break;  // Alt + 2
                case 2: switchPanel(new PKelLaporan()); break;  // Alt + 3
                case 3: switchPanel(new PKelUser()); break;   // Alt + 4
                case 4: switchPanel(new PKelBarang()); break;  // Alt + 5
                case 5: switchPanel(new PKatService()); break;   // Alt + 6
                case 6: switchPanel(new PKatBarang()); break;     // Alt + 7
                case 7: switchPanel(new PKelStok()); break; 
                case 8: switchPanel(new PKelPelanggan()); break;     // Alt + 7
                case 9: switchPanel(new Pengaturan()); break; // Alt + 8
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
    // Gunakan BorderLayout agar panel yang baru masuk otomatis memenuhi seluruh pMain
    pMain.setLayout(new java.awt.BorderLayout());
    pMain.add(panel, java.awt.BorderLayout.CENTER);
    pMain.repaint();
    pMain.revalidate();

// 🔥 Terapkan scaling ke panel baru
    scaleFont(panel, currentScale);
    scaleTables(panel, currentScale);
    
    
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
        java.awt.GridBagConstraints gridBagConstraints;

        pNav = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblTanggal = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        pSide = new javax.swing.JPanel();
        pMain = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1280, 720));
        setPreferredSize(new java.awt.Dimension(1280, 720));
        setResizable(false);

        pNav.setBackground(new java.awt.Color(4, 102, 200));
        pNav.setForeground(new java.awt.Color(0, 24, 69));
        pNav.setLayout(new java.awt.BorderLayout());

        jPanel1.setToolTipText("");
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        lblWelcome.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 14)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(255, 255, 255));
        lblWelcome.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblWelcome.setText("Welcome");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 339, 0, 276);
        jPanel1.add(lblWelcome, gridBagConstraints);

        lblTanggal.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 14)); // NOI18N
        lblTanggal.setForeground(new java.awt.Color(255, 255, 255));
        lblTanggal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTanggal.setText("Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 339, 0, 0);
        jPanel1.add(lblTanggal, gridBagConstraints);

        pNav.add(jPanel1, java.awt.BorderLayout.EAST);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jLabel2.setForeground(new java.awt.Color(0, 24, 69));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo ms.png"))); // NOI18N
        jPanel2.add(jLabel2);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Logotim.png"))); // NOI18N
        jPanel2.add(jLabel1);

        pNav.add(jPanel2, java.awt.BorderLayout.WEST);

        getContentPane().add(pNav, java.awt.BorderLayout.NORTH);

        pSide.setBackground(new java.awt.Color(204, 204, 204));
        pSide.setForeground(new java.awt.Color(204, 204, 204));
        pSide.setLayout(new java.awt.GridLayout());
        getContentPane().add(pSide, java.awt.BorderLayout.WEST);

        pMain.setLayout(new java.awt.GridBagLayout());
        getContentPane().add(pMain, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pMain;
    private javax.swing.JPanel pNav;
    private javax.swing.JPanel pSide;
    // End of variables declaration//GEN-END:variables
}
