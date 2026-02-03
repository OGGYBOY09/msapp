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

    // Constructor Menerima Parent
    public LapMingguan(PKelLaporan parent) {
        this.parent = parent; // Simpan referensi
        initComponents();
        loadComboStatus();
        loadComboKategori();
        tblLapMingguan = new javax.swing.JTable() {
            {
        setRowHeight(30); // Ubah angka 30 sesuai keinginanmu (semakin besar semakin tinggi)
        getTableHeader().setReorderingAllowed(false); // Opsional: Biar kolom gak bisa digeser-geser
    }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);

                // Ambil data dari kolom Status (indeks kolom terakhir atau sesuai model Anda)
                // Di tampilData() Anda, Status berada di kolom ke-11 (indeks 11)
                Object statusValue = getValueAt(row, 11); 

                if (statusValue != null) {
                    String status = statusValue.toString();

                    if (isRowSelected(row)) {
                        comp.setBackground(getSelectionBackground());
                    } else {
                        switch (status) {
                            case "Proses":
                                comp.setBackground(java.awt.Color.YELLOW);
                                comp.setForeground(java.awt.Color.BLACK);
                                break;
                            case "Selesai":
                                comp.setBackground(new java.awt.Color(144, 238, 144)); // Hijau Muda
                                comp.setForeground(java.awt.Color.BLACK);
                                break;
                            case "Dibatalkan":
                                comp.setBackground(new java.awt.Color(255, 182, 193)); // Merah Muda
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
                }
                return comp;
            }
        };
        // Jangan lupa pindahkan tblLapBulanan ke JScrollPane jika Anda membuatnya secara manual lewat kode
        jScrollPane1.setViewportView(tblLapMingguan);
        
        
        // Set Default: Akhir = Hari ini, Awal = 7 hari lalu
        resetTanggalMingguan();
        
        // Setup Listener (Otomatisasi Tanggal & Refresh Table)
        setupListeners();
        
        tampilData();
    }
    
    private void resetTanggalMingguan() {
        isUpdatingDate = true; // Matikan listener sementara
        Calendar cal = Calendar.getInstance();
        tglAkhir.setDate(cal.getTime()); // Hari ini
        cal.add(Calendar.DAY_OF_MONTH, -7);
        tglAwal.setDate(cal.getTime()); // 7 hari lalu
        isUpdatingDate = false; // Hidupkan listener lagi
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
    
    private void setupListeners() {
        // 1. Jika Tanggal AWAL diubah -> Tanggal AKHIR otomatis +7 hari
        tglAwal.addPropertyChangeListener("date", e -> {
            if (isUpdatingDate || tglAwal.getDate() == null) return;
            
            isUpdatingDate = true; // Cegah looping
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(tglAwal.getDate());
            cal.add(Calendar.DAY_OF_MONTH, 7); // Tambah 7 hari
            tglAkhir.setDate(cal.getTime());
            
            isUpdatingDate = false;
            tampilData(); // Refresh Tabel
        });
        
        // 2. Jika Tanggal AKHIR diubah -> Tanggal AWAL otomatis -7 hari
        tglAkhir.addPropertyChangeListener("date", e -> {
            if (isUpdatingDate || tglAkhir.getDate() == null) return;
            
            isUpdatingDate = true; // Cegah looping
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(tglAkhir.getDate());
            cal.add(Calendar.DAY_OF_MONTH, -7); // Kurang 7 hari
            tglAwal.setDate(cal.getTime());
            
            isUpdatingDate = false;
            tampilData(); // Refresh Tabel
        });
        
        // 3. Listener Status
        cbStatus.addActionListener(e -> tampilData());
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
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("No");
        model.addColumn("ID Servis");
        model.addColumn("Tanggal Masuk"); // <--- PERBAIKAN 1: Tambah Kolom Ini
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        // Model & Seri Dihapus agar ringkas
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Total Biaya"); 
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // PERBAIKAN 2: Tambahkan s.tanggal_masuk di Query
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, p.alamat, s.jenis_barang, "
                       + "s.merek, s.keluhan_awal, s.kelengkapan, s.status, s.harga, s.tanggal_masuk, s.status_barang " 
                       + "FROM servis s "
                       + "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + "WHERE 1=1 ";

            if (tglAwal.getDate() != null && tglAkhir.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String start = sdf.format(tglAwal.getDate());
                String end = sdf.format(tglAkhir.getDate());
                sql += "AND s.tanggal_masuk BETWEEN '" + start + "' AND '" + end + "' ";
            }

            String keyword = tfCari.getText();
            if (!keyword.isEmpty()) {
                sql += "AND (p.nama_pelanggan LIKE '%" + keyword + "%' "
                     + "OR s.id_servis LIKE '%" + keyword + "%' "
                     + "OR s.merek LIKE '%" + keyword + "%') ";
            }

            if (cbStatus.getSelectedIndex() > 0) {
                sql += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            
            }
            
            if (cbKategori.getSelectedIndex() > 0) {
            String kategoriTerpilih = cbKategori.getSelectedItem().toString();
            sql += "AND s.jenis_barang = '" + kategoriTerpilih + "' ";
        }
            
            sql += "ORDER BY s.tanggal_masuk DESC";

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
                    rs.getString("tanggal_masuk"), // <--- PERBAIKAN 3: Masukkan data tanggal
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"),
                    rs.getString("alamat"),
                    rs.getString("jenis_barang"),
                    rs.getString("merek"),
                    // Hapus Model & Seri di GUI
                    rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"),
                    hargaFmt, 
                    rs.getString("status"),
                    rs.getString("status_barang")
                });
            }
            tblLapMingguan.setModel(model);

        } catch (Exception e) {
            System.err.println("Error tampil data mingguan: " + e.getMessage());
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

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Tanggal S/D :");

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCari)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDetail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)
                        .addComponent(btCetakE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPdf)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNota)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1682, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tglAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tglAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(2, 2, 2))))
                    .addComponent(btnNota, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btCetakE, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
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
        resetTanggalMingguan(); // Reset ke logika Today & Today-7
        tampilData();
        
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
    private javax.swing.JTable tblLapMingguan;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglAkhir;
    private com.toedter.calendar.JDateChooser tglAwal;
    // End of variables declaration//GEN-END:variables
}
