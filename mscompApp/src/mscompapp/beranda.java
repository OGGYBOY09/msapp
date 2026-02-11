/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;


import config.Koneksi;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 *
 * @author Acer Aspire Lite 15
 */
public class Beranda extends javax.swing.JPanel {

    /**
     * Creates new form beranda
     */
    public Beranda() {
        initComponents();
        
        // PANGGIL SEMUA FUNGSI DATA DISINI
        tampilTotalUser();
        tampilTotalBarang();
        tampilTotalService(); // Perbaikan: Ditambahkan kembali
        tampilTotalStock();   // Perbaikan: Ditambahkan kembali
        
        tampilPendapatan();
        tampilStatusServis();
        
        initNavigation();
    }
    
    private void initNavigation() {
        // Mapping: Panel -> Target Halaman (dan index shortcut navigasi)
        
        // 1. Jumlah User -> Kelola User
        setupPanelAction(jPanel1, new PKelUser(), 7);
        
        // 2. Jumlah Barang -> Kelola Barang
        setupPanelAction(jPanel4, new PKelBarang(), 3);
        
        // 3. Jumlah Service -> Kelola Laporan (Sesuai request)
        setupPanelAction(jPanel5, new PKelLaporan(), 1);
        
        // 4. Total Stock -> Kelola Barang (Sesuai request)
        setupPanelAction(jPanel6, new PKelBarang(), 3);
        
        // 5. Pendapatan Harian -> Kelola Laporan
        setupPanelAction(jPanel7, new PKelLaporan(), 1);
        
        // 6. Pendapatan Bulanan -> Kelola Laporan
        setupPanelAction(jPanel8, new PKelLaporan(), 1);
        
        // 7. Servis Selesai -> Kelola Laporan
        setupPanelAction(jPanel9, new PKelLaporan(), 1);
        
        // 8. Servis Proses -> Kelola Laporan
        setupPanelAction(jPanel11, new PKelLaporan(), 1);
    }
    
    private void setupPanelAction(JPanel sourcePanel, JPanel targetPanel, int dashboardIndex) {
        // Efek Hover (Visual)
        sourcePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        sourcePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Mencari Parent Dashboard secara otomatis
                Dashboard dash = (Dashboard) SwingUtilities.getWindowAncestor(sourcePanel);
                
                if (dash != null) {
                    // Pindah panel
                    dash.switchPanel(targetPanel);
                    // Update index agar shortcut Alt+Angka tetap sinkron
                    dash.setPanelIndex(dashboardIndex); 
                }
            }

