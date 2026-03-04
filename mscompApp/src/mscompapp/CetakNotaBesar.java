package mscompapp;

import config.Koneksi;
import java.awt.*;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

public class CetakNotaBesar implements Printable {

    private String idServis;
    private String idAdmin;
    private String tglCetak;
    private String tglMasuk = "-";
    private String tglSelesai = "-";
    private String namaAdminStr = "-";
    private String namaTeknisi = "-";
    private String namaPelanggan = "-";
    private String noHpPelanggan = "-";
    private String alamatPelanggan = "-";
    
    private String perangkat = "-";
    private String noSeri = "-";
    private String kelengkapan = "-";
    private String keluhan = "-";
    private String kerusakan = "-";
    private String tindakan = "-";
    
    private int biayaJasa = 0;
    private int diskon = 0;
    private List<String[]> listSparepart = new ArrayList<>(); 

    public CetakNotaBesar(String idServis, String idAdmin) {
        this.idServis = idServis;
        this.idAdmin = idAdmin;
        this.tglCetak = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        loadData();
    }

    private void loadData() {
        try {
            Connection conn = Koneksi.configDB();
            
            // Ambil Nama Admin
            PreparedStatement psAdm = conn.prepareStatement("SELECT nama FROM tbl_user WHERE id_user=?");
            psAdm.setString(1, idAdmin);
            ResultSet rsAdm = psAdm.executeQuery();
            if(rsAdm.next()) this.namaAdminStr = rsAdm.getString("nama");
            
            // Ambil Data Servis & Pelanggan
            String sqlServis = "SELECT s.*, p.nama_pelanggan, p.no_hp, p.alamat " +
                               "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                               "WHERE s.id_servis = ?";
            PreparedStatement psSrv = conn.prepareStatement(sqlServis);
            psSrv.setString(1, idServis);
            ResultSet rsSrv = psSrv.executeQuery();
            if(rsSrv.next()) {
                this.namaPelanggan = rsSrv.getString("nama_pelanggan");
                this.noHpPelanggan = rsSrv.getString("no_hp");
                this.alamatPelanggan = rsSrv.getString("alamat");
                this.tglMasuk = rsSrv.getString("tanggal_masuk");
                this.perangkat = rsSrv.getString("jenis_barang") + " " + rsSrv.getString("merek") + " " + rsSrv.getString("model");
                this.noSeri = rsSrv.getString("no_seri");
                this.kelengkapan = rsSrv.getString("kelengkapan");
                this.keluhan = rsSrv.getString("keluhan_awal");
            }
            
            // Ambil Data Perbaikan
            String sqlFix = "SELECT p.*, u.nama AS nm_teknisi FROM perbaikan p " +
                            "LEFT JOIN tbl_user u ON p.id_teknisi = u.id_user " +
                            "WHERE p.id_servis = ?";
            PreparedStatement psFix = conn.prepareStatement(sqlFix);
            psFix.setString(1, idServis);
            ResultSet rsFix = psFix.executeQuery();
            if(rsFix.next()) {
                this.namaTeknisi = rsFix.getString("nm_teknisi");
                this.kerusakan = rsFix.getString("kerusakan");
                this.tindakan = rsFix.getString("tindakan");
                this.biayaJasa = rsFix.getInt("biaya_jasa");
                this.diskon = rsFix.getInt("diskon");
                this.tglSelesai = rsFix.getString("tanggal_selesai") != null ? rsFix.getString("tanggal_selesai") : "-";
            }
            
            // Ambil Data Sparepart
            String sqlPart = "SELECT b.nama_barang, ss.qty, ss.harga, ss.subtotal " +
                             "FROM servis_sparepart ss JOIN tbl_barang b ON ss.id_sparepart = b.kode_barang " +
                             "WHERE ss.id_servis = ?";
            PreparedStatement psPart = conn.prepareStatement(sqlPart);
            psPart.setString(1, idServis);
            ResultSet rsPart = psPart.executeQuery();
            while(rsPart.next()) {
                listSparepart.add(new String[]{ rsPart.getString("nama_barang"), rsPart.getString("qty"), rsPart.getString("harga"), rsPart.getString("subtotal") });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g = (Graphics2D) graphics;
        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Menggunakan font Monospaced agar terlihat seperti struk
        String fontType = "Monospaced";
        int x = 50; int y = 50; int width = 500;
        DecimalFormat df = new DecimalFormat("#,###");

        // Header
        g.setFont(new Font(fontType, Font.BOLD, 18));
        g.drawString("MS COMPUTER SERVICE", x, y); y += 20;
        g.setFont(new Font(fontType, Font.PLAIN, 10));
        g.drawString("Jl. Contoh No. 123, Pekalongan | Telp: 0812-3456-7890", x, y); y += 30;
        g.setStroke(new BasicStroke(1)); g.drawLine(x, y, x + width, y); y += 30;

        // Info Dokumen & Pelanggan (2 Kolom)
        g.setFont(new Font(fontType, Font.BOLD, 11));
        g.drawString("INVOICE SERVIS: " + idServis, x, y); 
        g.drawString("DATA PELANGGAN", x + 300, y); y += 20;
        
        g.setFont(new Font(fontType, Font.PLAIN, 9));
        g.drawString("Tgl. Masuk   : " + tglMasuk, x, y);
        g.drawString("Nama    : " + namaPelanggan, x + 300, y); y += 15;
        g.drawString("Tgl. Selesai : " + tglSelesai, x, y);
        g.drawString("No. HP  : " + noHpPelanggan, x + 300, y); y += 15;
        g.drawString("Admin/Kasir  : " + namaAdminStr, x, y);
        g.drawString("Alamat  : " + alamatPelanggan, x + 300, y); y += 15;
        g.drawString("Tgl. Cetak   : " + tglCetak, x, y); y += 40;

        // Detail Perangkat
        g.setFont(new Font(fontType, Font.BOLD, 10));
        g.drawString("RINCIAN PERANGKAT & KELUHAN", x, y); y += 15;
        g.setFont(new Font(fontType, Font.PLAIN, 9));
        g.drawString("Perangkat   : " + perangkat, x, y); y += 15;
        g.drawString("No. Seri    : " + noSeri, x, y); y += 15;
        g.drawString("Kelengkapan : " + kelengkapan, x, y); y += 15;
        g.drawString("Keluhan     : " + keluhan, x, y); y += 30;

        // Hasil Perbaikan
        g.setFont(new Font(fontType, Font.BOLD, 10));
        g.drawString("HASIL PENGECEKAN & TINDAKAN", x, y); y += 15;
        g.setFont(new Font(fontType, Font.PLAIN, 9));
        g.drawString("Teknisi     : " + namaTeknisi, x, y); y += 15;
        g.drawString("Kerusakan   : " + kerusakan, x, y); y += 15;
        g.drawString("Tindakan    : " + tindakan, x, y); y += 35;

        // Tabel Biaya
        g.setFont(new Font(fontType, Font.BOLD, 9));
        g.drawRect(x, y, width, 20);
        g.drawString("No", x + 5, y + 14); g.drawString("Deskripsi Item (Sparepart / Jasa)", x + 35, y + 14);
        g.drawString("Qty", x + 305, y + 14); g.drawString("Harga", x + 355, y + 14); g.drawString("Subtotal", x + 445, y + 14);
        y += 20; g.setFont(new Font(fontType, Font.PLAIN, 9));

        int no = 1; int totalSparepart = 0;
        for (String[] s : listSparepart) {
            g.drawRect(x, y, width, 20);
            g.drawString(String.valueOf(no++), x + 5, y + 14);
            g.drawString(s[0], x + 35, y + 14);
            g.drawString(s[1], x + 305, y + 14);
            g.drawString(df.format(Integer.parseInt(s[2])), x + 355, y + 14);
            g.drawString(df.format(Integer.parseInt(s[3])), x + 445, y + 14);
            totalSparepart += Integer.parseInt(s[3]); y += 20;
        }
        
        // Jasa Servis
        g.drawRect(x, y, width, 20);
        g.drawString(String.valueOf(no), x + 5, y + 14);
        g.drawString("Jasa Perbaikan / Servis", x + 35, y + 14);
        g.drawString("1", x + 305, y + 14);
        g.drawString(df.format(biayaJasa), x + 355, y + 14);
        g.drawString(df.format(biayaJasa), x + 445, y + 14); y += 40;

        // Summary
        int grandTotal = totalSparepart + biayaJasa - diskon;
        g.setFont(new Font(fontType, Font.BOLD, 11));
        g.drawString("TOTAL BIAYA : Rp " + df.format(grandTotal), x + 300, y);
        if(diskon > 0) { 
            y += 15; 
            g.setFont(new Font(fontType, Font.ITALIC, 9)); 
            g.drawString("(Potongan Diskon: Rp " + df.format(diskon) + ")", x + 300, y); 
        }

        // Bagian Tanda Tangan Tanpa Nama Cetak
        y += 80;
        g.setFont(new Font(fontType, Font.PLAIN, 9));
        g.drawString("Nama & Tanda Tangan Pelanggan,", x + 30, y);
        g.drawString("Nama & Tanda Tangan Kasir/Admin,", x + 330, y);
        y += 60;
        g.drawString("(..........................)", x + 30, y);
        g.drawString("(..........................)", x + 330, y);

        return PAGE_EXISTS;
    }

    public static void cetakNotaA4(String idServis, String idAdmin) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Nota_Besar_" + idServis);
        job.setPrintable(new CetakNotaBesar(idServis, idAdmin));
        if (job.printDialog()) {
            try { 
                job.print(); 
            } catch (PrinterException e) { 
                JOptionPane.showMessageDialog(null, "Gagal Mencetak: " + e.getMessage()); 
            }
        }
    }
}