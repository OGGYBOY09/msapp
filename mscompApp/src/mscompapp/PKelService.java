/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template

 */
package mscompapp;

import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import config.Koneksi;
import javax.swing.JOptionPane;
/**
 *
 * @author Acer Aspire Lite 15
 */
public class PKelService extends javax.swing.JPanel {   
    
    public int idPelanggan = 0; 

    public PKelService() {
        initComponents();
        auto_number_service();
        tampilkanAdmin();
        tampilTanggal();
        load_jenis_perangkat();
        load_status();
        load_table_service();
        tampilKategori();       // DATA KATEGORI DARI DATABASE
        
        // Default State
        rbLama.setSelected(true);
        btnCari.setEnabled(true);
        tNamaPelanggan.setEditable(false);
        tNoPelanggan.setEditable(false);
        tAlamatPelanggan.setEditable(false);
    }
    
    // --- LOAD DATA & HELPER METHODS ---
    
    private void tampilKategori() {
    try {
        // 1. Bersihkan dulu agar tidak double
        cbJenisBrg.removeAllItems();
        cbJenisBrg.addItem("- Pilih Jenis -");

        // 2. Query ke tbl_kategori
        String sql = "SELECT nama_kategori FROM tbl_kategori";
        java.sql.Connection conn = (java.sql.Connection)config.Koneksi.configDB();
        java.sql.Statement stm = conn.createStatement();
        java.sql.ResultSet res = stm.executeQuery(sql);

        // 3. Masukkan hasil ke ComboBox
        while (res.next()) {
            cbJenisBrg.addItem(res.getString("nama_kategori"));
        }
    } catch (Exception e) {
        // Ini akan membantu kita tahu kalau ada error database
        System.out.println("Error tampil kategori: " + e.getMessage());
    }
}
    
    private void tampilkanAdmin() {
        try {
            // Pastikan di Login.java sudah ada: public static String namaUser;
            String namaLog = Login.namaUser; 
            if (namaLog != null && !namaLog.isEmpty()) {
                tNamaAdmin.setText(namaLog);
            } else {
                tNamaAdmin.setText("Admin");
            }
        } catch (Exception e) {
            tNamaAdmin.setText("Admin");
        }
        tNamaAdmin.setEditable(false);
    }
    
    private void load_jenis_perangkat() {
        cbJenisBrg.removeAllItems();
        cbJenisBrg.addItem("- Pilih Jenis -");
        try {
            String sql = "SELECT nama_jenis FROM jenis_perangkat";
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            while(res.next()) {
                cbJenisBrg.addItem(res.getString("nama_jenis"));
            }
        } catch (Exception e) {}
    }
    
    private void load_status() {
        cbStatusServ.removeAllItems();
        cbStatusServ.addItem("Menunggu");
        cbStatusServ.addItem("Proses");
        cbStatusServ.addItem("Selesai");
        cbStatusServ.addItem("Dibatalkan");
    }
    
    public void pelangganTerpilih(String id, String nama, String hp, String alamat) {
        idPelanggan = Integer.parseInt(id);
        tNamaPelanggan.setText(nama);
        tNoPelanggan.setText(hp);
        tAlamatPelanggan.setText(alamat);
    }

    private void auto_number_service() {
        try {
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            String sql = "SELECT id_servis FROM servis ORDER BY id_servis DESC LIMIT 1";
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                String kode = res.getString("id_servis").substring(3); 
                int AN = Integer.parseInt(kode) + 1;
                String nol = (AN < 10) ? "000" : (AN < 100) ? "00" : (AN < 1000) ? "0" : "";
                tNomorServ.setText("SRV" + nol + AN);
            } else {
                tNomorServ.setText("SRV0001");
            }
            tNomorServ.setEditable(false);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void tampilTanggal() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        tTgl.setText(df.format(new Date()));
    }
    
