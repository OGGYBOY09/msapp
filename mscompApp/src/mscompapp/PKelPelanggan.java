/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


/**
 *
 * @author ASUS
 */
public class PKelPelanggan extends javax.swing.JPanel {

    /**
     * Creates new form PKelBarang
     */
    private boolean isEdit = false;

    public PKelPelanggan() {
        initComponents();
        resetForm();
        initKeyShortcuts();
    }
    
    
    
    private void initKeyShortcuts() {
        // Menggunakan WHEN_IN_FOCUSED_WINDOW agar shortcut jalan dimanapun fokus kursor berada
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // 1. ENTER -> Button Simpan
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cmdSimpan");
        am.put("cmdSimpan", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnSimpan.isEnabled()) btnSimpan.doClick();
            }
        });

        // 2. F2 -> Button Cari
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "cmdCari");
        am.put("cmdCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Opsional: Fokuskan ke textfield cari juga agar UX lebih enak
                tfCari.requestFocus();
                if (btnCari.isEnabled()) btnCari.doClick();
            }
        });

        // 3. F3 -> Button Refresh
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
        am.put("cmdRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnRefresh.isEnabled()) btnRefresh.doClick();
            }
        });

        // 4. F1 -> Button Edit
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "cmdEdit");
        am.put("cmdEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnEdit.isEnabled()) btnEdit.doClick();
            }
        });

        // 5. DELETE (DEL) -> Button Hapus
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cmdHapus");
        am.put("cmdHapus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnHapus.isEnabled()) btnHapus.doClick();
            }
        });
    }

    // --- 1. RESET FORM ---
    private void resetForm() {
        tNama.setText("");
        tNoHp.setText("");
        tAlamat.setText("");
        tfCari.setText("");
        
        load_table();
        auto_number(); 
        
        btnSimpan.setText("SIMPAN");
        tIdPelanggan.setEditable(false);
        isEdit = false;
    }

    // --- 2. AUTO NUMBER (B001) ---
    private void auto_number() {
        try {
            Connection conn = Koneksi.configDB();
            // Mengambil ID terbesar dari tabel tbl_kat_barang
            String sql = "SELECT MAX(id_pelanggan) FROM tbl_pelanggan";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            if (res.next()) {
                int maxId = res.getInt(1);
                // ID + 1
                tIdPelanggan.setText(String.valueOf(maxId + 1));
            } else {
                tIdPelanggan.setText("1");
            }
            
            tIdPelanggan.setEditable(false); 
            
        } catch (Exception e) {
            tIdPelanggan.setText("1");
        }
    }

    // --- 4. LOAD TABLE (LANGSUNG DARI TBL_BARANG) ---
    private void load_table() {
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No");
        model.addColumn("ID");
        model.addColumn("Nama");
        model.addColumn("No HP");
        model.addColumn("Alamat");

        try {
            // TIDAK PERLU JOIN LAGI
            // Karena nama kategori sudah tersimpan langsung di tabel barang
            String sql = "SELECT * FROM tbl_pelanggan ";
            
            if (!tfCari.getText().isEmpty()) {
                sql += "WHERE nama_pelanggan LIKE '%" + tfCari.getText() + "%' ";
            }
            
            sql += "ORDER BY id_pelanggan ASC";

            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_pelanggan"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"), // Pastikan nama kolom di DB 'kategori'
                    rs.getString("alamat")

                });
            }
            tPlgn.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Load Table: " + e.getMessage());
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

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tIdPelanggan = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tNama = new javax.swing.JTextField();
        tNoHp = new javax.swing.JTextField();
        tAlamat = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tPlgn = new javax.swing.JTable();
        tfCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();

        // SETUP LAYOUT UTAMA (RESPONSIF)
        setBackground(new java.awt.Color(255, 255, 255));
        this.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc;

        // ==========================================
        // PANEL KIRI (TAMBAH PELANGGAN) - LEBAR TETAP
        // ==========================================
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setPreferredSize(new java.awt.Dimension(340, 620));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(4, 102, 200));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); 
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH PELANGGAN");
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
        );
        jPanel3.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, 340, 40));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
        jLabel3.setText("ID Pelanggan :");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 160, 40));
        tIdPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); 
        tIdPelanggan.addActionListener(this::tIdPelangganActionPerformed);
        jPanel3.add(tIdPelanggan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 280, 40));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
        jLabel4.setText("Nama Pelanggan :");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, 160, 40));
        tNama.setFont(new java.awt.Font("Segoe UI", 0, 18)); 
        tNama.addActionListener(this::tNamaActionPerformed);
        jPanel3.add(tNama, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 280, 40));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
        jLabel5.setText("No HP :");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, 160, 40));
        tNoHp.setFont(new java.awt.Font("Segoe UI", 0, 18)); 
        tNoHp.addActionListener(this::tNoHpActionPerformed);
        jPanel3.add(tNoHp, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, 280, 40));

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
        jLabel8.setText("Alamat :");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 160, 40));
        tAlamat.setFont(new java.awt.Font("Segoe UI", 0, 18)); 
        jPanel3.add(tAlamat, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 280, 40));

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); 
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);
        jPanel3.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 440, 280, 50));

        // Tambahkan Panel Kiri ke Layout Utama
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(10, 10, 10, 5);
        gbc.fill = java.awt.GridBagConstraints.VERTICAL;
        add(jPanel3, gbc);

        // ==========================================
        // PANEL KANAN (DAFTAR PELANGGAN) - RESPONSIF
        // ==========================================
        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        // Header (Baris 0)
        jPanel2.setBackground(new java.awt.Color(4, 102, 200));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); 
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("DAFTAR PELANGGAN");
        
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
        );

        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        jPanel4.add(jPanel2, gbc);

        // Toolbar Cari & Tombol (Baris 1)
