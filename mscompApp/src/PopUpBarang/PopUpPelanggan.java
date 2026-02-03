/*
 * PopUpPelanggan.java
 */
package mscompapp;

import config.Koneksi;
import javax.swing.table.DefaultTableModel;

public class PopUpPelanggan extends javax.swing.JFrame {

    public PKelService serviceForm = null;

    public PopUpPelanggan() {
        initComponents();
        load_table();
    }

    private void load_table() {
DefaultTableModel model = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // SEMUA KOLOM TIDAK BISA DIEDIT
    }};        model.addColumn("ID");
        model.addColumn("Nama Pelanggan");
        model.addColumn("No Telp");
        model.addColumn("Alamat");

        try {
            // Mengambil data dari tbl_pelanggan
            String sql = "SELECT * FROM tbl_pelanggan";
            String cari = txtCari.getText();
            if (!cari.isEmpty()) {
                sql = "SELECT * FROM tbl_pelanggan WHERE nama_pelanggan LIKE '%" + cari + "%' OR no_telp LIKE '%" + cari + "%'";
            }

            java.sql.Connection conn = (java.sql.Connection) Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            
            while (res.next()) {
                model.addRow(new Object[]{
                    res.getString("id_pelanggan"),
                    res.getString("nama_pelanggan"),
                    res.getString("no_hp"),
                    res.getString("alamat")
                });
            }
            tblPilih.setModel(model);
        } catch (Exception e) {
            System.out.println("Error Load Pelanggan: " + e.getMessage());
        }
    }

    // --- KODE GUI GENERATED (Disederhanakan untuk Copy-Paste) ---
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPilih = new javax.swing.JTable();
        txtCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pilih Pelanggan");

        tblPilih.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Nama", "No HP", "Alamat"}
        ));
        tblPilih.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPilihMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPilih);

        btnCari.setText("Cari");
        btnCari.addActionListener(evt -> load_table());
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                load_table();
            }
        });

        // Layout sederhana
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtCari)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCari)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCari))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addContainerGap())
        );
        pack();
        setLocationRelativeTo(null);
    }

    private void tblPilihMouseClicked(java.awt.event.MouseEvent evt) {
        int baris = tblPilih.getSelectedRow();
        if (serviceForm != null && baris != -1) {
            // Mengirim data balik ke PKelService
            String id = tblPilih.getValueAt(baris, 0).toString();
            String nama = tblPilih.getValueAt(baris, 1).toString();
            String hp = tblPilih.getValueAt(baris, 2).toString();
            String alamat = tblPilih.getValueAt(baris, 3).toString();
            
            serviceForm.pelangganTerpilih(id, nama, hp, alamat);
            this.dispose();
        }
    }

    private javax.swing.JButton btnCari;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPilih;
    private javax.swing.JTextField txtCari;
}