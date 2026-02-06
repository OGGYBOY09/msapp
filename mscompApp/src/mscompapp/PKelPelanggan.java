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
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        tIdPelanggan1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tNama1 = new javax.swing.JTextField();
        tNoHp1 = new javax.swing.JTextField();
        tAlamat1 = new javax.swing.JTextField();
        btnSimpan1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tPlgn1 = new javax.swing.JTable();
        tfCari1 = new javax.swing.JTextField();
        btnCari1 = new javax.swing.JButton();
        btnRefresh1 = new javax.swing.JButton();
        btnEdit1 = new javax.swing.JButton();
        btnHapus1 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setPreferredSize(new java.awt.Dimension(312, 960));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("ID Pelanggan :");

        tIdPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tIdPelanggan.addActionListener(this::tIdPelangganActionPerformed);

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Nama Pelanggan :");

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("No HP :");

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel8.setText("Alamat :");

        tNama.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNama.addActionListener(this::tNamaActionPerformed);

        tNoHp.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        tAlamat.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        btnSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan.setText("SIMPAN [Enter]");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        jPanel1.setBackground(new java.awt.Color(4, 102, 200));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH PELANGGAN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(tIdPelanggan)
                    .addComponent(tNama)
                    .addComponent(tNoHp)
                    .addComponent(tAlamat)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tIdPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tNama, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tNoHp, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(426, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBackground(new java.awt.Color(4, 102, 200));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("DAFTAR PELANGGAN");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );

        tPlgn.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tPlgn.setModel(new javax.swing.table.DefaultTableModel(
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
        tPlgn.setRowHeight(35);
        jScrollPane1.setViewportView(tPlgn);

        tfCari.setText("Cari....");
        tfCari.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfCariFocusGained(evt);
            }
        });
        tfCari.addActionListener(this::tfCariActionPerformed);

        btnCari.setBackground(new java.awt.Color(204, 204, 204));
        btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnEdit.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnEdit.setText("EDIT [F1]");
        btnEdit.addActionListener(this::btnEditActionPerformed);

        btnHapus.setBackground(new java.awt.Color(255, 51, 51));
        btnHapus.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnHapus.setText("HAPUS [Del]");
        btnHapus.addActionListener(this::btnHapusActionPerformed);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1235, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnHapus)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 749, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.setMaximumSize(new java.awt.Dimension(1720, 960));
        jPanel5.setMinimumSize(new java.awt.Dimension(1720, 960));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setPreferredSize(new java.awt.Dimension(312, 960));

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel6.setText("ID Pelanggan :");

        tIdPelanggan1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tIdPelanggan1.addActionListener(this::tIdPelanggan1ActionPerformed);

        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel7.setText("Nama Pelanggan :");

        jLabel9.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel9.setText("No HP :");

        jLabel10.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel10.setText("Alamat :");

        tNama1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNama1.addActionListener(this::tNama1ActionPerformed);

        tNoHp1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        tAlamat1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        btnSimpan1.setBackground(new java.awt.Color(102, 255, 102));
        btnSimpan1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 24)); // NOI18N
        btnSimpan1.setText("SIMPAN [Enter]");
        btnSimpan1.addActionListener(this::btnSimpan1ActionPerformed);

        jPanel7.setBackground(new java.awt.Color(4, 102, 200));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel11.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("TAMBAH PELANGGAN");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel10)
                    .addComponent(tIdPelanggan1)
                    .addComponent(tNama1)
                    .addComponent(tNoHp1)
                    .addComponent(tAlamat1)
                    .addComponent(btnSimpan1, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tIdPelanggan1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tNama1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tNoHp1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tAlamat1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSimpan1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel9.setBackground(new java.awt.Color(4, 102, 200));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("DAFTAR PELANGGAN");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel8.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, 1287, -1));

        tPlgn1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tPlgn1.setModel(new javax.swing.table.DefaultTableModel(
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
        tPlgn1.setRowHeight(35);
        jScrollPane2.setViewportView(tPlgn1);

        jPanel8.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 192, 1235, 440));

        tfCari1.setText("Cari....");
        tfCari1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfCari1FocusGained(evt);
            }
        });
        tfCari1.addActionListener(this::tfCari1ActionPerformed);
        jPanel8.add(tfCari1, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 116, 472, 58));

        btnCari1.setBackground(new java.awt.Color(204, 204, 204));
        btnCari1.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnCari1.setText("CARI [F2]");
        btnCari1.addActionListener(this::btnCari1ActionPerformed);
        jPanel8.add(btnCari1, new org.netbeans.lib.awtextra.AbsoluteConstraints(519, 114, 111, 59));

        btnRefresh1.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh1.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnRefresh1.setText("REFRESH [F3]");
        btnRefresh1.addActionListener(this::btnRefresh1ActionPerformed);
        jPanel8.add(btnRefresh1, new org.netbeans.lib.awtextra.AbsoluteConstraints(636, 115, -1, 58));

        btnEdit1.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit1.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnEdit1.setText("EDIT [F1]");
        btnEdit1.addActionListener(this::btnEdit1ActionPerformed);
        jPanel8.add(btnEdit1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1007, 114, 111, 58));

        btnHapus1.setBackground(new java.awt.Color(255, 51, 51));
        btnHapus1.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnHapus1.setText("HAPUS [Del]");
        btnHapus1.addActionListener(this::btnHapus1ActionPerformed);
        jPanel8.add(btnHapus1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 114, -1, 58));

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        jPanel10.add(btnNextKiri, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 70, -1, -1));

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
        jPanel10.add(btnNextKanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 70, -1, -1));

        jPanel8.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 670, 1280, 290));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
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
                sql = "INSERT INTO tbl_pelanggan (id_ppelanggan, nama_pelanggan, no_hp, alamat) VALUES (?,?,?,?)";
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

    private void tIdPelanggan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tIdPelanggan1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tIdPelanggan1ActionPerformed

    private void tNama1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNama1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNama1ActionPerformed

    private void btnSimpan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpan1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSimpan1ActionPerformed

    private void tfCari1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfCari1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCari1FocusGained

    private void tfCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCari1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfCari1ActionPerformed

    private void btnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCari1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCari1ActionPerformed

    private void btnRefresh1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRefresh1ActionPerformed

    private void btnEdit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEdit1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEdit1ActionPerformed

    private void btnHapus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapus1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnHapus1ActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextKananActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnCari1;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEdit1;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnHapus1;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRefresh1;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnSimpan1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField tAlamat;
    private javax.swing.JTextField tAlamat1;
    private javax.swing.JTextField tIdPelanggan;
    private javax.swing.JTextField tIdPelanggan1;
    private javax.swing.JTextField tNama;
    private javax.swing.JTextField tNama1;
    private javax.swing.JTextField tNoHp;
    private javax.swing.JTextField tNoHp1;
    private javax.swing.JTable tPlgn;
    private javax.swing.JTable tPlgn1;
    private javax.swing.JTextField tfCari;
    private javax.swing.JTextField tfCari1;
    // End of variables declaration//GEN-END:variables
}
