/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;
import config.Koneksi;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;        // Untuk koneksi ke database
import java.sql.PreparedStatement; // Untuk menjalankan query SQL yang aman
import java.sql.ResultSet;         // Untuk menampung hasil data dari database
import java.sql.Statement;         // Untuk mengirim perintah SQL dasar
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;    // Untuk memunculkan pesan dialog (Pop-up)
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel; // Untuk mengatur data pada JTable
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
public class PKelUser extends javax.swing.JPanel {
    private boolean isEditMode = false;
    /**
     * Creates new form pDashboard
     */
    public PKelUser() {
        initComponents();
        load_table();
        auto_number();
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
    
    private void load_table() {
    DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
    model.addColumn("ID"); model.addColumn("Username"); 
    model.addColumn("Password"); model.addColumn("Nama"); model.addColumn("Role");
    try {
        String sql = "SELECT * FROM tbl_user";
        java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
        java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
        while(res.next()){
            model.addRow(new Object[]{res.getString(1), res.getString(2), 
                res.getString(3), res.getString(4), res.getString(5)});
        }
        tblUser.setModel(model);
    } catch (Exception e) { System.out.println(e.getMessage()); }
}

// Mengambil calon nomor ID selanjutnya secara otomatis
private void auto_number() {
    try {
        java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
        java.sql.ResultSet res = conn.createStatement().executeQuery("SELECT MAX(id_user) FROM tbl_user");
        if (res.next()) {
            tfNo.setText(String.valueOf(res.getInt(1) + 1));
        } else {
            tfNo.setText("1");
        }
    } catch (Exception e) { tfNo.setText("1"); }
}

// Membersihkan form dan meriset ke mode "Tambah"
private void bersihkanForm() {
    tfUsn.setText("");
    tfPass.setText("");
    tfNama.setText("");
    cbRole.setSelectedIndex(0);
    tfCari.setText(""); // Bersihkan kolom cari juga
    isEditMode = false;   // Kembali ke mode Tambah
    auto_number();        // Beri nomor ID baru otomatis
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
        jRadioButton1 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfUsn = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfPass = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        tfNama = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbRole = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        tfNo = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblUser = new javax.swing.JTable();
        tfCari = new javax.swing.JTextField();
        btnEdit = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnCari = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        jRadioButton1.setText("jRadioButton1");

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel2.setText("Username :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, 200, 40));

        tfUsn.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        tfUsn.addActionListener(this::tfUsnActionPerformed);
        jPanel1.add(tfUsn, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, 390, 40));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("Password :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 200, 40));

        tfPass.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        tfPass.addActionListener(this::tfPassActionPerformed);
        jPanel1.add(tfPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 410, 390, 40));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Nama :");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 200, 40));

        tfNama.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        tfNama.addActionListener(this::tfNamaActionPerformed);
        jPanel1.add(tfNama, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 390, 40));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("Role :");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 460, 200, 40));

        cbRole.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        cbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "teknisi" }));
        jPanel1.add(cbRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 500, 390, 40));

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel6.setText("No :");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 200, 40));

        tfNo.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        tfNo.addActionListener(this::tfNoActionPerformed);
        jPanel1.add(tfNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 390, 40));

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);
        jPanel1.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 570, 390, 70));

        jLabel1.setBackground(new java.awt.Color(4, 102, 200));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH USER");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 450, 70));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 450, 920));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblUser.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblUser.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "No", "Username", "Password", "Nama", "Role"
            }
        ));
        tblUser.setRowHeight(35);
        tblUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUserMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblUser);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 1170, 690));
        jPanel2.add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 400, 40));

        btnEdit.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnEdit.setText("EDIT [F1]");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel2.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 100, 150, 40));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        jPanel2.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 100, 170, 40));

        btnCari.setBackground(new java.awt.Color(204, 204, 204));
        btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        jPanel2.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 100, 150, 40));

        btnHapus.setBackground(new java.awt.Color(255, 0, 0));
        btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnHapus.setText("HAPUS [Del]");
        btnHapus.addActionListener(this::btnHapusActionPerformed);
        jPanel2.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 100, 150, 40));

        jLabel7.setBackground(new java.awt.Color(4, 102, 200));
        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("DAFTAR USER");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel7.setOpaque(true);
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1210, 70));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, 1210, 920));
    }// </editor-fold>//GEN-END:initComponents

    private void tfUsnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUsnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUsnActionPerformed

    private void tfNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNamaActionPerformed

    private void tfNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNoActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        try {
        java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
        String sql;

        if (isEditMode == false) {
            // JALANKAN TAMBAH USER (ID diabaikan karena Auto Increment)
            sql = "INSERT INTO tbl_user (username, password, nama, role) VALUES (?,?,?,?)";
        } else {
            // JALANKAN UBAH USER
            sql = "UPDATE tbl_user SET username=?, password=?, nama=?, role=? WHERE id_user=?";
        }

        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, tfUsn.getText());
        pst.setString(2, tfPass.getText());
        pst.setString(3, tfNama.getText());
        pst.setString(4, cbRole.getSelectedItem().toString());
        
        if (isEditMode) {
            pst.setString(5, tfNo.getText());
        }

        pst.execute();
        javax.swing.JOptionPane.showMessageDialog(null, "Berhasil!");
        load_table();
        bersihkanForm(); // Otomatis balik ke mode tambah & No baru
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
    }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        load_table();
    bersihkanForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = new DefaultTableModel();
    model.addColumn("No"); model.addColumn("Username"); 
    model.addColumn("Password"); model.addColumn("Nama"); model.addColumn("Role");
    try {
        // Mencari berdasarkan Username atau Nama
        String sql = "SELECT * FROM tbl_user WHERE username LIKE '%" + tfCari.getText() 
                     + "%' OR nama LIKE '%" + tfCari.getText() + "%'";
        java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
        java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
        while(res.next()){
            model.addRow(new Object[]{res.getString(1), res.getString(2), 
                res.getString(3), res.getString(4), res.getString(5)});
        }
        tblUser.setModel(model);
    } catch (Exception e) { 
        javax.swing.JOptionPane.showMessageDialog(this, e.getMessage()); 
    }
    }//GEN-LAST:event_btnCariActionPerformed

    private void tblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUserMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblUserMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int barisTerpilih = tblUser.getSelectedRow();

    // 2. Cek apakah ada baris yang diklik/dipilih
    if (barisTerpilih == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, "Pilih dulu baris di tabel yang ingin diedit!");
    } else {
        // 3. Ambil ID dan Nama untuk konfirmasi
        String id = tblUser.getValueAt(barisTerpilih, 0).toString();
        String nama = tblUser.getValueAt(barisTerpilih, 3).toString();

        // 4. Munculkan Pop-up Konfirmasi
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                "Apakah Anda ingin mengedit data " + nama + " (ID: " + id + ")?", 
                "Konfirmasi Edit", javax.swing.JOptionPane.YES_NO_OPTION);

        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            // 5. Pindahkan data dari tabel ke form sebelah kiri
            tfNo.setText(id);
            tfUsn.setText(tblUser.getValueAt(barisTerpilih, 1).toString());
            tfPass.setText(tblUser.getValueAt(barisTerpilih, 2).toString());
            tfNama.setText(nama);
            cbRole.setSelectedItem(tblUser.getValueAt(barisTerpilih, 4).toString());

            // 6. Ubah saklar isEditMode menjadi true
            isEditMode = true;
            
            // Beri notifikasi kecil agar admin tahu form sudah siap diubah
            javax.swing.JOptionPane.showMessageDialog(this, "Data sudah dipindahkan ke form. Silakan ubah lalu klik Simpan.");
        }
    }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here :
        int barisTerpilih = tblUser.getSelectedRow();

    // 2. Cek apakah ada baris yang dipilih
    if (barisTerpilih == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, "Klik dulu baris pada tabel yang ingin dihapus!");
    } else {
        // 3. Ambil ID dari kolom pertama (index 0) pada baris yang dipilih
        String idHapus = tblUser.getValueAt(barisTerpilih, 0).toString();
        String namaHapus = tblUser.getValueAt(barisTerpilih, 3).toString(); // Ambil kolom Nama untuk konfirmasi

        // 4. Konfirmasi penghapusan
        int konfirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                "Yakin ingin menghapus User: " + namaHapus + " (ID: " + idHapus + ")?", 
                "Konfirmasi Hapus", javax.swing.JOptionPane.YES_NO_OPTION);

        if (konfirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
                String sql = "DELETE FROM tbl_user WHERE id_user = '" + idHapus + "'";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.execute();

                javax.swing.JOptionPane.showMessageDialog(this, "Data Berhasil Dihapus!");
                
                // 5. Refresh data
                load_table();
                bersihkanForm(); // Agar Auto Number di form kiri juga terupdate
                
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal Menghapus: " + e.getMessage());
            }
        }
    }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void tfPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPassActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPassActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cbRole;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblUser;
    private javax.swing.JTextField tfCari;
    private javax.swing.JTextField tfNama;
    private javax.swing.JTextField tfNo;
    private javax.swing.JPasswordField tfPass;
    private javax.swing.JTextField tfUsn;
    // End of variables declaration//GEN-END:variables
}
