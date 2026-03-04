/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template

 */
package mscompapp;

import config.Koneksi;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Acer Aspire Lite 15
 */
public class PKelService extends javax.swing.JPanel {   
    
    public int idPelanggan = 0; 
    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    // Inner class untuk menampung data teknisi di ComboBox
    private class UserItem {
        String id, nama;
        public UserItem(String id, String nama) { this.id = id; this.nama = nama; }
        @Override public String toString() { return nama; }
    }

    public PKelService() {
        initComponents();
        auto_number_service();
        tampilkanAdmin();
        tampilTanggal();
        load_jenis_perangkat();
        load_status();       
        tampilKategori();  
        initKeyShortcuts();

        tblServis = new javax.swing.JTable() {
            {
                setRowHeight(30);
                getTableHeader().setReorderingAllowed(false);
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
                            case "Proses": comp.setBackground(java.awt.Color.YELLOW); break;
                            case "Selesai": comp.setBackground(new java.awt.Color(144, 238, 144)); break;
                            case "Dibatalkan": comp.setBackground(new java.awt.Color(255, 182, 193)); break;
                            default: comp.setBackground(java.awt.Color.WHITE); break;
                        }
                        comp.setForeground(java.awt.Color.BLACK);
                    }
                }
                return comp;
            }
        };
        jScrollPane2.setViewportView(tblServis);
        load_table_service();

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
    
    private String pilihTeknisiPopUp() {
        Vector<UserItem> model = new Vector<>();
        try {
            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_user, nama FROM tbl_user WHERE role='teknisi'");
            while(rs.next()) model.add(new UserItem(rs.getString("id_user"), rs.getString("nama")));
        } catch (Exception e) { e.printStackTrace(); }

        if(model.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data teknisi tidak ditemukan!");
            return null;
        }

        JComboBox<UserItem> cb = new JComboBox<>(model);
        int res = JOptionPane.showConfirmDialog(this, cb, "Pilih Teknisi Penanggung Jawab", JOptionPane.OK_CANCEL_OPTION);
        return (res == JOptionPane.OK_OPTION) ? ((UserItem) cb.getSelectedItem()).id : null;
    }
    
    private void tampilKategori() {
        try {
            cbJenisBrg.removeAllItems();
            cbJenisBrg.addItem("- Pilih Jenis -");
            Connection conn = Koneksi.configDB();
            ResultSet res = conn.createStatement().executeQuery("SELECT nama_jenis FROM tbl_jenis_perangkat");
            while (res.next()) cbJenisBrg.addItem(res.getString("nama_jenis"));
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }
    
    private void tampilkanAdmin() {
        tNamaAdmin.setText(Login.namaUser != null ? Login.namaUser : "Admin");
        tNamaAdmin.setEditable(false);
    }
    
    private void load_jenis_perangkat() {
        cbJenisBrg.removeAllItems();
        cbJenisBrg.addItem("- Pilih Jenis -");
        try {
            ResultSet res = Koneksi.configDB().createStatement().executeQuery("SELECT nama_jenis FROM tbl_jenis_perangkat");
            while(res.next()) cbJenisBrg.addItem(res.getString("nama_jenis"));
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
    
    private void aturLebarKolom() {
    // Pastikan ini dijalankan di akhir proses agar tidak Error
    javax.swing.SwingUtilities.invokeLater(() -> {
        if (tblServis.getColumnCount() > 0) {
            tblServis.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

            // Angka lebar yang sudah kita sepakati agar tidak terpotong
            int[] lebarKunci = {120, 200, 170, 180, 150, 150, 300, 150};

            for (int i = 0; i < tblServis.getColumnCount(); i++) {
                if (i < lebarKunci.length) {
                    javax.swing.table.TableColumn col = tblServis.getColumnModel().getColumn(i);
                    col.setPreferredWidth(lebarKunci[i]);
                    col.setMinWidth(lebarKunci[i]);
                }
            }

            // Header rata tengah
            javax.swing.table.DefaultTableCellRenderer headerRenderer = 
                (javax.swing.table.DefaultTableCellRenderer) tblServis.getTableHeader().getDefaultRenderer();
            headerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        }
    });
}

    private void load_table_service() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addColumn("No Servis"); model.addColumn("Nama Pelanggan");
        model.addColumn("No HP"); model.addColumn("Jenis Barang");
        model.addColumn("Merek"); model.addColumn("Model");
        model.addColumn("Keluhan"); model.addColumn("Status");

        try {
            String whereClause = "";
            String keyword = tfCari.getText();
            if (!keyword.isEmpty()) {
                whereClause = " WHERE p.nama_pelanggan LIKE ? OR s.id_servis LIKE ? ";
            }

            Connection conn = Koneksi.configDB();
            
            // 1. Hitung Total Data
            String sqlCount = "SELECT COUNT(*) AS total FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " + whereClause;
            PreparedStatement pstCount = conn.prepareStatement(sqlCount);
            if (!keyword.isEmpty()) {
                pstCount.setString(1, "%" + keyword + "%"); pstCount.setString(2, "%" + keyword + "%");
            }
            ResultSet rsCount = pstCount.executeQuery();
            int totalData = rsCount.next() ? rsCount.getInt("total") : 0;

            // 2. Query Data dengan LIMIT & OFFSET
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT s.id_servis, p.nama_pelanggan, p.no_hp, s.jenis_barang, s.merek, s.model, s.keluhan_awal, s.status " +
                         "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         whereClause + " ORDER BY s.tanggal_masuk DESC LIMIT ? OFFSET ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            if (!keyword.isEmpty()) {
                pst.setString(1, "%" + keyword + "%");
                pst.setString(2, "%" + keyword + "%");
                pst.setInt(3, PAGE_SIZE);
                pst.setInt(4, offset);
            } else {
                pst.setInt(1, PAGE_SIZE);
                pst.setInt(2, offset);
            }

            ResultSet res = pst.executeQuery();
            while(res.next()){
                model.addRow(new Object[]{
                    res.getString("id_servis"), res.getString("nama_pelanggan"),
                    res.getString("no_hp"), res.getString("jenis_barang"),
                    res.getString("merek"), res.getString("model"),
                    res.getString("keluhan_awal"), res.getString("status")
                });
            }
            tblServis.setModel(model);

            // 3. Update Status Tombol Pagination
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

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
    buttonGroup1 = new javax.swing.ButtonGroup();
    
    // Inisialisasi Objek Komponen
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    tNomorServ = new javax.swing.JTextField();
    tNamaAdmin = new javax.swing.JTextField();
    tTgl = new javax.swing.JLabel();
    cbStatusServ = new javax.swing.JComboBox<>();
    jPanel3 = new javax.swing.JPanel();
    rbBaru = new javax.swing.JRadioButton();
    rbLama = new javax.swing.JRadioButton();
    tNamaPelanggan = new javax.swing.JTextField();
    tNoPelanggan = new javax.swing.JTextField();
    tAlamatPelanggan = new javax.swing.JTextField();
    jLabel12 = new javax.swing.JLabel();
    btnCari = new javax.swing.JButton();
    jPanel4 = new javax.swing.JPanel();
    jLabel5 = new javax.swing.JLabel();
    cbJenisBrg = new javax.swing.JComboBox<>();
    tMerek = new javax.swing.JTextField();
    tModel = new javax.swing.JTextField();
    tSeri = new javax.swing.JTextField();
    tKelengkapan = new javax.swing.JTextField();
    tKeluhan = new javax.swing.JTextArea();
    jScrollPane1 = new javax.swing.JScrollPane();
    btSimpan = new javax.swing.JButton();
    btBatal = new javax.swing.JButton();
    jPanel7 = new javax.swing.JPanel();
    jLabel19 = new javax.swing.JLabel();
    tblServis = new javax.swing.JTable();
    jScrollPane2 = new javax.swing.JScrollPane();
    tfCari = new javax.swing.JTextField();
    btRefresh = new javax.swing.JButton();
    btEdit = new javax.swing.JButton();
    btnNextKiri = new javax.swing.JButton();
    btnNextKanan = new javax.swing.JButton();

    // TAMPILAN DASAR (Latar Belakang Abu-abu Terang agar Konten Putih Terlihat Jelas)
    setLayout(new java.awt.BorderLayout(10, 0));
    setBackground(new java.awt.Color(240, 240, 240));
    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // =========================================================================
    // AREA FORM (KIRI) - FIXED WIDTH 620
    // =========================================================================
    javax.swing.JPanel pnlKiriBase = new javax.swing.JPanel(new java.awt.GridBagLayout());
    pnlKiriBase.setPreferredSize(new java.awt.Dimension(620, 0));
    pnlKiriBase.setOpaque(false);
    java.awt.GridBagConstraints mainGbc = new java.awt.GridBagConstraints();
    mainGbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    mainGbc.anchor = java.awt.GridBagConstraints.NORTH;
    mainGbc.weightx = 1.0;

    // --- 1. PANEL INFO SERVIS (ATAS) ---
    jPanel1.setBackground(java.awt.Color.WHITE);
    jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
    jPanel1.setLayout(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints g1 = new java.awt.GridBagConstraints();
    
    jLabel1.setBackground(new java.awt.Color(3, 83, 164));
    jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel1.setForeground(java.awt.Color.WHITE);
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("INPUT DATA SERVIS");
    jLabel1.setOpaque(true);
    g1.gridx = 0; g1.gridy = 0; g1.gridwidth = 4; g1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    g1.anchor = java.awt.GridBagConstraints.PAGE_START; // Rapat Atas
    g1.ipady = 20; 
    g1.insets = new java.awt.Insets(0, 0, 10, 0); // Atas 0 = Putih Hilang
    jPanel1.add(jLabel1, g1);

    g1.ipady = 0; g1.gridwidth = 1; g1.gridy = 1; g1.weightx = 0; 
    g1.insets = new java.awt.Insets(5, 10, 5, 5);
    g1.gridx = 0; jPanel1.add(new javax.swing.JLabel("No Service:"), g1);
    g1.gridx = 1; g1.weightx = 0.5; jPanel1.add(tNomorServ, g1);
    g1.gridx = 2; g1.weightx = 0; jPanel1.add(new javax.swing.JLabel("Tanggal:"), g1);
    g1.gridx = 3; g1.weightx = 0.5; g1.insets = new java.awt.Insets(5, 5, 5, 10); jPanel1.add(tTgl, g1);

    g1.gridy = 2; g1.gridx = 0; g1.insets = new java.awt.Insets(5, 10, 15, 5);
    jPanel1.add(new javax.swing.JLabel("Admin:"), g1);
    g1.gridx = 1; jPanel1.add(tNamaAdmin, g1);
    g1.gridx = 2; jPanel1.add(new javax.swing.JLabel("Status:"), g1);
    g1.gridx = 3; g1.insets = new java.awt.Insets(5, 5, 15, 10); jPanel1.add(cbStatusServ, g1);

    mainGbc.gridy = 0; pnlKiriBase.add(jPanel1, mainGbc);

    // --- 2. PANEL TENGAH (DATA PELANGGAN & PERANGKAT) ---
    javax.swing.JPanel pnlSplit = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 10, 0));
    pnlSplit.setOpaque(false);

    // PANEL PELANGGAN
    jPanel3.setBackground(java.awt.Color.WHITE);
    jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
    jPanel3.setLayout(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints g3 = new java.awt.GridBagConstraints();

    jLabel12.setBackground(new java.awt.Color(3, 83, 164));
    jLabel12.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel12.setForeground(java.awt.Color.WHITE);
    jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel12.setText("DATA PELANGGAN");
    jLabel12.setOpaque(true);
    g3.gridx = 0; g3.gridy = 0; g3.gridwidth = 2; g3.fill = java.awt.GridBagConstraints.HORIZONTAL;
    g3.anchor = java.awt.GridBagConstraints.FIRST_LINE_START; // Kunci ke pojok kiri atas
    g3.ipady = 20; 
    g3.insets = new java.awt.Insets(0, 0, 10, 0); 
    jPanel3.add(jLabel12, g3);

    rbBaru.setText("Pelanggan Baru"); rbBaru.setOpaque(false);
    rbLama.setText("Pelanggan Lama"); rbLama.setOpaque(false);
    buttonGroup1.add(rbBaru); buttonGroup1.add(rbLama);
    g3.ipady = 0; g3.gridwidth = 2; g3.anchor = java.awt.GridBagConstraints.WEST;
    g3.gridy = 1; g3.insets = new java.awt.Insets(0, 10, 2, 10); jPanel3.add(rbBaru, g3);
    g3.gridy = 2; g3.insets = new java.awt.Insets(0, 10, 8, 10); jPanel3.add(rbLama, g3);

    btnCari.setText("Cari Pelanggan");
    g3.gridy = 3; g3.fill = java.awt.GridBagConstraints.HORIZONTAL; g3.insets = new java.awt.Insets(0, 10, 12, 10);
    jPanel3.add(btnCari, g3);

   g3.gridy = 4; g3.gridx = 0; g3.gridwidth = 1; 
    g3.weightx = 0; // Label tidak perlu melebar
    g3.fill = java.awt.GridBagConstraints.NONE;
    g3.anchor = java.awt.GridBagConstraints.WEST;
    g3.insets = new java.awt.Insets(0, 10, 5, 5);
    jPanel3.add(new javax.swing.JLabel("Nama:"), g3);

    g3.gridx = 1; 
    g3.weightx = 1.0; // KUNCI: Membuat text field menghabiskan sisa ruang ke kanan
    g3.fill = java.awt.GridBagConstraints.HORIZONTAL; // KUNCI: Memaksa text field memanjang
    g3.insets = new java.awt.Insets(0, 0, 5, 10);
    jPanel3.add(tNamaPelanggan, g3);

    // Pengaturan No HP
    g3.gridy = 5; g3.gridx = 0; g3.weightx = 0; g3.fill = java.awt.GridBagConstraints.NONE;
    g3.insets = new java.awt.Insets(0, 10, 5, 5);
    jPanel3.add(new javax.swing.JLabel("No HP:"), g3);

    g3.gridx = 1; g3.weightx = 1.0; g3.fill = java.awt.GridBagConstraints.HORIZONTAL;
    g3.insets = new java.awt.Insets(0, 0, 5, 10);
    jPanel3.add(tNoPelanggan, g3);
    
    // Pengaturan Alamat
    g3.gridy = 6; g3.gridx = 0; g3.weightx = 0; g3.fill = java.awt.GridBagConstraints.NONE;
    g3.anchor = java.awt.GridBagConstraints.NORTHWEST; // Label alamat tetap di atas jika field tinggi
    g3.insets = new java.awt.Insets(0, 10, 15, 5);
    jPanel3.add(new javax.swing.JLabel("Alamat:"), g3);

    g3.gridx = 1; g3.weightx = 1.0; g3.fill = java.awt.GridBagConstraints.HORIZONTAL;
    g3.weighty = 1.0; // Memberikan berat vertikal agar semua naik ke atas
    g3.insets = new java.awt.Insets(0, 0, 15, 10);
    jPanel3.add(tAlamatPelanggan, g3);
    
    pnlSplit.add(jPanel3);

    jPanel4.setBackground(java.awt.Color.WHITE);
    jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
    jPanel4.setLayout(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints g4 = new java.awt.GridBagConstraints();

    jLabel5.setBackground(new java.awt.Color(3, 83, 164));
    jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12));
    jLabel5.setForeground(java.awt.Color.WHITE);
    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel5.setText("DATA PERANGKAT");
    jLabel5.setOpaque(true);
    g4.gridx = 0; g4.gridy = 0; g4.gridwidth = 2; g4.fill = java.awt.GridBagConstraints.HORIZONTAL;
    g4.anchor = java.awt.GridBagConstraints.FIRST_LINE_START; 
    g4.ipady = 20; 
    g4.weightx = 1.0;
    g4.insets = new java.awt.Insets(0, 0, 10, 0); 
    jPanel4.add(jLabel5, g4);

    g4.ipady = 0; g4.gridwidth = 1; 
    g4.gridy = 1; g4.gridx = 0; g4.weightx = 0; g4.insets = new java.awt.Insets(5, 10, 5, 5);
    jPanel4.add(new javax.swing.JLabel("Jenis:"), g4);
    g4.gridx = 1; g4.weightx = 1.0; g4.insets = new java.awt.Insets(5, 0, 5, 10);
    jPanel4.add(cbJenisBrg, g4);
    g4.gridy = 2; g4.gridx = 0; g4.insets = new java.awt.Insets(0, 10, 5, 5);
    jPanel4.add(new javax.swing.JLabel("Merek:"), g4);
    g4.gridx = 1; g4.insets = new java.awt.Insets(0, 0, 5, 10);
    jPanel4.add(tMerek, g4);
    g4.gridy = 3; g4.gridx = 0; g4.insets = new java.awt.Insets(0, 10, 5, 5);
    jPanel4.add(new javax.swing.JLabel("Model:"), g4);
    g4.gridx = 1; g4.insets = new java.awt.Insets(0, 0, 5, 10);
    jPanel4.add(tModel, g4);
    g4.gridy = 4; g4.gridx = 0; g4.insets = new java.awt.Insets(0, 10, 5, 5);
    jPanel4.add(new javax.swing.JLabel("Seri:"), g4);
    g4.gridx = 1; g4.insets = new java.awt.Insets(0, 0, 5, 10);
    jPanel4.add(tSeri, g4);

    // Baris terakhir Panel Perangkat diberikan weighty = 1.0 agar semuanya naik ke atas
    g4.gridy = 5; g4.gridx = 0; g4.insets = new java.awt.Insets(0, 10, 15, 5);
    g4.anchor = java.awt.GridBagConstraints.NORTH;
    g4.weighty = 1.0;
    jPanel4.add(new javax.swing.JLabel("Kelengkapan:"), g4);
    g4.gridx = 1; g4.insets = new java.awt.Insets(0, 0, 15, 10);
    jPanel4.add(tKelengkapan, g4);

    pnlSplit.add(jPanel4);

    mainGbc.gridy = 1; mainGbc.insets = new java.awt.Insets(10, 0, 10, 0);
    pnlKiriBase.add(pnlSplit, mainGbc);

    // --- 3. PANEL KELUHAN & TOMBOL (BAWAH) ---
    javax.swing.JPanel pnlAction = new javax.swing.JPanel(new java.awt.GridBagLayout());
    pnlAction.setOpaque(false);
    java.awt.GridBagConstraints gb = new java.awt.GridBagConstraints();

    tKeluhan.setRows(4); tKeluhan.setLineWrap(true);
    jScrollPane1.setViewportView(tKeluhan);
    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK), "KELUHAN"));
    gb.gridx = 0; gb.gridy = 0; gb.weightx = 1.0; gb.weighty = 1.0; gb.fill = java.awt.GridBagConstraints.BOTH;
    pnlAction.add(jScrollPane1, gb);

    javax.swing.JPanel pnlBtns = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 8));
    pnlBtns.setOpaque(false);
    pnlBtns.setPreferredSize(new java.awt.Dimension(140, 100));
    btSimpan.setBackground(new java.awt.Color(0, 153, 51));
    btSimpan.setForeground(java.awt.Color.WHITE);
    btSimpan.setText("SIMPAN");
    btBatal.setBackground(new java.awt.Color(204, 0, 0));
    btBatal.setForeground(java.awt.Color.WHITE);
    btBatal.setText("BATAL");
    pnlBtns.add(btSimpan); pnlBtns.add(btBatal);

    gb.gridx = 1; gb.weightx = 0; gb.fill = java.awt.GridBagConstraints.VERTICAL;
    gb.insets = new java.awt.Insets(5, 10, 0, 0);
    pnlAction.add(pnlBtns, gb);

    mainGbc.gridy = 2; mainGbc.weighty = 0; mainGbc.insets = new java.awt.Insets(0, 0, 0, 0);
    pnlKiriBase.add(pnlAction, mainGbc);

    mainGbc.gridy = 3; mainGbc.weighty = 1.0;
    pnlKiriBase.add(javax.swing.Box.createVerticalGlue(), mainGbc);

    add(pnlKiriBase, java.awt.BorderLayout.WEST);

    // =========================================================================
    // AREA TABEL (KANAN)
    // =========================================================================
    jPanel7.setBackground(java.awt.Color.WHITE);
    jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
    jPanel7.setLayout(new java.awt.BorderLayout());

    // JUDUL DAFTAR
    jLabel19.setBackground(new java.awt.Color(3, 83, 164));
    jLabel19.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 14));
    jLabel19.setForeground(java.awt.Color.WHITE);
    jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel19.setText("DAFTAR ANTRIAN SERVIS");
    jLabel19.setOpaque(true);
    jLabel19.setPreferredSize(new java.awt.Dimension(0, 40));
    jPanel7.add(jLabel19, java.awt.BorderLayout.NORTH);

    javax.swing.JPanel pnlTableBase = new javax.swing.JPanel(new java.awt.BorderLayout());
    pnlTableBase.setOpaque(false);
    
    // --- TOOLBAR ATAS (Cari, Refresh, Edit) ---
    // Menggunakan hgap 10 dan vgap 10 agar ada jarak antar komponen
    javax.swing.JPanel pnlToolbar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 10));
    pnlToolbar.setOpaque(false);
    
    pnlToolbar.add(new javax.swing.JLabel("Cari:"));
    tfCari.setPreferredSize(new java.awt.Dimension(200, 25));
    pnlToolbar.add(tfCari);

    // Memberikan teks dan ukuran agar tombol terlihat jelas
    btRefresh.setText("Refresh [F3]");
    btRefresh.setPreferredSize(new java.awt.Dimension(130, 28));
    pnlToolbar.add(btRefresh);

    btEdit.setText("Edit [F1]");
    btEdit.setPreferredSize(new java.awt.Dimension(90, 28));
    pnlToolbar.add(btEdit);
    
    pnlTableBase.add(pnlToolbar, java.awt.BorderLayout.NORTH);

    // --- AREA TABEL ---
    tblServis.setRowHeight(30);
    jScrollPane2.setViewportView(tblServis);
    
    // Bungkus dengan invokeLater agar tidak Error ArrayIndexOutOfBounds
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            // Cek dulu apakah kolom sudah ada
            if (tblServis.getColumnCount() > 0) {
                tblServis.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

                // Angka lebar yang sangat aman (Anti-Terpotong)
                int[] lebarKunci = {120, 200, 170, 180, 150, 150, 300, 150};

                for (int i = 0; i < tblServis.getColumnCount(); i++) {
                    if (i < lebarKunci.length) {
                        javax.swing.table.TableColumn col = tblServis.getColumnModel().getColumn(i);
                        col.setPreferredWidth(lebarKunci[i]);
                        col.setMinWidth(lebarKunci[i]);
                    }
                }

                // Set Header rata tengah
                javax.swing.table.DefaultTableCellRenderer headerRenderer = 
                    (javax.swing.table.DefaultTableCellRenderer) tblServis.getTableHeader().getDefaultRenderer();
                headerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            }
        }
    });

    jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    pnlTableBase.add(jScrollPane2, java.awt.BorderLayout.CENTER);
    

    

    // --- NAVIGASI BAWAH (PREV & NEXT) ---
    // FlowLayout RIGHT membuat tombol menempel di pojok kanan bawah
    javax.swing.JPanel pnlNav = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 10));
    pnlNav.setOpaque(false);

    // Set teks untuk tombol navigasi (Gunakan simbol agar lebih keren)
    btnNextKiri.setText("❮ PREV");
    btnNextKiri.setPreferredSize(new java.awt.Dimension(100, 35));
    
    btnNextKanan.setText("NEXT ❯");
    btnNextKanan.setPreferredSize(new java.awt.Dimension(100, 35));

    pnlNav.add(btnNextKiri); 
    pnlNav.add(btnNextKanan);
    
    pnlTableBase.add(pnlNav, java.awt.BorderLayout.SOUTH);

    // Masukkan basis tabel ke panel utama
    jPanel7.add(pnlTableBase, java.awt.BorderLayout.CENTER);
    add(jPanel7, java.awt.BorderLayout.CENTER);

    // Tetap panggil initEventHandlers agar fungsi klik tombol tidak hilang
    initEventHandlers();
}

