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
public class LapHarian extends javax.swing.JPanel {

    /**
     * Creates new form LapHarian
     */
    private PKelLaporan parent;

    // --- UBAH CONSTRUCTOR MENERIMA PARENT ---
    public LapHarian(PKelLaporan parent) {
        this.parent = parent; // Simpan referensi
        initComponents();
        loadComboStatus();
        tglHarian.setDate(new Date());
        tampilData();
        addFilterListeners();
        loadComboKategori();
        aturWarnaBarisTabel();
        
    }
    
    private void aturWarnaBarisTabel() {
    // 1. Mengatur tinggi baris agar lebih lega
    tblLapHarian.setRowHeight(35); 
    
    // 2. Menerapkan logic pewarnaan cell (Renderer)
    tblLapHarian.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Mengambil komponen dasar (teks, font, dll)
            java.awt.Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Cek nilai status pada Kolom ke-11 (Ingat hitungan kolom mulai dari 0)
            // Di method tampilData(), kolom "Status" adalah kolom ke-12, jadi indexnya 11.
            Object statusObj = table.getValueAt(row, 11);
            String status = (statusObj != null) ? statusObj.toString() : "";

            // Logika Ganti Warna
            if (isSelected) {
                // Jika baris dipilih, gunakan warna seleksi biru (bawaan sistem)
                comp.setBackground(table.getSelectionBackground());
                comp.setForeground(table.getSelectionForeground());
            } else {
                // Jika tidak dipilih, warnai berdasarkan status
                switch (status) {
                    case "Proses":
                        comp.setBackground(new java.awt.Color(255, 255, 204)); // Kuning Kalem
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Selesai":
                        comp.setBackground(new java.awt.Color(204, 255, 204)); // Hijau Kalem
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Dibatalkan":
                        comp.setBackground(new java.awt.Color(255, 204, 204)); // Merah Kalem
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Menunggu":
                        comp.setBackground(java.awt.Color.WHITE);
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    default:
                        comp.setBackground(java.awt.Color.WHITE);
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                }
            }
            return comp;
        }
    });
}
    
    private void addFilterListeners() {
        tglHarian.addPropertyChangeListener("date", e -> tampilData());
        cbStatus.addActionListener(e -> tampilData());
    }
    
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
    }
    
    private void loadComboKategori() {
    cbKategori.removeAllItems();
    cbKategori.addItem("- Semua Kategori -"); // Opsi default
    try {
        String sql = "SELECT nama_jenis FROM tbl_jenis_perangkat";
        Connection conn = Koneksi.configDB();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            cbKategori.addItem(rs.getString("nama_jenis"));
        }
    } catch (Exception e) {
        System.err.println("Error load kategori: " + e.getMessage());
    }
}

    private void tampilData() {
        DefaultTableModel model = new DefaultTableModel();
        // --- STRUKTUR KOLOM DISAMAKAN DENGAN LAP BULANAN/MINGGUAN ---
        model.addColumn("No");
        model.addColumn("ID Servis"); 
        model.addColumn("Tanggal Masuk"); // <--- Penambahan Kolom Wajib
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        // Model & Seri tidak ditampilkan di tabel agar ringkas
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Total Biaya"); 
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // Update Query: Tambah s.tanggal_masuk
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, p.alamat, s.jenis_barang, "
                       + "s.merek, s.keluhan_awal, s.kelengkapan, s.status, s.harga, s.tanggal_masuk, s.status_barang "
                       + "FROM servis s "
                       + "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + "WHERE 1=1 ";

            // 1. Filter Tanggal
            if (tglHarian.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String tgl = sdf.format(tglHarian.getDate());
                sql += "AND s.tanggal_masuk = '" + tgl + "' ";
            }

            // 2. Filter Pencarian
            String keyword = tfCari.getText();
            if (!keyword.isEmpty()) {
                sql += "AND (p.nama_pelanggan LIKE '%" + keyword + "%' "
                     + "OR p.no_hp LIKE '%" + keyword + "%' "
                     + "OR s.id_servis LIKE '%" + keyword + "%' "
                     + "OR s.merek LIKE '%" + keyword + "%') ";
            }

            // 3. Filter Status
            if (cbStatus.getSelectedIndex() > 0) {
                sql += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            }
            
            if (cbKategori.getSelectedIndex() > 0) {
            String kategoriTerpilih = cbKategori.getSelectedItem().toString();
            sql += "AND s.jenis_barang = '" + kategoriTerpilih + "' ";
        }
            
            sql += "ORDER BY s.id_servis DESC";

            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            DecimalFormat df = new DecimalFormat("#,###");

            int no = 1;
            while (rs.next()) {
                double hargaVal = rs.getDouble("harga");
                String hargaFmt = "Rp " + df.format(hargaVal);
                
                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_servis"),
                    rs.getString("tanggal_masuk"), // <--- Masukkan Data Tanggal
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"),
                    rs.getString("alamat"),
                    rs.getString("jenis_barang"),
                    rs.getString("merek"),
                    rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"),
                    hargaFmt, 
                    rs.getString("status"),
                    rs.getString("status_barang")
                });
            }
            tblLapHarian.setModel(model);

        } catch (Exception e) {
            System.err.println("Error tampil data harian: " + e.getMessage());
        }
        
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
                
                // --- LISTENER SAAT POPUP DITUTUP ---
                ds.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        tampilData(); // Refresh Tabel Bulanan
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
            
            // Filter Tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl = "";
            if (tglHarian.getDate() != null) {
                tgl = sdf.format(tglHarian.getDate());
                sql += "AND DATE(tanggal_masuk) = '" + tgl + "' ";
            }
            
            // Filter Kategori (Jika ada)
            if (cbKategori.getSelectedIndex() > 0) {
                sql += "AND jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            
            // Format Tanggal untuk Judul (agar lebih cantik dibaca)
            SimpleDateFormat sdfView = new SimpleDateFormat("dd MMMM yyyy");
            String tglView = (tglHarian.getDate() != null) ? sdfView.format(tglHarian.getDate()) : "-";
            
            // KIRIM KE PARENT
            parent.setInfoPendapatan("Pendapatan Tanggal " + tglView + " :", total);
            
        } catch (Exception e) {
            System.out.println("Err Harian: " + e.getMessage());
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
        tblLapHarian = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        tglHarian = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        btnNota = new javax.swing.JButton();
        btnPdf = new javax.swing.JButton();
        btCetakE = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1720, 750));
        setMinimumSize(new java.awt.Dimension(1720, 750));
        setPreferredSize(new java.awt.Dimension(1720, 750));

        btnCari.setBackground(new java.awt.Color(102, 255, 102));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCari.setText("CARI");
        btnCari.addActionListener(this::btnCariActionPerformed);

        btnDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDetail.setText("DETAIL");
        btnDetail.addActionListener(this::btnDetailActionPerformed);

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("REFRESH");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        tblLapHarian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblLapHarian.setModel(new javax.swing.table.DefaultTableModel(
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
        tblLapHarian.setRowHeight(35);
        jScrollPane1.setViewportView(tblLapHarian);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Tanggal :");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Status :");

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnNota.setBackground(new java.awt.Color(102, 255, 102));
        btnNota.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNota.setText("Nota");
        btnNota.addActionListener(this::btnNotaActionPerformed);

        btnPdf.setBackground(new java.awt.Color(204, 204, 204));
        btnPdf.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPdf.setText("PDF");
        btnPdf.addActionListener(this::btnPdfActionPerformed);

        btCetakE.setBackground(new java.awt.Color(204, 204, 204));
        btCetakE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btCetakE.setText("Cetak Excel");
        btCetakE.addActionListener(this::btCetakEActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Kategori :");

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addItemListener(this::cbKategoriItemStateChanged);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1682, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCari)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDetail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(220, 220, 220)
                        .addComponent(btCetakE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNota)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglHarian, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tglHarian, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnNota, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btCetakE, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(btnPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(2, 2, 2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        cbStatus.setSelectedIndex(0);
        tglHarian.setDate(new Date());
        tampilData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        int row = tblLapHarian.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis terlebih dahulu!");
            return;
        }
        String idServis = tblLapHarian.getValueAt(row, 1).toString();
        bukaHalamanDetail(idServis);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btCetakEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCetakEActionPerformed
        // TODO add your handling code here:
        if (tglHarian.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal terlebih dahulu!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String strTanggal = sdf.format(tglHarian.getDate());
        
        String judul = "LAPORAN SERVIS HARIAN TANGGAL " + strTanggal;
        
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Harian_" + sdfFile.format(tglHarian.getDate());
        
        ExportExcel.exportJTableToExcelCustom(tblLapHarian, judul, suffix);
    }//GEN-LAST:event_btCetakEActionPerformed

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfActionPerformed
        // TODO add your handling code here:
        if (tglHarian.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal terlebih dahulu!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String strTanggal = sdf.format(tglHarian.getDate());
        
        String judul = "LAPORAN SERVIS HARIAN TANGGAL " + strTanggal;
        String statusFilter = "Status: " + cbStatus.getSelectedItem().toString();
        
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Harian_" + sdfFile.format(tglHarian.getDate());
        
        ExportPDF.exportToPDFCustom(tblLapHarian, judul, statusFilter, suffix);
    }//GEN-LAST:event_btnPdfActionPerformed

    private void btnNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotaActionPerformed
        // TODO add your handling code here:
        int row = tblLapHarian.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis yang ingin dicetak notanya!");
            return;
        }
        
        String idServis = tblLapHarian.getValueAt(row, 1).toString();
        CetakStruk.cetakStruk(idServis, Session.idUser);
    }//GEN-LAST:event_btnNotaActionPerformed

    private void cbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKategoriItemStateChanged
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_cbKategoriItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCetakE;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnNota;
    private javax.swing.JButton btnPdf;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblLapHarian;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglHarian;
    // End of variables declaration//GEN-END:variables
}
