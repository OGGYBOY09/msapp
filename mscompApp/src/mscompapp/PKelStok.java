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
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
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
    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

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
            @Override public void keyReleased(KeyEvent e) { hitungTotal(); }
        });
        
        dateAwal.addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date tglAwal = dateAwal.getDate();
                if (tglAwal != null) {
                    dateAkhir.setMinSelectableDate(tglAwal);
                    if (dateAkhir.getDate() != null && dateAkhir.getDate().before(tglAwal)) {
                        dateAkhir.setDate(tglAwal);
                    }
                } else { dateAkhir.setMinSelectableDate(null); }
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
    
    private void loadKategori(){
        cbKategori.removeAllItems(); cbKategori.addItem("-Pilih Kategori-");
        try {
            ResultSet res = Koneksi.configDB().createStatement().executeQuery("SELECT nama_kategori FROM tbl_kat_barang");
            while(res.next()) cbKategori.addItem(res.getString("nama_kategori"));
        } catch (Exception e) {}
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
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addColumn("No"); model.addColumn("ID Pembelian"); model.addColumn("Tanggal");
        model.addColumn("Kode Barang"); model.addColumn("Nama Barang"); model.addColumn("Kategori"); 
        model.addColumn("Harga"); model.addColumn("Jumlah Beli"); model.addColumn("Total Harga");

        try {
            String where = "";
            if (dateAwal.getDate() != null && dateAkhir.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                where = " WHERE tanggal BETWEEN '" + sdf.format(dateAwal.getDate()) + "' AND '" + sdf.format(dateAkhir.getDate()) + "'";
            }

            Connection conn = Koneksi.configDB();
            
            // 1. Count
            ResultSet rsC = conn.createStatement().executeQuery("SELECT COUNT(*) FROM tbl_pembelian" + where);
            int totalData = rsC.next() ? rsC.getInt(1) : 0;

            // 2. Load with Pagination
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT * FROM tbl_pembelian" + where + " ORDER BY id_pembelian DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;
            ResultSet res = conn.createStatement().executeQuery(sql);
            int no = offset + 1;
            while(res.next()){
                model.addRow(new Object[]{
                    no++, res.getString("id_pembelian"), res.getString("tanggal"), 
                    res.getString("kode_barang"), res.getString("nama_barang"), 
                    res.getString("kategori"), res.getInt("harga"), res.getInt("jumlah_beli"), res.getInt("total_harga")
                });
            }
            tblBarang.setModel(model);

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
        btRefresh = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        JTanggal = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tKodeBrg = new javax.swing.JTextField();
        tNamaBarang = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tJumlahBeli = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tTotalHarga = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btSimpan = new javax.swing.JButton();
        cbKategori = new javax.swing.JComboBox<>();
        btPilihKode = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        tHarga = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(1780, 960));
        setMinimumSize(new java.awt.Dimension(1780, 960));
        setPreferredSize(new java.awt.Dimension(1720, 960));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 1170, 540));

        btCari.setBackground(new java.awt.Color(204, 204, 204));
        btCari.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btCari.setText("Cari[F2]");
        btCari.addActionListener(this::btCariActionPerformed);
        jPanel2.add(btCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 120, 100, 40));
        jPanel2.add(dateAwal, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, 150, 40));
        jPanel2.add(dateAkhir, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 120, 150, 40));

        jLabel6.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel6.setText("Dari :");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 40));

        jLabel7.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel7.setText("Sampai :");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 120, -1, 40));

        jLabel10.setFont(new java.awt.Font("Swis721 LtEx BT", 0, 18)); // NOI18N
        jLabel10.setText("Filter Bulan :");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 40));

        btnedit.setBackground(new java.awt.Color(255, 255, 102));
        btnedit.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btnedit.setText("Edit[F1]");
        btnedit.addActionListener(this::btneditActionPerformed);
        jPanel2.add(btnedit, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 120, 100, 40));

        btndelete.setBackground(new java.awt.Color(255, 51, 51));
        btndelete.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btndelete.setText("Delete[Del]");
        btndelete.addActionListener(this::btndeleteActionPerformed);
        jPanel2.add(btndelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 120, 150, 40));

        btRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btRefresh.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btRefresh.setText("Refresh[F3]");
        btRefresh.addActionListener(this::btRefreshActionPerformed);
        jPanel2.add(btRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 120, 130, 40));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(844, Short.MAX_VALUE)
                .addComponent(btnNextKiri)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextKanan)
                .addGap(118, 118, 118))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 26, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNextKanan, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNextKiri, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 740, 1190, -1));

        jLabel11.setBackground(new java.awt.Color(4, 102, 200));
        jLabel11.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("DAFTAR PEMBELIAN STOK BARANG");
        jLabel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel11.setOpaque(true);
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1210, 70));
        jPanel2.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 120, 150, 40));

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, 1210, 920));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(4, 102, 200));
        jLabel1.setFont(new java.awt.Font("Swis721 WGL4 BT", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TAMBAH STOK BARANG");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setOpaque(true);
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 410, 70));

        jLabel2.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel2.setText("Tanggal :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 300, 40));

        JTanggal.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(JTanggal, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 350, 40));

        jLabel3.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel3.setText("Kode Barang :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 300, 40));

        jLabel5.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel5.setText("Nama Barang :");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, 300, 40));

        tKodeBrg.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tKodeBrg, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 270, 40));

        tNamaBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tNamaBarang, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, 350, 40));

        jLabel8.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel8.setText("Jumlah Beli :");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 300, 40));

        tJumlahBeli.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tJumlahBeli, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 590, 350, 40));

        jLabel9.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel9.setText("Total Harga :");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 640, 300, 40));

        tTotalHarga.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jPanel1.add(tTotalHarga, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 680, 350, 40));

        jLabel4.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        jLabel4.setText("Kategori Barang :");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 300, 40));

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btSimpan.setText("SIMPAN");
        btSimpan.addActionListener(this::btSimpanActionPerformed);
        jPanel1.add(btSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 750, 351, 60));

        cbKategori.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addActionListener(this::cbKategoriActionPerformed);
        jPanel1.add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 410, 350, 40));

        btPilihKode.setBackground(new java.awt.Color(204, 204, 204));
        btPilihKode.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        btPilihKode.setText("Pilih");
        btPilihKode.addActionListener(this::btPilihKodeActionPerformed);
        jPanel1.add(btPilihKode, new org.netbeans.lib.awtextra.AbsoluteConstraints(302, 230, 80, 40));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 500, 350, 40));

        tHarga.setFont(new java.awt.Font("Swis721 WGL4 BT", 0, 18)); // NOI18N
        tHarga.setText("Harga :");
        jPanel1.add(tHarga, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 460, 150, 40));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 410, 920));
    }// </editor-fold>//GEN-END:initComponents

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        // TODO add your handling code here:
        dateAwal.setDate(null); dateAkhir.setDate(null);
        currentPage = 0;
        loadData();
    }//GEN-LAST:event_btRefreshActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        loadData();
    }//GEN-LAST:event_btnNextKananActionPerformed

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            loadData();
        }
    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

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
        currentPage = 0; // Filter tanggal reset ke hal 1
        loadData();
        
    }                                      

    private void btPilihKodeActionPerformed(java.awt.event.ActionEvent evt) {                                            
        PopUpBarang popup = new PopUpBarang(); popup.stokForm = this; popup.setVisible(true);
    }                                           

    private void btLihatActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // Kosong (Untuk fitur laporan nanti)
        
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser JTanggal;
    private javax.swing.JButton btCari;
    private javax.swing.JButton btPilihKode;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btndelete;
    private javax.swing.JButton btnedit;
    private javax.swing.JComboBox<String> cbKategori;
    private com.toedter.calendar.JDateChooser dateAkhir;
    private com.toedter.calendar.JDateChooser dateAwal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel tHarga;
    private javax.swing.JTextField tJumlahBeli;
    private javax.swing.JTextField tKodeBrg;
    private javax.swing.JTextField tNamaBarang;
    private javax.swing.JTextField tTotalHarga;
    private javax.swing.JTable tblBarang;
    // End of variables declaration//GEN-END:variables
}
