package mscompapp;

import config.Koneksi;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import javax.swing.JOptionPane;

public class CetakTandaTerima implements Printable {

    private String idServis;

    public CetakTandaTerima(String idServis) {
        this.idServis = idServis;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Layout dipadatkan agar hanya mengisi 1/3 kertas B5
        int y = 15;
        int x = 0;
        int width = 430; 

        try {
            Connection conn = Koneksi.configDB();
            String sql = "SELECT s.*, p.nama_pelanggan, p.no_hp FROM servis s " +
                         "JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                         "WHERE s.id_servis = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idServis);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Header
                g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2d.drawString("MS COMPUTER - TANDA TERIMA SERVIS", x, y); y += 12;
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
                g2d.drawString("Jl. Contoh Alamat No. 123 | Telp: 08123456789", x, y); y += 5;
                g2d.drawLine(x, y, width, y); y += 12;

                // Info Utama
                g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2d.drawString("No. Servis : " + rs.getString("id_servis"), x, y);
                g2d.drawString("Tanggal: " + rs.getString("tanggal_masuk"), 280, y); y += 12;
                
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2d.drawString("Pelanggan  : " + rs.getString("nama_pelanggan"), x, y);
                g2d.drawString("No. HP : " + rs.getString("no_hp"), 280, y); y += 12;
                g2d.drawLine(x, y, width, y); y += 12;

                // Detail Perangkat (Model & Seri Dipisah)
                g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2d.drawString("DATA PERANGKAT:", x, y); y += 10;
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2d.drawString("- Jenis/Merk : " + rs.getString("jenis_barang") + " / " + rs.getString("merek"), x, y); y += 10;
                g2d.drawString("- Model      : " + rs.getString("model"), x, y); y += 10;
                g2d.drawString("- No. Seri   : " + rs.getString("no_seri"), x, y); y += 10;
                g2d.drawString("- Kelengkapan: " + rs.getString("kelengkapan"), x, y); y += 12;

                // Keluhan
                g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2d.drawString("KELUHAN:", x, y); y += 10;
                g2d.setFont(new Font("Monospaced", Font.ITALIC, 9));
                g2d.drawString(rs.getString("keluhan_awal"), x + 5, y); y += 25;

                // Footer (Tanda Tangan Kosong untuk isi manual)
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
                g2d.drawLine(x, y, width, y); y += 10;
                g2d.drawString("Hormat Kami,", 45, y);
                g2d.drawString("Pelanggan,", 305, y); y += 35;
                
                // Nama dihapus, diganti garis titik-titik
                g2d.drawString("(....................)", 25, y); 
                g2d.drawString("(....................)", 285, y); y += 12;
                
                g2d.setFont(new Font("Monospaced", Font.ITALIC, 7));
                g2d.drawString("* Nota ini adalah bukti sah penerimaan barang servis.", x, y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PAGE_EXISTS;
    }

    // Metode pemanggil (Printer vs PDF)
    public static void cetakTandaTerima(String idServis) {
        Object[] options = {"Printer / Struk", "Simpan PDF", "Batal"};
        int choice = JOptionPane.showOptionDialog(null, 
                "Pilih Metode Cetak Tanda Terima:", 
                "Opsi Cetak", 
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (choice == JOptionPane.CANCEL_OPTION || choice == -1) return;

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("TandaTerima_" + idServis);
        
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        double width = 499; // B5
        double height = 709; 
        paper.setSize(width, height);
        // Margin tetap, tapi isi hanya 1/3 tinggi
        paper.setImageableArea(36, 36, width - 72, height - 72);
        pf.setPaper(paper);

        job.setPrintable(new CetakTandaTerima(idServis), pf);

        if (choice == 0 || choice == 1) { 
            if (job.printDialog()) {
                try { job.print(); } catch (PrinterException e) { e.printStackTrace(); }
            }
        }
    }
}