// FUNGSI INI WAJIB ADA AGAR TIDAK ERROR
private void initEventHandlers() {
    tNomorServ.addActionListener(this::tNomorServActionPerformed);
    tNamaAdmin.addActionListener(this::tNamaAdminActionPerformed);
    cbStatusServ.addActionListener(this::cbStatusServActionPerformed);
    rbBaru.addActionListener(this::rbBaruActionPerformed);
    rbLama.addActionListener(this::rbLamaActionPerformed);
    btnCari.addActionListener(this::btnCariActionPerformed);
    btSimpan.addActionListener(this::btSimpanActionPerformed);
    btBatal.addActionListener(this::btBatalActionPerformed);
    btRefresh.addActionListener(this::btRefreshActionPerformed);
    btEdit.addActionListener(this::btEditActionPerformed);
    btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
    btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
    tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyReleased(java.awt.event.KeyEvent evt) { tfCariKeyReleased(evt); }
    });
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
            if (tNamaPelanggan.getText().isEmpty() || cbJenisBrg.getSelectedIndex() == 0 || tKeluhan.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Data Pelanggan dan Keluhan tidak boleh kosong!"); 
                return;
            }

            String idTeknisiTerpilih = pilihTeknisiPopUp();
            if (idTeknisiTerpilih == null) return;

            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false);

            String idServisSaatIni = tNomorServ.getText();
            int finalIdPelanggan = idPelanggan;

            // 1. Data Pelanggan
            if (rbBaru.isSelected()) {
                PreparedStatement pstPel = conn.prepareStatement("INSERT INTO tbl_pelanggan (nama_pelanggan, no_hp, alamat) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                pstPel.setString(1, tNamaPelanggan.getText());
                pstPel.setString(2, tNoPelanggan.getText());
                pstPel.setString(3, tAlamatPelanggan.getText());
                pstPel.executeUpdate();
                ResultSet rsId = pstPel.getGeneratedKeys();
                if (rsId.next()) finalIdPelanggan = rsId.getInt(1);
            }

            // 2. Data Servis
            String sqlService = "INSERT INTO servis (id_pelanggan, jenis_barang, merek, model, no_seri, kelengkapan, keluhan_awal, status, tanggal_masuk, id_admin, id_servis) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE id_pelanggan=VALUES(id_pelanggan), keluhan_awal=VALUES(keluhan_awal)";
            
            PreparedStatement pstServ = conn.prepareStatement(sqlService);
            pstServ.setInt(1, finalIdPelanggan);
            pstServ.setString(2, cbJenisBrg.getSelectedItem().toString());
            pstServ.setString(3, tMerek.getText());
            pstServ.setString(4, tModel.getText());
            pstServ.setString(5, tSeri.getText());
            pstServ.setString(6, tKelengkapan.getText());
            pstServ.setString(7, tKeluhan.getText());
            pstServ.setString(8, cbStatusServ.getSelectedItem().toString());
            pstServ.setString(9, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            pstServ.setString(10, Session.idUser);
            pstServ.setString(11, idServisSaatIni);
            pstServ.executeUpdate();

            // 3. Data Perbaikan
            PreparedStatement pstFix = conn.prepareStatement("INSERT IGNORE INTO perbaikan (id_servis, id_teknisi, biaya_jasa, diskon) VALUES (?, ?, 0, 0)");
            pstFix.setString(1, idServisSaatIni);
            pstFix.setString(2, idTeknisiTerpilih);
            pstFix.executeUpdate();

            conn.commit();
            
            // Panggil Dialog Pilihan Cetak (Printer/PDF)
            CetakTandaTerima.cetakTandaTerima(idServisSaatIni);

            load_table_service();
            btBatalActionPerformed(null);

        } catch (Exception e) {
            try { Koneksi.configDB().rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this, "Simpan Gagal: " + e.getMessage());
        }
        aturLebarKolom();
    }//GEN-LAST:event_btSimpanActionPerformed

    private void tMerekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tMerekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tMerekActionPerformed

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        PopUpPelanggan popup = new PopUpPelanggan();
        popup.serviceForm = this; // Sambungkan pop-up dengan form ini
        popup.setVisible(true);
        aturLebarKolom();
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
    aturLebarKolom();
    }//GEN-LAST:event_btBatalActionPerformed

    private void cbJenisBrgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbJenisBrgActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_cbJenisBrgActionPerformed

    private void tfCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCariKeyReleased
        // TODO add your handling code here:
        load_table_service();
        currentPage = 0;
        aturLebarKolom();
    }//GEN-LAST:event_tfCariKeyReleased

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        load_table_service();
        currentPage = 0;
        aturLebarKolom();
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
        aturLebarKolom();
    }//GEN-LAST:event_btEditActionPerformed

    private void tKelengkapanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tKelengkapanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tKelengkapanActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        load_table_service();
                aturLebarKolom();

    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            load_table_service();
        }
                aturLebarKolom();

    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void cbStatusServActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStatusServActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbStatusServActionPerformed

    private void tAlamatPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tAlamatPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tAlamatPelangganActionPerformed

    private void tNamaAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tNamaAdminActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tNamaAdminActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBatal;
    private javax.swing.JButton btEdit;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
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
