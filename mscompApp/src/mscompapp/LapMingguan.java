/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
/**
 *
 * @author Acer Aspire Lite 15
 */
public class LapMingguan extends javax.swing.JPanel {

    /**
     * Creates new form LapMingguan
     */
    private boolean isUpdatingDate = false;
    
    // Referensi ke Induk (PKelLaporan)
    private PKelLaporan parent;

    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    public LapMingguan(PKelLaporan parent) {
        this.parent = parent; 
        initComponents();
        loadComboStatus();
        loadComboKategori();
        
        // Custom Renderer (Warna)
        tblLapMingguan = new javax.swing.JTable() {
            { setRowHeight(30); getTableHeader().setReorderingAllowed(false); }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
                Object statusValue = getValueAt(row, 11); 
                if (statusValue != null) {
                    String status = statusValue.toString();
                    if (isRowSelected(row)) comp.setBackground(getSelectionBackground());
                    else {
                        switch (status) {
                            case "Proses": comp.setBackground(java.awt.Color.YELLOW); break;
                            case "Selesai": comp.setBackground(new java.awt.Color(144, 238, 144)); break;
                            case "Dibatalkan": comp.setBackground(new java.awt.Color(255, 182, 193)); break;
                            default: comp.setBackground(java.awt.Color.WHITE); break;
                        }
                    }
                }
                return comp;
            }
        };
        jScrollPane1.setViewportView(tblLapMingguan);
        