    // Helper untuk mengambil ID Admin dari tabel User berdasarkan Nama di Textfield
    private int getAdminId() {
        try {
            String sql = "SELECT id_user FROM tbl_user WHERE username = ?";
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tNamaAdmin.getText());
            java.sql.ResultSet res = pst.executeQuery();
            if (res.next()) {
                return res.getInt("id_user");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 1; // Default jika gagal (pastikan ID 1 ada di tbl_user)
    }

    // Helper untuk menggabungkan Checkbox
    private String getKelengkapan() {
        StringBuilder sb = new StringBuilder();
        if (ck1.isSelected()) sb.append("Charger, ");
        if (ck2.isSelected()) sb.append("Mouse, ");
        if (ck3.isSelected()) sb.append("Tas, ");
        if (ck4.isSelected()) sb.append("Kabel, ");
        
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 2); // Hapus koma terakhir
        }
        return "Unit Only";
    }

    private void load_table_service() {
        DefaultTableModel model = new DefaultTableModel();
        // Sesuaikan urutan dengan JTable Design kamu
        model.addColumn("No Servis");
        model.addColumn("Nama");
        model.addColumn("HP");
        model.addColumn("Jenis");
        model.addColumn("Merek");
        model.addColumn("Model");
        model.addColumn("Keluhan");
        model.addColumn("Status");

        try {
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, s.jenis_barang, s.merek, s.model, s.keluhan_awal, s.status " +
                         "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "ORDER BY s.tanggal_masuk DESC";
            
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            
            while(res.next()){
                model.addRow(new Object[]{
                    res.getString("id_servis"),
                    res.getString("nama_pelanggan"),
                    res.getString("no_hp"),
                    res.getString("jenis_barang"),
                    res.getString("merek"),
                    res.getString("model"),
                    res.getString("keluhan_awal"),
                    res.getString("status")
                });
            }
            tblServis.setModel(model);
        } catch (Exception e) {
            System.out.println("Error Load Table: " + e.getMessage());
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

        jSeparator1 = new javax.swing.JSeparator();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tNomorServ = new javax.swing.JTextField();
        tNamaAdmin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tTgl = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cbStatusServ = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        rbBaru = new javax.swing.JRadioButton();
        rbLama = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tNamaPelanggan = new javax.swing.JTextField();
        tNoPelanggan = new javax.swing.JTextField();
        tAlamatPelanggan = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        btBatal = new javax.swing.JButton();
        btSimpan = new javax.swing.JButton();
        btReset = new javax.swing.JButton();
        btnCari = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        cbJenisBrg = new javax.swing.JComboBox<>();
        tMerek = new javax.swing.JTextField();
        tModel = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        tSeri = new javax.swing.JTextField();
        ck1 = new javax.swing.JCheckBox();
        ck2 = new javax.swing.JCheckBox();
        ck3 = new javax.swing.JCheckBox();
        ck4 = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tKeluhan = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblServis = new javax.swing.JTable();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBackground(new java.awt.Color(102, 204, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel1.setText("INPUT DATA SERVICE");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Nomor Service :");

        tNomorServ.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNomorServ.addActionListener(this::tNomorServActionPerformed);

        tNamaAdmin.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Admin :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Tanggal :");

        tTgl.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        tTgl.setText("000");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Status :");

        cbStatusServ.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbStatusServ.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Proses", "Menunggu", "Selesai" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tNomorServ, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tNamaAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(84, 84, 84)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(461, 461, 461)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tTgl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbStatusServ, 0, 147, Short.MAX_VALUE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tNomorServ)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tTgl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tNamaAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbStatusServ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        buttonGroup1.add(rbBaru);
        rbBaru.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        rbBaru.setText("Pelanggan Baru");
        rbBaru.addActionListener(this::rbBaruActionPerformed);

        buttonGroup1.add(rbLama);
        rbLama.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        rbLama.setText("Pelangggan Lama");
        rbLama.addActionListener(this::rbLamaActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel4.setText("Cari No Hp / Nama :");

        jLabel9.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel9.setText("Nama :");

        jLabel13.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel13.setText("No Hp :");

        jLabel14.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel14.setText("Alamat :");

        tNamaPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNamaPelanggan.addActionListener(this::tNamaPelangganActionPerformed);

        tNoPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNoPelanggan.addActionListener(this::tNoPelangganActionPerformed);

        tAlamatPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jPanel6.setBackground(new java.awt.Color(102, 204, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel12.setText("Data Pelanggan");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel12)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 83, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel12)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        btBatal.setBackground(new java.awt.Color(255, 51, 51));
        btBatal.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        btBatal.setText("BATAL");
        btBatal.addActionListener(this::btBatalActionPerformed);

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        btSimpan.setText("SIMPAN");
        btSimpan.addActionListener(this::btSimpanActionPerformed);

        btReset.setBackground(new java.awt.Color(255, 255, 102));
        btReset.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        btReset.setText("RESET");
        btReset.addActionListener(this::btResetActionPerformed);

        btnCari.setText("cari");
        btnCari.addActionListener(this::btnCariActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btSimpan)
                        .addGap(54, 54, 54)
                        .addComponent(btReset, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel14)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(tAlamatPelanggan))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel13)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(tNoPelanggan))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel9)
                                    .addGap(18, 18, 18)
                                    .addComponent(tNamaPelanggan))
                                .addComponent(jLabel4)
                                .addComponent(rbLama)
                                .addComponent(rbBaru))
                            .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(btBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(rbBaru)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbLama)
                .addGap(41, 41, 41)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(tNamaPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(tNoPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(tAlamatPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSimpan)
                    .addComponent(btReset))
                .addGap(18, 18, 18)
                .addComponent(btBatal)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel5.setBackground(new java.awt.Color(102, 204, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel5.setText("Data Perangkat / Service");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 84, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(0, 21, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addGap(0, 22, Short.MAX_VALUE)))
        );

        cbJenisBrg.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbJenisBrg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbJenisBrg.addActionListener(this::cbJenisBrgActionPerformed);

        tMerek.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tMerek.addActionListener(this::tMerekActionPerformed);

        tModel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tModel.addActionListener(this::tModelActionPerformed);

        jLabel8.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel8.setText("Merek :");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("Kelengkapan :");

        jLabel15.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel15.setText("Jenis Barang :");

        jLabel16.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel16.setText("Model / Tipe :");

        jLabel17.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel17.setText("No Seri :");

        tSeri.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tSeri.addActionListener(this::tSeriActionPerformed);

        ck1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ck1.setText("Charger");
        ck1.addActionListener(this::ck1ActionPerformed);

        ck2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ck2.setText("Mouse");
        ck2.addActionListener(this::ck2ActionPerformed);

        ck3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ck3.setText("Tas");

        ck4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ck4.setText("Kabel");

        jLabel18.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel18.setText("Keluhan :");

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        tKeluhan.setColumns(20);
        tKeluhan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tKeluhan.setRows(5);
        tKeluhan.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(tKeluhan);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(ck2)
                        .addGap(34, 34, 34)
                        .addComponent(ck4))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel15))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tMerek)
                            .addComponent(tModel)
                            .addComponent(cbJenisBrg, 0, 293, Short.MAX_VALUE)
                            .addComponent(tSeri)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(ck1)
                        .addGap(26, 26, 26)
                        .addComponent(ck3)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tMerek, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbJenisBrg, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addGap(70, 70, 70)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tModel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(16, 16, 16)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(tSeri, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ck1)
                    .addComponent(ck3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ck2)
                    .addComponent(ck4))
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel7.setPreferredSize(new java.awt.Dimension(815, 960));

        jPanel8.setBackground(new java.awt.Color(102, 204, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel19.setText("Daftar Service");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel19)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 76, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(0, 17, Short.MAX_VALUE)
                    .addComponent(jLabel19)
                    .addGap(0, 18, Short.MAX_VALUE)))
        );

        tblServis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblServis);
        if (tblServis.getColumnModel().getColumnCount() > 0) {
            tblServis.getColumnModel().getColumn(7).setResizable(false);
            tblServis.getColumnModel().getColumn(8).setResizable(false);
        }

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 948, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbBaruActionPerformed
        btnCari.setEnabled(false);
        tNamaPelanggan.setEditable(true);
        tNoPelanggan.setEditable(true);
        tAlamatPelanggan.setEditable(true);

        tNamaPelanggan.setText("");
        tNoPelanggan.setText("");
        tAlamatPelanggan.setText("");
        tNamaPelanggan.requestFocus();
        idPelanggan = 0; // Reset ID
    }//GEN-LAST:event_rbBaruActionPerformed

    private void rbLamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbLamaActionPerformed
        // TODO add your handling code here:
        btnCari.setEnabled(true);
        tNamaPelanggan.setEditable(false);
        tNoPelanggan.setEditable(false);
        tAlamatPelanggan.setEditable(false);

        tNamaPelanggan.setText("");
        tNoPelanggan.setText("");
        tAlamatPelanggan.setText("");
    }//GEN-LAST:event_rbLamaActionPerformed

    private void tNamaPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNamaPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNamaPelangganActionPerformed

    private void tModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tModelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tModelActionPerformed

    private void tSeriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSeriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tSeriActionPerformed

    private void ck1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ck1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ck1ActionPerformed

    private void ck2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ck2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ck2ActionPerformed

    private void tNomorServActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNomorServActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNomorServActionPerformed

    private void tNoPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNoPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNoPelangganActionPerformed

    private void btSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSimpanActionPerformed

        try {
            // 1. Validasi Input
            if (tNamaPelanggan.getText().isEmpty() || cbJenisBrg.getSelectedIndex() == 0 || tKeluhan.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama, Jenis Barang, dan Keluhan wajib diisi!");
                return;
            }

            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            int finalIdPelanggan = idPelanggan;

            // 2. Cek apakah Pelanggan Baru? Jika ya, simpan dulu ke tbl_pelanggan
            if (rbBaru.isSelected()) {
                String sqlPelanggan = "INSERT INTO tbl_pelanggan (nama_pelanggan, no_hp, alamat) VALUES (?, ?, ?)";
                // Gunakan RETURN_GENERATED_KEYS untuk mengambil ID auto increment
                java.sql.PreparedStatement pstPel = conn.prepareStatement(sqlPelanggan, Statement.RETURN_GENERATED_KEYS);
                pstPel.setString(1, tNamaPelanggan.getText());
                pstPel.setString(2, tNoPelanggan.getText());
                pstPel.setString(3, tAlamatPelanggan.getText());
                pstPel.executeUpdate();
                
                // Ambil ID yang baru dibuat
                java.sql.ResultSet rsId = pstPel.getGeneratedKeys();
                if (rsId.next()) {
                    finalIdPelanggan = rsId.getInt(1);
                }
            } else {
                // Jika pelanggan lama tapi ID masih 0 (belum pilih dari pop up)
                if (finalIdPelanggan == 0) {
                    JOptionPane.showMessageDialog(this, "Mohon cari dan pilih data pelanggan lama!");
                    return;
                }
            }

            // 3. Simpan ke Tabel Servis
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl = sdf.format(new Date()); // Tanggal hari ini
            int idAdmin = getAdminId(); // Ambil ID Admin
            String kelengkapan = getKelengkapan();

            String sqlService = "INSERT INTO servis (id_servis, id_pelanggan, id_admin, tanggal_masuk, jenis_barang, merek, model, no_seri, kelengkapan, keluhan_awal, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            java.sql.PreparedStatement pstServ = conn.prepareStatement(sqlService);
            
            pstServ.setString(1, tNomorServ.getText());
            pstServ.setInt(2, finalIdPelanggan);
            pstServ.setInt(3, idAdmin);
            pstServ.setString(4, tgl);
            pstServ.setString(5, cbJenisBrg.getSelectedItem().toString());
            pstServ.setString(6, tMerek.getText());
            pstServ.setString(7, tModel.getText());
            pstServ.setString(8, tSeri.getText());
            pstServ.setString(9, kelengkapan);
            pstServ.setString(10, tKeluhan.getText());
            pstServ.setString(11, cbStatusServ.getSelectedItem().toString());
            
            pstServ.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data Service Berhasil Disimpan!\nNo: " + tNomorServ.getText());
            
            // Refresh
            load_table_service();
            btResetActionPerformed(null); // Reset Form

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btSimpanActionPerformed

    private void tMerekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tMerekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tMerekActionPerformed

    private void btResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btResetActionPerformed
        // TODO add your handling code here:
        btBatalActionPerformed(null); // Panggil fungsi batal untuk form pelanggan
        
        // Reset Form Barang
        cbJenisBrg.setSelectedIndex(0);
        tMerek.setText("");
        tModel.setText("");
        tSeri.setText("");
        tKeluhan.setText("");
        ck1.setSelected(false);
        ck2.setSelected(false);
        ck3.setSelected(false);
        ck4.setSelected(false);
        cbStatusServ.setSelectedIndex(0);
        
        auto_number_service();
    }//GEN-LAST:event_btResetActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        PopUpPelanggan popup = new PopUpPelanggan();
        popup.serviceForm = this; // Sambungkan pop-up dengan form ini
        popup.setVisible(true);
    }//GEN-LAST:event_btnCariActionPerformed

    private void btBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBatalActionPerformed
        // TODO add your handling code here:
        tNamaPelanggan.setText("");
        tNoPelanggan.setText("");
        tAlamatPelanggan.setText("");
        rbLama.setSelected(true);
        btnCari.setEnabled(true);
        tNamaPelanggan.setEditable(false);
        tNoPelanggan.setEditable(false);
        tAlamatPelanggan.setEditable(false);
        idPelanggan = 0;
    }//GEN-LAST:event_btBatalActionPerformed

    private void cbJenisBrgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbJenisBrgActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_cbJenisBrgActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBatal;
    private javax.swing.JButton btReset;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btnCari;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbJenisBrg;
    private javax.swing.JComboBox<String> cbStatusServ;
    private javax.swing.JCheckBox ck1;
    private javax.swing.JCheckBox ck2;
    private javax.swing.JCheckBox ck3;
    private javax.swing.JCheckBox ck4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton rbBaru;
    private javax.swing.JRadioButton rbLama;
    private javax.swing.JTextField tAlamatPelanggan;
    private javax.swing.JTextArea tKeluhan;
    private javax.swing.JTextField tMerek;
    private javax.swing.JTextField tModel;
    private javax.swing.JTextField tNamaAdmin;
    private javax.swing.JTextField tNamaPelanggan;
    private javax.swing.JTextField tNoPelanggan;
    private javax.swing.JTextField tNomorServ;
    private javax.swing.JTextField tSeri;
    private javax.swing.JLabel tTgl;
    private javax.swing.JTable tblServis;
    // End of variables declaration//GEN-END:variables
}
