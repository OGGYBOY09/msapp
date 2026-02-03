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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfIdKatBarang = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfNmKatBarang = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        tfKetKatBarang = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKatBarang = new javax.swing.JTable();
        tf_cari = new javax.swing.JTextField();
        btn_refresh = new javax.swing.JButton();
        btn_edit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btn_cari = new javax.swing.JButton();

        jButton1.setText("jButton1");

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel3.setBackground(new java.awt.Color(4, 102, 200));

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH KATEGORI BARANG");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel2.setText("ID Kategori :");

        tfIdKatBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfIdKatBarang.addActionListener(this::tfIdKatBarangActionPerformed);

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("Nama Kategori :");

        tfNmKatBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Keterangan :");

        tfKetKatBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(tfIdKatBarang)
                    .addComponent(tfNmKatBarang)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKetKatBarang)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfIdKatBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNmKatBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKetKatBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel4.setBackground(new java.awt.Color(4, 102, 200));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("DAFTAR KATEGORI BARANG SERVICE");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );

        tblKatBarang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblKatBarang.setModel(new javax.swing.table.DefaultTableModel(
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
        tblKatBarang.setRowHeight(35);
        jScrollPane1.setViewportView(tblKatBarang);

        tf_cari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        tf_cari.addActionListener(this::tf_cariActionPerformed);

        btn_refresh.setBackground(new java.awt.Color(204, 204, 204));
        btn_refresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btn_refresh.setText("Refresh [F3]");
        btn_refresh.addActionListener(this::btn_refreshActionPerformed);

        btn_edit.setBackground(new java.awt.Color(255, 255, 102));
        btn_edit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btn_edit.setText("Edit [F1]");
        btn_edit.addActionListener(this::btn_editActionPerformed);

        btnDelete.setBackground(new java.awt.Color(255, 0, 51));
        btnDelete.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnDelete.setText("Hapus [Del]");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btn_cari.setBackground(new java.awt.Color(204, 204, 204));
        btn_cari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btn_cari.setText("Cari [F2]");
        btn_cari.addActionListener(this::btn_cariActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(49, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(tf_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(btn_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_refresh)
                        .addGap(18, 18, 18)
                        .addComponent(btn_edit, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete)))
                .addGap(46, 46, 46))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_edit, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tf_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 691, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblKatBarang;
    private javax.swing.JTextField tfIdKatBarang;
    private javax.swing.JTextField tfKetKatBarang;
    private javax.swing.JTextField tfNmKatBarang;
    private javax.swing.JTextField tf_cari;
    // End of variables declaration//GEN-END:variables
}
