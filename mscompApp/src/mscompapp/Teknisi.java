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
/**
 *
 * @author Acer Aspire Lite 15
 */
public class Teknisi extends javax.swing.JPanel {

    private String idTeknisi;

    public Teknisi() {
        initComponents();
        
        // Proteksi Null Pointer Session
        if (Session.idUser != null) {
            this.idTeknisi = Session.idUser;
        } else {
            this.idTeknisi = "0"; 
        }        
        
        // 1. Load Data ComboBox
        loadComboStatus();
        loadComboKategori();
        
        // 2. Tampilkan Data Awal
        tampilData();
        
        // 3. Tambahkan Event Listener
        addFilterListeners();
        
        tblServ = new javax.swing.JTable() {
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
        jScrollPane1.setViewportView(tblServ);
    }
    
    // --- LISTENER FILTER ---
    private void addFilterListeners() {
        cbStatus.addActionListener(e -> tampilData());
        cbKategori.addActionListener(e -> tampilData());
        dtFilterdate.addPropertyChangeListener("date", e -> tampilData());
    }

    // --- LOAD COMBO STATUS ---
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
    }

    // --- LOAD COMBO KATEGORI (DARI tbl_jenis_perangkat) ---
    private void loadComboKategori() {
        cbKategori.removeAllItems();
        cbKategori.addItem("- Semua Kategori -");
        
        try {
            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            // Mengambil nama_jenis dari tabel master jenis perangkat
            String sql = "SELECT nama_jenis FROM tbl_jenis_perangkat ORDER BY nama_jenis ASC";
            ResultSet rs = stm.executeQuery(sql);
            
            while (rs.next()) {
                cbKategori.addItem(rs.getString("nama_jenis"));
            }
        } catch (Exception e) {
            System.err.println("Gagal load kategori: " + e.getMessage());
        }
    }

