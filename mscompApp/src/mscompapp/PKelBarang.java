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
public class PKelBarang extends javax.swing.JPanel {

    /**
     * Creates new form PKelBarang
     */
    private boolean isEdit = false;

    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    public PKelBarang() {
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
        tfNamaBarang.setText("");
        tfHarga.setText("");
        tfKeterangan.setText("");
        tfCari.setText("");
        currentPage = 0;
        load_kategori();
        load_table();
        auto_number(); 
        
        btnSimpan.setText("SIMPAN");
        tfKodeBarang.setEditable(false);
        isEdit = false;
    }

    // --- 2. AUTO NUMBER (B001) ---
    private void auto_number() {
        try {
            Connection conn = Koneksi.configDB();
            String sql = "SELECT kode_barang FROM tbl_barang ORDER BY kode_barang DESC LIMIT 1";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                String id = rs.getString(1); 
                // Asumsi ID formatnya "B001", ambil angka mulai index 1
                int angka = Integer.parseInt(id.substring(1)); 
                tfKodeBarang.setText(String.format("B%04d", angka + 1));
            } else {
                tfKodeBarang.setText("B0001");
            }
        } catch (Exception e) {
            tfKodeBarang.setText("B0001");
        }
    }

    // --- 3. LOAD KATEGORI (Untuk ComboBox) ---
    private void load_kategori() {
        cbKategori.removeAllItems();
        cbKategori.addItem("-Pilih-");
        try {
            ResultSet rs = Koneksi.configDB().createStatement().executeQuery("SELECT nama_kategori FROM tbl_kat_barang");
            while (rs.next()) cbKategori.addItem(rs.getString("nama_kategori"));
        } catch (Exception e) {}
    }

    // --- 4. LOAD TABLE (LANGSUNG DARI TBL_BARANG) ---
    private void load_table() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addColumn("No"); model.addColumn("Kode"); model.addColumn("Nama Barang");
        model.addColumn("Kategori"); model.addColumn("Harga"); model.addColumn("Stok"); model.addColumn("Ket");

        try {
            String where = "";
            if (!tfCari.getText().isEmpty() && !tfCari.getText().equals("Cari....")) {
                where = "WHERE nama_barang LIKE '%" + tfCari.getText() + "%' ";
            }

            // 1. Hitung Total Data
            Connection conn = Koneksi.configDB();
            ResultSet rsCount = conn.createStatement().executeQuery("SELECT COUNT(*) FROM tbl_barang " + where);
            int totalData = 0;
            if (rsCount.next()) totalData = rsCount.getInt(1);

            // 2. Load Data dengan LIMIT & OFFSET
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT * FROM tbl_barang " + where + " ORDER BY kode_barang ASC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            int no = offset + 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++, rs.getString("kode_barang"), rs.getString("nama_barang"),
                    rs.getString("kategori"), rs.getInt("harga"), rs.getInt("stok"), rs.getString("keterangan")
                });
            }
            tblBarang.setModel(model);

            // 3. Update Tombol Pagination
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) { e.printStackTrace(); }
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
        tfKodeBarang = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tfNamaBarang = new javax.swing.JTextField();
        cbKategori = new javax.swing.JComboBox<>();
        tfHarga = new javax.swing.JTextField();
        tfKeterangan = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        tfCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setPreferredSize(new java.awt.Dimension(312, 960));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("KODE BARANG :");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 200, 40));

        tfKodeBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKodeBarang.addActionListener(this::tfKodeBarangActionPerformed);
        jPanel3.add(tfKodeBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 340, 40));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("NAMA BARANG :");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 200, 40));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("HARGA :");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, 200, 40));

        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel7.setText("KATEGORI :");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 290, 200, 40));

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel8.setText("KETERANGAN :");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 470, 200, 40));

        tfNamaBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfNamaBarang.addActionListener(this::tfNamaBarangActionPerformed);
        jPanel3.add(tfNamaBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, 340, 40));

        cbKategori.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 340, 40));

        tfHarga.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel3.add(tfHarga, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 420, 340, 40));

        tfKeterangan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel3.add(tfKeterangan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 510, 340, 40));

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);
        jPanel3.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 580, 340, 60));

        jLabel1.setBackground(new java.awt.Color(4, 102, 200));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH BARANG");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 70));

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 400, 920));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setBackground(new java.awt.Color(4, 102, 200));
        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("DAFTAR BARANG");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel2.setOpaque(true);
        jPanel4.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1270, 70));

        tblBarang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "KODE BARANG", "NAMA BARANG", "KATEGORI", "HARGA", "STOK", "KETERANGAN"
            }
        ));
        tblBarang.setRowHeight(35);
        jScrollPane1.setViewportView(tblBarang);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 1230, 610));

        tfCari.setText("Cari....");
        tfCari.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfCariFocusGained(evt);
            }
        });
        tfCari.addActionListener(this::tfCariActionPerformed);
        jPanel4.add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 472, 40));

        btnCari.setBackground(new java.awt.Color(204, 204, 204));
        btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        jPanel4.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 100, 150, 40));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        jPanel4.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 100, 180, 40));

        btnEdit.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnEdit.setText("EDIT [F1]");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel4.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 100, 150, 40));

        btnHapus.setBackground(new java.awt.Color(255, 51, 51));
        btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnHapus.setText("HAPUS [Del]");
        btnHapus.addActionListener(this::btnHapusActionPerformed);
        jPanel4.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 100, 150, 40));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
        jPanel5.add(btnNextKiri, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 20, -1, 48));

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
        jPanel5.add(btnNextKanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, -1, 48));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 780, 1270, 140));

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 1270, 920));
    }// </editor-fold>//GEN-END:initComponents

    private void tfKodeBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKodeBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKodeBarangActionPerformed

    private void tfNamaBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNamaBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNamaBarangActionPerformed

    private void tfCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCariActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tfCariActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        load_table();
        currentPage = 0;
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        try {
            String kode = tfKodeBarang.getText();
            String nama = tfNamaBarang.getText();
            String harga = tfHarga.getText();
            String ket = tfKeterangan.getText();
            
            if (cbKategori.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Pilih Kategori dulu!");
                return;
            }
            
            // LANGSUNG AMBIL TEXT DARI COMBOBOX
            String namaKategori = cbKategori.getSelectedItem().toString();

            Connection conn = Koneksi.configDB();
            String sql;
            PreparedStatement ps;

            if (isEdit) {
                // UPDATE: Simpan 'namaKategori' langsung ke kolom 'kategori'
                sql = "UPDATE tbl_barang SET nama_barang=?, kategori=?, harga=?, keterangan=? WHERE kode_barang=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, nama);
                ps.setString(2, namaKategori); // Masukkan String Nama (Laptop, dll)
                ps.setString(3, harga);
                ps.setString(4, ket);
                ps.setString(5, kode);
            } else {
                // INSERT: Simpan 'namaKategori' langsung
                sql = "INSERT INTO tbl_barang (kode_barang, nama_barang, kategori, harga, stok, keterangan) VALUES (?,?,?,?,0,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, kode);
                ps.setString(2, nama);
                ps.setString(3, namaKategori); // Masukkan String Nama
                ps.setString(4, harga);
                ps.setString(5, ket);
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
        int row = tblBarang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris tabel dulu!");
            return;
        }

        tfKodeBarang.setText(tblBarang.getValueAt(row, 1).toString());
        tfNamaBarang.setText(tblBarang.getValueAt(row, 2).toString());
        
        // Langsung set text combobox karena di tabel isinya sudah Nama Kategori
        String kat = tblBarang.getValueAt(row, 3).toString();
        cbKategori.setSelectedItem(kat);
        
        tfHarga.setText(tblBarang.getValueAt(row, 4).toString());
        tfKeterangan.setText(tblBarang.getValueAt(row, 6).toString());

        isEdit = true;
        btnSimpan.setText("UBAH");
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        int row = tblBarang.getSelectedRow();
        if (row == -1) return;

        String kode = tblBarang.getValueAt(row, 1).toString();
        int tanya = JOptionPane.showConfirmDialog(this, "Hapus " + kode + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (tanya == JOptionPane.YES_OPTION) {
            try {
                Connection conn = Koneksi.configDB();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_barang WHERE kode_barang=?");
                ps.setString(1, kode);
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

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        load_table();
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            load_table();
        }
    }//GEN-LAST:event_btnNextKiriActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextField tfCari;
    private javax.swing.JTextField tfHarga;
    private javax.swing.JTextField tfKeterangan;
    private javax.swing.JTextField tfKodeBarang;
    private javax.swing.JTextField tfNamaBarang;
    // End of variables declaration//GEN-END:variables
}
