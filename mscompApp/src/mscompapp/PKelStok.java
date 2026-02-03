/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.awt.event.KeyAdapter; // Import baru untuk event ngetik
import java.awt.event.KeyEvent;
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
public class PKelStok extends javax.swing.JPanel {

    private boolean isEditMode = false;
    // [BARU] Variabel untuk menyimpan ID dan Jumlah Lama saat proses Edit
    private String idPembelianEdit;
    private int oldQty; 

    /**
     * Creates new form PKelStok
     */
    public PKelStok() {
        initComponents();
        loadData();
        loadKategori(); 
        initKeyShortcuts();
        JTanggal.setDate(new java.util.Date());
        
        tKodeBrg.setEditable(false);
        tNamaBarang.setEditable(false);
        jTextField1.setEditable(false); 
        tTotalHarga.setEditable(false);
        
        tJumlahBeli.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungTotal();
            }
        });
    }
    
    private void initKeyShortcuts() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // ENTER -> btnSimpan
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cmdSimpan");
        am.put("cmdSimpan", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btSimpan.isEnabled()) btSimpan.doClick();
            }
        });

        // F1 -> btn_edit
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "cmdEdit");
        am.put("cmdEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnedit.isEnabled()) btnedit.doClick();
            }
        });

        // DEL -> btnDelete
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "cmdDelete");
        am.put("cmdDelete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btndelete.isEnabled()) btndelete.doClick();
            }
        });

        // F2 -> btn_cari
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "cmdCari");
        am.put("cmdCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btCari.isEnabled()) btCari.doClick();
            }
        });

        
    }
    
    public void itemTerpilih(String kode, String nama, String kat, String harga) {
        tKodeBrg.setText(kode);
        tNamaBarang.setText(nama);
        cbKategori.setSelectedItem(kat);
        jTextField1.setText(harga); 
        tJumlahBeli.requestFocus(); 
    }
    
    private void hitungTotal() {
        try {
            int harga = Integer.parseInt(jTextField1.getText());
            int jumlah = Integer.parseInt(tJumlahBeli.getText());
            int total = harga * jumlah;
            tTotalHarga.setText(String.valueOf(total));
        } catch (NumberFormatException e) {
            tTotalHarga.setText("0");
        }
    }
    
    private void bersihkan() {
        tKodeBrg.setText("");
        tNamaBarang.setText("");
        cbKategori.setSelectedIndex(0);
        jTextField1.setText("");
        tJumlahBeli.setText("");
        tTotalHarga.setText("");
        JTanggal.setDate(new Date());
        
        // Reset Mode Edit
        isEditMode = false;
        btSimpan.setText("SIMPAN");
        tKodeBrg.setEnabled(true);
        btPilihKode.setEnabled(true);
    }

    private String generateIdPembelian() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateNow = sdf.format(new Date());
            
            String sql = "SELECT MAX(id_pembelian) FROM tbl_pembelian WHERE id_pembelian LIKE '" + dateNow + "%'";
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            
            if (res.next()) {
                String lastID = res.getString(1);
                if (lastID != null) {
                    String seq = lastID.substring(lastID.length() - 3);
                    int nextSeq = Integer.parseInt(seq) + 1;
                    return dateNow + String.format("%03d", nextSeq);
                }
            }
            return dateNow + "001";
        } catch (Exception e) {
            System.out.println("Error Generate ID: " + e.getMessage());
            return null;
        }
    }

    private void loadData(){
        DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};
        model.addColumn("No");
        model.addColumn("ID Pembelian"); 
        model.addColumn("Tanggal");
        model.addColumn("Kode Barang");
        model.addColumn("Nama Barang"); 
        model.addColumn("Kategori"); 
        model.addColumn("Harga");
        model.addColumn("Jumlah Beli");
        model.addColumn("Total Harga");
        try {
            String sql = "SELECT * FROM tbl_pembelian ORDER BY id_pembelian DESC";
            int no = 1;
            java.sql.Connection conn = (java.sql.Connection)config.Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            while(res.next()){
                model.addRow(new Object[]{
                    no++,
                    res.getString("id_pembelian"),
                    res.getString("tanggal"), 
                    res.getString("kode_barang"), 
                    res.getString("nama_barang"), 
                    res.getString("kategori"),    
                    res.getInt("harga"),    
                    res.getInt("jumlah_beli"),
                    res.getInt("total_harga")
                });
            }
            tblBarang.setModel(model);
        } catch (Exception e) { System.out.println("Gagal Load: " + e.getMessage()); }
    }
    
    private void loadKategori(){
        cbKategori.removeAllItems();
        cbKategori.addItem("-Pilih Kategori-");
        try {
            String sql = "SELECT nama_kategori FROM tbl_kat_barang";
            java.sql.Connection conn = (java.sql.Connection)config.Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            while(res.next()){
                cbKategori.addItem(res.getString("nama_kategori"));
            }
        } catch (Exception e) {
            System.out.println("Gagal load kategori: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        JTanggal = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tKodeBrg = new javax.swing.JTextField();
        btPilihKode = new javax.swing.JButton();
        tNamaBarang = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tJumlahBeli = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tTotalHarga = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btSimpan = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        btCari = new javax.swing.JButton();
        dateAwal = new com.toedter.calendar.JDateChooser();
        dateAkhir = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        btnedit = new javax.swing.JButton();
        btndelete = new javax.swing.JButton();
        cbKategori = new javax.swing.JComboBox<>();
        tHarga = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(1780, 960));
        setMinimumSize(new java.awt.Dimension(1780, 960));
        setPreferredSize(new java.awt.Dimension(1780, 960));

        jPanel1.setBackground(new java.awt.Color(4, 102, 200));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH STOK BARANG");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel2.setText("Tanggal :");

        JTanggal.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("Kode Barang :");

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("Nama Barang :");

        tKodeBrg.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        btPilihKode.setBackground(new java.awt.Color(204, 204, 204));
        btPilihKode.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btPilihKode.setText("Pilih");
        btPilihKode.addActionListener(this::btPilihKodeActionPerformed);

        tNamaBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel8.setText("Jumlah Beli :");

        tJumlahBeli.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel9.setText("Total Harga :");

        tTotalHarga.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Kategori Barang :");

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btSimpan.setText("SIMPAN");
        btSimpan.addActionListener(this::btSimpanActionPerformed);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tblBarang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Tanggal", "Kode Barang", "Nama Barang", "Kategori ", "Harga", "Jumlah", "Total Harga"
            }
        ));
        tblBarang.setRowHeight(35);
        tblBarang.setSelectionBackground(new java.awt.Color(204, 204, 204));
        jScrollPane1.setViewportView(tblBarang);

        btCari.setBackground(new java.awt.Color(204, 204, 204));
        btCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btCari.setText("Cari");
        btCari.addActionListener(this::btCariActionPerformed);

        jLabel6.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel6.setText("Dari :");

        jLabel7.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel7.setText("Sampai :");

        jLabel10.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel10.setText("Filter Bulan :");

        btnedit.setBackground(new java.awt.Color(255, 255, 102));
        btnedit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnedit.setText("EDIT");
        btnedit.addActionListener(this::btneditActionPerformed);

        btndelete.setBackground(new java.awt.Color(255, 51, 51));
        btndelete.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btndelete.setText("DELETE");
        btndelete.addActionListener(this::btndeleteActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49)
                                .addComponent(jLabel7)
                                .addGap(18, 18, 18)
                                .addComponent(dateAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btCari, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                        .addComponent(btnedit, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btndelete, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dateAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btndelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnedit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(btCari)))
                .addGap(19, 19, 19)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                .addContainerGap())
        );

        cbKategori.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tHarga.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        tHarga.setText("Harga :");

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btSimpan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tKodeBrg)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btPilihKode))
                    .addComponent(JTanggal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tNamaBarang, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tHarga)))
                    .addComponent(tJumlahBeli, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tTotalHarga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tKodeBrg, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btPilihKode, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbKategori)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tJumlahBeli, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tTotalHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(151, 151, 151))
        );
    }// </editor-fold>//GEN-END:initComponents

   private void btSimpanActionPerformed(java.awt.event.ActionEvent evt) {                                         
        try {
            if(tKodeBrg.getText().isEmpty() || tJumlahBeli.getText().isEmpty()){
                JOptionPane.showMessageDialog(this, "Data Barang dan Jumlah Beli harus diisi!");
                return;
            }

            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl = sdf.format(JTanggal.getDate());
            
            if (isEditMode == false) {
                // --- MODE SIMPAN BARU ---
                String idPembelian = generateIdPembelian();
                
                String sql = "INSERT INTO tbl_pembelian VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, idPembelian);
                pst.setString(2, tgl);
                pst.setString(3, tKodeBrg.getText());
                pst.setString(4, tNamaBarang.getText());
                pst.setString(5, cbKategori.getSelectedItem().toString());
                pst.setInt(6, Integer.parseInt(jTextField1.getText())); 
                pst.setInt(7, Integer.parseInt(tJumlahBeli.getText()));
                pst.setInt(8, Integer.parseInt(tTotalHarga.getText()));
                pst.execute();
                
                // Update Tambah Stok
                String sqlUp = "UPDATE tbl_barang SET stok = stok + ? WHERE kode_barang = ?";
                java.sql.PreparedStatement pstUp = conn.prepareStatement(sqlUp);
                pstUp.setInt(1, Integer.parseInt(tJumlahBeli.getText()));
                pstUp.setString(2, tKodeBrg.getText());
                pstUp.execute();
                
                JOptionPane.showMessageDialog(this, "Data Disimpan! ID: " + idPembelian);
                
            } else {
                // --- MODE UPDATE (EDIT) ---
                String sql = "UPDATE tbl_pembelian SET tanggal=?, kategori=?, jumlah_beli=?, total_harga=? WHERE id_pembelian=?";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, tgl);
                pst.setString(2, cbKategori.getSelectedItem().toString());
                pst.setInt(3, Integer.parseInt(tJumlahBeli.getText()));
                pst.setInt(4, Integer.parseInt(tTotalHarga.getText()));
                pst.setString(5, idPembelianEdit);
                pst.execute();
                
                // [PENTING] Update Koreksi Stok
                // Rumus: Stok Sekarang + (Jumlah Baru - Jumlah Lama)
                int newQty = Integer.parseInt(tJumlahBeli.getText());
                int diff = newQty - oldQty;
                
                String sqlUp = "UPDATE tbl_barang SET stok = stok + ? WHERE kode_barang = ?";
                java.sql.PreparedStatement pstUp = conn.prepareStatement(sqlUp);
                pstUp.setInt(1, diff); // Menambah/Mengurang selisih
                pstUp.setString(2, tKodeBrg.getText());
                pstUp.execute();
                
                JOptionPane.showMessageDialog(this, "Data Berhasil Diubah!");
            }
            
            loadData();
            bersihkan();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Proses Gagal: " + e.getMessage());
        }
    }                                        

    // [BARU] Logika Edit Data (Mengambil data ke form)
    private void btneditActionPerformed(java.awt.event.ActionEvent evt) {                                        
        int baris = tblBarang.getSelectedRow();
        if (baris != -1) {
            // Ambil data dari tabel
            // Kolom index: 1=ID, 2=Tgl, 3=Kode, 4=Nama, 5=Kat, 6=Hrg, 7=Jml, 8=Total
            idPembelianEdit = tblBarang.getValueAt(baris, 1).toString();
            String tgl = tblBarang.getValueAt(baris, 2).toString();
            String kode = tblBarang.getValueAt(baris, 3).toString();
            String nama = tblBarang.getValueAt(baris, 4).toString();
            String kat = tblBarang.getValueAt(baris, 5).toString();
            String hrg = tblBarang.getValueAt(baris, 6).toString();
            String jml = tblBarang.getValueAt(baris, 7).toString();
            String total = tblBarang.getValueAt(baris, 8).toString();
            
            // Set ke Form
            try {
                java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(tgl);
                JTanggal.setDate(date);
            } catch (Exception e) {}
            
            tKodeBrg.setText(kode);
            tNamaBarang.setText(nama);
            cbKategori.setSelectedItem(kat);
            jTextField1.setText(hrg);
            tJumlahBeli.setText(jml);
            tTotalHarga.setText(total);
            
            // Simpan jumlah lama untuk hitungan stok nanti
            oldQty = Integer.parseInt(jml);
            
            // Ubah tampilan jadi Mode Edit
            isEditMode = true;
            btSimpan.setText("UBAH");
            tKodeBrg.setEnabled(false); // Kode barang gaboleh ganti saat edit
            btPilihKode.setEnabled(false);
            
            JOptionPane.showMessageDialog(this, "Silahkan edit jumlah atau tanggal, lalu tekan UBAH");
            
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit!");
        }
    }                                       

    // [BARU] Logika Delete Data (Hapus & Kembalikan Stok)
    private void btndeleteActionPerformed(java.awt.event.ActionEvent evt) {                                          
        int baris = tblBarang.getSelectedRow();
        if (baris != -1) {
            String id = tblBarang.getValueAt(baris, 1).toString();
            String kode = tblBarang.getValueAt(baris, 3).toString();
            int qty = Integer.parseInt(tblBarang.getValueAt(baris, 7).toString());
            
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus data pembelian ID: " + id + "?\nStok barang akan dikurangi otomatis.", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
                    
                    // 1. Hapus Data Pembelian
                    String sql = "DELETE FROM tbl_pembelian WHERE id_pembelian = ?";
                    java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, id);
                    pst.execute();
                    
                    // 2. Kembalikan Stok (Kurangi stok di tbl_barang)
                    String sqlStok = "UPDATE tbl_barang SET stok = stok - ? WHERE kode_barang = ?";
                    java.sql.PreparedStatement pstStok = conn.prepareStatement(sqlStok);
                    pstStok.setInt(1, qty);
                    pstStok.setString(2, kode);
                    pstStok.execute();
                    
                    JOptionPane.showMessageDialog(this, "Data dihapus dan stok dikembalikan.");
                    loadData();
                    bersihkan();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus!");
        }
    }                                         

    // [BARU] Logika Filter Data Berdasarkan Tanggal (btCari)
    private void btCariActionPerformed(java.awt.event.ActionEvent evt) {                                       
        if (dateAwal.getDate() == null || dateAkhir.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih Tanggal Awal dan Akhir dulu!");
            return;
        }
        
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("No");
        model.addColumn("ID Pembelian");
        model.addColumn("Tanggal");
        model.addColumn("Kode Barang");
        model.addColumn("Nama Barang"); 
        model.addColumn("Kategori"); 
        model.addColumn("Harga");
        model.addColumn("Jumlah Beli");
        model.addColumn("Total Harga");
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl1 = sdf.format(dateAwal.getDate());
            String tgl2 = sdf.format(dateAkhir.getDate());
            
            // Query filter
            String sql = "SELECT * FROM tbl_pembelian WHERE tanggal BETWEEN '" + tgl1 + "' AND '" + tgl2 + "'";
            
            java.sql.Connection conn = (java.sql.Connection)Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            
            int no = 1;
            while(res.next()){
                model.addRow(new Object[]{
                    no++,
                    res.getString("id_pembelian"),
                    res.getString("tanggal"), 
                    res.getString("kode_barang"), 
                    res.getString("nama_barang"), 
                    res.getString("kategori"),    
                    res.getInt("harga"),    
                    res.getInt("jumlah_beli"),
                    res.getInt("total_harga")
                });
            }
            tblBarang.setModel(model);
        } catch (Exception e) {
            System.out.println("Gagal Filter: " + e.getMessage());
        }
    }                                      

    private void btPilihKodeActionPerformed(java.awt.event.ActionEvent evt) {                                            
        PopUpBarang popup = new PopUpBarang();
        popup.stokForm = this; 
        popup.setVisible(true);
    }                                           

    private void btLihatActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // Kosong (Untuk fitur laporan nanti)
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser JTanggal;
    private javax.swing.JButton btCari;
    private javax.swing.JButton btPilihKode;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btndelete;
    private javax.swing.JButton btnedit;
    private javax.swing.JComboBox<String> cbKategori;
    private com.toedter.calendar.JDateChooser dateAkhir;
    private com.toedter.calendar.JDateChooser dateAwal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel tHarga;
    private javax.swing.JTextField tJumlahBeli;
    private javax.swing.JTextField tKodeBrg;
    private javax.swing.JTextField tNamaBarang;
    private javax.swing.JTextField tTotalHarga;
    private javax.swing.JTable tblBarang;
    // End of variables declaration//GEN-END:variables
}
