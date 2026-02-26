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
import java.util.Vector;
import javax.swing.JComboBox;
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
        String id;
        String nama;

        public UserItem(String id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        @Override
        public String toString() {
            return nama;
        }
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
        Vector<UserItem> listTeknisi = new Vector<>();
        try {
            Connection conn = Koneksi.configDB();
            // Mengambil user dengan role teknisi (atau admin juga jika bisa memperbaiki)
            String sql = "SELECT id_user, nama FROM tbl_user WHERE role = 'teknisi' OR role = 'admin' ORDER BY nama ASC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) {
                listTeknisi.add(new UserItem(rs.getString("id_user"), rs.getString("nama")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (listTeknisi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data teknisi di database!");
            return null;
        }

        JComboBox<UserItem> cbTeknisi = new JComboBox<>(listTeknisi);
        int result = JOptionPane.showConfirmDialog(this, cbTeknisi, 
                "Pilih Teknisi Penanggung Jawab", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return ((UserItem) cbTeknisi.getSelectedItem()).id;
        }
        return null;
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
        java.awt.GridBagConstraints gridBagConstraints;

        jSeparator1 = new javax.swing.JSeparator();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
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
        jLabel12 = new javax.swing.JLabel();
        btBatal = new javax.swing.JButton();
        btSimpan = new javax.swing.JButton();
        btnCari = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
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
        jLabel19 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblServis = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        tfCari = new javax.swing.JTextField();
        btRefresh = new javax.swing.JButton();
        btEdit = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1720, 960));
        setMinimumSize(new java.awt.Dimension(1160, 640));
        setPreferredSize(new java.awt.Dimension(1160, 640));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setBackground(new java.awt.Color(3, 83, 164));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("INPUT DATA SERVIS");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 438;
        gridBagConstraints.ipady = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(-5, 0, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel2.setText("Nomor Service :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 10);
        jPanel1.add(jLabel2, gridBagConstraints);

        tNomorServ.addActionListener(this::tNomorServActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 136;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 15);
        jPanel1.add(tNomorServ, gridBagConstraints);

        tNamaAdmin.addActionListener(this::tNamaAdminActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 136;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(tNamaAdmin, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel3.setText("Admin :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 48;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 10);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel6.setText("Tanggal :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 10);
        jPanel1.add(jLabel6, gridBagConstraints);

        tTgl.setFont(new java.awt.Font("Segoe UI Historic", 1, 12)); // NOI18N
        tTgl.setText("000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 101;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 15);
        jPanel1.add(tTgl, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel7.setText("Status [F7] :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 10);
        jPanel1.add(jLabel7, gridBagConstraints);

        cbStatusServ.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Proses", "Menunggu", "Selesai", "Dibatalkan" }));
        cbStatusServ.addActionListener(this::cbStatusServActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 27;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 15);
        jPanel1.add(cbStatusServ, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jPanel1, gridBagConstraints);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setMinimumSize(new java.awt.Dimension(572, 457));
        jPanel3.setPreferredSize(new java.awt.Dimension(472, 457));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(rbBaru);
        rbBaru.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        rbBaru.setText("Pelanggan Baru");
        rbBaru.addActionListener(this::rbBaruActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        jPanel3.add(rbBaru, gridBagConstraints);

        buttonGroup1.add(rbLama);
        rbLama.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        rbLama.setText("Pelangggan Lama");
        rbLama.addActionListener(this::rbLamaActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 77;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        jPanel3.add(rbLama, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel4.setText("Cari No Hp / Nama :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 89;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel3.add(jLabel4, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel9.setText("Nama :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel3.add(jLabel9, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel13.setText("No Hp :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel3.add(jLabel13, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel14.setText("Alamat :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel3.add(jLabel14, gridBagConstraints);

        tNamaPelanggan.addActionListener(this::tNamaPelangganActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, -450, 0, 0);
        jPanel3.add(tNamaPelanggan, gridBagConstraints);

        tNoPelanggan.addActionListener(this::tNoPelangganActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, -450, 0, 0);
        jPanel3.add(tNoPelanggan, gridBagConstraints);

        tAlamatPelanggan.addActionListener(this::tAlamatPelangganActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, -450, 0, 0);
        jPanel3.add(tAlamatPelanggan, gridBagConstraints);

        jLabel12.setBackground(new java.awt.Color(3, 83, 164));
        jLabel12.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("DATA PELANGGAN");
        jLabel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel12.setMaximumSize(new java.awt.Dimension(122, 19));
        jLabel12.setMinimumSize(new java.awt.Dimension(122, 19));
        jLabel12.setOpaque(true);
        jLabel12.setPreferredSize(new java.awt.Dimension(122, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 438;
        gridBagConstraints.ipady = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jLabel12, gridBagConstraints);

        btBatal.setBackground(new java.awt.Color(255, 51, 51));
        btBatal.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btBatal.setText("BATAL [DEL]");
        btBatal.addActionListener(this::btBatalActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 85;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 60, 0);
        jPanel3.add(btBatal, gridBagConstraints);

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btSimpan.setText("SIMPAN [SHIFT+ENTER]");
        btSimpan.addActionListener(this::btSimpanActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 14;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 0, 0);
        jPanel3.add(btSimpan, gridBagConstraints);

        btnCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        btnCari.setText("Cari... [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 30, 0, 0);
        jPanel3.add(btnCari, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = -10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(jPanel3, gridBagConstraints);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel5.setBackground(new java.awt.Color(3, 83, 164));
        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("DATA PERANGKAT/SERVIS");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel5.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 130;
        gridBagConstraints.ipady = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel4.add(jLabel5, gridBagConstraints);

        cbJenisBrg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbJenisBrg.addActionListener(this::cbJenisBrgActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 68;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel4.add(cbJenisBrg, gridBagConstraints);

        tMerek.addActionListener(this::tMerekActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 76;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel4.add(tMerek, gridBagConstraints);

        tModel.addActionListener(this::tModelActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 76;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel4.add(tModel, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel8.setText("Merek :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 49;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel4.add(jLabel8, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel10.setText("Kelengkapan :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel4.add(jLabel10, gridBagConstraints);

        jLabel15.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel15.setText("Jenis Barang [F8] :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        jPanel4.add(jLabel15, gridBagConstraints);

        jLabel16.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel16.setText("Model / Tipe :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel4.add(jLabel16, gridBagConstraints);

        jLabel17.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel17.setText("No Seri :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 42;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel4.add(jLabel17, gridBagConstraints);

        tSeri.addActionListener(this::tSeriActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 76;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel4.add(tSeri, gridBagConstraints);

        jLabel18.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 12)); // NOI18N
        jLabel18.setText("Keluhan :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 37;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 0, 0);
        jPanel4.add(jLabel18, gridBagConstraints);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        tKeluhan.setColumns(20);
        tKeluhan.setRows(5);
        tKeluhan.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane1.setViewportView(tKeluhan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 134;
        gridBagConstraints.ipady = 34;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel4.add(jScrollPane1, gridBagConstraints);

        tKelengkapan.addActionListener(this::tKelengkapanActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 76;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel4.add(tKelengkapan, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel4.add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(jPanel4, gridBagConstraints);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel7.setPreferredSize(new java.awt.Dimension(815, 960));
        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel19.setBackground(new java.awt.Color(3, 83, 164));
        jLabel19.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("DAFTAR SERVIS");
        jLabel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel19.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 482;
        gridBagConstraints.ipady = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel7.add(jLabel19, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 544;
        gridBagConstraints.ipady = 380;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        jPanel7.add(jScrollPane2, gridBagConstraints);

        jLabel20.setText("Cari [F6] :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        jPanel7.add(jLabel20, gridBagConstraints);

        tfCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfCariKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 96;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        jPanel7.add(tfCari, gridBagConstraints);

        btRefresh.setText("Refresh [F3]");
        btRefresh.addActionListener(this::btRefreshActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 0, 0);
        jPanel7.add(btRefresh, gridBagConstraints);

        btEdit.setText("Edit [F1]");
        btEdit.addActionListener(this::btEditActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 47;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        jPanel7.add(btEdit, gridBagConstraints);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 12;
        gridBagConstraints.ipadx = 810;
        gridBagConstraints.ipady = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(258, 10, 0, 0);
        jPanel7.add(jPanel9, gridBagConstraints);

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 90, 0, 0);
        jPanel7.add(btnNextKiri, gridBagConstraints);

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
        jPanel7.add(btnNextKanan, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = -235;
        gridBagConstraints.ipady = -340;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jPanel7, gridBagConstraints);
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
                JOptionPane.showMessageDialog(this, "Nama, Jenis Barang, dan Keluhan wajib diisi!");
                return;
            }

            String idTeknisiTerpilih = pilihTeknisiPopUp();
            if (idTeknisiTerpilih == null) return; 

            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false); 

            int finalIdPelanggan = idPelanggan;
            String idServisSaatIni = tNomorServ.getText();

            if (rbBaru.isSelected()) {
                PreparedStatement pstPel = conn.prepareStatement("INSERT INTO tbl_pelanggan (nama_pelanggan, no_hp, alamat) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                pstPel.setString(1, tNamaPelanggan.getText());
                pstPel.setString(2, tNoPelanggan.getText());
                pstPel.setString(3, tAlamatPelanggan.getText());
                pstPel.executeUpdate();
                ResultSet rsId = pstPel.getGeneratedKeys();
                if (rsId.next()) finalIdPelanggan = rsId.getInt(1);
            } else {
                if (finalIdPelanggan == 0) { JOptionPane.showMessageDialog(this, "Pilih pelanggan lama!"); return; }
                PreparedStatement pstUpPel = conn.prepareStatement("UPDATE tbl_pelanggan SET nama_pelanggan=?, no_hp=?, alamat=? WHERE id_pelanggan=?");
                pstUpPel.setString(1, tNamaPelanggan.getText());
                pstUpPel.setString(2, tNoPelanggan.getText());
                pstUpPel.setString(3, tAlamatPelanggan.getText());
                pstUpPel.setInt(4, finalIdPelanggan);
                pstUpPel.executeUpdate();
            }

            PreparedStatement pstCheck = conn.prepareStatement("SELECT COUNT(*) FROM servis WHERE id_servis = ?");
            pstCheck.setString(1, idServisSaatIni);
            ResultSet rsCheck = pstCheck.executeQuery(); rsCheck.next();
            boolean isUpdate = rsCheck.getInt(1) > 0;

            String sqlService = isUpdate 
                ? "UPDATE servis SET id_pelanggan=?, jenis_barang=?, merek=?, model=?, no_seri=?, kelengkapan=?, keluhan_awal=?, status=? WHERE id_servis=?"
                : "INSERT INTO servis (id_pelanggan, jenis_barang, merek, model, no_seri, kelengkapan, keluhan_awal, status, tanggal_masuk, id_admin, id_servis) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement pstServ = conn.prepareStatement(sqlService);
            pstServ.setInt(1, finalIdPelanggan);
            pstServ.setString(2, cbJenisBrg.getSelectedItem().toString());
            pstServ.setString(3, tMerek.getText());
            pstServ.setString(4, tModel.getText());
            pstServ.setString(5, tSeri.getText());
            pstServ.setString(6, tKelengkapan.getText());
            pstServ.setString(7, tKeluhan.getText());
            pstServ.setString(8, cbStatusServ.getSelectedItem().toString());

            if (isUpdate) {
                pstServ.setString(9, idServisSaatIni);
            } else {
                pstServ.setString(9, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                pstServ.setString(10, Session.idUser);
                pstServ.setString(11, idServisSaatIni);
            }
            pstServ.executeUpdate();

            if (!isUpdate) {
                String sqlFix = "INSERT INTO perbaikan (id_servis, id_teknisi, kerusakan, tindakan, biaya_jasa, diskon) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstFix = conn.prepareStatement(sqlFix);
                pstFix.setString(1, idServisSaatIni);
                pstFix.setString(2, idTeknisiTerpilih);
                pstFix.setString(3, ""); 
                pstFix.setString(4, "");
                pstFix.setInt(5, 0);
                pstFix.setInt(6, 0);
                pstFix.executeUpdate();
            } else {
                PreparedStatement pstFixUp = conn.prepareStatement("UPDATE perbaikan SET id_teknisi = ? WHERE id_servis = ?");
                pstFixUp.setString(1, idTeknisiTerpilih);
                pstFixUp.setString(2, idServisSaatIni);
                pstFixUp.executeUpdate();
            }

            conn.commit(); 
            
            // --- FITUR AUTO CETAK ---
            Object[] options = {"Struk Kecil (Thermal)", "Nota Besar (A4)", "Tidak Cetak"};
            int choice = JOptionPane.showOptionDialog(this, 
                    "Data servis berhasil disimpan.\nApakah Anda ingin mencetak nota sekarang?", 
                    "Opsi Cetak Nota", 
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, options, options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                CetakStruk.cetakStruk(idServisSaatIni, Session.idUser);
            } else if (choice == JOptionPane.NO_OPTION) {
                CetakNotaBesar.cetakNotaA4(idServisSaatIni, Session.idUser);
            }
            // --- END FITUR AUTO CETAK ---

            load_table_service();
            btBatalActionPerformed(null);
            
        } catch (Exception e) {
            try { Koneksi.configDB().rollback(); } catch (Exception ex) {}
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage());
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
        currentPage = 0;
    }//GEN-LAST:event_tfCariKeyReleased

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        load_table_service();
        currentPage = 0;
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

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        load_table_service();
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            load_table_service();
        }
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