    // --- TAMPILKAN DATA (FILTER & SORTING) ---
    public void tampilData() {
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No");
        model.addColumn("ID Servis");
        model.addColumn("Tanggal Masuk");
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        model.addColumn("Model/Tipe");
        model.addColumn("Nomor Seri");
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Harga Servis");
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // Query Dasar
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.id_servis, p.nama_pelanggan, p.no_hp, p.alamat, s.jenis_barang, ")
               .append("s.tanggal_masuk, s.merek, s.model, s.harga, s.no_seri, s.keluhan_awal, s.kelengkapan, s.status, s.status_barang ")
               .append("FROM servis s ")
               .append("INNER JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan ")
               .append("WHERE 1=1 "); // 1=1 memudahkan penambahan AND dinamis

            // 1. Filter Status
            if (cbStatus.getSelectedIndex() > 0) {
                String statusPilih = cbStatus.getSelectedItem().toString();
                sql.append("AND s.status = '").append(statusPilih).append("' ");
            }

            // 2. Filter Kategori (Jenis Barang)
            if (cbKategori.getSelectedIndex() > 0) {
                String katPilih = cbKategori.getSelectedItem().toString();
                sql.append("AND s.jenis_barang = '").append(katPilih).append("' ");
            }

            // 3. Filter Tanggal
            if (dtFilterdate.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String tglPilih = sdf.format(dtFilterdate.getDate());
                sql.append("AND s.tanggal_masuk = '").append(tglPilih).append("' ");
            }
            
            // 4. Filter Pencarian (txtCari)
            String keyword = txtCari.getText().trim();
            if (!keyword.isEmpty()) {
                sql.append("AND (p.nama_pelanggan LIKE '%").append(keyword).append("%' ")
                   .append("OR s.id_servis LIKE '%").append(keyword).append("%' ")
                   .append("OR s.merek LIKE '%").append(keyword).append("%') ");
            }
            
            // 5. Sorting (Terbaru Paling Atas)
            sql.append("ORDER BY s.tanggal_masuk DESC");

            // Eksekusi
            Connection conn = Koneksi.configDB();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());

            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_servis"),
                    rs.getString("tanggal_masuk"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"),
                    rs.getString("alamat"),
                    rs.getString("jenis_barang"),
                    rs.getString("merek"),
                    rs.getString("model"),
                    rs.getString("no_seri"),
                    rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"),
                    rs.getInt("harga"),
                    rs.getString("status"),
                    rs.getString("status_barang")
                });
            }
            tblServ.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
            e.printStackTrace();
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnLihatDetail = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        dtFilterdate = new com.toedter.calendar.JDateChooser();
        btnRefresh = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblServ = new javax.swing.JTable();
        cbKategori = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        txtCari = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBackground(new java.awt.Color(4, 102, 200));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("DASHBOARD TEKNISI");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnLihatDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnLihatDetail.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        btnLihatDetail.setText("LIHAT DETAIL");
        btnLihatDetail.addActionListener(this::btnLihatDetailActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setText("Tanggal :");

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setText(" Status :");

        btnRefresh.setBackground(new java.awt.Color(102, 255, 102));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel4.setText("Daftar Service Bulanan :");

        tblServ.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblServ.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan", "Perbaikan", "Part yang Diganti", "Status"
            }
        ));
        tblServ.setRowHeight(35);
        jScrollPane1.setViewportView(tblServ);

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addActionListener(this::cbKategoriActionPerformed);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel5.setText("Kategori :");

        txtCari.addActionListener(this::txtCariActionPerformed);
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCariKeyReleased(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel6.setText("Cari :");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnLihatDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(21, 21, 21)
                                .addComponent(jLabel5)
                                .addGap(12, 12, 12)
                                .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dtFilterdate, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCari, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                                .addGap(27, 27, 27)
                                .addComponent(btnRefresh)
                                .addGap(11, 11, 11)))))
                .addGap(28, 28, 28))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnLihatDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                                .addComponent(dtFilterdate, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLihatDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLihatDetailActionPerformed
        // TODO add your handling code here:
        int row = tblServ.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data service terlebih dahulu!");
            return;
        }
        
        String idServis = tblServ.getValueAt(row, 1).toString();
        String tglMasuk = tblServ.getValueAt(row, 2).toString();
        String nama = tblServ.getValueAt(row, 3).toString();
        String noHp = tblServ.getValueAt(row, 4).toString();
        String alamat = tblServ.getValueAt(row, 5).toString();
        String jenis = tblServ.getValueAt(row, 6).toString();
        String merek = tblServ.getValueAt(row, 7).toString();
        String model = tblServ.getValueAt(row, 8).toString();
        String noSeri = tblServ.getValueAt(row, 9).toString();
        String keluhan = tblServ.getValueAt(row, 10).toString();
        String kelengkapan = tblServ.getValueAt(row, 11).toString();
        String status = tblServ.getValueAt(row, 12).toString();
        String statusBarang = tblServ.getValueAt(row, 13).toString();
        
        // Membuka DetailService
        DetailService ds = new DetailService(
                idServis, tglMasuk, nama, noHp, alamat, jenis, merek,
                model, noSeri, keluhan, kelengkapan, status, statusBarang, this.idTeknisi
        );
        ds.setLocationRelativeTo(null); // Agar muncul di tengah
        ds.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            // Memanggil method tampilData() milik Teknisi.java
            tampilData(); 
        }
    });
        ds.setVisible(true);
    }//GEN-LAST:event_btnLihatDetailActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        cbStatus.setSelectedIndex(0);
        cbKategori.setSelectedIndex(0);
        dtFilterdate.setDate(null);
        
        
        tampilData(); // Muat ulang data mentah
        JOptionPane.showMessageDialog(this, "Data direfresh & Filter direset!");
  
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void txtCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCariActionPerformed

    private void txtCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariKeyReleased
        // TODO add your handling code here:
        String cari = txtCari.getText().trim();
        tampilData(); // Muat ulang data mentah

    }//GEN-LAST:event_txtCariKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLihatDetail;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private com.toedter.calendar.JDateChooser dtFilterdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblServ;
    private javax.swing.JTextField txtCari;
    // End of variables declaration//GEN-END:variables
}
