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
            aturKolomTabel();

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

    private void aturKolomTabel() {
    if (tblLapMingguan.getColumnCount() > 0) {
        int[] lebarMinimal = {50, 100, 130, 150, 120, 180, 120, 120, 200, 180, 120, 100, 180};
        int totalLebarMinimal = 1750; 

        int lebarWadah = jScrollPane1.getViewport().getWidth();

        if (lebarWadah > totalLebarMinimal) {
            tblLapMingguan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            tblLapMingguan.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        }

        for (int i = 0; i < tblLapMingguan.getColumnCount(); i++) {
            if (i < lebarMinimal.length) {
                javax.swing.table.TableColumn col = tblLapMingguan.getColumnModel().getColumn(i);
                col.setPreferredWidth(lebarMinimal[i]);
                col.setMinWidth(lebarMinimal[i]);
            }
        }
        
        tblLapMingguan.setFillsViewportHeight(true);
        
        javax.swing.table.DefaultTableCellRenderer headerRenderer = 
            (javax.swing.table.DefaultTableCellRenderer) tblLapMingguan.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
    }
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
    java.awt.GridBagConstraints gbc;

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
    btnNextKanan = new javax.swing.JButton();
    btnNextKiri = new javax.swing.JButton();

    setBackground(new java.awt.Color(255, 255, 255));
    setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    // GANTI KE GRIDBAGLAYOUT
    setLayout(new java.awt.GridBagLayout());

    // --- BARIS 1: TOMBOL AKSI & TANGGAL ---
    gbc = new java.awt.GridBagConstraints();
gbc.insets = new java.awt.Insets(10, 10, 5, 5);
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

// --- MEMPERPANJANG TF CARI ---
gbc.gridx = 0; 
gbc.gridy = 0; 
gbc.ipadx = 180; // Ubah dari 100 ke 180 atau lebih sesuai keinginan Anda
gbc.weightx = 0.5; // Memberikan bobot agar tfCari lebih fleksibel mengambil ruang kosong
add(tfCari, gbc);

// --- TOMBOL CARI ---
btnCari.setBackground(new java.awt.Color(102, 255, 102));
btnCari.setFont(new java.awt.Font("Segoe UI", 1, 12));
btnCari.setText("CARI [F2]");
btnCari.addActionListener(this::btnCariActionPerformed);
gbc.gridx = 1; 
gbc.ipadx = 0; 
gbc.weightx = 0; // Kembalikan ke 0 agar tombol tidak ikut memanjang secara berlebihan
add(btnCari, gbc);

    btnDetail.setBackground(new java.awt.Color(204, 204, 204));
    btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnDetail.setText("DETAIL");
    btnDetail.addActionListener(this::btnDetailActionPerformed);
    gbc.gridx = 2; add(btnDetail, gbc);

    btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
    btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnRefresh.setText("REFRESH [F3]");
    btnRefresh.addActionListener(this::btnRefreshActionPerformed);
    gbc.gridx = 3; add(btnRefresh, gbc);

    btCetakE.setBackground(new java.awt.Color(204, 204, 204));
    btCetakE.setText("Cetak Excel");
    btCetakE.addActionListener(this::btCetakEActionPerformed);
    gbc.gridx = 4; add(btCetakE, gbc);

    btnPdf.setBackground(new java.awt.Color(204, 204, 204));
    btnPdf.setText("PDF");
    btnPdf.addActionListener(this::btnPdfActionPerformed);
    gbc.gridx = 5; add(btnPdf, gbc);

    btnNota.setBackground(new java.awt.Color(102, 255, 102));
    btnNota.setText("Nota");
    btnNota.addActionListener(this::btnNotaActionPerformed);
    gbc.gridx = 6; add(btnNota, gbc);

    // --- BARIS 1: FILTER TANGGAL (SEJAJAR DENGAN COMBOBOX) ---
gbc = new java.awt.GridBagConstraints();
gbc.gridy = 0; // Baris pertama
gbc.insets = new java.awt.Insets(10, 5, 5, 5);
gbc.anchor = java.awt.GridBagConstraints.EAST;

// Label "Tanggal S/D :"
jLabel1.setText("Tanggal S/D :");
gbc.gridx = 7; 
gbc.weightx = 1.0; // Mendorong filter ke ujung kanan panel
add(jLabel1, gbc);

// Date Chooser Awal (Diperpanjang)
gbc.gridx = 8;
gbc.weightx = 0;
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; // Agar mau memanjang
gbc.ipadx = 60; // Menambah lebar internal agar kolom lebih panjang
add(tglAwal, gbc);

// Label pemisah (Opsional, jika ingin ada strip/tanda di tengah)
// Jika tidak ada jLabel baru, kita langsung ke tglAkhir

// Date Chooser Akhir (Diperpanjang & Sejajar Ujung)
gbc.gridx = 9;
gbc.gridwidth = 2; // Mengambil dua slot agar sejajar dengan posisi ComboBox Status
gbc.ipadx = 60; 
gbc.insets = new java.awt.Insets(10, 5, 5, 15); // Margin kanan 15 agar sejajar dengan ComboBox
add(tglAkhir, gbc);

    // --- BARIS 2: KATEGORI & STATUS ---
    gbc = new java.awt.GridBagConstraints();
    gbc.gridy = 1;
    gbc.insets = new java.awt.Insets(5, 5, 10, 5);
    gbc.anchor = java.awt.GridBagConstraints.EAST;

    jLabel4.setText("Kategori :");
    gbc.gridx = 7; add(jLabel4, gbc);

    gbc.gridx = 8; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    add(cbKategori, gbc);

    gbc.gridx = 9; gbc.fill = java.awt.GridBagConstraints.NONE;
    jLabel2.setText("Status :");
    add(jLabel2, gbc);

    gbc.gridx = 10; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gbc.insets = new java.awt.Insets(5, 5, 10, 15);
    add(cbStatus, gbc);

    // --- BARIS 3: TABEL (UTAMA) ---
    tblLapMingguan.setRowHeight(30);
    jScrollPane1.setViewportView(tblLapMingguan);
    jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.gridwidth = 11;
    gbc.fill = java.awt.GridBagConstraints.BOTH;
    gbc.weightx = 1.0; gbc.weighty = 1.0; 
    gbc.insets = new java.awt.Insets(0, 10, 10, 10);
    add(jScrollPane1, gbc);

    jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent evt) {
            aturKolomTabel();
        }
    });

    // --- BARIS 4: NAVIGASI ---
    javax.swing.JPanel pnlNav = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
    pnlNav.setOpaque(false);
    pnlNav.add(btnNextKiri);
    pnlNav.add(btnNextKanan);

    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 3;
    gbc.gridwidth = 11;
    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gbc.insets = new java.awt.Insets(0, 0, 10, 10);
    add(pnlNav, gbc);

    // Panggil pengaturan kolom awal
    javax.swing.SwingUtilities.invokeLater(() -> aturKolomTabel());
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
    
    String idServis = tblLapMingguan.getValueAt(row, 1).toString();
    
    // Dialog pilihan ukuran kertas
    Object[] options = {"Struk Kecil (Thermal)", "Nota Besar (A4)"};
    int choice = JOptionPane.showOptionDialog(this, 
            "Pilih ukuran kertas yang ingin digunakan:", 
            "Opsi Cetak Nota", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

    if (choice == JOptionPane.YES_OPTION) {
        // Panggil Struk Kecil yang sudah ada
        CetakStruk.cetakStruk(idServis, Session.idUser);
    } else if (choice == JOptionPane.NO_OPTION) {
        // Panggil Nota Besar yang baru dibuat
        CetakNotaBesar.cetakNotaA4(idServis, Session.idUser);
    }
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblLapMingguan;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglAkhir;
    private com.toedter.calendar.JDateChooser tglAwal;
    // End of variables declaration//GEN-END:variables
}
