/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template

 */
package mscompapp;

import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import config.Koneksi;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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
public class PKelService extends javax.swing.JPanel {   
    
    public int idPelanggan = 0; 

    public PKelService() {
        
        initComponents();
        auto_number_service();
        tampilkanAdmin();
        tampilTanggal();
        load_jenis_perangkat();
        load_status();       
        tampilKategori();  
        initKeyShortcuts();

        // Custom Renderer untuk Tabel
        tblServis = new javax.swing.JTable() {
            {
        setRowHeight(30); // Ubah angka 30 sesuai keinginanmu (semakin besar semakin tinggi)
        getTableHeader().setReorderingAllowed(false); // Opsional: Biar kolom gak bisa digeser-geser
    }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
                Object statusValue = getValueAt(row, 7); 

                if (statusValue != null) {
                    String status = statusValue.toString();
                    if (isRowSelected(row)) {
                        comp.setBackground(getSelectionBackground());
                    } else {
                        switch (status) {
                            case "Proses":
                                comp.setBackground(java.awt.Color.YELLOW);
                                comp.setForeground(java.awt.Color.BLACK); break;
                            case "Selesai":
                                comp.setBackground(new java.awt.Color(144, 238, 144)); 
                                comp.setForeground(java.awt.Color.BLACK); break;
                            case "Dibatalkan":
                                comp.setBackground(new java.awt.Color(255, 182, 193)); 
                                comp.setForeground(java.awt.Color.BLACK); break;
                            default: // Menunggu
                                comp.setBackground(java.awt.Color.WHITE);
                                comp.setForeground(java.awt.Color.BLACK); break;
                        }
                    }
                }
                return comp;
            }
        };
        javax.swing.SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        jScrollPane2.setViewportView(tblServis);

        load_table_service();