gbc = new java.awt.GridBagConstraints();
gbc.gridy = 1;
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
gbc.anchor = java.awt.GridBagConstraints.WEST; // Memastikan barisan menempel ke kiri

// --- Text Field Cari (Dibatasi lebarnya agar tidak terlalu melar) ---
tfCari.setText("Cari....");
tfCari.addFocusListener(new java.awt.event.FocusAdapter() {
    public void focusGained(java.awt.event.FocusEvent evt) {
        tfCariFocusGained(evt);
    }
});
tfCari.addActionListener(this::tfCariActionPerformed);
gbc.gridx = 0; 
gbc.weightx = 0.2; // Nilai kecil agar tidak menghabiskan ruang
gbc.insets = new java.awt.Insets(10, 10, 5, 5);
tfCari.setPreferredSize(new java.awt.Dimension(180, 30)); 
jPanel4.add(tfCari, gbc);

gbc.weightx = 0; // Kembalikan ke 0 untuk tombol agar ukurannya tetap (Fixed)

// --- Tombol CARI [F2] ---
gbc.gridx = 1; 
btnCari.setBackground(new java.awt.Color(204, 204, 204));
btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
btnCari.setText("CARI [F2]");
btnCari.setPreferredSize(new java.awt.Dimension(110, 30));
btnCari.addActionListener(this::btnCariActionPerformed);
jPanel4.add(btnCari, gbc);

// --- Tombol REFRESH [F3] (DIPERLEBAR) ---
gbc.gridx = 2;
btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
btnRefresh.setText("REFRESH [F3]");
btnRefresh.setPreferredSize(new java.awt.Dimension(150, 30)); // Ditambah agar teks aman
btnRefresh.addActionListener(this::btnRefreshActionPerformed);
jPanel4.add(btnRefresh, gbc);

// --- Tombol EDIT [F1] ---
gbc.gridx = 3;
btnEdit.setBackground(new java.awt.Color(255, 255, 102));
btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
btnEdit.setText("EDIT [F1]");
btnEdit.setPreferredSize(new java.awt.Dimension(110, 30));
btnEdit.addActionListener(this::btnEditActionPerformed);
jPanel4.add(btnEdit, gbc);