        resetTanggalMingguan();
        setupListeners();
        tampilData();
    }
    
    private void resetTanggalMingguan() {
        isUpdatingDate = true;
        Calendar cal = Calendar.getInstance();
        tglAkhir.setDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -7);
        tglAwal.setDate(cal.getTime());
        isUpdatingDate = false;
    }
    
    private void loadComboKategori() {
    cbKategori.removeAllItems(); cbKategori.addItem("- Semua Kategori -");
        try {
            ResultSet rs = Koneksi.configDB().createStatement().executeQuery("SELECT nama_jenis FROM tbl_jenis_perangkat");
            while (rs.next()) cbKategori.addItem(rs.getString("nama_jenis"));
        } catch (Exception e) {}
}
    
    private void setupListeners() {
        tglAwal.addPropertyChangeListener("date", e -> {
            if (isUpdatingDate || tglAwal.getDate() == null) return;
            isUpdatingDate = true;
            Calendar cal = Calendar.getInstance();
            cal.setTime(tglAwal.getDate()); cal.add(Calendar.DAY_OF_MONTH, 7);
            tglAkhir.setDate(cal.getTime());
            isUpdatingDate = false;
            currentPage = 0; tampilData(); 
        });
        
        tglAkhir.addPropertyChangeListener("date", e -> {
            if (isUpdatingDate || tglAkhir.getDate() == null) return;
            isUpdatingDate = true;
            Calendar cal = Calendar.getInstance();
            cal.setTime(tglAkhir.getDate()); cal.add(Calendar.DAY_OF_MONTH, -7);
            tglAwal.setDate(cal.getTime());
            isUpdatingDate = false;
            currentPage = 0; tampilData();
        });
        
        cbStatus.addActionListener(e -> { currentPage = 0; tampilData(); });
        cbKategori.addActionListener(e -> { currentPage = 0; tampilData(); });
    }
    
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
        cbStatus.setSelectedIndex(0);
    }

    private void tampilData() {
        DefaultTableModel model = new DefaultTableModel(){ @Override public boolean isCellEditable(int r, int c) { return false; } };
        model.addColumn("No"); model.addColumn("ID Servis"); model.addColumn("Tanggal Masuk"); model.addColumn("Nama");
        model.addColumn("Nomor HP"); model.addColumn("Alamat"); model.addColumn("Jenis Barang"); model.addColumn("Merek");
        model.addColumn("Keluhan"); model.addColumn("Kelengkapan"); model.addColumn("Total Biaya"); model.addColumn("Status"); model.addColumn("Status Barang");

        try {
            String where = "WHERE 1=1 ";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (tglAwal.getDate() != null && tglAkhir.getDate() != null) {
                where += "AND s.tanggal_masuk BETWEEN '" + sdf.format(tglAwal.getDate()) + "' AND '" + sdf.format(tglAkhir.getDate()) + "' ";
            }
            if (!tfCari.getText().isEmpty()) {
                where += "AND (p.nama_pelanggan LIKE '%"+tfCari.getText()+"%' OR s.id_servis LIKE '%"+tfCari.getText()+"%') ";
            }
            if (cbStatus.getSelectedIndex() > 0) where += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            if (cbKategori.getSelectedIndex() > 0) where += "AND s.jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";

            // Count
            Connection conn = Koneksi.configDB();
            ResultSet rsC = conn.createStatement().executeQuery("SELECT COUNT(*) AS total FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " + where);
            int totalData = rsC.next() ? rsC.getInt("total") : 0;

            // Select with Limit
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " 
                       + where + "ORDER BY s.tanggal_masuk DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            DecimalFormat df = new DecimalFormat("#,###");
            int no = offset + 1;
            while (rs.next()) {
                model.addRow(new Object[]{ no++, rs.getString("id_servis"), rs.getString("tanggal_masuk"), rs.getString("nama_pelanggan"), rs.getString("no_hp"), rs.getString("alamat"), rs.getString("jenis_barang"), rs.getString("merek"), rs.getString("keluhan_awal"), rs.getString("kelengkapan"), "Rp " + df.format(rs.getDouble("harga")), rs.getString("status"), rs.getString("status_barang") });
            }
            tblLapMingguan.setModel(model);

            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) { e.printStackTrace(); }
        hitungDanKirimPendapatan();
    }
    
    private void bukaHalamanDetail(String idServis) {
        try {
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat "
                       + "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + "WHERE s.id_servis = '" + idServis + "'";
            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                DetailService ds = new DetailService(
                    rs.getString("id_servis"), rs.getString("tanggal_masuk"), rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"), rs.getString("alamat"), rs.getString("jenis_barang"),
                    rs.getString("merek"), rs.getString("model"), rs.getString("no_seri"),
                    rs.getString("keluhan_awal"), rs.getString("kelengkapan"), rs.getString("status"), rs.getString("status_barang"), Session.idUser
                );
                
                // --- LISTENER SAAT POPUP DITUTUP (AUTO REFRESH) ---
                ds.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        tampilData(); // Refresh Tabel Mingguan
                        
                    }
                });
                
                ds.setLocationRelativeTo(null);
                ds.setVisible(true);
            }
        } catch (Exception e) {}
    }
    
    private void hitungDanKirimPendapatan() {
        if (parent == null) return;
        
        try {
            String sql = "SELECT SUM(harga) AS total FROM servis WHERE status='Selesai' ";
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String start = "", end = "";
            
            if (tglAwal.getDate() != null && tglAkhir.getDate() != null) {
                start = sdf.format(tglAwal.getDate());
                end = sdf.format(tglAkhir.getDate());
                sql += "AND tanggal_masuk BETWEEN '" + start + "' AND '" + end + "' ";
            }
            
            if (cbKategori.getSelectedIndex() > 0) {
                sql += "AND jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            
            // Format Judul
            SimpleDateFormat sdfView = new SimpleDateFormat("dd MMM yyyy");
            String startV = (tglAwal.getDate() != null) ? sdfView.format(tglAwal.getDate()) : "-";
            String endV = (tglAkhir.getDate() != null) ? sdfView.format(tglAkhir.getDate()) : "-";
            
            // KIRIM KE PARENT
            parent.setInfoPendapatan("Pendapatan Periode " + startV + " s/d " + endV + " :", total);
            
        } catch (Exception e) {
            System.out.println("Err Mingguan: " + e.getMessage());
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

        tfCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        btnDetail = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLapMingguan = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        tglAwal = new com.toedter.calendar.JDateChooser();
        tglAkhir = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        btnNota = new javax.swing.JButton();
        btnPdf = new javax.swing.JButton();
        btCetakE = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1680, 760));
        setMinimumSize(new java.awt.Dimension(1680, 760));
        setPreferredSize(new java.awt.Dimension(1680, 760));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 20, 246, 50));

        btnCari.setBackground(new java.awt.Color(102, 255, 102));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 18, -1, 50));

        btnDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDetail.setText("DETAIL");
        btnDetail.addActionListener(this::btnDetailActionPerformed);
        add(btnDetail, new org.netbeans.lib.awtextra.AbsoluteConstraints(386, 18, -1, 50));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(484, 18, -1, 50));

        tblLapMingguan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblLapMingguan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Tanggal", "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan", "Status"
            }
        ));
        tblLapMingguan.setRowHeight(35);
        jScrollPane1.setViewportView(tblLapMingguan);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 88, 1682, 525));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Tanggal S/D :");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, -1, 50));
        add(tglAwal, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 20, 123, 50));
        add(tglAkhir, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 20, 126, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Status :");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1500, 20, -1, 50));

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbStatus.addActionListener(this::cbStatusActionPerformed);
        add(cbStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1550, 20, 154, 50));

        btnNota.setBackground(new java.awt.Color(102, 255, 102));
        btnNota.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNota.setText("Nota");
        btnNota.addActionListener(this::btnNotaActionPerformed);
        add(btnNota, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 10, -1, 63));

        btnPdf.setBackground(new java.awt.Color(204, 204, 204));
        btnPdf.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPdf.setText("PDF");
        btnPdf.addActionListener(this::btnPdfActionPerformed);
        add(btnPdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 10, -1, 51));

        btCetakE.setBackground(new java.awt.Color(204, 204, 204));
        btCetakE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btCetakE.setText("Cetak Excel");
        btCetakE.addActionListener(this::btCetakEActionPerformed);
        add(btCetakE, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 10, -1, 51));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Kategori :");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1290, 20, -1, 50));

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addItemListener(this::cbKategoriItemStateChanged);
        cbKategori.addActionListener(this::cbKategoriActionPerformed);
        add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(1360, 20, 132, 50));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(1476, Short.MAX_VALUE)
                .addComponent(btnNextKiri)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextKanan)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNextKanan, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                    .addComponent(btnNextKiri, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 1710, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        currentPage = 0; tampilData();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText(""); cbStatus.setSelectedIndex(0); resetTanggalMingguan(); currentPage = 0; tampilData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        int row = tblLapMingguan.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis terlebih dahulu!");
            return;
        }
        String idServis = tblLapMingguan.getValueAt(row, 1).toString();
        bukaHalamanDetail(idServis);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btCetakEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCetakEActionPerformed
        // TODO add your handling code here:
        if (tglAwal.getDate() == null || tglAkhir.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih rentang tanggal terlebih dahulu!");
            return;
        }

        // Format tanggal untuk Judul
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String strAwal = sdf.format(tglAwal.getDate());
        String strAkhir = sdf.format(tglAkhir.getDate());
        
        // Buat Judul Custom
        String judul = "LAPORAN SERVIS PERIODE " + strAwal + " s/d " + strAkhir;
        
        // Buat nama file suffix (agar nama file unik)
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Mingguan_" + sdfFile.format(tglAwal.getDate()) + "_sd_" + sdfFile.format(tglAkhir.getDate());
        
        // Panggil Method Export Excel Custom
        ExportExcel.exportJTableToExcelCustom(tblLapMingguan, judul, suffix);
    }//GEN-LAST:event_btCetakEActionPerformed

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfActionPerformed
        // TODO add your handling code here:
        if (tglAwal.getDate() == null || tglAkhir.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih rentang tanggal terlebih dahulu!");
            return;
        }

        // Format tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String strAwal = sdf.format(tglAwal.getDate());
        String strAkhir = sdf.format(tglAkhir.getDate());
        
        // Judul & Status
        String judul = "LAPORAN SERVIS PERIODE " + strAwal + " s/d " + strAkhir;
        String statusFilter = "Status: " + cbStatus.getSelectedItem().toString();
        
        // Suffix filename
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Mingguan_" + sdfFile.format(tglAwal.getDate()) + "_sd_" + sdfFile.format(tglAkhir.getDate());
        
        // Panggil Method Export PDF Custom
        ExportPDF.exportToPDFCustom(tblLapMingguan, judul, statusFilter, suffix);
    }//GEN-LAST:event_btnPdfActionPerformed

    private void btnNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotaActionPerformed
        // TODO add your handling code here:
        int row = tblLapMingguan.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis yang ingin dicetak notanya!");
            return;
        }
        
        // Ambil ID Servis (Pastikan kolom ke-1 di tabel adalah ID Servis)
        String idServis = tblLapMingguan.getValueAt(row, 1).toString();
        
        // Panggil CetakStruk
        CetakStruk.cetakStruk(idServis, Session.idUser);
    }//GEN-LAST:event_btnNotaActionPerformed

    private void cbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKategoriItemStateChanged
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_cbKategoriItemStateChanged

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) { currentPage--; tampilData(); }
    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++; tampilData();
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void cbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbStatusActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCetakE;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btnNota;
    private javax.swing.JButton btnPdf;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblLapMingguan;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglAkhir;
    private com.toedter.calendar.JDateChooser tglAwal;
    // End of variables declaration//GEN-END:variables
}
