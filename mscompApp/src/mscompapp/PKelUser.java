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
import java.sql.Statement;
import java.sql.SQLException;

/**
 *
 * @author ASUS
 */
public class PKelUser extends javax.swing.JPanel {
    private boolean isEditMode = false;
    private String idTerpilih = "";
    
    private java.util.HashMap<String, String> mapLevelToId = new java.util.HashMap<>();
    private java.util.HashMap<String, String> mapIdToLevel = new java.util.HashMap<>();
    /**
     * Creates new form pDashboard
     */
    public PKelUser() {
        initComponents();
        load_table();
        reset_form();
        auto_number(); // Tambahkan ini
        initKeyShortcuts();
        isiLevel();
        updateLevelState();
    }
    
    private void initKeyShortcuts() {
        // Menggunakan WHEN_IN_FOCUSED_WINDOW agar shortcut jalan dimanapun fokus kursor berada
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // 1. ENTER -> Button Simpan
        // 1. ENTER -> Button Simpan
im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cmdSimpan");
am.put("cmdSimpan", new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Pindahkan fokus agar input yang sedang diketik ter-commit
        btnSimpan.requestFocusInWindow(); 
        
        if (btnSimpan.isEnabled()) {
            btnSimpan.doClick();
        }
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
        // 3. F3 -> Button Refresh
im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
am.put("cmdRefresh", new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Tambahkan ini: Pindahkan fokus ke panel agar tidak 'nyangkut' di tabel/input
        requestFocusInWindow(); 
        
        if (btnRefresh.isEnabled()) {
            btnRefresh.doClick();
        }
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
    cbLevel.removeAllItems();
    mapLevelToId.clear();
    mapIdToLevel.clear();

    cbLevel.addItem("Semua");
    
    try {
        // Menggunakan Koneksi.configDB() sesuai file Koneksi.java kamu
        Connection conn = config.Koneksi.configDB();
        Statement stmt = conn.createStatement();
        String sql = "SELECT id_kategori, nama_jenis FROM tbl_jenis_perangkat";
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            String idKategori = rs.getString("id_kategori");
            String namaJenis = rs.getString("nama_jenis");
            
            cbLevel.addItem(namaJenis);
            
            // Simpan mapping ke HashMap
            mapLevelToId.put(namaJenis, idKategori);
            mapIdToLevel.put(idKategori, namaJenis);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data level: " + e.getMessage());
    }
}
    
    
    private void load_table() {
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Ini akan membuat semua sel tidak bisa diklik/ketik
        }
    };
    model.addColumn("No");         // Indeks 0
    model.addColumn("ID User");    // Indeks 1 (Akan disembunyikan)
    model.addColumn("Username");   // Indeks 2
    model.addColumn("Nama");       // Indeks 3
    model.addColumn("Password");   // Indeks 4
    model.addColumn("Role");       // Indeks 5
    model.addColumn("Level");      // Indeks 6

    try {
        int no = 1;
        Connection conn = Koneksi.configDB();
        
        // Ambil kata kunci dari textfield cari
        String cari = tfCari.getText();
        String sql = "SELECT id_user, username, nama, password, role, level FROM tbl_user";
        
        // Jika tfCari tidak kosong, tambahkan perintah WHERE
        if (!cari.isEmpty()) {
            sql += " WHERE username LIKE '%" + cari + "%' OR nama LIKE '%" + cari + "%'";
        }

        java.sql.Statement stat = conn.createStatement();
        ResultSet res = stat.executeQuery(sql);
        
        while(res.next()) {
            model.addRow(new Object[]{
                no++,
                res.getString("id_user"),
                res.getString("username"),
                res.getString("nama"),
                res.getString("password"),
                res.getString("role"),
                res.getString("level")
            });
        }
        tblUser.setModel(model);

        // Tetap sembunyikan kolom ID agar tampilan rapi
        tblUser.getColumnModel().getColumn(1).setMinWidth(0);
        tblUser.getColumnModel().getColumn(1).setMaxWidth(0);
        tblUser.getColumnModel().getColumn(1).setWidth(0);

        conn.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}

// Mengambil calon nomor ID selanjutnya secara otomatis
private void auto_number() {
    try {
        Connection conn = Koneksi.configDB();
        Statement st = conn.createStatement();
        // Query ini akan mencari ID terkecil yang belum terpakai (celah kosong)
        String sql = "SELECT (t1.id_user + 1) AS gap_id " +
                     "FROM tbl_user t1 " +
                     "LEFT JOIN tbl_user t2 ON t1.id_user + 1 = t2.id_user " +
                     "WHERE t2.id_user IS NULL ORDER BY t1.id_user LIMIT 1";
        ResultSet rs = st.executeQuery(sql);
        
        if (rs.next()) {
            tfNo.setText(rs.getString("gap_id"));
        } else {
            // Jika tabel benar-benar kosong
            tfNo.setText("1");
        }
        conn.close();
    } catch (Exception e) {
        // Fallback ke MAX+1 jika query di atas bermasalah
        // (Gunakan kode MAX(id_user) yang sebelumnya jika ingin lebih sederhana)
    }
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
        auto_number(); // Panggil di sini agar ID refresh setelah simpan/batal
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    // Objek GridBagConstraints untuk mengatur responsifitas
    java.awt.GridBagConstraints gbc;

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

    // SETINGAN UTAMA AGAR MEMENUHI pMain & RESPONSIF
    this.setLayout(new java.awt.GridBagLayout());

    // ==========================================
    // PANEL 1 (FORM TAMBAH USER - KIRI)
    // ==========================================
    jPanel1.setBackground(new java.awt.Color(255, 255, 255));
    jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel1.setMinimumSize(new java.awt.Dimension(340, 600));
    jPanel1.setPreferredSize(new java.awt.Dimension(340, 600));
    jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    // ... (Komponen jPanel1 tetap AbsoluteLayout sesuai desain aslimu)
    jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel2.setText("Username :");
    jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 300, 30));

