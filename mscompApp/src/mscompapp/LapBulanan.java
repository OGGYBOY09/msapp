/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;
import config.Koneksi;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement; // Tambahan untuk keamanan query
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
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
public class LapBulanan extends javax.swing.JPanel {

    /**
     * Creates new form LapBulanan
     */
    private PKelLaporan parent;

    
    // --- FITUR PAGINATION ---
    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    public LapBulanan(PKelLaporan parent) {
        this.parent = parent;
        initComponents();
        loadComboStatus();
        loadComboKategori();
        initKeyShortcuts();
        
        // Custom Table Renderer (Warna baris berdasarkan status)
        tblLapBulanan = new javax.swing.JTable() {
            {
                setRowHeight(30);
                getTableHeader().setReorderingAllowed(false);
            }
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component comp = super.prepareRenderer(renderer, row, column);
                Object statusValue = getValueAt(row, 11); 

                if (statusValue != null) {
                    String status = statusValue.toString();
                    if (isRowSelected(row)) {
                        comp.setBackground(getSelectionBackground());
                    } else {
                        switch (status) {
                            case "Proses": comp.setBackground(java.awt.Color.YELLOW); break;
                            case "Selesai": comp.setBackground(new java.awt.Color(144, 238, 144)); break;
                            case "Dibatalkan": comp.setBackground(new java.awt.Color(255, 182, 193)); break;
                            case "Menunggu": comp.setBackground(java.awt.Color.WHITE); break;
                            default: comp.setBackground(java.awt.Color.WHITE); break;
                        }
                        comp.setForeground(java.awt.Color.BLACK);
                    }
                }
                return comp;
            }
        };
        jScrollPane1.setViewportView(tblLapBulanan);
        
        tampilData();