            // Efek Hover: Ubah warna sedikit saat mouse masuk
            @Override
            public void mouseEntered(MouseEvent e) {
                sourcePanel.setBackground(new Color(220, 220, 220)); // Sedikit lebih gelap dari putih/abu
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Kembalikan ke warna asal (sesuaikan dengan warna di desainmu)
                // jPanel1-11 di desainmu sepertinya menggunakan warna background yang berbeda-beda
                // Logika di bawah untuk mengembalikan warna default panel masing-masing
                if (sourcePanel == jPanel2) {
                     sourcePanel.setBackground(new Color(4, 102, 200));
                } else if (sourcePanel == jPanel3) {
                     sourcePanel.setBackground(new Color(255, 255, 255));
                } else {
                     sourcePanel.setBackground(new Color(204, 204, 204)); // Warna default panel kartu
                }
            }
        });
    }
    
    // --- HELPER: FORMAT ANGKA (Titik per 3 nol) ---
    private String formatRupiah(int angka) {
        DecimalFormat df = new DecimalFormat("#,###");
        return "Rp " + df.format(angka);
    }

    // --- 1. PENDAPATAN HARIAN & BULANAN ---
    private void tampilPendapatan() {
        try {
            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            
            // Harian
            String sqlHarian = "SELECT SUM(harga) FROM servis WHERE status='Selesai' AND DATE(tanggal_masuk) = CURDATE()";
            ResultSet resHarian = stm.executeQuery(sqlHarian);
            if (resHarian.next()) {
                int totalHarian = resHarian.getInt(1);
                lblPenHarian.setText(formatRupiah(totalHarian));
            }

            // Bulanan
            String sqlBulanan = "SELECT SUM(harga) FROM servis WHERE status='Selesai' " +
                                "AND MONTH(tanggal_masuk) = MONTH(CURDATE()) " +
                                "AND YEAR(tanggal_masuk) = YEAR(CURDATE())";
            ResultSet resBulanan = stm.executeQuery(sqlBulanan);
            if (resBulanan.next()) {
                int totalBulanan = resBulanan.getInt(1);
                lblPenBulanan.setText(formatRupiah(totalBulanan));
            }
            
        } catch (Exception e) {
            System.out.println("Error Pendapatan: " + e.getMessage());
        }
    }

    // --- 2. STATUS SERVIS (Selesai vs Proses) ---
    private void tampilStatusServis() {
        try {
            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            
            // Selesai
            String sqlSelesai = "SELECT COUNT(*) FROM servis WHERE status='Selesai'";
            ResultSet resSelesai = stm.executeQuery(sqlSelesai);
            if (resSelesai.next()) {
                lblSerSelesai.setText(resSelesai.getString(1));
            }
            
            // Proses (Menunggu + Proses)
            String sqlProses = "SELECT COUNT(*) FROM servis WHERE status IN ('Menunggu', 'Proses')";
            ResultSet resProses = stm.executeQuery(sqlProses);
            if (resProses.next()) {
                lblSerProses.setText(resProses.getString(1));
            }
            
        } catch (Exception e) {
            System.out.println("Error Status Servis: " + e.getMessage());
        }
    }

    // --- 3. TOTAL SERVIS (Semua Riwayat) ---
    private void tampilTotalService() {
        try {
            String sql = "SELECT COUNT(*) FROM servis";
            Connection conn = Koneksi.configDB();
            ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                lblTotalService.setText(res.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Error Total Service: " + e.getMessage());
        }
    }

    // --- 4. TOTAL STOK BARANG (Sum Stok) ---
    private void tampilTotalStock() {
        try {
            // Menggunakan SUM agar yang dihitung jumlah stoknya, bukan jumlah barisnya
            String sql = "SELECT SUM(stok) FROM tbl_barang";
            Connection conn = Koneksi.configDB();
            ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                String total = res.getString(1);
                // Cek null jika tabel kosong
                if (total == null) {
                    lblTotalStock.setText("0");
                } else {
                    lblTotalStock.setText(total);
                }
            }
        } catch (Exception e) {
            System.out.println("Error Total Stock: " + e.getMessage());
        }
    }

    // --- 5. TOTAL DATA LAINNYA ---
    private void tampilTotalUser() {
        try {
            String sql = "SELECT COUNT(*) FROM tbl_user";
            Connection conn = Koneksi.configDB();
            ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                lblTotalUser.setText(res.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Error User: " + e.getMessage());
        }
    }
    
    private void tampilTotalBarang() {
        try {
            String sql = "SELECT COUNT(*) FROM tbl_barang";
            Connection conn = Koneksi.configDB();
            ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                lblTotalBarang.setText(res.getString(1));
            }
        } catch (Exception e) {
            System.out.println("Error Barang: " + e.getMessage());
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

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTotalUser = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblTotalBarang = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        lblTotalService = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblTotalStock = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        lblPenHarian = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        lblPenBulanan = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        lblSerSelesai = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        pppp = new javax.swing.JLabel();
        lblSerProses = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/group.png"))); // NOI18N
        jLabel1.setText("Jumlah User");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblTotalUser.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblTotalUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalUser.setText("0");
        lblTotalUser.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel1.add(lblTotalUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, 150, 40));

        jPanel3.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 400, 200));

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/package-box.png"))); // NOI18N
        jLabel3.setText("Jumlah Barang");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblTotalBarang.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblTotalBarang.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalBarang.setText("0");
        jPanel4.add(lblTotalBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, 150, 40));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, 400, 200));

        jPanel5.setBackground(new java.awt.Color(204, 204, 204));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/service.png"))); // NOI18N
        jLabel4.setText("Jumlah Service");
        jPanel5.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblTotalService.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblTotalService.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalService.setText("0");
        jPanel5.add(lblTotalService, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 170, 40));

        jPanel3.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 20, 400, 200));

        jPanel6.setBackground(new java.awt.Color(204, 204, 204));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/report.png"))); // NOI18N
        jLabel5.setText("Total Stock Barang");
        jPanel6.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblTotalStock.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        lblTotalStock.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalStock.setText("0");
        lblTotalStock.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel6.add(lblTotalStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, 150, 40));

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1280, 20, 400, 200));

        jPanel7.setBackground(new java.awt.Color(204, 204, 204));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel7.setMaximumSize(new java.awt.Dimension(396, 196));
        jPanel7.setMinimumSize(new java.awt.Dimension(396, 196));
        jPanel7.setPreferredSize(new java.awt.Dimension(396, 196));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/wallet.png"))); // NOI18N
        jLabel6.setText("Pendapatan Harian");
        jPanel7.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblPenHarian.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblPenHarian.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPenHarian.setText("0");
        jPanel7.add(lblPenHarian, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 130, 260, 40));

        jPanel3.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, 400, 200));

        jPanel8.setBackground(new java.awt.Color(204, 204, 204));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel8.setMaximumSize(new java.awt.Dimension(396, 196));
        jPanel8.setMinimumSize(new java.awt.Dimension(396, 196));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/money.png"))); // NOI18N
        jLabel7.setText("Pendapatan Bulanan");
        jPanel8.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblPenBulanan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblPenBulanan.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPenBulanan.setText("0");
        jPanel8.add(lblPenBulanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 130, 230, 40));

        jPanel3.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 250, 400, 200));

        jPanel9.setBackground(new java.awt.Color(204, 204, 204));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel9.setMaximumSize(new java.awt.Dimension(396, 196));
        jPanel9.setMinimumSize(new java.awt.Dimension(396, 196));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/result.png"))); // NOI18N
        jLabel8.setText("Service Selesai");
        jPanel9.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblSerSelesai.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSerSelesai.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSerSelesai.setText("0");
        lblSerSelesai.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel9.add(lblSerSelesai, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, 150, 40));

        jPanel3.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 250, 400, 200));

        jPanel11.setBackground(new java.awt.Color(204, 204, 204));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel11.setMaximumSize(new java.awt.Dimension(396, 196));
        jPanel11.setMinimumSize(new java.awt.Dimension(396, 196));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pppp.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        pppp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pppp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/time-management.png"))); // NOI18N
        pppp.setText("Service Pending");
        jPanel11.add(pppp, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 400, 50));

        lblSerProses.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSerProses.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSerProses.setText("0");
        lblSerProses.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel11.add(lblSerProses, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, 150, 40));

        jPanel3.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(1280, 250, 400, 200));

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, -3, 1718, 960));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1720, 960));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lblPenBulanan;
    private javax.swing.JLabel lblPenHarian;
    private javax.swing.JLabel lblSerProses;
    private javax.swing.JLabel lblSerSelesai;
    private javax.swing.JLabel lblTotalBarang;
    private javax.swing.JLabel lblTotalService;
    private javax.swing.JLabel lblTotalStock;
    private javax.swing.JLabel lblTotalUser;
    private javax.swing.JLabel pppp;
    // End of variables declaration//GEN-END:variables
}
