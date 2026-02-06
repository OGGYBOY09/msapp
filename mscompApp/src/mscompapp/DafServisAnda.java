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
            // QUERY JOIN: Hanya ambil servis yang dikerjakan oleh User Login (Session.idUser)
            String sql = "SELECT s.id_servis, s.tanggal_masuk, p.nama_pelanggan, " +
                         "s.jenis_barang, s.merek, s.keluhan_awal, s.status " +
                         "FROM perbaikan pb " +
                         "JOIN servis s ON pb.id_servis = s.id_servis " +
                         "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "WHERE pb.id_teknisi = ? "; // Filter Wajib (Tugas Saya)
            
            // Filter Pencarian (Keyword)
            String cari = tfCari.getText();
            if(!cari.isEmpty()) {
                sql += "AND (s.id_servis LIKE '%" + cari + "%' " +
                       "OR p.nama_pelanggan LIKE '%" + cari + "%' " +
                       "OR s.jenis_barang LIKE '%" + cari + "%' " +
                       "OR s.merek LIKE '%" + cari + "%') ";
            }
            
            // Filter Status
            String status = cbStatus.getSelectedItem().toString();
            if(!"Semua".equals(status)) {
                sql += "AND s.status = '" + status + "' ";
            }
            
            // Filter Bulan (Berdasarkan JDateChooser)
            if(jDateChooser1.getDate() != null) {
                SimpleDateFormat sdfBulan = new SimpleDateFormat("MM");
                SimpleDateFormat sdfTahun = new SimpleDateFormat("yyyy");
                String bulan = sdfBulan.format(jDateChooser1.getDate());
                String tahun = sdfTahun.format(jDateChooser1.getDate());
                
                sql += "AND MONTH(s.tanggal_masuk) = '" + bulan + "' " +
                       "AND YEAR(s.tanggal_masuk) = '" + tahun + "' ";
            }
            
            sql += "ORDER BY s.tanggal_masuk DESC";
            
            Connection conn = Koneksi.configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, Session.idUser); // Isi param id_teknisi
            
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

        // ENTER -> Buka Detail
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cmdDetail");
        am.put("cmdDetail", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnDetail.isEnabled()) bukaDetail();
            }
        });
        
        // F5 -> Refresh
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "cmdRefresh");
        am.put("cmdRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnRefresh.isEnabled()) btnRefresh.doClick();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(3, 83, 164));

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Daftar Servis Yang Anda Kerjakan");

        lblNmTeknisi.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblNmTeknisi.setForeground(new java.awt.Color(255, 255, 255));
        lblNmTeknisi.setText("Nama Teknisi");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblNmTeknisi)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblNmTeknisi)
                        .addGap(0, 31, Short.MAX_VALUE)))
                .addContainerGap())
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, 1718, -1));

        tfCari.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 89, 418, 47));

        btnCari.setBackground(new java.awt.Color(255, 255, 102));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCari.setText("Cari[F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(443, 89, 105, 47));

        btnDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDetail.setText("Detail");
        btnDetail.addActionListener(this::btnDetailActionPerformed);
        add(btnDetail, new org.netbeans.lib.awtextra.AbsoluteConstraints(554, 89, 105, 47));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("Refresh [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(665, 89, -1, 47));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Tanggal :");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1290, 100, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Status :");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1508, 102, -1, -1));

        cbStatus.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cbStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1589, 89, 124, 47));

        tblSerAnda.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblSerAnda.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Id Servis", "Tanggal Masuk", "Nama", "Jenis Barang", "Keluhan", "Kelengkapan", "Harga Servis", "Status"
            }
        ));
        tblSerAnda.setRowHeight(35);
        jScrollPane1.setViewportView(tblSerAnda);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 154, 1706, 650));
        add(jDateChooser1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1381, 89, 115, 47));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(1446, Short.MAX_VALUE)
                .addComponent(btnNextKiri)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextKanan)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNextKanan, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                    .addComponent(btnNextKiri, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 810, 1680, 60));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        cbStatus.setSelectedIndex(0); // Kembali ke Semua
        jDateChooser1.setDate(null);  // Reset tanggal
        tampilData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        bukaDetail();
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextKiriActionPerformed


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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblNmTeknisi;
    private javax.swing.JTable tblSerAnda;
    private javax.swing.JTextField tfCari;
    // End of variables declaration//GEN-END:variables
}
