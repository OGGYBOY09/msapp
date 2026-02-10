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
 * @author Acer Aspire Lite 15
 */
public class PKatBarang extends javax.swing.JPanel {

    /**
     * Creates new form KategoriServis
     */
    private boolean isEditMode = false;

    public PKatBarang() {
        initComponents();
        load_table();
        auto_number();
        bersihkan(); 
        initKeyShortcuts();
        
        // Contoh pengaturan manual untuk JTable (misal namanya tblKategori)
// Mengatur tinggi baris agar teks tidak sesak
tblKatServis.setRowHeight(30); 

// Menghilangkan garis agar tampilan lebih bersih (Clean)
tblKatServis.setShowGrid(false); 
tblKatServis.setIntercellSpacing(new java.awt.Dimension(0, 0));

// Pengaturan Header (Judul Kolom)
tblKatServis.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
tblKatServis.getTableHeader().setOpaque(false);
tblKatServis.getTableHeader().setBackground(new java.awt.Color(32, 136, 203)); // Biru Header

// INI PERBAIKANNYA: Mengatur warna teks judul menjadi HITAM
tblKatServis.getTableHeader().setForeground(java.awt.Color.BLACK); 

// Opsional: Jika teks header masih membandel tidak mau hitam di beberapa tema OS, 
// gunakan baris ini untuk memaksa perataan dan warna:
((javax.swing.table.DefaultTableCellRenderer)tblKatServis.getTableHeader().getDefaultRenderer())
    .setHorizontalAlignment(javax.swing.JLabel.CENTER);    }
    
    
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
                jTextField1.requestFocus();
                if (jButton1.isEnabled()) jButton1.doClick();
            }
        });

        // 3. F3 -> Button Refresh
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
        am.put("cmdRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton2.isEnabled()) jButton2.doClick();
            }
        });

        // 4. F1 -> Button Edit
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "cmdEdit");
        am.put("cmdEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton3.isEnabled()) jButton3.doClick();
            }
        });

        // 5. DELETE (DEL) -> Button Hapus
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cmdHapus");
        am.put("cmdHapus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jButton4.isEnabled()) jButton4.doClick();
            }
        });
    }
    
    // --- 1. LOGIKA AUTO NUMBER ID (tbl_kat_barang) ---
    private void auto_number() {
        try {
            Connection conn = Koneksi.configDB();
            // Mengambil ID terbesar dari tabel tbl_kat_barang
            String sql = "SELECT MAX(id_kategori) FROM tbl_kat_barang";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            if (res.next()) {
                int maxId = res.getInt(1);
                // ID + 1
                tfIdKatServis.setText(String.valueOf(maxId + 1));
            } else {
                tfIdKatServis.setText("1");
            }
            
            tfIdKatServis.setEditable(false); 
            
        } catch (Exception e) {
            tfIdKatServis.setText("1");
        }
    }

    // --- 2. LOAD TABEL ---
    private void load_table() {
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No");
        model.addColumn("ID Kategori");
        model.addColumn("Nama Kategori");
        model.addColumn("Keterangan");

        try {
            String sql = "SELECT * FROM tbl_kat_barang";
            
            // Logika Pencarian (jTextField1 adalah kolom cari di desainmu)
            String cari = jTextField1.getText();
            if(!cari.isEmpty()){
                sql += " WHERE nama_kategori LIKE '%" + cari + "%' OR keterangan LIKE '%" + cari + "%'";
            }
            
            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            ResultSet res = stm.executeQuery(sql);
            
            int no = 1;
            while (res.next()) {
                model.addRow(new Object[]{
                    no++,
                    res.getString("id_kategori"),
                    res.getString("nama_kategori"),
                    res.getString("keterangan")
                });
            }
            // Menggunakan tblKatServis (Nama variabel tabel di desainmu)
            tblKatServis.setModel(model);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }

    // --- 3. BERSIHKAN FORM ---
    private void bersihkan() {
        tfNmKatServis.setText("");
        tfKetKatServis.setText("");
        jTextField1.setText(""); // Kolom cari
        
        isEditMode = false;
        btnSimpan.setText("SIMPAN");
        tfIdKatServis.setEditable(false);
        auto_number(); 
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
        jLabel3 = new javax.swing.JLabel();
        tfIdKatServis = new javax.swing.JTextField();
        tfNmKatServis = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tfKetKatServis = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKatServis = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(4, 102, 200));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 26)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH KATEGORI SPAREPART");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 450, 70));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("ID Kategori :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 200, 40));

        tfIdKatServis.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tfIdKatServis, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 390, 50));

        tfNmKatServis.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tfNmKatServis, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, 390, 50));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Nama Kategori :");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 200, 40));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("Keterangan :");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 300, 200, 40));

        tfKetKatServis.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tfKetKatServis, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, 390, 50));

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);
        jPanel1.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(27, 430, 390, 62));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 450, 930));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setBackground(new java.awt.Color(4, 102, 200));
        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("DAFTAR KATEGORI SPAREPART");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel2.setOpaque(true);
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1220, 70));

        tblKatServis.setFont(new java.awt.Font("Segoe UI Historic", 0, 14)); // NOI18N
        tblKatServis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "NAMA KATEGORI", "KETERANGAN"
            }
        ));
        tblKatServis.setRowHeight(35);
        jScrollPane1.setViewportView(tblKatServis);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 1170, 730));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel3.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 450, 40));

        jButton1.setBackground(new java.awt.Color(204, 204, 204));
        jButton1.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jButton1.setText("CARI [F2]");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 100, 150, 40));

        jButton2.setBackground(new java.awt.Color(204, 204, 204));
        jButton2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jButton2.setText("REFRESH [F3]");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel3.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 100, 170, 40));

        jButton3.setBackground(new java.awt.Color(255, 255, 102));
        jButton3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jButton3.setText("EDIT [F1]");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel3.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 100, 150, 40));

        jButton4.setBackground(new java.awt.Color(255, 0, 51));
        jButton4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jButton4.setText("HAPUS [Del]");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel3.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 100, 150, 40));

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 1220, 930));
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        String nama = tfNmKatServis.getText();
        String ket = tfKetKatServis.getText();
        String id = tfIdKatServis.getText();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Kategori tidak boleh kosong!");
            return;
        }

        try {
            Connection conn = Koneksi.configDB();
            PreparedStatement pst;
            
            if (isEditMode) {
                // UPDATE
                String sql = "UPDATE tbl_kat_barang SET nama_kategori=?, keterangan=? WHERE id_kategori=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setString(2, ket);
                pst.setString(3, id);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data Berhasil Diubah");
            } else {
                // INSERT
                String sql = "INSERT INTO tbl_kat_barang (id_kategori, nama_kategori, keterangan) VALUES (?,?,?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, id);
                pst.setString(2, nama);
                pst.setString(3, ket);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan");
            }
            
            load_table();
            bersihkan();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        load_table();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        int row = tblKatServis.getSelectedRow();
        if (row != -1) {
            // Ambil data dari tabel ke form
            tfIdKatServis.setText(tblKatServis.getValueAt(row, 1).toString());
            tfNmKatServis.setText(tblKatServis.getValueAt(row, 2).toString());
            tfKetKatServis.setText(tblKatServis.getValueAt(row, 3).toString());
            
            // Ubah mode jadi EDIT
            isEditMode = true;
            btnSimpan.setText("UBAH");
            tfIdKatServis.setEditable(false);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan diedit terlebih dahulu!");
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        bersihkan();
        load_table();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        int row = tblKatServis.getSelectedRow();
        if (row != -1) {
            String id = tblKatServis.getValueAt(row, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus kategori ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Connection conn = Koneksi.configDB();
                    String sql = "DELETE FROM tbl_kat_barang WHERE id_kategori=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, id);
                    pst.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Data Berhasil Dihapus");
                    load_table();
                    bersihkan();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Gagal Hapus (Mungkin data sedang digunakan): " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris data yang ingin dihapus terlebih dahulu");
        }
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTable tblKatServis;
    private javax.swing.JTextField tfIdKatServis;
    private javax.swing.JTextField tfKetKatServis;
    private javax.swing.JTextField tfNmKatServis;
    // End of variables declaration//GEN-END:variables
}
