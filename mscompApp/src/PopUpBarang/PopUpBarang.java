/*
 * PopUpBarang.java
 * Class ini menangani jendela pop-up untuk memilih barang
 */
package mscompapp;

import config.Koneksi;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyEvent;

public class PopUpBarang extends javax.swing.JFrame {
    
    // Variabel untuk menyimpan data yang dipilih
    public PKelStok stokForm = null;

    public PopUpBarang() {
        initComponents();
        load_table();
    }

    private void load_table() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Kode Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Kategori");
        model.addColumn("Harga");
        model.addColumn("Stok");

        try {
            String sql = "SELECT * FROM tbl_barang";
            java.sql.Connection conn = (java.sql.Connection) Koneksi.configDB();
            java.sql.ResultSet res = conn.createStatement().executeQuery(sql);
            while (res.next()) {
                model.addRow(new Object[]{
                    res.getString(1), res.getString(2), 
                    res.getString(3), res.getString(4), res.getString(5)
                });
            }
            tblPilih.setModel(model);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Kode GUI Manual (karena kamu copy paste file ini)
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPilih = new javax.swing.JTable();
        txtCari = new javax.swing.JTextField();
        btnCari = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pilih Data Barang");
        
        tblPilih.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Kode", "Nama", "Kategori", "Harga", "Stok"}
        ));
        // Event saat baris tabel diklik
        tblPilih.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelKlik(evt);
            }
        });
        jScrollPane1.setViewportView(tblPilih);

        btnCari.setText("Cari");

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
        setLocationRelativeTo(null); // Muncul di tengah
    }

    // Method saat baris diklik
    private void tabelKlik(java.awt.event.MouseEvent evt) {
        int baris = tblPilih.getSelectedRow();
        if (stokForm != null && baris != -1) {
            // Mengirim data balik ke form PKelStok
            stokForm.itemTerpilih(
                tblPilih.getValueAt(baris, 0).toString(), // Kode
                tblPilih.getValueAt(baris, 1).toString(), // Nama
                tblPilih.getValueAt(baris, 2).toString(), // Kategori
                tblPilih.getValueAt(baris, 3).toString()  // Harga
            );
            this.dispose(); // Tutup PopUp
        }
    }

    private javax.swing.JButton btnCari;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPilih;
    private javax.swing.JTextField txtCari;
}