// --- Tombol HAPUS [Del] (DIPERLEBAR) ---
gbc.gridx = 4;
btnHapus.setBackground(new java.awt.Color(255, 51, 51));
btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); 
btnHapus.setForeground(new java.awt.Color(255, 255, 255));
btnHapus.setText("HAPUS [Del]");
btnHapus.setPreferredSize(new java.awt.Dimension(200, 30)); // Ditambah agar teks aman
btnHapus.addActionListener(this::btnHapusActionPerformed);
gbc.insets = new java.awt.Insets(10, 5, 5, 10);
jPanel4.add(btnHapus, gbc);

// --- SPACER TRANSPARAN (PENTING) ---
// Ini berfungsi untuk mendorong semua komponen di atas ke arah kiri
gbc.gridx = 5;
gbc.weightx = 1.0; 
jPanel4.add(new javax.swing.JLabel(""), gbc);

        // Tabel (Baris 2)
        tPlgn.setFont(new java.awt.Font("Segoe UI", 0, 14)); 
        tPlgn.setRowHeight(35);
        jScrollPane1.setViewportView(tPlgn);

        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Ini yang menarik tabel memenuhi sisa ruang bawah
        gbc.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jScrollPane1, gbc);

        // Tambahkan Panel Kanan ke Layout Utama
        gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0; 
        gbc.weighty = 1.0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(10, 5, 10, 10);
        add(jPanel4, gbc);
    }// </editor-fold>//GEN-END:initComponents

    private void tIdPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tIdPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tIdPelangganActionPerformed

    private void tNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNamaActionPerformed

    private void tfCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCariActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tfCariActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        load_table();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        try {
            String id = tIdPelanggan.getText();
            String nama = tNama.getText();
            String noHp = tNoHp.getText();
            String alamat = tAlamat.getText();
            
            
            Connection conn = Koneksi.configDB();
            String sql;
            PreparedStatement ps;

            if (isEdit) {
                // UPDATE: Simpan 'namaKategori' langsung ke kolom 'kategori'
                sql = "UPDATE tbl_pelanggan SET nama_pelanggan=?, no_hp=?, alamat=? WHERE id_pelanggan=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, nama);
                ps.setString(2, noHp); 
                ps.setString(3, alamat);
                ps.setString(4, id);
                
            } else {
                // INSERT: Simpan 'namaKategori' langsung
                sql = "INSERT INTO tbl_pelanggan (id_pelanggan, nama_pelanggan, no_hp, alamat) VALUES (?,?,?,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.setString(2, nama);
                ps.setString(3, noHp); // Masukkan String Nama
                ps.setString(4, alamat);
            }

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Berhasil Disimpan!");
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = tPlgn.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris tabel dulu!");
            return;
        }

        tIdPelanggan.setText(tPlgn.getValueAt(row, 1).toString());
        tNama.setText(tPlgn.getValueAt(row, 2).toString());
        
        tNoHp.setText(tPlgn.getValueAt(row, 3).toString());
        tAlamat.setText(tPlgn.getValueAt(row, 4).toString());

        isEdit = true;
        btnSimpan.setText("UBAH");
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        int row = tPlgn.getSelectedRow();
        if (row == -1) return;

        String id = tPlgn.getValueAt(row, 1).toString();
        int tanya = JOptionPane.showConfirmDialog(this, "Hapus " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (tanya == JOptionPane.YES_OPTION) {
            try {
                Connection conn = Koneksi.configDB();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_pelanggan WHERE id_pelanggan=?");
                ps.setString(1, id);
                ps.executeUpdate();
                resetForm();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        resetForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tfCariFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfCariFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCariFocusGained

    private void tNoHpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNoHpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNoHpActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField tAlamat;
    private javax.swing.JTextField tIdPelanggan;
    private javax.swing.JTextField tNama;
    private javax.swing.JTextField tNoHp;
    private javax.swing.JTable tPlgn;
    private javax.swing.JTextField tfCari;
    // End of variables declaration//GEN-END:variables
}
