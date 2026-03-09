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
            aturKolom();

            // 3. Update Tombol Pagination
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void aturKolom() {
    if (tblBarang.getColumnCount() > 0) {
        // Angka minimal agar tidak gepeng (Total sekitar 1050px)
        int[] lebarMinimal = {50, 120, 200, 150, 150, 80, 300};
        int totalLebarMinimal = 1050; 

        // Cek lebar wadah (Viewport) tabel saat ini
        int lebarWadah = jScrollPane1.getViewport().getWidth();

        if (lebarWadah > totalLebarMinimal) {
            // JIKA LAYAR LEBAR (1980): Gunakan mode SUBSEQUENT agar kolom melar memenuhi ruang kosong
            tblBarang.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            // JIKA LAYAR KECIL (1366): Gunakan mode OFF agar scrollbar horizontal muncul
            tblBarang.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        }

        for (int i = 0; i < tblBarang.getColumnCount(); i++) {
            if (i < lebarMinimal.length) {
                javax.swing.table.TableColumn col = tblBarang.getColumnModel().getColumn(i);
                if (i == 6) { 
                    col.setHeaderValue("Keterangan"); 
                }
                col.setPreferredWidth(lebarMinimal[i]);
                col.setMinWidth(lebarMinimal[i]); // Kunci batas bawahnya
            }
        }
        
        // Memastikan background tabel menutupi seluruh area scrollpane
        tblBarang.setFillsViewportHeight(true);
        
        // Header rata tengah
        javax.swing.table.DefaultTableCellRenderer headerRenderer = 
            (javax.swing.table.DefaultTableCellRenderer) tblBarang.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
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

    // SETUP UTAMA: Memenuhi pMain dan Responsif
    this.setLayout(new java.awt.GridBagLayout());
    this.setBackground(new java.awt.Color(245, 245, 245));

    // ==========================================
    // PANEL 3 (TAMBAH BARANG - KIRI)
    // ==========================================
    jPanel3.setBackground(new java.awt.Color(255, 255, 255));
    jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel3.setPreferredSize(new java.awt.Dimension(300, 620));
    jPanel3.setMinimumSize(new java.awt.Dimension(300, 620));
    jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel1.setBackground(new java.awt.Color(4, 102, 200));
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel1.setForeground(new java.awt.Color(255, 255, 255));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("TAMBAH BARANG");
    jLabel1.setOpaque(true);
    jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 300, 40));

    jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel3.setText("KODE BARANG :");
    jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 55, 120, -1));
    jPanel3.add(tfKodeBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 270, 35));

    jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel4.setText("NAMA BARANG :");
    jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 120, -1));
    jPanel3.add(tfNamaBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 155, 270, 35));

    jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel7.setText("KATEGORI :");
    jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 205, 120, -1));
    cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Pilih-", "SSD", "RAM", "Harddisk", "VGA" }));
    jPanel3.add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 270, 35));

    jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel5.setText("HARGA :");
    jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 120, -1));
    jPanel3.add(tfHarga, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 305, 270, 35));

    jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel8.setText("KETERANGAN :");
    jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 355, 120, -1));
    jPanel3.add(tfKeterangan, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 270, 35));

    btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
    btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24));
    btnSimpan.setText("SIMPAN");
    btnSimpan.addActionListener(this::btnSimpanActionPerformed);
    jPanel3.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 440, 270, 60));

    // Add Panel Kiri
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.insets = new java.awt.Insets(10, 10, 10, 5);
    gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(jPanel3, gbc);

    // ==========================================
    // PANEL 4 (DAFTAR BARANG - KANAN)
    // ==========================================
    jPanel4.setBackground(new java.awt.Color(255, 255, 255));
    jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel4.setLayout(new java.awt.BorderLayout());

    jLabel2.setBackground(new java.awt.Color(4, 102, 200));
    jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel2.setForeground(new java.awt.Color(255, 255, 255));
    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel2.setText("DAFTAR BARANG");
    jLabel2.setOpaque(true);
    jLabel2.setPreferredSize(new java.awt.Dimension(100, 40));
    jPanel4.add(jLabel2, java.awt.BorderLayout.NORTH);

    // Panel Toolbar (Search & Buttons)
    javax.swing.JPanel pnlToolbar = new javax.swing.JPanel(new org.netbeans.lib.awtextra.AbsoluteLayout());
    pnlToolbar.setBackground(new java.awt.Color(255, 255, 255));
    pnlToolbar.setPreferredSize(new java.awt.Dimension(840, 60));

    tfCari.setText("Cari....");
    tfCari.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent evt) { tfCariFocusGained(evt); }
    });
    pnlToolbar.add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 15, 250, 30));

    btnCari.setBackground(new java.awt.Color(204, 204, 204));
    btnCari.setText("CARI [F2]");
    btnCari.addActionListener(this::btnCariActionPerformed);
    pnlToolbar.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 15, 110, 30));

    btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
    btnRefresh.setText("REFRESH [F3]");
    btnRefresh.addActionListener(this::btnRefreshActionPerformed);
    pnlToolbar.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 15, 150, 30));

    btnEdit.setBackground(new java.awt.Color(255, 255, 102));
    btnEdit.setText("EDIT [F1]");
    btnEdit.addActionListener(this::btnEditActionPerformed);
    pnlToolbar.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 15, 110, 30));

    btnHapus.setBackground(new java.awt.Color(255, 51, 51));
    btnHapus.setForeground(new java.awt.Color(255, 255, 255));
    btnHapus.setText("HAPUS [Del]");
    btnHapus.addActionListener(this::btnHapusActionPerformed);
    pnlToolbar.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 15, 150, 30));

    // Bagian Tabel
    tblBarang.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] { "No", "Kode", "Nama Barang", "Kategori", "Harga", "Stok", "KETERANGAN" }
    ));
    tblBarang.setRowHeight(35);
    // Setting Lebar Kolom agar Keterangan tidak jadi "Ket"
    tblBarang.getColumnModel().getColumn(6).setPreferredWidth(150); 
    
    jScrollPane1.setViewportView(tblBarang);
    jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));

    // Panel Bawah (Tabel + Pagination)
    javax.swing.JPanel pnlMainKanan = new javax.swing.JPanel(new java.awt.BorderLayout());
    pnlMainKanan.setOpaque(false);
    pnlMainKanan.add(pnlToolbar, java.awt.BorderLayout.NORTH);
    pnlMainKanan.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    // Pagination
    jPanel5.setBackground(new java.awt.Color(255, 255, 255));
    jPanel5.setPreferredSize(new java.awt.Dimension(100, 60));
    jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));

    btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
    btnNextKiri.setText("PREV");
    btnNextKiri.setPreferredSize(new java.awt.Dimension(100, 35));
    btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
    
    btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
    btnNextKanan.setText("NEXT");
    btnNextKanan.setPreferredSize(new java.awt.Dimension(100, 35));
    btnNextKanan.addActionListener(this::btnNextKananActionPerformed);

    jPanel5.add(btnNextKiri);
    jPanel5.add(btnNextKanan);
    pnlMainKanan.add(jPanel5, java.awt.BorderLayout.SOUTH);

    jPanel4.add(pnlMainKanan, java.awt.BorderLayout.CENTER);

    // Add Panel Kanan (MELAR)
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 1; gbc.gridy = 0;
    gbc.weightx = 1.0; gbc.weighty = 1.0;
    gbc.fill = java.awt.GridBagConstraints.BOTH;
    gbc.insets = new java.awt.Insets(10, 5, 10, 10);
    add(jPanel4, gbc);

    jPanel4.addComponentListener(new java.awt.event.ComponentAdapter() {
    @Override
    public void componentResized(java.awt.event.ComponentEvent evt) {
        aturKolom();
    }
});
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
        aturKolom();
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
