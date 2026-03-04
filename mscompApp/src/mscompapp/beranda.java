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
public class beranda extends javax.swing.JPanel {

    /**
     * Creates new form beranda
     */
    public beranda() {
        initComponents();

        // PANGGIL SEMUA FUNGSI DATA DISINI
        java.awt.Color abuAbu = new java.awt.Color(204, 204, 204);
        buatPanelMelengkung(jPanel1, abuAbu);
        buatPanelMelengkung(jPanel4, abuAbu);
        buatPanelMelengkung(jPanel5, abuAbu);
        buatPanelMelengkung(jPanel6, abuAbu);
        buatPanelMelengkung(jPanel7, abuAbu);
        buatPanelMelengkung(jPanel8, abuAbu);
        buatPanelMelengkung(jPanel9, abuAbu);
        buatPanelMelengkung(jPanel11, abuAbu);
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
        // Memberikan kursor tangan agar user tahu ini bisa diklik
        sourcePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sourcePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Berubah jadi sedikit lebih terang saat disentuh kursor
                sourcePanel.setBackground(new Color(230, 230, 230));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Kembali ke warna asli (abu-abu terang sesuai desain Anda)
                sourcePanel.setBackground(new Color(204, 204, 204));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Mengambil instance Dashboard untuk memanggil fungsi switchPanel
                Dashboard dash = (Dashboard) SwingUtilities.getWindowAncestor(sourcePanel);
                if (dash != null) {
                    dash.setPanelIndex(dashboardIndex); // Update index shortcut agar sinkron
                    dash.switchPanel(targetPanel);      // Pindah ke halaman tujuan
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
            String sqlBulanan = "SELECT SUM(harga) FROM servis WHERE status='Selesai' "
                    + "AND MONTH(tanggal_masuk) = MONTH(CURDATE()) "
                    + "AND YEAR(tanggal_masuk) = YEAR(CURDATE())";
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

    private void buatPanelMelengkung(javax.swing.JPanel panel, java.awt.Color warnaBack) {
        panel.setOpaque(false); // Penting agar sudut kotak asli tidak terlihat
        panel.setBorder(new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // 1. Mewarnai isi panel dengan abu-abu
                g2.setColor(warnaBack);
                g2.fillRoundRect(x, y, width - 1, height - 1, 30, 30);

                // 2. Menggambar garis pinggir (stroke) abu-abu yang lebih tegas
                g2.setColor(new java.awt.Color(180, 180, 180));
                g2.drawRoundRect(x, y, width - 1, height - 1, 30, 30);

                g2.dispose();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

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
    jPanel2 = new javax.swing.JPanel();

    setBackground(new java.awt.Color(255, 255, 255));
    setLayout(new java.awt.GridBagLayout());

    jPanel3.setBackground(new java.awt.Color(255, 255, 255));
    jPanel3.setLayout(new java.awt.GridBagLayout());
    // PreferredSize dihapus agar LayoutManager bisa menghitung ukuran otomatis

    // --- KARTU 1: Jumlah User ---
    jPanel1.setBackground(new java.awt.Color(204, 204, 204));
    jPanel1.setOpaque(false);
    jPanel1.setLayout(new java.awt.GridBagLayout());
    
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/group.png")));
    jLabel1.setText("Jumlah User");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel1.add(jLabel1, gridBagConstraints);

    lblTotalUser.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblTotalUser.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblTotalUser.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel1.add(lblTotalUser, gridBagConstraints);

    // Tambah Kartu 1 ke Panel Utama
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel1, gridBagConstraints);

    // --- KARTU 2: Jumlah Barang ---
    jPanel4.setBackground(new java.awt.Color(204, 204, 204));
    jPanel4.setOpaque(false);
    jPanel4.setLayout(new java.awt.GridBagLayout());

    jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/package-box.png")));
    jLabel3.setText("Jumlah Barang");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel4.add(jLabel3, gridBagConstraints);

    lblTotalBarang.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblTotalBarang.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblTotalBarang.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel4.add(lblTotalBarang, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel4, gridBagConstraints);

    // --- KARTU 3: Jumlah Service ---
    jPanel5.setOpaque(false);
    jPanel5.setLayout(new java.awt.GridBagLayout());

    jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/service.png")));
    jLabel4.setText("Jumlah Service");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel5.add(jLabel4, gridBagConstraints);

    lblTotalService.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblTotalService.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblTotalService.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel5.add(lblTotalService, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel5, gridBagConstraints);

    // --- KARTU 4: Total Stock ---
    jPanel6.setOpaque(false);
    jPanel6.setLayout(new java.awt.GridBagLayout());

    jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/report.png")));
    jLabel5.setText("Total Stock Barang");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel6.add(jLabel5, gridBagConstraints);

    lblTotalStock.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblTotalStock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblTotalStock.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel6.add(lblTotalStock, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel6, gridBagConstraints);

    // --- BARIS 2 ---
    // --- KARTU 5: Pendapatan Harian ---
    jPanel7.setOpaque(false);
    jPanel7.setLayout(new java.awt.GridBagLayout());
    jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/wallet.png")));
    jLabel6.setText("Pendapatan Harian");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel7.add(jLabel6, gridBagConstraints);

    lblPenHarian.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblPenHarian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblPenHarian.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel7.add(lblPenHarian, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel7, gridBagConstraints);

    // --- KARTU 6: Pendapatan Bulanan ---
    jPanel8.setOpaque(false);
    jPanel8.setLayout(new java.awt.GridBagLayout());
    jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/money.png")));
    jLabel7.setText("Pendapatan Bulanan");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel8.add(jLabel7, gridBagConstraints);

    lblPenBulanan.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblPenBulanan.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblPenBulanan.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel8.add(lblPenBulanan, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel8, gridBagConstraints);

    // --- KARTU 7: Service Selesai ---
    jPanel9.setOpaque(false);
    jPanel9.setLayout(new java.awt.GridBagLayout());
    jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/result.png")));
    jLabel8.setText("Service Selesai");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel9.add(jLabel8, gridBagConstraints);

    lblSerSelesai.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblSerSelesai.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblSerSelesai.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel9.add(lblSerSelesai, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel9, gridBagConstraints);

    // --- KARTU 8: Service Pending ---
    jPanel11.setOpaque(false);
    jPanel11.setLayout(new java.awt.GridBagLayout());
    pppp.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    pppp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    pppp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/time-management.png")));
    pppp.setText("Service Pending");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
    jPanel11.add(pppp, gridBagConstraints);

    lblSerProses.setFont(new java.awt.Font("Segoe UI Historic", 1, 14));
    lblSerProses.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblSerProses.setText("0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
    jPanel11.add(lblSerProses, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
    jPanel3.add(jPanel11, gridBagConstraints);

    // --- PANEL PENGISI (Agar elemen tetap di atas) ---
    jPanel2.setOpaque(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0; // Ini akan menarik semua kartu ke atas
    jPanel3.add(jPanel2, gridBagConstraints);

    // --- TERAKHIR: Tambahkan jPanel3 ke Container Utama ---
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel3, gridBagConstraints);
}

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
