/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mscompapp;

import config.Koneksi;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
/**
 *
 * @author Acer Aspire Lite 15
 */
public class LapHarian extends javax.swing.JPanel {

    /**
     * Creates new form LapHarian
     */
    private PKelLaporan parent;

    private int currentPage = 0;
    private final int PAGE_SIZE = 20;

    public LapHarian(PKelLaporan parent) {
        this.parent = parent; 
        initComponents();
        loadComboStatus();
        loadComboKategori();
        tglHarian.setDate(new Date());
        
        aturWarnaBarisTabel();
        addFilterListeners();
        tampilData();
    }
    
    private void aturWarnaBarisTabel() {
    // 1. Mengatur tinggi baris agar lebih lega
    tblLapHarian.setRowHeight(35); 
    
    // 2. Menerapkan logic pewarnaan cell (Renderer)
    tblLapHarian.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        
        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Mengambil komponen dasar (teks, font, dll)
            java.awt.Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Cek nilai status pada Kolom ke-11 (Ingat hitungan kolom mulai dari 0)
            // Di method tampilData(), kolom "Status" adalah kolom ke-12, jadi indexnya 11.
            Object statusObj = table.getValueAt(row, 11);
            String status = (statusObj != null) ? statusObj.toString() : "";

            // Logika Ganti Warna
            if (isSelected) {
                // Jika baris dipilih, gunakan warna seleksi biru (bawaan sistem)
                comp.setBackground(table.getSelectionBackground());
                comp.setForeground(table.getSelectionForeground());
            } else {
                // Jika tidak dipilih, warnai berdasarkan status
                switch (status) {
                    case "Proses":
                        comp.setBackground(java.awt.Color.YELLOW);
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Selesai":
                        comp.setBackground(new java.awt.Color(144, 238, 144)); // Hijau Muda
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Dibatalkan":
                        comp.setBackground(new java.awt.Color(255, 182, 193)); // Merah Muda
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    case "Menunggu":
                        comp.setBackground(java.awt.Color.WHITE);
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                    default:
                        comp.setBackground(java.awt.Color.WHITE);
                        comp.setForeground(java.awt.Color.BLACK);
                        break;
                }
            }
            return comp;
        }
    });
}
    
    private void addFilterListeners() {
       tglHarian.addPropertyChangeListener("date", e -> { currentPage = 0; tampilData(); });
        cbStatus.addActionListener(e -> { currentPage = 0; tampilData(); });
        cbKategori.addActionListener(e -> { currentPage = 0; tampilData(); });
    }
    
    private void loadComboStatus() {
        cbStatus.removeAllItems();
        cbStatus.addItem("- Semua Status -");
        cbStatus.addItem("Menunggu");
        cbStatus.addItem("Proses");
        cbStatus.addItem("Selesai");
        cbStatus.addItem("Dibatalkan");
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
        } catch (Exception e) { System.err.println("Err: " + e.getMessage()); }
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
            // 1. Membangun Where Clause
            String where = "WHERE 1=1 ";
            if (tglHarian.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                where += "AND s.tanggal_masuk = '" + sdf.format(tglHarian.getDate()) + "' ";
            }
            if (!tfCari.getText().isEmpty()) {
                String key = tfCari.getText();
                where += "AND (p.nama_pelanggan LIKE '%"+key+"%' OR p.no_hp LIKE '%"+key+"%' OR s.id_servis LIKE '%"+key+"%') ";
            }
            if (cbStatus.getSelectedIndex() > 0) {
                where += "AND s.status = '" + cbStatus.getSelectedItem().toString() + "' ";
            }
            if (cbKategori.getSelectedIndex() > 0) {
                where += "AND s.jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            // 2. Hitung Total untuk Pagination
            Connection conn = Koneksi.configDB();
            ResultSet rsCount = conn.createStatement().executeQuery("SELECT COUNT(*) AS total FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " + where);
            int totalData = 0;
            if (rsCount.next()) totalData = rsCount.getInt("total");

            // 3. Query Utama dengan Limit
            int offset = currentPage * PAGE_SIZE;
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat "
                       + "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan "
                       + where + "ORDER BY s.id_servis DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

            ResultSet rs = conn.createStatement().executeQuery(sql);
            DecimalFormat df = new DecimalFormat("#,###");
            int no = offset + 1;

            while (rs.next()) {
                model.addRow(new Object[]{
                    no++, rs.getString("id_servis"), rs.getString("tanggal_masuk"),
                    rs.getString("nama_pelanggan"), rs.getString("no_hp"), rs.getString("alamat"),
                    rs.getString("jenis_barang"), rs.getString("merek"), rs.getString("keluhan_awal"),
                    rs.getString("kelengkapan"), "Rp " + df.format(rs.getDouble("harga")), 
                    rs.getString("status"), rs.getString("status_barang")
                });
            }
            tblLapHarian.setModel(model);
            aturKolomTabel();

            // Update Status Tombol
            btnNextKiri.setEnabled(currentPage > 0);
            btnNextKanan.setEnabled((offset + PAGE_SIZE) < totalData);

        } catch (Exception e) { System.err.println("Err: " + e.getMessage()); }
        hitungDanKirimPendapatan();
    }

    private void aturKolomTabel() {
    // Pastikan tabel tidak null dan sudah memiliki kolom
    if (tblLapHarian != null && tblLapHarian.getColumnCount() > 0) {
        
        // 1. Matikan Auto Resize agar Scrollbar Horizontal berfungsi
        tblLapHarian.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        // 2. Tentukan lebar tiap kolom (No, Tgl, Nama, HP, Alamat, dst)
        // Sesuaikan angka ini jika ada kolom yang masih kurang lebar
        int[] lebarKunci = {40, 100, 150, 120, 200, 120, 100, 120, 120, 200, 150, 120};

        for (int i = 0; i < tblLapHarian.getColumnCount(); i++) {
            javax.swing.table.TableColumn col = tblLapHarian.getColumnModel().getColumn(i);
            
            // Gunakan lebar dari array, jika index melebihi array gunakan default 100
            int lebar = (i < lebarKunci.length) ? lebarKunci[i] : 100;
            
            col.setPreferredWidth(lebar);
            col.setMinWidth(lebar); // Mengunci agar tidak menciut
        }

        // 3. Set Header rata tengah
        javax.swing.table.DefaultTableCellRenderer headerRenderer = 
            (javax.swing.table.DefaultTableCellRenderer) tblLapHarian.getTableHeader().getDefaultRenderer();
        if (headerRenderer != null) {
            headerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        }
    }
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
                        tampilData(); // Refresh Tabel Bulanan
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
            String sql = "SELECT SUM(harga) AS total FROM servis WHERE status='Selesai' ";
            
            // Filter Tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl = "";
            if (tglHarian.getDate() != null) {
                tgl = sdf.format(tglHarian.getDate());
                sql += "AND DATE(tanggal_masuk) = '" + tgl + "' ";
            }
            
            // Filter Kategori (Jika ada)
            if (cbKategori.getSelectedIndex() > 0) {
                sql += "AND jenis_barang = '" + cbKategori.getSelectedItem().toString() + "' ";
            }

            Connection conn = Koneksi.configDB();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            
            // Format Tanggal untuk Judul (agar lebih cantik dibaca)
            SimpleDateFormat sdfView = new SimpleDateFormat("dd MMMM yyyy");
            String tglView = (tglHarian.getDate() != null) ? sdfView.format(tglHarian.getDate()) : "-";
            
            // KIRIM KE PARENT
            parent.setInfoPendapatan("Pendapatan Tanggal " + tglView + " :", total);
            
        } catch (Exception e) {
            System.out.println("Err Harian: " + e.getMessage());
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
    java.awt.GridBagConstraints gbc;

    tfCari = new javax.swing.JTextField();
    btnCari = new javax.swing.JButton();
    btnDetail = new javax.swing.JButton();
    btnRefresh = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblLapHarian = new javax.swing.JTable();
    jLabel1 = new javax.swing.JLabel();
    tglHarian = new com.toedter.calendar.JDateChooser();
    jLabel2 = new javax.swing.JLabel();
    cbStatus = new javax.swing.JComboBox<>();
    btnNota = new javax.swing.JButton();
    btnPdf = new javax.swing.JButton();
    btCetakE = new javax.swing.JButton();
    jLabel4 = new javax.swing.JLabel();
    cbKategori = new javax.swing.JComboBox<>();
    btnNextKanan = new javax.swing.JButton();
    btnNextKiri = new javax.swing.JButton();

    setBackground(new java.awt.Color(255, 255, 255));
    setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    setLayout(new java.awt.GridBagLayout());

    // --- BARIS 1: PENCARIAN & ACTION BUTTONS ---
    gbc = new java.awt.GridBagConstraints();
    gbc.insets = new java.awt.Insets(10, 10, 5, 5);
    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

    // tfCari diperpanjang
    gbc.gridx = 0; gbc.gridy = 0; gbc.ipadx = 180; gbc.weightx = 0.5;
    add(tfCari, gbc);

    gbc.weightx = 0; gbc.ipadx = 0;
    btnCari.setBackground(new java.awt.Color(102, 255, 102));
    btnCari.setFont(new java.awt.Font("Segoe UI", 1, 12));
    btnCari.setText("CARI [F2]");
    btnCari.addActionListener(this::btnCariActionPerformed);
    gbc.gridx = 1; add(btnCari, gbc);

    btnDetail.setBackground(new java.awt.Color(204, 204, 204));
    btnDetail.setText("DETAIL");
    btnDetail.addActionListener(this::btnDetailActionPerformed);
    gbc.gridx = 2; add(btnDetail, gbc);

    btnRefresh.setBackground(new java.awt.Color(204, 204, 204));
    btnRefresh.setText("REFRESH [F3]");
    btnRefresh.addActionListener(this::btnRefreshActionPerformed);
    gbc.gridx = 3; add(btnRefresh, gbc);

    btCetakE.setBackground(new java.awt.Color(204, 204, 204));
    btCetakE.setText("Cetak Excel");
    btCetakE.addActionListener(this::btCetakEActionPerformed);
    gbc.gridx = 4; add(btCetakE, gbc);

    btnPdf.setBackground(new java.awt.Color(204, 204, 204));
    btnPdf.setText("PDF");
    btnPdf.addActionListener(this::btnPdfActionPerformed);
    gbc.gridx = 5; add(btnPdf, gbc);

    btnNota.setBackground(new java.awt.Color(102, 255, 102));
    btnNota.setText("Nota");
    btnNota.addActionListener(this::btnNotaActionPerformed);
    gbc.gridx = 6; add(btnNota, gbc);

    // --- BARIS 1: FILTER TANGGAL ---
gbc = new java.awt.GridBagConstraints();
gbc.gridy = 0;

// Label "Tanggal :"
jLabel1.setText("Tanggal :");
gbc.gridx = 7; 
gbc.weightx = 1.0; // Memberikan bobot penuh agar kolom label tidak terhimpit
gbc.anchor = java.awt.GridBagConstraints.EAST;
gbc.fill = java.awt.GridBagConstraints.NONE;
gbc.insets = new java.awt.Insets(10, 10, 5, 5);
add(jLabel1, gbc);

// Date Chooser (tglHarian) - Dikecilkan tapi tidak kekecilan
gbc.gridx = 8;
gbc.weightx = 0; // Mengunci agar tidak melebar otomatis
gbc.ipadx = 70;  // Ukuran sedang (setengah dari sebelumnya yang 150)
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
gbc.insets = new java.awt.Insets(10, 5, 5, 5);
add(tglHarian, gbc);

// --- BARIS 2: KATEGORI & STATUS ---
gbc = new java.awt.GridBagConstraints();
gbc.gridy = 1;

// Label "Kategori :"
jLabel4.setText("Kategori :");
gbc.gridx = 7; 
gbc.weightx = 1.0; // Samakan bobot dengan label atas
gbc.anchor = java.awt.GridBagConstraints.EAST;
gbc.insets = new java.awt.Insets(5, 10, 10, 5);
add(jLabel4, gbc);

// ComboBox Kategori (Sejajar dengan tanggal)
gbc.gridx = 8; 
gbc.weightx = 0;
gbc.ipadx = 0; // Reset ipadx karena ComboBox punya ukuran default yang pas
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
gbc.insets = new java.awt.Insets(5, 5, 10, 5);
add(cbKategori, gbc);

// Label "Status :"
gbc.gridx = 9; 
gbc.weightx = 0;
gbc.anchor = java.awt.GridBagConstraints.EAST;
gbc.insets = new java.awt.Insets(5, 15, 10, 5);
jLabel2.setText("Status :");
add(jLabel2, gbc);

// ComboBox Status
gbc.gridx = 10; 
gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
gbc.insets = new java.awt.Insets(5, 5, 10, 15);
add(cbStatus, gbc);

    // --- BARIS 3: TABEL ---
    tblLapHarian.setRowHeight(30);
    jScrollPane1.setViewportView(tblLapHarian);
    jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.gridwidth = 11;
    gbc.fill = java.awt.GridBagConstraints.BOTH;
    gbc.weightx = 1.0; gbc.weighty = 1.0;
    gbc.insets = new java.awt.Insets(0, 10, 10, 10);
    add(jScrollPane1, gbc);

    // --- BARIS 4: NAVIGASI ---
    javax.swing.JPanel pnlNav = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
    pnlNav.setOpaque(false);
    pnlNav.add(btnNextKiri);
    pnlNav.add(btnNextKanan);

    gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 3;
    gbc.gridwidth = 11;
    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gbc.insets = new java.awt.Insets(0, 0, 10, 10);
    add(pnlNav, gbc);

    // Atur lebar kolom awal
    javax.swing.SwingUtilities.invokeLater(() -> aturKolomTabel());
}// </editor-fold>//GEN-END:initComponents

    private void btnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariActionPerformed
        // TODO add your handling code here:
        tampilData();
    }//GEN-LAST:event_btnCariActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        tfCari.setText(""); 
        cbStatus.setSelectedIndex(0); 
        tglHarian.setDate(new Date()); 
        currentPage = 0; tampilData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailActionPerformed
        // TODO add your handling code here:
        int row = tblLapHarian.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data servis terlebih dahulu!");
            return;
        }
        String idServis = tblLapHarian.getValueAt(row, 1).toString();
        bukaHalamanDetail(idServis);
    }//GEN-LAST:event_btnDetailActionPerformed

    private void btCetakEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCetakEActionPerformed
        // TODO add your handling code here:
        if (tglHarian.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal terlebih dahulu!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String strTanggal = sdf.format(tglHarian.getDate());
        
        String judul = "LAPORAN SERVIS HARIAN TANGGAL " + strTanggal;
        
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Harian_" + sdfFile.format(tglHarian.getDate());
        
        ExportExcel.exportJTableToExcelCustom(tblLapHarian, judul, suffix);
    }//GEN-LAST:event_btCetakEActionPerformed

    private void btnPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfActionPerformed
        // TODO add your handling code here:
        if (tglHarian.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal terlebih dahulu!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String strTanggal = sdf.format(tglHarian.getDate());
        
        String judul = "LAPORAN SERVIS HARIAN TANGGAL " + strTanggal;
        String statusFilter = "Status: " + cbStatus.getSelectedItem().toString();
        
        SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyyyy");
        String suffix = "Harian_" + sdfFile.format(tglHarian.getDate());
        
        ExportPDF.exportToPDFCustom(tblLapHarian, judul, statusFilter, suffix);
    }//GEN-LAST:event_btnPdfActionPerformed

    private void btnNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotaActionPerformed
        // TODO add your handling code here:
        int row = tblLapHarian.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data servis yang ingin dicetak notanya!");
        return;
    }
    
    String idServis = tblLapHarian.getValueAt(row, 1).toString();
    
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

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed


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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblLapHarian;
    private javax.swing.JTextField tfCari;
    private com.toedter.calendar.JDateChooser tglHarian;
    // End of variables declaration//GEN-END:variables
}