        // --- LISTENERS (RESET KE HALAMAN 1 JIKA FILTER BERUBAH) ---
        mcBulan.addPropertyChangeListener("month", e -> { currentPage = 0; tampilData(); });
        thTahun.addPropertyChangeListener("year", e -> { currentPage = 0; tampilData(); });
        cbStatus.addActionListener(e -> { currentPage = 0; tampilData(); }); 
        cbKategori.addActionListener(e -> { currentPage = 0; tampilData(); });
    }
    
    private void initKeyShortcuts() {
        // Menggunakan WHEN_IN_FOCUSED_WINDOW agar shortcut jalan dimanapun fokus kursor berada
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        // 1. ENTER -> Button Simpan
        
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
        


    }
    
    private void loadComboKategori() {
        cbKategori.removeAllItems();
        cbKategori.addItem("- Semua Kategori -");
        try {
            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery("SELECT nama_jenis FROM tbl_jenis_perangkat");
            while (rs.next()) {
                cbKategori.addItem(rs.getString("nama_jenis"));
            }
        } catch (Exception e) {
            System.err.println("Error load kategori: " + e.getMessage());
        }
    }
    
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
        cbStatus.setSelectedIndex(0);
    }
    public void tampilData() {
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };        
        model.addColumn("No");
        model.addColumn("ID Servis");
        model.addColumn("Tanggal Masuk");
        model.addColumn("Nama");
        model.addColumn("Nomor HP");
        model.addColumn("Alamat");
        model.addColumn("Jenis Barang");
        model.addColumn("Merek");
        model.addColumn("Keluhan");
        model.addColumn("Kelengkapan");
        model.addColumn("Total Biaya"); 
        model.addColumn("Status");
        model.addColumn("Status Barang");

        try {
            // 1. Persiapan Filter
            String whereClause = "WHERE 1=1 ";
            int bulan = mcBulan.getMonth() + 1;
            int tahun = thTahun.getYear();
            whereClause += "AND MONTH(s.tanggal_masuk) = " + bulan + " AND YEAR(s.tanggal_masuk) = " + tahun + " ";

            if (!tfCari.getText().isEmpty()) {
                whereClause += "AND (p.nama_pelanggan LIKE '%" + tfCari.getText() + "%' OR s.id_servis LIKE '%" + tfCari.getText() + "%') ";
            }

            if (cbStatus.getSelectedIndex() > 0) {
                whereClause += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            }
            
            if (cbKategori.getSelectedIndex() > 0) {
                whereClause += "AND s.jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            // 2. Hitung Total Data (Untuk mematikan/menghidupkan tombol Next)
            Connection conn = Koneksi.configDB();
            String sqlCount = "SELECT COUNT(*) AS total FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " + whereClause;
            ResultSet rsCount = conn.createStatement().executeQuery(sqlCount);
            int totalData = 0;
            if (rsCount.next()) totalData = rsCount.getInt("total");

            // 3. Query Utama dengan LIMIT dan OFFSET
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat "
                       + "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + whereClause
                       + "ORDER BY s.tanggal_masuk DESC "
                       + "LIMIT " + PAGE_SIZE + " OFFSET " + offset;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            DecimalFormat df = new DecimalFormat("#,###");
            
            int no = offset + 1; // Nomor urut menyesuaikan halaman
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getString("id_servis"),
                    rs.getString("tanggal_masuk"),
                    rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"),
                    rs.getString("alamat"),
                    rs.getString("jenis_barang"),
                    rs.getString("merek"),
                    rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"),
                    "Rp " + df.format(rs.getDouble("harga")), 
                    rs.getString("status"),
                    rs.getString("status_barang")
                });
            }
            tblLapBulanan.setModel(model);

            // 4. Atur Button State (Biar tidak error kalau data habis)
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) {
            System.err.println("Error tampil data: " + e.getMessage());
        }
        hitungDanKirimPendapatan();
    }
    
    private void bukaHalamanDetail(String idServis) {
        try {
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat "
                       + "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + "WHERE s.id_servis = '" + idServis + "'";
            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) {
                DetailService ds = new DetailService(
                    rs.getString("id_servis"), rs.getString("tanggal_masuk"), rs.getString("nama_pelanggan"),
                    rs.getString("no_hp"), rs.getString("alamat"), rs.getString("jenis_barang"),
                    rs.getString("merek"), rs.getString("model"), rs.getString("no_seri"),
                    rs.getString("keluhan_awal"), rs.getString("kelengkapan"), rs.getString("status"), rs.getString("status_barang"), Session.idUser
                );
                
                // --- LISTENER SAAT POPUP DITUTUP ---
                ds.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        tampilData(); 
                    }
                });
                
                ds.setLocationRelativeTo(null);
                ds.setVisible(true);
            }
        } catch (Exception e) {}
    }
    
    private void hitungDanKirimPendapatan() {
        if (parent == null) return;
        try {
            int bulan = mcBulan.getMonth() + 1;
            int tahun = thTahun.getYear();
            String sql = "SELECT SUM(harga) AS total FROM servis WHERE status='Selesai' "
                       + "AND MONTH(tanggal_masuk) = " + bulan + " AND YEAR(tanggal_masuk) = " + tahun;
            
            if (cbKategori.getSelectedIndex() > 0) {
                sql += " AND jenis_barang = '" + cbKategori.getSelectedItem().toString() + "'";
            }

            Connection conn = Koneksi.configDB();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            int total = 0;
            if (rs.next()) total = rs.getInt("total");
            
            String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                                  "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
            parent.setInfoPendapatan("Pendapatan Bulan " + namaBulan[mcBulan.getMonth()] + " " + tahun + " :", total);
        } catch (Exception e) { System.out.println("Err Bulanan: " + e.getMessage()); }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tfCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();
        btnDetail = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLapBulanan = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        mcBulan = new com.toedter.calendar.JMonthChooser();
        jLabel3 = new javax.swing.JLabel();
        thTahun = new com.toedter.calendar.JYearChooser();
        btCetakE = new javax.swing.JButton();
        btnPdf = new javax.swing.JButton();
        btnNota = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        btnNextKiri = new javax.swing.JButton();
        btnNextKanan = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(1680, 760));
        setMinimumSize(new java.awt.Dimension(1680, 760));
        setPreferredSize(new java.awt.Dimension(1680, 760));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        add(tfCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 20, 259, 50));

        btnCari.setBackground(new java.awt.Color(102, 255, 102));
        btnCari.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCari.setText("CARI [F2]");
        btnCari.addActionListener(this::btnCariActionPerformed);
        add(btnCari, new org.netbeans.lib.awtextra.AbsoluteConstraints(283, 18, -1, 50));

        btnDetail.setBackground(new java.awt.Color(204, 204, 204));
        btnDetail.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDetail.setText("DETAIL");
        btnDetail.addActionListener(this::btnDetailActionPerformed);
        add(btnDetail, new org.netbeans.lib.awtextra.AbsoluteConstraints(399, 18, -1, 50));

        btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("REFRESH [F3]");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);
        add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(497, 18, -1, 50));

        tblLapBulanan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblLapBulanan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Tanggal", "Nama", "Nomor HP", "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", "Keluhan", "Kelengkapan", "Status", "Status Barang"
            }
        ));
        tblLapBulanan.setRowHeight(30);
        jScrollPane1.setViewportView(tblLapBulanan);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 1660, 540));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Bulan :");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 20, -1, 50));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Status :");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1490, 20, -1, 50));

        cbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cbStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(1550, 20, 116, 50));

        mcBulan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(mcBulan, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 20, 121, 50));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Tahun :");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, -1, 50));
        add(thTahun, new org.netbeans.lib.awtextra.AbsoluteConstraints(1190, 20, 100, 50));

        btCetakE.setBackground(new java.awt.Color(204, 204, 204));
        btCetakE.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btCetakE.setText("Cetak Excel");
        btCetakE.addActionListener(this::btCetakEActionPerformed);
        add(btCetakE, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 20, -1, 50));

        btnPdf.setBackground(new java.awt.Color(204, 204, 204));
        btnPdf.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnPdf.setText("PDF");
        btnPdf.addActionListener(this::btnPdfActionPerformed);
        add(btnPdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 20, -1, 50));

        btnNota.setBackground(new java.awt.Color(102, 255, 102));
        btnNota.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNota.setText("Nota");
        btnNota.addActionListener(this::btnNotaActionPerformed);
        add(btnNota, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 20, -1, 50));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Kategori :");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1300, 20, -1, 50));

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbKategori.addItemListener(this::cbKategoriItemStateChanged);
        cbKategori.addActionListener(this::cbKategoriActionPerformed);
        add(cbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(1370, 20, 112, 50));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnNextKiri.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKiri.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKiri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/image.png"))); // NOI18N
        btnNextKiri.setText("NEXT");
        btnNextKiri.addActionListener(this::btnNextKiriActionPerformed);
        jPanel1.add(btnNextKiri, new org.netbeans.lib.awtextra.AbsoluteConstraints(1340, 20, -1, -1));

        btnNextKanan.setBackground(new java.awt.Color(204, 204, 204));
        btnNextKanan.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        btnNextKanan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_12143770 (4).png"))); // NOI18N
        btnNextKanan.setText("NEXT");
        btnNextKanan.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNextKanan.addActionListener(this::btnNextKananActionPerformed);
        jPanel1.add(btnNextKanan, new org.netbeans.lib.awtextra.AbsoluteConstraints(1480, 20, -1, -1));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 1680, 120));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        tampilData();
        currentPage = 0;
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText("");
        cbStatus.setSelectedIndex(0);
        cbKategori.setSelectedIndex(0);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        mcBulan.setMonth(cal.get(java.util.Calendar.MONTH));
        thTahun.setYear(cal.get(java.util.Calendar.YEAR));
        currentPage = 0; // Reset pagination
        tampilData();
        
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        int row = tblLapBulanan.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis terlebih dahulu!");
            return;
        }
        String idServis = tblLapBulanan.getValueAt(row, 1).toString();
        bukaHalamanDetail(idServis);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btCetakEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCetakEActionPerformed
        // TODO add your handling code here:
        int bulanIndex = mcBulan.getMonth(); // 0 = Januari
        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                              "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        String bulanText = namaBulan[bulanIndex];
        
        // 2. Ambil Data Tahun
        int tahun = thTahun.getYear();
        
        // 3. Panggil ExportExcel dengan parameter tambahan
        ExportExcel.exportJTableToExcel(tblLapBulanan, bulanText, tahun);
    }//GEN-LAST:event_btCetakEActionPerformed

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfActionPerformed
        // TODO add your handling code here:
        int bulanIndex = mcBulan.getMonth();
        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                              "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        String bulanText = namaBulan[bulanIndex];
        int tahun = thTahun.getYear();
        
        // 2. Ambil Keterangan Status untuk Sub-Judul
        // Misal: "Status: Semua Status" atau "Status: Proses"
        String statusFilter = "Status: " + cbStatus.getSelectedItem().toString();
        
        // 3. Panggil Fungsi Export PDF
        ExportPDF.exportToPDF(tblLapBulanan, bulanText, tahun, statusFilter);
    
    }//GEN-LAST:event_btnPdfActionPerformed

    private void btnNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotaActionPerformed
        // TODO add your handling code here:
        int row = tblLapBulanan.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data servis yang ingin dicetak notanya!");
        return;
    }
    
    String idServis = tblLapBulanan.getValueAt(row, 1).toString();
    
    // Dialog pilihan ukuran kertas
    Object[] options = {"Struk Kecil (Thermal)", "Nota Besar (A4)"};
    int choice = JOptionPane.showOptionDialog(this, 
            "Pilih ukuran kertas yang ingin digunakan:", 
            "Opsi Cetak Nota", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

    if (choice == JOptionPane.YES_OPTION) {
        // Panggil Struk Kecil yang sudah ada
        CetakStruk.cetakStruk(idServis, Session.idUser);
    } else if (choice == JOptionPane.NO_OPTION) {
        // Panggil Nota Besar yang baru dibuat
        CetakNotaBesar.cetakNotaA4(idServis, Session.idUser);
    }
    }//GEN-LAST:event_btnNotaActionPerformed

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed

    private void cbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKategoriItemStateChanged
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_cbKategoriItemStateChanged

    private void btnNextKiriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKiriActionPerformed
        // TODO add your handling code here:
        if (currentPage > 0) {
            currentPage--;
            tampilData();
        }
    }//GEN-LAST:event_btnNextKiriActionPerformed

    private void btnNextKananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextKananActionPerformed
        // TODO add your handling code here:
        currentPage++;
        tampilData();
    }//GEN-LAST:event_btnNextKananActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCetakE;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnDetail;
    private javax.swing.JButton btnNextKanan;
    private javax.swing.JButton btnNextKiri;
    private javax.swing.JButton btnNota;
    private javax.swing.JButton btnPdf;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JMonthChooser mcBulan;
    public javax.swing.JTable tblLapBulanan;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JYearChooser thTahun;
    // End of variables declaration//GEN-END:variables
}
