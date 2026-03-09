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
public class PKatService extends javax.swing.JPanel {

    /**
     * Creates new form PKatService
     */
    private boolean isEditMode = false;
    
    public PKatService() {
        initComponents();
        load_table();
        auto_number();
        bersihkan(); 
        initKeyShortcuts();
    }
    
    private void initKeyShortcuts() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // ENTER -> btnSimpan
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cmdSimpan");
        am.put("cmdSimpan", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnSimpan.isEnabled()) btnSimpan.doClick();
            }
        });

        // F1 -> btn_edit
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "cmdEdit");
        am.put("cmdEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btn_edit.isEnabled()) btn_edit.doClick();
            }
        });

        // DEL -> btnDelete
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cmdDelete");
        am.put("cmdDelete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnDelete.isEnabled()) btnDelete.doClick();
            }
        });

        // F2 -> btn_cari
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "cmdCari");
        am.put("cmdCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tf_cari.requestFocus();
                if (btn_cari.isEnabled()) btn_cari.doClick();
            }
        });

        // F3 -> btn_refresh
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
        am.put("cmdRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btn_refresh.isEnabled()) btn_refresh.doClick();
            }
        });
    }
    
    // --- 1. LOGIKA AUTO NUMBER ID ---
    private void auto_number() {
        try {
            Connection conn = Koneksi.configDB();
            // Mengambil ID terbesar dari tabel tbl_jenis_perangkat
            String sql = "SELECT MAX(id_kategori) FROM tbl_jenis_perangkat";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(sql);
            
            if (res.next()) {
                int maxId = res.getInt(1);
                // Jika data kosong (maxId = 0), mulai dari 1. Jika ada, tambah 1
                tfIdKatBarang.setText(String.valueOf(maxId + 1));
            } else {
                tfIdKatBarang.setText("1");
            }
            
            tfIdKatBarang.setEditable(false); // ID tidak boleh diedit manual
            
        } catch (Exception e) {
            tfIdKatBarang.setText("1"); // Default jika error/tabel baru
        }
    }

    // --- 2. LOAD TABEL (TAMPIL DATA) ---
    private void load_table() {
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No");
        model.addColumn("ID Jenis");
        model.addColumn("Nama Jenis Perangkat");
        model.addColumn("Keterangan");

        try {
            String sql = "SELECT * FROM tbl_jenis_perangkat";
            
            // Logika Pencarian
            String cari = tf_cari.getText();
            if(!cari.isEmpty()){
                sql += " WHERE nama_jenis LIKE '%" + cari + "%' OR keterangan LIKE '%" + cari + "%'";
            }
            
            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            ResultSet res = stm.executeQuery(sql);
            
            int no = 1;
            while (res.next()) {
                model.addRow(new Object[]{
                    no++,
                    res.getString("id_kategori"),
                    res.getString("nama_jenis"),
                    res.getString("keterangan")
                });
            }
            tblKatBarang.setModel(model);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
        aturKolomTabel();
    }

    // --- 3. BERSIHKAN FORM ---
    private void bersihkan() {
        tfNmKatBarang.setText("");
        tfKetKatBarang.setText("");
        tf_cari.setText("");
        
        isEditMode = false;
        btnSimpan.setText("SIMPAN");
        tfIdKatBarang.setEditable(false);
        auto_number(); // Reset ID ke auto number terbaru
        load_table(); // <--- TAMBAHKAN INI agar tabel langsung terupdate (Real-Time)
    }

    private void aturKolomTabel() {
    if (tblKatBarang.getColumnCount() > 0) {
        // Tentukan lebar minimal agar teks tidak terpotong (...)
        // No (50), ID Jenis (100), Nama Jenis (350), Keterangan (400)
        int[] lebarMinimal = {50, 100, 350, 400};
        int totalLebarMinimal = 900; 

        int lebarWadah = jScrollPane1.getViewport().getWidth();

        // KUNCI UTAMA: Jika lebar layar lebih kecil dari total lebar minimal, 
        // matikan AutoResize agar Scrollbar muncul.
        if (lebarWadah > totalLebarMinimal) {
            tblKatBarang.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            tblKatBarang.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        }

        for (int i = 0; i < tblKatBarang.getColumnCount(); i++) {
            if (i < lebarMinimal.length) {
                javax.swing.table.TableColumn col = tblKatBarang.getColumnModel().getColumn(i);
                col.setPreferredWidth(lebarMinimal[i]);
                col.setMinWidth(lebarMinimal[i]); 
            }
        }
        
        tblKatBarang.setFillsViewportHeight(true);
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
    java.awt.GridBagConstraints gbc;

    jButton1 = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    tfIdKatBarang = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    tfNmKatBarang = new javax.swing.JTextField();
    btnSimpan = new javax.swing.JButton();
    jLabel4 = new javax.swing.JLabel();
    tfKetKatBarang = new javax.swing.JTextField();
    jPanel2 = new javax.swing.JPanel();
    jLabel5 = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblKatBarang = new javax.swing.JTable();
    tf_cari = new javax.swing.JTextField();
    btn_refresh = new javax.swing.JButton();
    btn_edit = new javax.swing.JButton();
    btnDelete = new javax.swing.JButton();
    btn_cari = new javax.swing.JButton();

    jButton1.setText("jButton1");

    // SETUP CONTAINER UTAMA: GridBagLayout agar responsif memenuhi pMain
    this.setLayout(new java.awt.GridBagLayout());
    this.setPreferredSize(new java.awt.Dimension(1160, 640));

    // ==========================================
    // PANEL 1 (TAMBAH KATEGORI - KIRI)
    // ==========================================
    jPanel1.setBackground(new java.awt.Color(255, 255, 255));
    jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel1.setPreferredSize(new java.awt.Dimension(280, 620));
    jPanel1.setMinimumSize(new java.awt.Dimension(280, 620));
    jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel1.setBackground(new java.awt.Color(4, 102, 200));
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel1.setForeground(new java.awt.Color(255, 255, 255));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("TAMBAH KATEGORI");
    jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jLabel1.setOpaque(true);
    jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 40));

    jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel2.setText("ID Kategori :");
    jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 180, 20));

    tfIdKatBarang.addActionListener(this::tfIdKatBarangActionPerformed);
    jPanel1.add(tfIdKatBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 85, 240, 35));

    jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel3.setText("Nama Kategori :");
    jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 180, 20));
    jPanel1.add(tfNmKatBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 155, 240, 35));

    jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel4.setText("Keterangan :");
    jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 180, 20));

    tfKetKatBarang.addActionListener(this::tfKetKatBarangActionPerformed);
    jPanel1.add(tfKetKatBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 225, 240, 35));

    btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
    btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    btnSimpan.setText("SIMPAN [Enter]");
    btnSimpan.addActionListener(this::btnSimpanActionPerformed);
    jPanel1.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 240, 50));

    // Menambahkan Panel Kiri ke Layout Utama
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new java.awt.Insets(10, 10, 10, 5);
    gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(jPanel1, gbc);

    // ==========================================
    // PANEL 2 (DAFTAR KATEGORI - KANAN)
    // ==========================================
    jPanel2.setBackground(new java.awt.Color(255, 255, 255));
    jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel2.setLayout(new java.awt.BorderLayout());

    jLabel5.setBackground(new java.awt.Color(4, 102, 200));
    jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel5.setForeground(new java.awt.Color(255, 255, 255));
    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel5.setText("DAFTAR KATEGORI JENIS PERANGKAT");
    jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jLabel5.setOpaque(true);
    jLabel5.setPreferredSize(new java.awt.Dimension(860, 40));
    jPanel2.add(jLabel5, java.awt.BorderLayout.NORTH);

    // Sub-Panel untuk Toolbar (Cari, Refresh, Edit, Delete)
    javax.swing.JPanel pnlToolbar = new javax.swing.JPanel(new org.netbeans.lib.awtextra.AbsoluteLayout());
    pnlToolbar.setBackground(new java.awt.Color(255, 255, 255));
    pnlToolbar.setPreferredSize(new java.awt.Dimension(840, 60));

    tf_cari.setFont(new java.awt.Font("Segoe UI", 1, 12));
    tf_cari.addActionListener(this::tf_cariActionPerformed);
    pnlToolbar.add(tf_cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 15, 260, 30));

    btn_cari.setBackground(new java.awt.Color(204, 204, 204));
    btn_cari.setText("Cari [F2]");
    btn_cari.addActionListener(this::btn_cariActionPerformed);
    pnlToolbar.add(btn_cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 15, 130, 30));

    btn_refresh.setBackground(new java.awt.Color(204, 204, 204));
    btn_refresh.setText("Refresh [F3]");
    btn_refresh.addActionListener(this::btn_refreshActionPerformed);
    pnlToolbar.add(btn_refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 15, 130, 30));

    btn_edit.setBackground(new java.awt.Color(255, 255, 102));
    btn_edit.setText("Edit [F1]");
    btn_edit.addActionListener(this::btn_editActionPerformed);
    pnlToolbar.add(btn_edit, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 15, 130, 30));

    btnDelete.setBackground(new java.awt.Color(255, 0, 51));
    btnDelete.setForeground(new java.awt.Color(255, 255, 255));
    btnDelete.setText("Hapus [Del]");
    btnDelete.addActionListener(this::btnDeleteActionPerformed);
    pnlToolbar.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 15, 130, 30));

    // Bagian Tengah (Tabel)
    tblKatBarang.setFont(new java.awt.Font("Segoe UI", 0, 14));
    tblKatBarang.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] { "ID", "NAMA KATEGORI", "KETERANGAN" }
    ));
    tblKatBarang.setRowHeight(35);
    jScrollPane1.setViewportView(tblKatBarang);
    jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));

    jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent evt) {
            aturKolomTabel();
        }
    });

    // Satukan Toolbar dan Tabel ke dalam panel konten
    javax.swing.JPanel pnlContent = new javax.swing.JPanel(new java.awt.BorderLayout());
    pnlContent.setOpaque(false);
    pnlContent.add(pnlToolbar, java.awt.BorderLayout.NORTH);
    pnlContent.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    jPanel2.add(pnlContent, java.awt.BorderLayout.CENTER);

    // Menambahkan Panel Kanan ke Layout Utama (MELAR RESPONSIF)
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = java.awt.GridBagConstraints.BOTH;
    gbc.insets = new java.awt.Insets(10, 5, 10, 10);
    add(jPanel2, gbc);
}// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        try {
            // Validasi Input Kosong
            if (tfNmKatBarang.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama Kategori tidak boleh kosong!");
                return;
            }

            Connection conn = Koneksi.configDB();
            PreparedStatement pst;
            String sql;

            if (isEditMode) {
                // --- PERBAIKAN UTAMA DISINI ---
                // JANGAN update kolom 'id_kategori' di bagian SET.
                // Cukup update Nama & Keterangan. ID hanya untuk WHERE.
                sql = "UPDATE tbl_jenis_perangkat SET nama_jenis=?, keterangan=? WHERE id_kategori=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, tfNmKatBarang.getText());
                pst.setString(2, tfKetKatBarang.getText());
                pst.setInt(3, Integer.parseInt(tfIdKatBarang.getText())); // WHERE ID = ...
                
            } else {
                // LOGIKA INSERT (Data Baru)
                // Cek apakah ID sudah ada (menghindari duplikat jika auto number meleset)
                String cekSql = "SELECT count(*) FROM tbl_jenis_perangkat WHERE id_kategori = ?";
                PreparedStatement pstCek = conn.prepareStatement(cekSql);
                pstCek.setString(1, tfIdKatBarang.getText());
                ResultSet rsCek = pstCek.executeQuery();
                rsCek.next();
                if (rsCek.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "ID Kategori sudah ada, silakan Refresh!");
                    return;
                }

                sql = "INSERT INTO tbl_jenis_perangkat (id_kategori, nama_jenis, keterangan) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(tfIdKatBarang.getText()));
                pst.setString(2, tfNmKatBarang.getText());
                pst.setString(3, tfKetKatBarang.getText());
            }

            // Eksekusi Query
            pst.executeUpdate();
            
            String pesan = isEditMode ? "Data Berhasil Diperbarui" : "Data Berhasil Disimpan";
            JOptionPane.showMessageDialog(this, pesan);
            
            bersihkan(); // Reset form kembali ke kondisi awal

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Proses: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void tf_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_cariActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tf_cariActionPerformed

    private void btn_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_editActionPerformed
        // TODO add your handling code here:
        int baris = tblKatBarang.getSelectedRow();
        
        if (baris != -1) {
            // Ambil data dari tabel ke textfield
            tfIdKatBarang.setText(tblKatBarang.getValueAt(baris, 1).toString());
            tfNmKatBarang.setText(tblKatBarang.getValueAt(baris, 2).toString());
            
            // Handle kemungkinan Keterangan null
            Object ketObj = tblKatBarang.getValueAt(baris, 3);
            tfKetKatBarang.setText(ketObj != null ? ketObj.toString() : "");

            // Masuk Mode Edit
            isEditMode = true;
            btnSimpan.setText("UBAH"); // Ubah teks tombol agar user sadar sedang mode edit
            
            // KUNCI ID AGAR TIDAK BISA DIUBAH (Mencegah Error Foreign Key)
            tfIdKatBarang.setEditable(false); 
            tfNmKatBarang.requestFocus(); // Fokus ke nama
            
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data di tabel terlebih dahulu!");
        }
    }//GEN-LAST:event_btn_editActionPerformed

    private void btn_refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_refreshActionPerformed
        // TODO add your handling code here:
        bersihkan();
        load_table();
    }//GEN-LAST:event_btn_refreshActionPerformed

    private void tfIdKatBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfIdKatBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfIdKatBarangActionPerformed

    private void btn_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cariActionPerformed
        // TODO add your handling code here:
        load_table();
    }//GEN-LAST:event_btn_cariActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int baris = tblKatBarang.getSelectedRow();
        
        if (baris != -1) {
            String id = tblKatBarang.getValueAt(baris, 1).toString();
            String nama = tblKatBarang.getValueAt(baris, 2).toString();
            
            int konfirmasi = JOptionPane.showConfirmDialog(this, 
                    "Apakah Anda yakin menghapus kategori: " + nama + "?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);
            
            if (konfirmasi == JOptionPane.YES_OPTION) {
                try {
                    String sql = "DELETE FROM tbl_jenis_perangkat WHERE id_kategori=?";
                    Connection conn = Koneksi.configDB();
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, id);
                    pst.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Data Berhasil Dihapus");
                    bersihkan();
                    
                } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                    // Tangkap Error jika data sedang dipakai di tabel lain
                    JOptionPane.showMessageDialog(this, 
                        "Gagal Hapus! Kategori ini sedang digunakan pada Data Barang atau Servis.",
                        "Error Relasi Data",
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus!");
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tfKetKatBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKetKatBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKetKatBarangActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btn_cari;
    private javax.swing.JButton btn_edit;
    private javax.swing.JButton btn_refresh;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblKatBarang;
    private javax.swing.JTextField tfIdKatBarang;
    private javax.swing.JTextField tfKetKatBarang;
    private javax.swing.JTextField tfNmKatBarang;
    private javax.swing.JTextField tf_cari;
    // End of variables declaration//GEN-END:variables
}
