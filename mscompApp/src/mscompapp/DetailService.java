/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mscompapp;
import java.sql.*;
import config.Koneksi;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author ASUS
 */
public class DetailService extends javax.swing.JFrame {
    
    private String idServis;
    private String idTeknisi;
    private int currentIdPerbaikan = 0; 
    
    // --- FITUR BARU: BUKU CATATAN PERUBAHAN STOK ---
    // Key: Kode Barang, Value: Jumlah Perubahan (Negatif = Berkurang, Positif = Bertambah)
    private HashMap<String, Integer> logPerubahanStok = new HashMap<>();
    private boolean isDataSaved = false; // Penanda apakah data sudah disimpan

    public DetailService(
        String idServis, String tglMasuk, String nama, String noHp, String alamat,
        String jenis, String merek, String model, String noSeri,
        String keluhan, String kelengkapan, String status, String idTeknisi
    ) {
        initComponents();
        
        // --- 1. SETTING TOMBOL X (CLOSE) ---
        // Ubah default close operation agar kita bisa mencegat event penutupannya
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Tambahkan Listener saat jendela ditutup
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tutupHalaman(); // Panggil fungsi custom kita
            }
        });
        
        this.idServis = idServis;
        this.idTeknisi = Session.idUser;

        // Set Data ke Textfield
        txtIdService.setText(idServis);
        txtTglMasuk.setText(tglMasuk);
        txtNama.setText(nama);
        txtNohp.setText(noHp);
        txtAlamat.setText(alamat);
        txtJnsBarang.setText(jenis);
        txtMerek.setText(merek);
        txtModel.setText(model);
        txtNoSeri.setText(noSeri);
        txtKeluhan.setText(keluhan);
        txtKelengkapan.setText(kelengkapan);
        tStatus.setSelectedItem(status);
        txtIdService.setEditable(false);
        
        if ("Selesai".equalsIgnoreCase(status) || "Dibatalkan".equalsIgnoreCase(status)) {
            btSelesai.setVisible(false);
            btSimpan.setEnabled(false);
            btnHapusBrg.setEnabled(false);
            btPilih.setEnabled(false);
            btnEdit.setEnabled(false); // Matikan edit juga
        }

        loadPerbaikan(idServis); 
    }
    
    // --- FUNGSI BARU: CATAT PERUBAHAN STOK ---
    // Dipanggil setiap kali ada interaksi DB (Tambah/Hapus/Edit)
    private void catatLogStok(String kodeBarang, int perubahan) {
        // Ambil perubahan sebelumnya (jika ada)
        int currentVal = logPerubahanStok.getOrDefault(kodeBarang, 0);
        // Tambahkan dengan perubahan baru
        logPerubahanStok.put(kodeBarang, currentVal + perubahan);
    }
    
    // --- FUNGSI BARU: ROLLBACK / KEMBALIKAN STOK ---
    private void rollbackStok() {
        if (logPerubahanStok.isEmpty()) return; // Tidak ada yang perlu dikembalikan

        try {
            Connection conn = Koneksi.configDB();
            PreparedStatement ps = conn.prepareStatement("UPDATE tbl_barang SET stok = stok - ? WHERE kode_barang = ?");
            
            // Loop semua catatan di buku log
            for (Map.Entry<String, Integer> entry : logPerubahanStok.entrySet()) {
                String kode = entry.getKey();
                int perubahanDilakukan = entry.getValue();
                
                // Logika Pembalik:
                // Jika tadi stok berkurang (-5), maka dikurangi -5 = Ditambah 5 (Kembali)
                // Jika tadi stok bertambah (+5 / hapus item), maka dikurangi 5 (Balik ke awal)
                
                if (perubahanDilakukan != 0) {
                    ps.setInt(1, perubahanDilakukan);
                    ps.setString(2, kode);
                    ps.executeUpdate();
                }
            }
            System.out.println("Rollback stok berhasil dilakukan.");
            
        } catch (Exception e) {
            System.out.println("Gagal Rollback: " + e.getMessage());
        }
    }
    
    // --- FUNGSI BARU: LOGIKA TUTUP HALAMAN ---
    private void tutupHalaman() {
        if (!isDataSaved && !logPerubahanStok.isEmpty()) {
            // Jika belum disimpan TAPI ada perubahan stok -> Konfirmasi
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Anda belum menyimpan perubahan.\nData sparepart akan direset dan stok dikembalikan.\nLanjutkan keluar?", 
                "Konfirmasi Batal", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                rollbackStok(); // KEMBALIKAN STOK
                this.dispose();
            }
        } else {
            // Jika sudah save atau tidak ada perubahan, langsung tutup
            this.dispose();
        }
    }

    private void loadPerbaikan(String idServis) {
        try {
            String sql = "SELECT * FROM perbaikan WHERE id_servis=?";
            Connection conn = Koneksi.configDB();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, idServis);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentIdPerbaikan = rs.getInt("id_perbaikan");
                txtKerusakan.setText(rs.getString("kerusakan"));
                txtPerbaikan.setText(rs.getString("tindakan"));
                tBiayaJasa.setText(rs.getString("biaya_jasa"));
                loadSparepart(idServis);
            } else {
                currentIdPerbaikan = 0; 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load perbaikan: " + e.getMessage());
        }
    }
    
    private void loadSparepart(String idServis) {
        try {
            DefaultTableModel model = (DefaultTableModel) tblGanti.getModel();
            model.setRowCount(0);

            String sql = """
                SELECT b.kode_barang, b.nama_barang,
                       ps.qty, ps.harga, ps.subtotal
                FROM servis_sparepart ps
                JOIN tbl_barang b ON ps.id_sparepart = b.kode_barang
                WHERE ps.id_servis=?
            """;

            Connection conn = Koneksi.configDB();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, idServis); 

            ResultSet rs = ps.executeQuery();

            int no = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    no++,
                    rs.getString("kode_barang"),
                    rs.getString("nama_barang"),
                    rs.getInt("qty"),
                    rs.getInt("harga"),
                    rs.getInt("subtotal")
                });
            }
            hitungTotal();
        } catch (Exception e) {
             System.out.println("Error Load Sparepart: " + e.getMessage());
        }
    }

    // Dipanggil dari PopUp Sparepart
    public void tambahSparepart(String idBarang, String namaBarang, int harga, int qty) {
        DefaultTableModel model = (DefaultTableModel) tblGanti.getModel();
        boolean ditemukan = false;
        
        // --- CATAT PENGURANGAN STOK ---
        catatLogStok(idBarang, -qty); // Catat: Stok berkurang X

        for (int i = 0; i < model.getRowCount(); i++) {
            String idTabel = model.getValueAt(i, 1).toString();
            if (idTabel.equals(idBarang)) {
                int qtyLama = Integer.parseInt(model.getValueAt(i, 3).toString());
                int qtyBaru = qtyLama + qty;
                int subtotalBaru = qtyBaru * harga;

                model.setValueAt(qtyBaru, i, 3);
                model.setValueAt(subtotalBaru, i, 5);
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            int no = model.getRowCount() + 1;
            model.addRow(new Object[]{
                no,
                idBarang,
                namaBarang,
                qty,
                harga,
                harga * qty
            });
        }
        hitungTotal();
    }
    
    private void hitungTotal() {
        int total = 0;
        for (int i = 0; i < tblGanti.getRowCount(); i++) {
            total += Integer.parseInt(tblGanti.getValueAt(i, 5).toString());
        }
        tTotalBrg.setText(String.valueOf(total));
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtTglMasuk = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtJnsBarang = new javax.swing.JTextField();
        txtKelengkapan = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtIdService = new javax.swing.JTextField();
        txtKeluhan = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMerek = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtNohp = new javax.swing.JTextField();
        txtAlamat = new javax.swing.JTextField();
        txtModel = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtNoSeri = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtKerusakan = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtPerbaikan = new javax.swing.JTextArea();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblGanti = new javax.swing.JTable();
        btPilih = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        tTotalBrg = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        tBiayaJasa = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        tStatus = new javax.swing.JComboBox<>();
        btSimpan = new javax.swing.JButton();
        btSelesai = new javax.swing.JButton();
        btKembali = new javax.swing.JButton();
        btnHapusBrg = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 204, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Historic", 1, 36)); // NOI18N
        jLabel1.setText("Detail Service");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        txtTglMasuk.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel12.setText("No. Seri :");

        txtJnsBarang.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        txtKelengkapan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtKelengkapan.addActionListener(this::txtKelengkapanActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel6.setText("Jenis Barang :");

        jLabel13.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel13.setText("Kelengkapan :");

        jLabel8.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel8.setText("ID Service :");

        jLabel14.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel14.setText("Keluhan :");

        txtIdService.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        txtKeluhan.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel9.setText("Tanggal Masuk :");

        jLabel10.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel10.setText("Merek :");

        txtMerek.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtMerek.addActionListener(this::txtMerekActionPerformed);

        jLabel11.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel11.setText("Model :");

        jLabel3.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel3.setText("Nama :");

        txtNama.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel4.setText("No. HP :");

        txtNohp.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        txtAlamat.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtAlamat.addActionListener(this::txtAlamatActionPerformed);

        txtModel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        jLabel5.setText("Alamat :");

        txtNoSeri.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel8)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtAlamat, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                            .addComponent(txtNohp, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtNama, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtIdService)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtJnsBarang, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                            .addComponent(txtTglMasuk)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtKeluhan)
                            .addComponent(txtKelengkapan)
                            .addComponent(txtModel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtMerek)
                            .addComponent(txtNoSeri))))
                .addGap(16, 16, 16))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtNohp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtTglMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtJnsBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtMerek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtNoSeri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtKelengkapan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtKeluhan, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Hasil Pengecekan Teknisi");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Kerusakan :");

        txtKerusakan.setColumns(20);
        txtKerusakan.setRows(5);
        jScrollPane1.setViewportView(txtKerusakan);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel15.setText("Perbaikan :");

        txtPerbaikan.setColumns(20);
        txtPerbaikan.setRows(5);
        jScrollPane2.setViewportView(txtPerbaikan);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("Sparepart Diganti ");

        jLabel17.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        jLabel17.setText("Daftar Sparepart :");

        tblGanti.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Kode Barang", "Nama Barang", "Qty", "Harga", "Subtotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblGanti);
        if (tblGanti.getColumnModel().getColumnCount() > 0) {
            tblGanti.getColumnModel().getColumn(0).setResizable(false);
            tblGanti.getColumnModel().getColumn(0).setPreferredWidth(50);
            tblGanti.getColumnModel().getColumn(1).setResizable(false);
            tblGanti.getColumnModel().getColumn(2).setResizable(false);
            tblGanti.getColumnModel().getColumn(3).setResizable(false);
            tblGanti.getColumnModel().getColumn(4).setResizable(false);
            tblGanti.getColumnModel().getColumn(5).setResizable(false);
        }

        btPilih.setBackground(new java.awt.Color(204, 204, 204));
        btPilih.setFont(new java.awt.Font("Segoe UI Historic", 1, 14)); // NOI18N
        btPilih.setText("PIlih Barang");
        btPilih.addActionListener(this::btPilihActionPerformed);

        jLabel18.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        jLabel18.setText("Total Harga Sparepart :");

        tTotalBrg.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        tTotalBrg.setText("00000");

        jLabel20.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        jLabel20.setText("Biaya Jasa :");

        tBiayaJasa.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel21.setFont(new java.awt.Font("Segoe UI Historic", 0, 18)); // NOI18N
        jLabel21.setText("Status Service :");

        tStatus.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Proses", "Menunggu", "Selesai", "Dibatalkan", " " }));

        btSimpan.setBackground(new java.awt.Color(102, 255, 102));
        btSimpan.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        btSimpan.setText("Simpan Perbaikan");
        btSimpan.addActionListener(this::btSimpanActionPerformed);

        btSelesai.setBackground(new java.awt.Color(102, 255, 102));
        btSelesai.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        btSelesai.setText("Selesai Service");
        btSelesai.addActionListener(this::btSelesaiActionPerformed);

        btKembali.setBackground(new java.awt.Color(255, 51, 51));
        btKembali.setFont(new java.awt.Font("Segoe UI Historic", 1, 24)); // NOI18N
        btKembali.setText("Kembali");
        btKembali.addActionListener(this::btKembaliActionPerformed);

        btnHapusBrg.setBackground(new java.awt.Color(255, 51, 51));
        btnHapusBrg.setFont(new java.awt.Font("Segoe UI Historic", 1, 14)); // NOI18N
        btnHapusBrg.setText("Hapus Barang");
        btnHapusBrg.addActionListener(this::btnHapusBrgActionPerformed);

        btnEdit.setBackground(new java.awt.Color(255, 255, 102));
        btnEdit.setText("Edit");
        btnEdit.addActionListener(this::btnEditActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btSimpan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btSelesai)
                        .addGap(18, 18, 18)
                        .addComponent(btKembali))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel18)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tTotalBrg, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel20)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tBiayaJasa, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel17)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnHapusBrg, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btPilih, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(8, 8, 8))))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(9, 9, 9)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(btPilih)
                    .addComponent(btnHapusBrg)
                    .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(tTotalBrg)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(tBiayaJasa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(tStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSimpan)
                    .addComponent(btSelesai)
                    .addComponent(btKembali))
                .addContainerGap(488, Short.MAX_VALUE))
        );

        jScrollPane4.setViewportView(jPanel3);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 636, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 860, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btPilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPilihActionPerformed
        // TODO add your handling code here:
        pilihsparepart dialog = new pilihsparepart(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_btPilihActionPerformed

    private void btSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSimpanActionPerformed
        // TODO add your handling code here:
    Connection conn = null;
        try {
            conn = Koneksi.configDB();
            conn.setAutoCommit(false); 

            if (currentIdPerbaikan != 0) {
                String sqlUpdate = "UPDATE perbaikan SET kerusakan=?, tindakan=?, biaya_jasa=? WHERE id_servis=?";
                PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                ps.setString(1, txtKerusakan.getText());
                ps.setString(2, txtPerbaikan.getText());
                ps.setInt(3, Integer.parseInt(tBiayaJasa.getText()));
                ps.setString(4, idServis);
                ps.executeUpdate();
            } else {
                String sqlInsert = "INSERT INTO perbaikan (id_servis, id_teknisi, kerusakan, tindakan, biaya_jasa) VALUES (?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, idServis);
                ps.setString(2, Session.idUser);
                ps.setString(3, txtKerusakan.getText());
                ps.setString(4, txtPerbaikan.getText());
                ps.setInt(5, Integer.parseInt(tBiayaJasa.getText()));
                ps.executeUpdate();
                
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    currentIdPerbaikan = rs.getInt(1); 
                }
            }

            String sqlDel = "DELETE FROM servis_sparepart WHERE id_servis=?";
            PreparedStatement psDel = conn.prepareStatement(sqlDel);
            psDel.setString(1, idServis);
            psDel.executeUpdate();

            for (int i = 0; i < tblGanti.getRowCount(); i++) {
                String idBarang = tblGanti.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(tblGanti.getValueAt(i, 3).toString());
                int harga = Integer.parseInt(tblGanti.getValueAt(i, 4).toString());
                int subtotal = Integer.parseInt(tblGanti.getValueAt(i, 5).toString());

                String sql2 = "INSERT INTO servis_sparepart (id_servis, id_sparepart, qty, harga, subtotal) VALUES (?,?,?,?,?)";
                PreparedStatement ps2 = conn.prepareStatement(sql2);
                ps2.setString(1, idServis); 
                ps2.setString(2, idBarang);
                ps2.setInt(3, qty);
                ps2.setInt(4, harga);
                ps2.setInt(5, subtotal);
                ps2.executeUpdate();
            }

            String status = tStatus.getSelectedItem().toString();
            String sql3 = "UPDATE servis SET status=? WHERE id_servis=?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setString(1, status);
            ps3.setString(2, idServis);
            ps3.executeUpdate();

            conn.commit(); 
            
            // --- BERSIHKAN LOG KARENA SUDAH DISIMPAN ---
            isDataSaved = true;
            logPerubahanStok.clear(); 
            
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }//GEN-LAST:event_btSimpanActionPerformed

    private void btKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btKembaliActionPerformed
        // TODO add your handling code here:
        tutupHalaman();
    }//GEN-LAST:event_btKembaliActionPerformed

    private void btSelesaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSelesaiActionPerformed
        // TODO add your handling code here:
    int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin servis ini sudah selesai?\nStatus akan berubah permanen.",
            "Konfirmasi Selesai",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            tStatus.setSelectedItem("Selesai");
            btSimpanActionPerformed(evt); // Simpan dulu
            
            try {
                Connection conn = Koneksi.configDB();
                String sql = "UPDATE perbaikan SET tanggal_selesai = CURDATE() WHERE id_servis=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, idServis);
                ps.executeUpdate();
                
                // Pastikan isDataSaved true agar tidak rollback saat dispose
                isDataSaved = true; 
                this.dispose(); 
                
            } catch (Exception e) {
                System.out.println("Gagal update tanggal selesai: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btSelesaiActionPerformed

    private void btnHapusBrgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusBrgActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tblGanti.getModel();
        int row = tblGanti.getSelectedRow();
        
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang akan dihapus!");
            return;
        }
        
        String kodeBarang = model.getValueAt(row, 1).toString();
        int qtyKembali = Integer.parseInt(model.getValueAt(row, 3).toString());
        
        try {
            Connection conn = Koneksi.configDB();
            String sql = "UPDATE tbl_barang SET stok = stok + ? WHERE kode_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, qtyKembali);
            ps.setString(2, kodeBarang);
            ps.executeUpdate();
            
            // --- CATAT PENGEMBALIAN STOK ---
            catatLogStok(kodeBarang, qtyKembali); // Catat: Stok bertambah/kembali X
            
            model.removeRow(row);
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }
            hitungTotal();
            
        } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Gagal mengembalikan stok: " + e.getMessage());
        }
    }//GEN-LAST:event_btnHapusBrgActionPerformed

    private void txtKelengkapanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKelengkapanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKelengkapanActionPerformed

    private void txtAlamatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAlamatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAlamatActionPerformed

    private void txtMerekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMerekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMerekActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tblGanti.getModel();
        int row = tblGanti.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih sparepart yang ingin diedit jumlahnya!");
            return;
        }

        String kodeBarang = model.getValueAt(row, 1).toString();
        String namaBarang = model.getValueAt(row, 2).toString();
        int qtyLama = Integer.parseInt(model.getValueAt(row, 3).toString());
        int harga = Integer.parseInt(model.getValueAt(row, 4).toString());

        String inputBaru = JOptionPane.showInputDialog(this, 
                "Masukkan jumlah baru untuk " + namaBarang + ":", 
                String.valueOf(qtyLama));

        if (inputBaru != null && !inputBaru.isEmpty()) {
            try {
                int qtyBaru = Integer.parseInt(inputBaru);
                if (qtyBaru <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!");
                    return;
                }
                
                int selisih = qtyBaru - qtyLama; // Positif (Nambah), Negatif (Kurang)
                if (selisih == 0) return;

                Connection conn = Koneksi.configDB();
                
                // Validasi Stok (Jika nambah)
                if (selisih > 0) {
                    String sqlCek = "SELECT stok FROM tbl_barang WHERE kode_barang = ?";
                    PreparedStatement psCek = conn.prepareStatement(sqlCek);
                    psCek.setString(1, kodeBarang);
                    ResultSet rsCek = psCek.executeQuery();
                    if (rsCek.next()) {
                        int stokDb = rsCek.getInt("stok");
                        if (stokDb < selisih) {
                             JOptionPane.showMessageDialog(this, 
                                "Stok tidak cukup! Sisa: " + stokDb,
                                "Peringatan Stok", JOptionPane.WARNING_MESSAGE);
                             return; 
                        }
                    }
                }

                // Update DB
                String sqlUpdate = "UPDATE tbl_barang SET stok = stok - ? WHERE kode_barang = ?";
                PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                ps.setInt(1, selisih);
                ps.setString(2, kodeBarang);
                ps.executeUpdate();
                
                // --- CATAT EDIT STOK ---
                catatLogStok(kodeBarang, -selisih); // Ingat: selisih positif artinya stok berkurang (negatif)

                // Update Table
                int subtotalBaru = qtyBaru * harga;
                model.setValueAt(qtyBaru, row, 3);
                model.setValueAt(subtotalBaru, row, 5);
                hitungTotal();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnEditActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DetailService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new DetailService("","","","","","","","","","","","","").setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btKembali;
    private javax.swing.JButton btPilih;
    private javax.swing.JButton btSelesai;
    private javax.swing.JButton btSimpan;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapusBrg;
    private javax.swing.JButton jButton1;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField tBiayaJasa;
    private javax.swing.JComboBox<String> tStatus;
    private javax.swing.JLabel tTotalBrg;
    private javax.swing.JTable tblGanti;
    private javax.swing.JTextField txtAlamat;
    private javax.swing.JTextField txtIdService;
    private javax.swing.JTextField txtJnsBarang;
    private javax.swing.JTextField txtKelengkapan;
    private javax.swing.JTextField txtKeluhan;
    private javax.swing.JTextArea txtKerusakan;
    private javax.swing.JTextField txtMerek;
    private javax.swing.JTextField txtModel;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNoSeri;
    private javax.swing.JTextField txtNohp;
    private javax.swing.JTextArea txtPerbaikan;
    private javax.swing.JTextField txtTglMasuk;
    // End of variables declaration//GEN-END:variables
}
