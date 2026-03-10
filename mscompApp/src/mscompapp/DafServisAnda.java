/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// IMPORT UNTUK SHORTCUT & EVENT
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
/**
 *
 * @author Acer Aspire Lite 15
 */
public class DafServisAnda extends javax.swing.JPanel {

    /**
     * Creates new form DafServisAnda
     */
    public DafServisAnda() {
        initComponents();
        
        // Set Nama Teknisi di Label
        lblNmTeknisi.setText("Teknisi: " + Session.namaUser);
        
        // Setup Tabel (Custom Warna Status)
        setupTableRenderer();
        
        // Load Pilihan Status
        loadComboStatus();
        
        // Load Data Awal
        tampilData();
        
        // Listener Filter
        addListeners();
        
        // Shortcut Keyboard
        initKeyShortcuts();
    }
    
    // --- 1. SETUP RENDERER TABEL (WARNA STATUS) ---
    private void setupTableRenderer() {
        
        
        tblSerAnda = new javax.swing.JTable() {
            
            {
        setRowHeight(30); // Ubah angka 30 sesuai keinginanmu (semakin besar semakin tinggi)
        getTableHeader().setReorderingAllowed(false); // Opsional: Biar kolom gak bisa digeser-geser
    }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
                Object statusObj = getValueAt(row, 6); // Asumsi Kolom 6 adalah Status
                String status = (statusObj != null) ? statusObj.toString() : "";

                if (isRowSelected(row)) {
                    comp.setBackground(getSelectionBackground());
                    comp.setForeground(getSelectionForeground());
                } else {
                    switch (status) {
                        case "Proses":
                            comp.setBackground(java.awt.Color.YELLOW);
                            comp.setForeground(java.awt.Color.BLACK); break;
                        case "Selesai":
                            comp.setBackground(new java.awt.Color(144, 238, 144)); // Hijau Muda
                            comp.setForeground(java.awt.Color.BLACK); break;
                        case "Dibatalkan":
                            comp.setBackground(new java.awt.Color(255, 182, 193)); // Merah Muda
                            comp.setForeground(java.awt.Color.BLACK); break;
                        default:
                            comp.setBackground(java.awt.Color.WHITE);
                            comp.setForeground(java.awt.Color.BLACK); break;
                    }
                }
                return comp;
            }
        };
        jScrollPane1.setViewportView(tblSerAnda);
    }
    
    // --- 2. SETUP COMBOBOX & LISTENER ---
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("Semua");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
    }
    
    private void addListeners() {
        // Listener saat Tanggal Berubah -> Filter Bulan
        if(jDateChooser1.getDateEditor() != null) {
            jDateChooser1.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("date".equals(evt.getPropertyName())) {
                        tampilData();
                    }
                }
            });
        }
        
        // Listener saat Status Berubah
        cbStatus.addItemListener(e -> tampilData());
    }

    // --- 3. LOAD DATA UTAMA (QUERY) ---
   private void tampilData() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        model.addColumn("ID Servis");
        model.addColumn("Tanggal");
        model.addColumn("Pelanggan");
        model.addColumn("Barang");
        model.addColumn("Merek");
        model.addColumn("Keluhan");
        model.addColumn("Status");
        
        try {
            // 1. QUERY DASAR
            String sql = "SELECT s.id_servis, s.tanggal_masuk, p.nama_pelanggan, " +
                         "s.jenis_barang, s.merek, s.keluhan_awal, s.status " +
                         "FROM perbaikan pb " +
                         "JOIN servis s ON pb.id_servis = s.id_servis " +
                         "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "WHERE pb.id_teknisi = ? ";
            
            // 2. FILTER PENCARIAN (KEYWORD)
            String cari = tfCari.getText();
            if(!cari.isEmpty()) {
                sql += "AND (s.id_servis LIKE '%" + cari + "%' " +
                       "OR p.nama_pelanggan LIKE '%" + cari + "%' " +
                       "OR s.jenis_barang LIKE '%" + cari + "%' " +
                       "OR s.merek LIKE '%" + cari + "%') ";
            }
            
            // 3. FILTER STATUS
            String status = cbStatus.getSelectedItem().toString();
            if(!"Semua".equals(status)) {
                sql += "AND s.status = '" + status + "' ";
            }
            
            // 4. FILTER TANGGAL PRESISI (PERBAIKAN)
            if(jDateChooser1.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String tanggalPilih = sdf.format(jDateChooser1.getDate());
                sql += "AND DATE(s.tanggal_masuk) = '" + tanggalPilih + "' ";
            }
            
            sql += "ORDER BY s.tanggal_masuk DESC";
            
            Connection conn = Koneksi.configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, Session.idUser); 
            
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[] {
                    rs.getString("id_servis"),
                    rs.getString("tanggal_masuk"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("jenis_barang"),
                    rs.getString("merek"),
                    rs.getString("keluhan_awal"),
                    rs.getString("status")
                });
            }
            
            tblSerAnda.setModel(model);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }
    
    // --- 4. MEMBUKA DETAIL SERVICE ---
    private void bukaDetail() {
        int row = tblSerAnda.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis di tabel dulu!");
            return;
        }
        
        String idServis = tblSerAnda.getValueAt(row, 0).toString();
        
        try {
            Connection conn = Koneksi.configDB();
            // Ambil Data Lengkap untuk dikirim ke Form Detail
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat " +
                         "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "WHERE s.id_servis = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idServis);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                new DetailService(
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
                    rs.getString("status"),
                    rs.getString("status_barang"),
                    Session.idUser 
                ).setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error buka detail: " + e.getMessage());
        }
    }
    
    // --- 5. SHORTCUT KEYBOARD ---
    private void initKeyShortcuts() {
    InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = this.getActionMap();

    
    // 2. F2 -> Fokus ke pencarian dan jalankan Cari
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "cmdCari");
    am.put("cmdCari", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            tfCari.requestFocusInWindow(); // Opsional: arahkan kursor ke kotak cari
            tampilData();
        }
    });

    // 3. F3 -> Refresh Data
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
    am.put("cmdRefresh", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Memanggil langsung logika refresh yang sudah kamu buat
            tfCari.setText("");
            cbStatus.setSelectedIndex(0);
            jDateChooser1.setDate(null);
            tampilData();
        }
    });
    
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    // 1. Inisialisasi Komponen (Variabel asli Anda)
    jPanel3 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    lblNmTeknisi = new javax.swing.JLabel();
    tfCari = new javax.swing.JTextField();
    btnCari = new javax.swing.JButton();
    btnDetail = new javax.swing.JButton();
    btnRefresh = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    cbStatus = new javax.swing.JComboBox<>();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblSerAnda = new javax.swing.JTable();
    jDateChooser1 = new com.toedter.calendar.JDateChooser();
    jPanel2 = new javax.swing.JPanel();
    btnNextKanan = new javax.swing.JButton();
    btnNextKiri = new javax.swing.JButton();

    // Setup Dasar Layout Utama
    this.setBackground(new java.awt.Color(245, 247, 251));
    this.setLayout(new java.awt.BorderLayout());

    // --- HEADER (Warna Biru Sesuai Kode Awal) ---
    jPanel1.setBackground(new java.awt.Color(3, 83, 164));
    jPanel1.setPreferredSize(new java.awt.Dimension(1160, 60));
    
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); 
    jLabel1.setForeground(new java.awt.Color(255, 255, 255));
    jLabel1.setText("Daftar Servis Yang Anda Kerjakan");

    lblNmTeknisi.setFont(new java.awt.Font("Segoe UI", 1, 14)); 
    lblNmTeknisi.setForeground(new java.awt.Color(255, 255, 255));
    lblNmTeknisi.setText("Nama Teknisi");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(25, 25, 25)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 700, Short.MAX_VALUE)
            .addComponent(lblNmTeknisi)
            .addGap(25, 25, 25))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jLabel1, -1, 60, Short.MAX_VALUE)
        .addComponent(lblNmTeknisi, -1, -1, Short.MAX_VALUE)
    );

    // --- PANEL CONTENT (Putih) ---
    jPanel3.setBackground(new java.awt.Color(255, 255, 255));
    jPanel3.setLayout(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

    // --- BARIS FILTER & BUTTONS ---
    javax.swing.JPanel filterBar = new javax.swing.JPanel(new java.awt.GridBagLayout());
    filterBar.setOpaque(false);
    java.awt.GridBagConstraints fGbc = new java.awt.GridBagConstraints();
    fGbc.insets = new java.awt.Insets(10, 5, 10, 5);
    
    // Setup Teks & Style Button agar tidak hilang
    tfCari.setPreferredSize(new java.awt.Dimension(150, 35));
    
    btnCari.setBackground(new java.awt.Color(255, 255, 102));
    btnCari.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnCari.setText("Cari [F2]"); // Set teks eksplisit
    btnCari.setPreferredSize(new java.awt.Dimension(100, 35));

    btnDetail.setBackground(new java.awt.Color(204, 204, 204));
    btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnDetail.setText("Detail"); // Set teks eksplisit
    btnDetail.setPreferredSize(new java.awt.Dimension(100, 35));

    btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
    btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnRefresh.setText("Refresh [F3]"); // Set teks eksplisit
    btnRefresh.setPreferredSize(new java.awt.Dimension(150, 35));

    jLabel2.setText("Tanggal :");
    jLabel3.setText("Status :");

    // Tata Letak Filter (Responsif)
    fGbc.gridx = 0; fGbc.weightx = 1.0; fGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    filterBar.add(tfCari, fGbc);

    fGbc.gridx = 1; fGbc.weightx = 0; fGbc.fill = java.awt.GridBagConstraints.NONE;
    filterBar.add(btnCari, fGbc);

    fGbc.gridx = 2;
    filterBar.add(btnDetail, fGbc);

    fGbc.gridx = 3;
    filterBar.add(btnRefresh, fGbc);

    fGbc.gridx = 4; fGbc.insets = new java.awt.Insets(10, 20, 10, 5);
    filterBar.add(jLabel2, fGbc);

    fGbc.gridx = 5; fGbc.ipadx = 100; fGbc.insets = new java.awt.Insets(10, 5, 10, 5);
    filterBar.add(jDateChooser1, fGbc);

    fGbc.gridx = 6; fGbc.ipadx = 0;
    filterBar.add(jLabel3, fGbc);

    fGbc.gridx = 7; fGbc.ipadx = 80;
    cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua", "Proses", "Selesai", "Batal" }));
    filterBar.add(cbStatus, fGbc);

    // Masukkan Filter ke Panel Utama
    gbc.gridx = 0; gbc.gridy = 0; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0; gbc.insets = new java.awt.Insets(15, 15, 10, 15);
    jPanel3.add(filterBar, gbc);

    // --- TABEL ---
    tblSerAnda.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] { "No", "Id Servis", "Tanggal Masuk", "Nama", "Jenis Barang", "Keluhan", "Kelengkapan", "Harga Servis", "Status" }
    ));
    tblSerAnda.setRowHeight(35);
    jScrollPane1.setViewportView(tblSerAnda);

    gbc.gridy = 1; gbc.fill = java.awt.GridBagConstraints.BOTH; gbc.weighty = 1.0;
    gbc.insets = new java.awt.Insets(0, 15, 10, 15);
    jPanel3.add(jScrollPane1, gbc);

    // --- FOOTER (Navigasi) ---
    jPanel2.setOpaque(false);
    btnNextKiri.setText("PREV");
    btnNextKanan.setText("NEXT");
    
    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup()
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap(-1, Short.MAX_VALUE)
            .addComponent(btnNextKiri, 100, 100, 100)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(btnNextKanan, 100, 100, 100))
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(btnNextKanan, 35, 35, 35)
            .addComponent(btnNextKiri, 35, 35, 35))
    );

    gbc.gridy = 2; gbc.weighty = 0; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gbc.insets = new java.awt.Insets(5, 15, 15, 15);
    jPanel3.add(jPanel2, gbc);

    // Final Gabung
    this.add(jPanel1, java.awt.BorderLayout.NORTH);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);

    // Action Listeners (Tetap terhubung ke fungsi asli Anda)
    tfCari.addActionListener(this::tfCariActionPerformed);
    btnCari.addActionListener(this::btnCariActionPerformed);
    btnDetail.addActionListener(this::btnDetailActionPerformed);
    btnRefresh.addActionListener(this::btnRefreshActionPerformed);
    cbStatus.addActionListener(this::cbStatusActionPerformed);
    btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
    btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
}// </editor-fold>//GEN-END:initComponents

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        bukaDetail();
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        cbStatus.setSelectedIndex(0); // Kembali ke Semua
        jDateChooser1.setDate(null);  // Reset tanggal
        tampilData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbStatusActionPerformed

    private void tfCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCariActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbStatus;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblNmTeknisi;
    private javax.swing.JTable tblSerAnda;
    private javax.swing.JTextField tfCari;
    // End of variables declaration//GEN-END:variables
}
