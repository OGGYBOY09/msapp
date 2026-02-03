/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;
import config.Koneksi;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

/**
 *
 * @author Acer Aspire Lite 15
 */
public class LapBulanan extends javax.swing.JPanel {

    /**
     * Creates new form LapBulanan
     */
    private PKelLaporan parent;

    // Constructor Menerima Parent
    public LapBulanan(PKelLaporan parent) {
        this.parent = parent;
        initComponents();
        loadComboStatus();
        loadComboKategori(); // <--- Tambahkan ini
        
        // Tambahkan ini di dalam Constructor LapBulanan
        tblLapBulanan = new javax.swing.JTable() {
            {
        setRowHeight(30); // Ubah angka 30 sesuai keinginanmu (semakin besar semakin tinggi)
        getTableHeader().setReorderingAllowed(false); // Opsional: Biar kolom gak bisa digeser-geser
    }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
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
        jScrollPane1.setViewportView(tblLapBulanan);
        
                tampilData();

        // Listener
        mcBulan.addPropertyChangeListener("month", e -> tampilData());
        thTahun.addPropertyChangeListener("year", e -> tampilData());
        cbStatus.addActionListener(e -> tampilData()); 
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
    
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
        cbStatus.setSelectedIndex(0);
    }

    public void tampilData() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("No");
        model.addColumn("ID Servis");
        model.addColumn("Tanggal Masuk"); // <--- KOLOM BARU DI GUI
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        // Model & Seri Dihapus dari GUI agar tidak sempit
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Total Biaya"); 
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // Update Query
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, p.alamat, s.jenis_barang, "
                       + "s.merek, s.keluhan_awal, s.kelengkapan, s.status,s.status_barang , s.harga, s.tanggal_masuk " // <-- Ambil tanggal
                       + "FROM servis s "
                       + "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + "WHERE 1=1 ";

            int bulan = mcBulan.getMonth() + 1;
            int tahun = thTahun.getYear();
            sql += "AND MONTH(s.tanggal_masuk) = " + bulan + " AND YEAR(s.tanggal_masuk) = " + tahun + " ";

            String keyword = tfCari.getText();
            if (!keyword.isEmpty()) {
                sql += "AND (p.nama_pelanggan LIKE '%" + keyword + "%' "
                     + "OR s.id_servis LIKE '%" + keyword + "%') ";
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
                    rs.getString("tanggal_masuk"), // <--- Masukkan Data Tanggal
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
            tblLapBulanan.setModel(model);

        } catch (Exception e) {
            System.err.println("Error tampil data bulanan: " + e.getMessage());
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
                        tampilData(); 
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
            
            int bulan = mcBulan.getMonth() + 1;
            int tahun = thTahun.getYear();
            sql += "AND MONTH(tanggal_masuk) = " + bulan + " AND YEAR(tanggal_masuk) = " + tahun + " ";
            
            if (cbKategori.getSelectedIndex() > 0) {
                sql += "AND jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            
            // Nama Bulan untuk Judul
            String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                                  "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
            String bulanStr = namaBulan[mcBulan.getMonth()];
            
            // KIRIM KE PARENT
            parent.setInfoPendapatan("Pendapatan Bulan " + bulanStr + " " + tahun + " :", total);
            
        } catch (Exception e) {
            System.out.println("Err Bulanan: " + e.getMessage());
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
        tblLapBulanan = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        mcBulan = new com.toedter.calendar.JMonthChooser();
        jLabel3 = new javax.swing.JLabel();
        thTahun = new com.toedter.calendar.JYearChooser();
        btCetakE = new javax.swing.JButton();
        btnPdf = new javax.swing.JButton();
        btnNota = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1720, 750));
        setMinimumSize(new java.awt.Dimension(1720, 750));

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

        tblLapBulanan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblLapBulanan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Tanggal", "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan", "Status", "Status Barang"
            }
        ));
        tblLapBulanan.setRowHeight(35);
        jScrollPane1.setViewportView(tblLapBulanan);
        if (tblLapBulanan.getColumnModel().getColumnCount() > 0) {
            tblLapBulanan.getColumnModel().getColumn(0).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(1).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(2).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(3).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(4).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(5).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(6).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(7).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(8).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(9).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(10).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(11).setResizable(false);
            tblLapBulanan.getColumnModel().getColumn(12).setResizable(false);
        }

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Bulan :");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Status :");

        cbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        mcBulan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Tahun :");

        btCetakE.setBackground(new java.awt.Color(204, 204, 204));
        btCetakE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btCetakE.setText("Cetak Excel");
        btCetakE.addActionListener(this::btCetakEActionPerformed);

        btnPdf.setBackground(new java.awt.Color(204, 204, 204));
        btnPdf.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPdf.setText("PDF");
        btnPdf.addActionListener(this::btnPdfActionPerformed);

        btnNota.setBackground(new java.awt.Color(102, 255, 102));
        btnNota.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNota.setText("Nota");
        btnNota.addActionListener(this::btnNotaActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Kategori :");

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addItemListener(this::cbKategoriItemStateChanged);
        cbKategori.addActionListener(this::cbKategoriActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCari)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDetail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addGap(129, 129, 129)
                        .addComponent(btCetakE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPdf)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNota)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mcBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(thTahun, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 13, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btCetakE, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(mcBulan, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(thTahun, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnPdf, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNota, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 668, Short.MAX_VALUE)
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
        java.util.Calendar cal = java.util.Calendar.getInstance();
        mcBulan.setMonth(cal.get(java.util.Calendar.MONTH));
        thTahun.setYear(cal.get(java.util.Calendar.YEAR));
        tampilData();
        
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        int row = tblLapBulanan.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis terlebih dahulu!");
            return;
        }
        String idServis = tblLapBulanan.getValueAt(row, 1).toString();
        bukaHalamanDetail(idServis);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btCetakEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCetakEActionPerformed
        // TODO add your handling code here:
        int bulanIndex = mcBulan.getMonth(); // 0 = Januari
        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                              "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        String bulanText = namaBulan[bulanIndex];
        
        // 2. Ambil Data Tahun
        int tahun = thTahun.getYear();
        
        // 3. Panggil ExportExcel dengan parameter tambahan
        ExportExcel.exportJTableToExcel(tblLapBulanan, bulanText, tahun);
    }//GEN-LAST:event_btCetakEActionPerformed

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfActionPerformed
        // TODO add your handling code here:
        int bulanIndex = mcBulan.getMonth();
        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                              "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        String bulanText = namaBulan[bulanIndex];
        int tahun = thTahun.getYear();
        
        // 2. Ambil Keterangan Status untuk Sub-Judul
        // Misal: "Status: Semua Status" atau "Status: Proses"
        String statusFilter = "Status: " + cbStatus.getSelectedItem().toString();
        
        // 3. Panggil Fungsi Export PDF
        ExportPDF.exportToPDF(tblLapBulanan, bulanText, tahun, statusFilter);
    
    }//GEN-LAST:event_btnPdfActionPerformed

    private void btnNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotaActionPerformed
        // TODO add your handling code here:
        int row = tblLapBulanan.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis yang ingin dicetak notanya!");
            return;
        }
        
        // 2. Ambil ID Servis
        String idServis = tblLapBulanan.getValueAt(row, 1).toString();
        
        // 3. Panggil Class CetakStruk
        // Parameter: ID Servis dan ID User yang sedang Login (Admin)
        // Session.idUser didapat dari sistem login kamu
        CetakStruk.cetakStruk(idServis, Session.idUser);
    }//GEN-LAST:event_btnNotaActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed

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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JMonthChooser mcBulan;
    public javax.swing.JTable tblLapBulanan;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JYearChooser thTahun;
    // End of variables declaration//GEN-END:variables
}