    tfUser.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    tfUser.addActionListener(this::tfUserActionPerformed);
    jPanel1.add(tfUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 300, 30));

    jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel3.setText("Password :");
    jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 300, 30));

    tfPass.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    tfPass.addActionListener(this::tfPassActionPerformed);
    jPanel1.add(tfPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 300, 30));

    jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel4.setText("Nama :");
    jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 300, 30));

    tfNama.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    tfNama.addActionListener(this::tfNamaActionPerformed);
    jPanel1.add(tfNama, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 300, 30));

    jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel5.setText("Role :");
    jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 300, 30));

    cbRole.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    cbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "teknisi" }));
    cbRole.addActionListener(this::cbRoleActionPerformed);
    jPanel1.add(cbRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 300, 30));

    jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel6.setText("No :");
    jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 200, 30));

    tfNo.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    tfNo.addActionListener(this::tfNoActionPerformed);
    jPanel1.add(tfNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 300, 30));

    btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
    btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    btnSimpan.setText("SIMPAN [Enter]");
    btnSimpan.addActionListener(this::btnSimpanActionPerformed);
    jPanel1.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 480, 300, 40));

    jLabel1.setBackground(new java.awt.Color(4, 102, 200));
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel1.setForeground(new java.awt.Color(255, 255, 255));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("TAMBAH USER");
    jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jLabel1.setOpaque(true);
    jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 340, 40));

    jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12));
    jLabel8.setText("level :");
    jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 300, 30));

    cbLevel.setFont(new java.awt.Font("Segoe UI Historic", 0, 12));
    cbLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "teknisi" }));
    cbLevel.addActionListener(this::cbLevelActionPerformed);
    jPanel1.add(cbLevel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 440, 300, 30));

    // Tambah Panel 1 ke Layout Utama
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new java.awt.Insets(10, 10, 10, 5);
    gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(jPanel1, gbc);

    // ==========================================
    // ==========================================
    // PANEL 2 (DAFTAR USER - KANAN)
    // ==========================================
    jPanel2.setBackground(new java.awt.Color(255, 255, 255));
    jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    // Menggunakan BorderLayout agar komponen di dalamnya bisa melar
    jPanel2.setLayout(new java.awt.BorderLayout());

    // --- PERBAIKAN JUDUL ---
    // Jangan masukkan jLabel7 ke dalam pnlHeaderKanan yang Absolute, 
    // tapi letakkan langsung di NORTH dari jPanel2
    jLabel7.setBackground(new java.awt.Color(4, 102, 200));
    jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18));
    jLabel7.setForeground(new java.awt.Color(255, 255, 255));
    jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel7.setText("DAFTAR USER");
    jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jLabel7.setOpaque(true);
    jLabel7.setPreferredSize(new java.awt.Dimension(100, 40)); // Lebar 100 hanya formalitas, akan melar karena BorderLayout
    jPanel2.add(jLabel7, java.awt.BorderLayout.NORTH);

    // Sub-Panel untuk menampung Tombol-tombol saja
    javax.swing.JPanel pnlTombol = new javax.swing.JPanel();
    pnlTombol.setBackground(new java.awt.Color(255, 255, 255));
    pnlTombol.setPreferredSize(new java.awt.Dimension(860, 60)); 
    pnlTombol.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    // tfCari - Lebar 250
    tfCari.addActionListener(this::tfCariActionPerformed);
    tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) { tfCariKeyPressed(evt); }
        public void keyReleased(java.awt.event.KeyEvent evt) { tfCariKeyReleased(evt); }
    });
    pnlTombol.add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 250, 30));

    // Button Cari - Lebar 110
    btnCari.setBackground(new java.awt.Color(204, 204, 204));
    btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    btnCari.setText("CARI [F2]");
    btnCari.addActionListener(this::btnCariActionPerformed);
    pnlTombol.add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 15, 110, 30));

    // Button Refresh - Lebar 150
    btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
    btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    btnRefresh.setText("REFRESH [F3]");
    btnRefresh.addActionListener(this::btnRefreshActionPerformed);
    pnlTombol.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 15, 150, 30));

    // Button Edit - Lebar 110
    btnEdit.setBackground(new java.awt.Color(255, 255, 102));
    btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    btnEdit.setText("EDIT [F1]");
    btnEdit.addActionListener(this::btnEditActionPerformed);
    pnlTombol.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 15, 110, 30));

    // --- PERBAIKAN: BUTTON HAPUS DIPERPANJANG KE 160 ---
    btnHapus.setBackground(new java.awt.Color(255, 0, 0));
    btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    btnHapus.setText("HAPUS [Del]");
    btnHapus.addActionListener(this::btnHapusActionPerformed);
    // Lebar diatur ke 160 agar teks "[Del]" tidak terpotong
    pnlTombol.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 15, 160, 30));

    // Panel Tengah untuk menampung pnlTombol dan Tabel
    javax.swing.JPanel pnlContent = new javax.swing.JPanel(new java.awt.BorderLayout());
    pnlContent.setOpaque(false);
    pnlContent.add(pnlTombol, java.awt.BorderLayout.NORTH);

    // Bagian Tabel
    tblUser.setFont(new java.awt.Font("Segoe UI", 0, 14));
    tblUser.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null}
        },
        new String [] { "No", "Username", "Password", "Nama", "Role", "Level" }
    ));
    tblUser.setRowHeight(35);
    jScrollPane2.setViewportView(tblUser);
    jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
    
    pnlContent.add(jScrollPane2, java.awt.BorderLayout.CENTER);
    
    // Masukkan semua konten ke CENTER jPanel2
    jPanel2.add(pnlContent, java.awt.BorderLayout.CENTER);

    // Tambah Panel 2 ke Layout Utama (MELEBAR/RESPONSIF)
    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0; 
    gbc.weighty = 1.0; 
    gbc.fill = java.awt.GridBagConstraints.BOTH;
    gbc.insets = new java.awt.Insets(10, 5, 10, 10);
    add(jPanel2, gbc);
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
        // 1. Validasi: Cek apakah ada field yang kosong
    String idUser = tfNo.getText();
    String nama = tfNama.getText();
    String username = tfUser.getText();
    String pass = new String(tfPass.getPassword());
    String role = cbRole.getSelectedItem().toString();
    
    // Logika penentuan ID Kategori vs "Semua"
    String selectedLevelText = cbLevel.getSelectedItem().toString();
    String levelValue;
    if (selectedLevelText.equals("Semua")) {
        levelValue = "Semua";
    } else {
        levelValue = mapLevelToId.get(selectedLevelText);
    }

    if (nama.isEmpty() || username.isEmpty() || pass.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
        return;
    }

    try {
        Connection conn = config.Koneksi.configDB();
        if (isEditMode) {
            String sql = "UPDATE tbl_user SET nama=?, username=?, password=?, role=?, level=? WHERE id_user=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, nama);
            pst.setString(2, username);
            pst.setString(3, pass);
            pst.setString(4, role);
            pst.setString(5, levelValue); 
            pst.setString(6, idTerpilih);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Diupdate");
        } else {
            String sql = "INSERT INTO tbl_user (id_user, nama, username, password, role, level) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idUser);
            pst.setString(2, nama);
            pst.setString(3, username);
            pst.setString(4, pass);
            pst.setString(5, role);
            pst.setString(6, levelValue);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan");
        }
        load_table();
        reset_form();
        auto_number(); // Memastikan ID baru ter-generate setelah simpan
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saat menyimpan: " + e.getMessage());
    }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText(""); // Sangat penting agar filter pencarian hilang
        load_table();
        reset_form();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
                load_table(); // Ini akan memanggil fungsi yang strukturnya sudah benar

       
    }//GEN-LAST:event_btnCariActionPerformed

    private void tblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUserMouseClicked
        // TODO add your handling code here:
        int baris = tblUser.rowAtPoint(evt.getPoint());
    isEditMode = true;
    btnSimpan.setText("Update");
    
    // Mengisi data ke TextField
    idTerpilih = tblUser.getValueAt(baris, 0).toString();
    tfNo.setText(idTerpilih);
    tfNama.setText(tblUser.getValueAt(baris, 1).toString());
    tfUser.setText(tblUser.getValueAt(baris, 2).toString());
    tfPass.setText(tblUser.getValueAt(baris, 3).toString());
    cbRole.setSelectedItem(tblUser.getValueAt(baris, 4).toString());
    
    // Menampilkan Nama di cbLevel berdasarkan ID yang tersimpan di database
    String levelDariDB = tblUser.getValueAt(baris, 5).toString();
    if (levelDariDB.equals("Semua")) {
        cbLevel.setSelectedItem("Semua");
    } else {
        String namaJenis = mapIdToLevel.get(levelDariDB);
        if (namaJenis != null) {
            cbLevel.setSelectedItem(namaJenis);
        } else {
            cbLevel.setSelectedItem("Semua");
        }
    }
    updateLevelState();
    }//GEN-LAST:event_tblUserMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int baris = tblUser.getSelectedRow();
    if (baris != -1) {
        isEditMode = true;
        
        // Ambil ID dari kolom ke-1 (indeks 1) karena indeks 0 adalah No urut
        idTerpilih = tblUser.getValueAt(baris, 1).toString(); 
        
        // Update Field No dengan ID User yang mau diedit
        tfNo.setText(idTerpilih); 
        
        // Ambil data lainnya (sesuaikan indeks kolom dengan load_table)
        tfUser.setText(tblUser.getValueAt(baris, 2).toString());
        tfNama.setText(tblUser.getValueAt(baris, 3).toString());
        tfPass.setText(tblUser.getValueAt(baris, 4).toString()); // Password sekarang ada di tabel
        cbRole.setSelectedItem(tblUser.getValueAt(baris, 5).toString());
        cbLevel.setSelectedItem(tblUser.getValueAt(baris, 6).toString());
        
        btnSimpan.setText("Perbarui");
    } else {
        JOptionPane.showMessageDialog(this, "Pilih data yang akan diedit!");
    }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here :
        int baris = tblUser.getSelectedRow();
    if (baris != -1) {
        // Ambil ID dari kolom indeks 1 (ID User), bukan indeks 0 (No urut)
        String id = tblUser.getValueAt(baris, 1).toString(); 
        
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus data dengan ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = Koneksi.configDB();
                // Query hapus berdasarkan id_user
                PreparedStatement pst = conn.prepareStatement("DELETE FROM tbl_user WHERE id_user=?");
                pst.setString(1, id);
                pst.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Data Berhasil Dihapus");
                load_table();
                reset_form();
                conn.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Pilih data di tabel yang ingin dihapus!");
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
        // TODO add your handling code her
    }//GEN-LAST:event_tfCariKeyReleased

    private void tfCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCariActionPerformed
        // TODO add your handling code here
    }//GEN-LAST:event_tfCariActionPerformed

    private void btnCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCariKeyReleased
        // TODO add your handling code here:
        load_table(); // Ini akan memanggil fungsi yang strukturnya sudah benar
    }//GEN-LAST:event_btnCariKeyReleased

    private void tfCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCariKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCariKeyPressed

    private void btnRefreshKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnRefreshKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRefreshKeyReleased


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
