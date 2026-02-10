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

    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    public LapHarian(PKelLaporan parent) {
        this.parent = parent; 
        initComponents();
        loadComboStatus();
        loadComboKategori();
        tglHarian.setDate(new Date());
        
        aturWarnaBarisTabel();
        addFilterListeners();
        tampilData();
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
            return comp;
        }
    });
}
    
    private void addFilterListeners() {
       tglHarian.addPropertyChangeListener("date", e -> { currentPage = 0; tampilData(); });
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
    }
    
    private void loadComboKategori() {
    cbKategori.removeAllItems();
        cbKategori.addItem("- Semua Kategori -");
        try {
            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery("SELECT nama_jenis FROM tbl_jenis_perangkat");
            while (rs.next()) {
                cbKategori.addItem(rs.getString("nama_jenis"));
            }
        } catch (Exception e) { System.err.println("Err: " + e.getMessage()); }
}

    public void tampilData() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addColumn("No");
        model.addColumn("ID Servis"); 
        model.addColumn("Tanggal Masuk");
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Total Biaya"); 
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // 1. Membangun Where Clause
            String where = "WHERE 1=1 ";
            if (tglHarian.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                where += "AND s.tanggal_masuk = '" + sdf.format(tglHarian.getDate()) + "' ";
            }
            if (!tfCari.getText().isEmpty()) {
                String key = tfCari.getText();
                where += "AND (p.nama_pelanggan LIKE '%"+key+"%' OR p.no_hp LIKE '%"+key+"%' OR s.id_servis LIKE '%"+key+"%') ";
            }
            if (cbStatus.getSelectedIndex() > 0) {
                where += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            }
            if (cbKategori.getSelectedIndex() > 0) {
                where += "AND s.jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            // 2. Hitung Total untuk Pagination
            Connection conn = Koneksi.configDB();
            ResultSet rsCount = conn.createStatement().executeQuery("SELECT COUNT(*) AS total FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " + where);
            int totalData = 0;
            if (rsCount.next()) totalData = rsCount.getInt("total");

            // 3. Query Utama dengan Limit
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat "
                       + "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + where + "ORDER BY s.id_servis DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            DecimalFormat df = new DecimalFormat("#,###");
            int no = offset + 1;

            while (rs.next()) {
                model.addRow(new Object[]{
                    no++, rs.getString("id_servis"), rs.getString("tanggal_masuk"),
                    rs.getString("nama_pelanggan"), rs.getString("no_hp"), rs.getString("alamat"),
                    rs.getString("jenis_barang"), rs.getString("merek"), rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"), "Rp " + df.format(rs.getDouble("harga")), 
                    rs.getString("status"), rs.getString("status_barang")
                });
            }
            tblLapHarian.setModel(model);

            // Update Status Tombol
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) { System.err.println("Err: " + e.getMessage()); }
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
        jPanel1 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1680, 760));
        setMinimumSize(new java.awt.Dimension(1680, 760));
        setPreferredSize(new java.awt.Dimension(1680, 760));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 19, 269, 50));

        btnCari.setBackground(new java.awt.Color(102, 255, 102));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(293, 17, -1, 50));

        btnDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDetail.setText("DETAIL");
        btnDetail.addActionListener(this::btnDetailActionPerformed);
        add(btnDetail, new org.netbeans.lib.awtextra.AbsoluteConstraints(409, 17, -1, 50));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(507, 17, -1, 50));

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

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 1660, 528));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Tanggal :");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1270, 20, -1, 50));
        add(tglHarian, new org.netbeans.lib.awtextra.AbsoluteConstraints(1340, 20, 130, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Status :");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1480, 20, -1, 50));

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cbStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1530, 20, 123, 50));

        btnNota.setBackground(new java.awt.Color(102, 255, 102));
        btnNota.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNota.setText("Nota");
        btnNota.addActionListener(this::btnNotaActionPerformed);
        add(btnNota, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 20, -1, 50));

        btnPdf.setBackground(new java.awt.Color(204, 204, 204));
        btnPdf.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPdf.setText("PDF");
        btnPdf.addActionListener(this::btnPdfActionPerformed);
        add(btnPdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, -1, 50));

        btCetakE.setBackground(new java.awt.Color(204, 204, 204));
        btCetakE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btCetakE.setText("Cetak Excel");
        btCetakE.addActionListener(this::btCetakEActionPerformed);
        add(btCetakE, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 20, -1, 51));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Kategori :");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 20, -1, 50));

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addItemListener(this::cbKategoriItemStateChanged);
        add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, 119, 50));

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
                .addContainerGap(1366, Short.MAX_VALUE)
                .addComponent(btnNextKiri)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextKanan)
                .addGap(86, 86, 86))
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

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 1680, -1));
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
        currentPage = 0; tampilData();
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

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            tampilData();
        }
    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        tampilData();
    }//GEN-LAST:event_btnNextKananActionPerformed


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
    private javax.swing.JTable tblLapHarian;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglHarian;
    // End of variables declaration//GEN-END:variables
}