        // Default State
        rbLama.setSelected(true);
        btnCari.setEnabled(true);
        tNamaPelanggan.setEditable(false);
        tNoPelanggan.setEditable(false);
        tAlamatPelanggan.setEditable(false);
        
    }
    
    // --- FITUR BARU: INISIALISASI SHORTCUT KEYBOARD ---
    private void initKeyShortcuts() {
        // PERBAIKAN DISINI: Ganti WHEN_ANCESTOR_OF_FOCUSED_COMPONENT menjadi WHEN_IN_FOCUSED_WINDOW
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // 1. btSimpan = SHIFT + ENTER
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "cmdSimpan");
        am.put("cmdSimpan", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btSimpan.isEnabled()) btSimpan.doClick();
            }
        });

        // 2. btBatal = DELETE
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cmdBatal");
        am.put("cmdBatal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btBatal.isEnabled()) btBatal.doClick();
            }
        });

        
        // 4. btnCari (Pelanggan) = F2
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "cmdCariPelanggan");
        am.put("cmdCariPelanggan", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnCari.isEnabled()) btnCari.doClick();
            }
        });

        // 5. btRefresh = F3
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "cmdRefresh");
        am.put("cmdRefresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btRefresh.isEnabled()) btRefresh.doClick();
            }
        });

        // 6. btEdit = F1
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "cmdEdit");
        am.put("cmdEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btEdit.isEnabled()) btEdit.doClick();
            }
        });

        // 7. tfCari (Fokus Pencarian) = F6
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "focusCari");
        am.put("focusCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfCari.requestFocus();
            }
        });

        // 8. cbStatusServ (Fokus & Buka) = F7
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "focusStatus");
        am.put("focusStatus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbStatusServ.requestFocus();
                // Opsional: Langsung buka popup dropdown
                try { cbStatusServ.showPopup(); } catch (Exception ex) {} 
            }
        });

        // 9. cbJenisBrg (Fokus & Buka) = F8
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "focusJenis");
        am.put("focusJenis", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbJenisBrg.requestFocus();
                // Opsional: Langsung buka popup dropdown
                try { cbJenisBrg.showPopup(); } catch (Exception ex) {}
            }
        });
    }
    
    // --- LOAD DATA & HELPER METHODS ---
    
    private void tampilKategori() {
    try {
        cbJenisBrg.removeAllItems();
        cbJenisBrg.addItem("- Pilih Jenis -");
        String sql = "SELECT nama_jenis FROM tbl_jenis_perangkat";
        java.sql.Connection conn = (java.sql.Connection)config.Koneksi.configDB();
        java.sql.Statement stm = conn.createStatement();
        java.sql.ResultSet res = stm.executeQuery(sql);
        while (res.next()) {
            cbJenisBrg.addItem(res.getString("nama_jenis"));
        }
    } catch (Exception e) {
        System.out.println("Error tampil kategori: " + e.getMessage());
    }
}
    
    private void tampilkanAdmin() {
        try {
            String namaLog = Login.namaUser; 
            if (namaLog != null && !namaLog.isEmpty()) {
                tNamaAdmin.setText(namaLog);
            } else {
                tNamaAdmin.setText("Admin");
            }
        } catch (Exception e) {
            tNamaAdmin.setText("Admin");
        }
        tNamaAdmin.setEditable(false);
    }
    
    private void load_jenis_perangkat() {
        cbJenisBrg.removeAllItems();
        cbJenisBrg.addItem("- Pilih Jenis -");
        try {
            String sql = "SELECT nama_jenis FROM tbl_jenis_perangkat";
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            while(res.next()) {
                cbJenisBrg.addItem(res.getString("nama_jenis"));
            }
        } catch (Exception e) {}
    }
    
    private void load_status() {
        cbStatusServ.removeAllItems();
        cbStatusServ.addItem("Menunggu");
        cbStatusServ.addItem("Proses");
        cbStatusServ.addItem("Selesai");
        cbStatusServ.addItem("Dibatalkan");
    }
    
    public void pelangganTerpilih(String id, String nama, String hp, String alamat) {
        idPelanggan = Integer.parseInt(id);
        tNamaPelanggan.setText(nama);
        tNoPelanggan.setText(hp);
        tAlamatPelanggan.setText(alamat);
    }

    private void auto_number_service() {
        try {
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            String sql = "SELECT id_servis FROM servis ORDER BY id_servis DESC LIMIT 1";
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            if (res.next()) {
                String kode = res.getString("id_servis").substring(3); 
                int AN = Integer.parseInt(kode) + 1;
                String nol = (AN < 10) ? "000" : (AN < 100) ? "00" : (AN < 1000) ? "0" : "";
                tNomorServ.setText("SRV" + nol + AN);
            } else {
                tNomorServ.setText("SRV0001");
            }
            tNomorServ.setEditable(false);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void tampilTanggal() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        tTgl.setText(df.format(new Date()));
    }
    
    private int getAdminId() {
        try {
            String sql = "SELECT id_user FROM tbl_user WHERE username = ?";
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tNamaAdmin.getText());
            java.sql.ResultSet res = pst.executeQuery();
            if (res.next()) {
                return res.getInt("id_user");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 1; 
    }

    private void load_table_service() {
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No Servis");
        model.addColumn("Nama Pelanggan");
        model.addColumn("No HP");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        model.addColumn("Model");
        model.addColumn("Keluhan");
        model.addColumn("Status");

        try {
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, s.jenis_barang, s.merek, s.model, s.keluhan_awal, s.status " +
                         "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan ";

            String keyword = tfCari.getText();

            if (!keyword.isEmpty()) {
                sql += " WHERE p.nama_pelanggan LIKE ? OR s.id_servis LIKE ? ";
            }

            sql += " ORDER BY s.tanggal_masuk DESC";

            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);

            if (!keyword.isEmpty()) {
                pst.setString(1, "%" + keyword + "%");
                pst.setString(2, "%" + keyword + "%");
            }

            java.sql.ResultSet res = pst.executeQuery();

            while(res.next()){
                model.addRow(new Object[]{
                    
                    res.getString("id_servis"),
                    res.getString("nama_pelanggan"),
                    res.getString("no_hp"),
                    res.getString("jenis_barang"),
                    res.getString("merek"),
                    res.getString("model"),
                    res.getString("keluhan_awal"),
                    res.getString("status")
                       
                        
                });
            }
            tblServis.setModel(model);

        } catch (Exception e) {
            System.out.println("Error Load Table: " + e.getMessage());
            e.printStackTrace();
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

        jSeparator1 = new javax.swing.JSeparator();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tNomorServ = new javax.swing.JTextField();
        tNamaAdmin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tTgl = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cbStatusServ = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        rbBaru = new javax.swing.JRadioButton();
        rbLama = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tNamaPelanggan = new javax.swing.JTextField();
        tNoPelanggan = new javax.swing.JTextField();
        tAlamatPelanggan = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        btBatal = new javax.swing.JButton();
        btSimpan = new javax.swing.JButton();
        btnCari = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        cbJenisBrg = new javax.swing.JComboBox<>();
        tMerek = new javax.swing.JTextField();
        tModel = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        tSeri = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tKeluhan = new javax.swing.JTextArea();
        tKelengkapan = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblServis = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        tfCari = new javax.swing.JTextField();
        btRefresh = new javax.swing.JButton();
        btEdit = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1720, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBackground(new java.awt.Color(3, 83, 164));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 26)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("INPUT DATA SERVIS");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
        );

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel2.setText("Nomor Service :");

        tNomorServ.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNomorServ.addActionListener(this::tNomorServActionPerformed);

        tNamaAdmin.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("Admin :");

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel6.setText("Tanggal :");

        tTgl.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        tTgl.setText("000");

        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel7.setText("Status [F7] :");

        cbStatusServ.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbStatusServ.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Proses", "Menunggu", "Selesai", "Dibatalkan" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tNomorServ, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tNamaAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(84, 84, 84)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(461, 461, 461)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tTgl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbStatusServ, 0, 147, Short.MAX_VALUE))
                .addContainerGap(48, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tNomorServ)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tTgl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tNamaAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbStatusServ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        buttonGroup1.add(rbBaru);
        rbBaru.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        rbBaru.setText("Pelanggan Baru");
        rbBaru.addActionListener(this::rbBaruActionPerformed);

        buttonGroup1.add(rbLama);
        rbLama.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        rbLama.setText("Pelangggan Lama");
        rbLama.addActionListener(this::rbLamaActionPerformed);

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Cari No Hp / Nama :");

        jLabel9.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel9.setText("Nama :");

        jLabel13.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel13.setText("No Hp :");

        jLabel14.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel14.setText("Alamat :");

        tNamaPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNamaPelanggan.addActionListener(this::tNamaPelangganActionPerformed);

        tNoPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tNoPelanggan.addActionListener(this::tNoPelangganActionPerformed);

        tAlamatPelanggan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jPanel6.setBackground(new java.awt.Color(3, 83, 164));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 26)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("DATA PELANGGAN");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
        );

        btBatal.setBackground(new java.awt.Color(255, 51, 51));
        btBatal.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); // NOI18N
        btBatal.setText("BATAL [DEL]");
        btBatal.addActionListener(this::btBatalActionPerformed);

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 18)); // NOI18N
        btSimpan.setText("SIMPAN [SHIFT+ENTER]");
        btSimpan.addActionListener(this::btSimpanActionPerformed);

        btnCari.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnCari.setText("Cari... [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(18, 18, 18)
                                .addComponent(tAlamatPelanggan))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(22, 22, 22)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tNamaPelanggan)
                                    .addComponent(tNoPelanggan)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbLama, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbBaru, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(37, 37, 37))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btSimpan, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(btBatal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(37, 37, 37))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(rbBaru)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbLama)
                .addGap(41, 41, 41)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tNamaPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(tNoPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(tAlamatPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addComponent(btSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(177, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel5.setBackground(new java.awt.Color(3, 83, 164));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 26)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("DATA PERANGKAT/SERVIS");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
        );

        cbJenisBrg.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbJenisBrg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbJenisBrg.addActionListener(this::cbJenisBrgActionPerformed);

        tMerek.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tMerek.addActionListener(this::tMerekActionPerformed);

        tModel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tModel.addActionListener(this::tModelActionPerformed);

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel8.setText("Merek :");

        jLabel10.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel10.setText("Kelengkapan :");

        jLabel15.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel15.setText("Jenis Barang [F8] :");

        jLabel16.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel16.setText("Model / Tipe :");

        jLabel17.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel17.setText("No Seri :");

        tSeri.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tSeri.addActionListener(this::tSeriActionPerformed);

        jLabel18.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel18.setText("Keluhan :");

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        tKeluhan.setColumns(20);
        tKeluhan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tKeluhan.setRows(5);
        tKeluhan.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(tKeluhan);

        tKelengkapan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tKelengkapan.addActionListener(this::tKelengkapanActionPerformed);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel15)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel18)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tKelengkapan, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(tMerek)
                        .addComponent(tModel)
                        .addComponent(cbJenisBrg, 0, 293, Short.MAX_VALUE)
                        .addComponent(tSeri)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbJenisBrg, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tMerek, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tModel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addGap(33, 33, 33)
                        .addComponent(jLabel17))
                    .addComponent(tSeri, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tKelengkapan, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(39, 39, 39)
                        .addComponent(jLabel11))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel7.setPreferredSize(new java.awt.Dimension(815, 960));

        jPanel8.setBackground(new java.awt.Color(3, 83, 164));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel19.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("DAFTAR SERVIS");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                .addContainerGap())
        );

        tblServis.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblServis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblServis.setEditingColumn(0);
        tblServis.setEditingRow(0);
        tblServis.setRowHeight(35);
        jScrollPane2.setViewportView(tblServis);
        if (tblServis.getColumnModel().getColumnCount() > 0) {
            tblServis.getColumnModel().getColumn(7).setResizable(false);
            tblServis.getColumnModel().getColumn(8).setResizable(false);
        }

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel20.setText("Cari [F6] :");

        tfCari.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfCariKeyReleased(evt);
            }
        });

        btRefresh.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btRefresh.setText("Refresh [F3]");
        btRefresh.addActionListener(this::btRefreshActionPerformed);

        btEdit.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btEdit.setText("Edit [F1]");
        btEdit.addActionListener(this::btEditActionPerformed);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(90, 90, 90)
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btRefresh)
                        .addComponent(btEdit))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(tfCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbBaruActionPerformed
        btnCari.setEnabled(false);
        tNamaPelanggan.setEditable(true);
        tNoPelanggan.setEditable(true);
        tAlamatPelanggan.setEditable(true);

        tNamaPelanggan.setText("");
        tNoPelanggan.setText("");
        tAlamatPelanggan.setText("");
        tNamaPelanggan.requestFocus();
        idPelanggan = 0; // Reset ID
    }//GEN-LAST:event_rbBaruActionPerformed

    private void rbLamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbLamaActionPerformed
        // TODO add your handling code here:
        btnCari.setEnabled(true);
        tNamaPelanggan.setEditable(false);
        tNoPelanggan.setEditable(false);
        tAlamatPelanggan.setEditable(false);

        tNamaPelanggan.setText("");
        tNoPelanggan.setText("");
        tAlamatPelanggan.setText("");
    }//GEN-LAST:event_rbLamaActionPerformed

    private void tNamaPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNamaPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNamaPelangganActionPerformed

    private void tModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tModelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tModelActionPerformed

    private void tSeriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSeriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tSeriActionPerformed

    private void tNomorServActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNomorServActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNomorServActionPerformed

    private void tNoPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNoPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNoPelangganActionPerformed

    private void btSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSimpanActionPerformed

        try {
            // 1. Validasi Input
            if (tNamaPelanggan.getText().isEmpty() || cbJenisBrg.getSelectedIndex() == 0 || tKeluhan.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama, Jenis Barang, dan Keluhan wajib diisi!");
                return;
            }

            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            int finalIdPelanggan = idPelanggan;

            // 2. Logika Pelanggan
            if (rbBaru.isSelected()) {
                String sqlPelanggan = "INSERT INTO tbl_pelanggan (nama_pelanggan, no_hp, alamat) VALUES (?, ?, ?)";
                java.sql.PreparedStatement pstPel = conn.prepareStatement(sqlPelanggan, java.sql.Statement.RETURN_GENERATED_KEYS);
                pstPel.setString(1, tNamaPelanggan.getText());
                pstPel.setString(2, tNoPelanggan.getText());
                pstPel.setString(3, tAlamatPelanggan.getText());
                pstPel.executeUpdate();

                java.sql.ResultSet rsId = pstPel.getGeneratedKeys();
                if (rsId.next()) {
                    finalIdPelanggan = rsId.getInt(1);
                }
            } else {
                if (finalIdPelanggan == 0) {
                    JOptionPane.showMessageDialog(this, "Mohon cari dan pilih data pelanggan lama!");
                    return;
                }
                // Opsional: Update data pelanggan lama jika ada perubahan di form
                String updatePel = "UPDATE tbl_pelanggan SET nama_pelanggan=?, no_hp=?, alamat=? WHERE id_pelanggan=?";
                java.sql.PreparedStatement pstUpPel = conn.prepareStatement(updatePel);
                pstUpPel.setString(1, tNamaPelanggan.getText());
                pstUpPel.setString(2, tNoPelanggan.getText());
                pstUpPel.setString(3, tAlamatPelanggan.getText());
                pstUpPel.setInt(4, finalIdPelanggan);
                pstUpPel.executeUpdate();
            }

            // 3. Logika Simpan atau Update Tabel Servis
            // Cek apakah ID Servis sudah ada
            String checkSql = "SELECT COUNT(*) FROM servis WHERE id_servis = ?";
            java.sql.PreparedStatement pstCheck = conn.prepareStatement(checkSql);
            pstCheck.setString(1, tNomorServ.getText());
            java.sql.ResultSet rsCheck = pstCheck.executeQuery();
            rsCheck.next();
            boolean isUpdate = rsCheck.getInt(1) > 0;

            String sqlService;
            if (isUpdate) {
                // Query UPDATE
                sqlService = "UPDATE servis SET id_pelanggan=?, jenis_barang=?, merek=?, model=?, no_seri=?, kelengkapan=?, keluhan_awal=?, status=? WHERE id_servis=?";
            } else {
                // Query INSERT
                sqlService = "INSERT INTO servis (id_pelanggan, jenis_barang, merek, model, no_seri, kelengkapan, keluhan_awal, status, tanggal_masuk, id_admin, id_servis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

            java.sql.PreparedStatement pstServ = conn.prepareStatement(sqlService);

            // Set parameter (Urutan harus sama dengan query di atas)
            pstServ.setInt(1, finalIdPelanggan);
            pstServ.setString(2, cbJenisBrg.getSelectedItem().toString());
            pstServ.setString(3, tMerek.getText());
            pstServ.setString(4, tModel.getText());
            pstServ.setString(5, tSeri.getText());
            pstServ.setString(6, tKelengkapan.getText());
            pstServ.setString(7, tKeluhan.getText());
            pstServ.setString(8, cbStatusServ.getSelectedItem().toString());

            if (isUpdate) {
                pstServ.setString(9, tNomorServ.getText());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                pstServ.setString(9, sdf.format(new java.util.Date())); // tanggal_masuk
                pstServ.setInt(10, getAdminId()); // id_admin
                pstServ.setString(11, tNomorServ.getText()); // id_servis
            }

            pstServ.executeUpdate();

            String pesan = isUpdate ? "Data Berhasil Diperbarui!" : "Data Berhasil Disimpan!";
            JOptionPane.showMessageDialog(this, pesan + "\nNo: " + tNomorServ.getText());

            load_table_service();
            btBatalActionPerformed(null);
            tNomorServ.setEditable(true); // Aktifkan kembali ID field setelah reset

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Proses: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btSimpanActionPerformed

    private void tMerekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tMerekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tMerekActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        PopUpPelanggan popup = new PopUpPelanggan();
        popup.serviceForm = this; // Sambungkan pop-up dengan form ini
        popup.setVisible(true);
    }//GEN-LAST:event_btnCariActionPerformed

    private void btBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBatalActionPerformed
        // TODO add your handling code here:
        tNamaPelanggan.setText("");
    tNoPelanggan.setText("");
    tAlamatPelanggan.setText("");
    rbLama.setSelected(true);
    btnCari.setEnabled(true);
    tNamaPelanggan.setEditable(false);
    tNoPelanggan.setEditable(false);
    tAlamatPelanggan.setEditable(false);
    idPelanggan = 0;

    // 2. Membersihkan Data Perangkat/Servis
    cbJenisBrg.setSelectedIndex(0);
    tMerek.setText("");
    tModel.setText("");
    tSeri.setText("");
    tKelengkapan.setText("");
    tKeluhan.setText("");
    cbStatusServ.setSelectedIndex(0);

    // 3. Mengatur ulang ID Service ke Auto Number terbaru
    auto_number_service();
    tNomorServ.setEditable(false);
    }//GEN-LAST:event_btBatalActionPerformed

    private void cbJenisBrgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbJenisBrgActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_cbJenisBrgActionPerformed

    private void tfCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCariKeyReleased
        // TODO add your handling code here:
        load_table_service();
    }//GEN-LAST:event_tfCariKeyReleased

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        load_table_service();
    }//GEN-LAST:event_btRefreshActionPerformed

    private void btEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditActionPerformed
        // TODO add your handling code here:
        int baris = tblServis.getSelectedRow();
        
        if (baris != -1) {
            // 1. Ambil ID Servis dari baris yang diklik sebagai kunci pencarian
            String id_servis = tblServis.getValueAt(baris, 0).toString();

            try {
                // 2. Query lengkap untuk mengambil data dari tabel servis dan pelanggan
                String sql = "SELECT s.*, p.* FROM servis s " +
                             "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                             "WHERE s.id_servis = '" + id_servis + "'";

                java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
                java.sql.ResultSet res = conn.createStatement().executeQuery(sql);

                if (res.next()) {
                    idPelanggan = res.getInt("id_pelanggan");
                    // --- BAGIAN INPUT DATA SERVIS ---
                    tNomorServ.setText(res.getString("id_servis"));
                    // Mengatur ComboBox Status & Jenis
                    cbStatusServ.setSelectedItem(res.getString("status"));
                    cbJenisBrg.setSelectedItem(res.getString("jenis_barang"));

                    tMerek.setText(res.getString("merek"));
                    tModel.setText(res.getString("model"));
                    tSeri.setText(res.getString("no_seri"));
                    tKeluhan.setText(res.getString("keluhan_awal"));
                    tKelengkapan.setText(res.getString("kelengkapan"));

                    // --- BAGIAN DATA PELANGGAN ---
                    tNamaPelanggan.setText(res.getString("nama_pelanggan"));
                    tNoPelanggan.setText(res.getString("no_hp"));
                    tAlamatPelanggan.setText(res.getString("alamat"));

                    // --- PENGATURAN RADIO BUTTON ---
                    // Set ke "Pelanggan Lama" secara otomatis
                    rbLama.setSelected(true);

                    // Kunci ID Servis agar tidak bisa diubah saat mode edit
                    tNomorServ.setEditable(false);

                    System.out.println("Data berhasil dimuat dari database.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal mengambil data: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel lebih dulu!");
        }
    }//GEN-LAST:event_btEditActionPerformed

    private void tKelengkapanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tKelengkapanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tKelengkapanActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBatal;
    private javax.swing.JButton btEdit;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btnCari;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbJenisBrg;
    private javax.swing.JComboBox<String> cbStatusServ;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton rbBaru;
    private javax.swing.JRadioButton rbLama;
    private javax.swing.JTextField tAlamatPelanggan;
    private javax.swing.JTextField tKelengkapan;
    private javax.swing.JTextArea tKeluhan;
    private javax.swing.JTextField tMerek;
    private javax.swing.JTextField tModel;
    private javax.swing.JTextField tNamaAdmin;
    private javax.swing.JTextField tNamaPelanggan;
    private javax.swing.JTextField tNoPelanggan;
    private javax.swing.JTextField tNomorServ;
    private javax.swing.JTextField tSeri;
    private javax.swing.JLabel tTgl;
    private javax.swing.JTable tblServis;
    private javax.swing.JTextField tfCari;
    // End of variables declaration//GEN-END:variables
}
