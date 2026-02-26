/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;
import config.Koneksi;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.AbstractAction;

/**
 *
 * @author ASUS
 */
public class PKelUser extends javax.swing.JPanel {
    private boolean isEditMode = false;
    private String idTerpilih = "";
    /**
     * Creates new form pDashboard
     */
    public PKelUser() {
        initComponents();
        load_table();
        reset_form();
        initKeyShortcuts();
        isiLevel();
        updateLevelState();
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
    
    private void updateLevelState() {
        if (cbRole.getSelectedItem() != null) {
            if (cbRole.getSelectedItem().toString().equalsIgnoreCase("Admin")) {
                cbLevel.setSelectedItem("Semua");
                cbLevel.setEnabled(false);
            } else {
                cbLevel.setEnabled(true);
            }
        }
    }
    
    private void isiLevel() {
        cbLevel.removeAllItems(); // Bersihkan dulu agar tidak duplikat
        cbLevel.addItem("Semua");
        cbLevel.addItem("PC/Mini PC");
        cbLevel.addItem("Laptop");
        cbLevel.addItem("Printer");
    }
    
    
    private void load_table() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID User");
        model.addColumn("Nama");
        model.addColumn("Username");
        model.addColumn("Role");
        model.addColumn("Level");

        try {
            // Kita gunakan IFNULL untuk menangani data level yang masih kosong di database
            String sql = "SELECT id_user, nama, username, role, IFNULL(level, 'Semua') as level_user " +
                         "FROM tbl_user WHERE nama LIKE ? OR username LIKE ?";
            Connection conn = Koneksi.configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + tfCari.getText() + "%");
            pst.setString(2, "%" + tfCari.getText() + "%");
            ResultSet res = pst.executeQuery();
            while (res.next()) {
                model.addRow(new Object[]{
                    res.getString("id_user"),
                    res.getString("nama"),
                    res.getString("username"),
                    res.getString("role"),
                    res.getString("level_user") // Menggunakan alias 'level_user'
                });
            }
            tblUser.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat tabel: " + e.getMessage());
        }
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
private void reset_form() {
        tfNama.setText("");
        tfUser.setText("");
        tfPass.setText("");
        cbRole.setSelectedIndex(0);
        cbLevel.setSelectedIndex(0); // Kembali ke 'Semua'
        isEditMode = false;
        idTerpilih = "";
        btnSimpan.setText("Simpan");
        updateLevelState();
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
        tfUser = new javax.swing.JTextField();
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
        jLabel8 = new javax.swing.JLabel();
        cbLevel = new javax.swing.JComboBox<>();
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
        setMinimumSize(new java.awt.Dimension(1160, 640));
        setPreferredSize(new java.awt.Dimension(1160, 640));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel2.setText("Username :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 300, 30));

        tfUser.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        tfUser.addActionListener(this::tfUserActionPerformed);
        jPanel1.add(tfUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 300, 30));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel3.setText("Password :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 300, 30));

        tfPass.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        tfPass.addActionListener(this::tfPassActionPerformed);
        jPanel1.add(tfPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 300, 30));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel4.setText("Nama :");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 300, 30));

        tfNama.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        tfNama.addActionListener(this::tfNamaActionPerformed);
        jPanel1.add(tfNama, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 300, 30));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel5.setText("Role :");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 300, 30));

        cbRole.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        cbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "teknisi" }));
        cbRole.addActionListener(this::cbRoleActionPerformed);
        jPanel1.add(cbRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 300, 30));

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel6.setText("No :");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 200, 30));

        tfNo.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        tfNo.addActionListener(this::tfNoActionPerformed);
        jPanel1.add(tfNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 300, 30));

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);
        jPanel1.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 480, 300, 40));

        jLabel1.setBackground(new java.awt.Color(4, 102, 200));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH USER");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 340, 40));

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel8.setText("level :");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 300, 30));

        cbLevel.setFont(new java.awt.Font("Segoe UI Historic", 0, 12)); // NOI18N
        cbLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "teknisi" }));
        cbLevel.addActionListener(this::cbLevelActionPerformed);
        jPanel1.add(cbLevel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 440, 300, 30));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 340, 600));

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

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 770, 460));

        tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfCariKeyReleased(evt);
            }
        });
        jPanel2.add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 190, 30));

        btnEdit.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnEdit.setText("EDIT [F1]");
        btnEdit.addActionListener(this::btnEditActionPerformed);
        jPanel2.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 60, 120, 30));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        jPanel2.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, 120, 30));

        btnCari.setBackground(new java.awt.Color(204, 204, 204));
        btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        jPanel2.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 60, 120, 30));

        btnHapus.setBackground(new java.awt.Color(255, 0, 0));
        btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnHapus.setText("HAPUS [Del]");
        btnHapus.addActionListener(this::btnHapusActionPerformed);
        jPanel2.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 60, 120, 30));

        jLabel7.setBackground(new java.awt.Color(4, 102, 200));
        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("DAFTAR USER");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel7.setOpaque(true);
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 790, 40));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 10, 790, 600));
    }// </editor-fold>//GEN-END:initComponents

    private void tfUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUserActionPerformed

    private void tfNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNamaActionPerformed

    private void tfNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNoActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        String nama = tfNama.getText();
        String user = tfUser.getText();
        String pass = tfPass.getText();
        String role = cbRole.getSelectedItem().toString();
        String level = cbLevel.getSelectedItem().toString();

        if (nama.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Username wajib diisi!");
            return;
        }

        try {
            Connection conn = Koneksi.configDB();
            if (isEditMode) {
                // UPDATE: menyertakan kolom level
                String sql = "UPDATE tbl_user SET nama=?, username=?, password=?, role=?, level=? WHERE id_user=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setString(2, user);
                pst.setString(3, pass);
                pst.setString(4, role);
                pst.setString(5, level);
                pst.setString(6, idTerpilih);
                pst.executeUpdate();
            } else {
                // INSERT: menyertakan kolom level
                String sql = "INSERT INTO tbl_user (nama, username, password, role, level) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setString(2, user);
                pst.setString(3, pass);
                pst.setString(4, role);
                pst.setString(5, level);
                pst.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan");
            load_table();
            reset_form();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Simpan: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        load_table();
        reset_form();
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
        int baris = tblUser.getSelectedRow();
        if (baris != -1) {
            isEditMode = true;
            idTerpilih = tblUser.getValueAt(baris, 0).toString();
            tfNama.setText(tblUser.getValueAt(baris, 1).toString());
            tfUser.setText(tblUser.getValueAt(baris, 2).toString());
            cbRole.setSelectedItem(tblUser.getValueAt(baris, 3).toString());
            
            // Set level dari tabel ke combobox
            String levelVal = tblUser.getValueAt(baris, 4).toString();
            cbLevel.setSelectedItem(levelVal);
            
            updateLevelState();
            btnSimpan.setText("Update");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel dulu!");
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here :
        int baris = tblUser.getSelectedRow();
        if (baris != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String id = tblUser.getValueAt(baris, 0).toString();
                    Connection conn = Koneksi.configDB();
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM tbl_user WHERE id_user=?");
                    pst.setString(1, id);
                    pst.executeUpdate();
                    load_table();
                    reset_form();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void tfPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPassActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPassActionPerformed

    private void cbRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRoleActionPerformed
        // TODO add your handling code here:
        updateLevelState();
    }//GEN-LAST:event_cbRoleActionPerformed

    private void cbLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLevelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbLevelActionPerformed

    private void tfCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCariKeyReleased
        // TODO add your handling code here
        load_table();
    }//GEN-LAST:event_tfCariKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cbLevel;
    private javax.swing.JComboBox<String> cbRole;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblUser;
    private javax.swing.JTextField tfCari;
    private javax.swing.JTextField tfNama;
    private javax.swing.JTextField tfNo;
    private javax.swing.JPasswordField tfPass;
    private javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables
}
