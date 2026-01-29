/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

public class CetakStruk implements Printable {

    private String idServis;
    private String namaAdmin;
    
    // Data-data yang akan dicetak (disimpan di variabel global class agar bisa dibaca method print)
    private String tglCetak;
    private String namaPelanggan;
    private String noHp;
    private String namaTeknisi;
    
    private String jenis, merek, model, seri, keluhan;
    private String status;
    
    // Data Perbaikan (Bisa null jika status Menunggu)
    private String kerusakanTeknisi = "-";
    private String tindakan = "-";
    private int biayaJasa = 0;
    
    // List Sparepart
    private List<String[]> listSparepart = new ArrayList<>(); // [Nama, Qty, Harga, Subtotal]
    
    // Font Setting
    private Font fontRegular = new Font("Monospaced", Font.PLAIN, 8);
    private Font fontBold = new Font("Monospaced", Font.BOLD, 8);
    private Font fontHeader = new Font("Monospaced", Font.BOLD, 10);

    public CetakStruk(String idServis, String idAdmin) {
        this.idServis = idServis;
        this.tglCetak = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
        
        // Ambil data dari database saat class dibuat
        loadData(idAdmin);
    }

    private void loadData(String idAdmin) {
        Connection conn = null;
        try {
            conn = Koneksi.configDB();
            
            // 1. AMBIL NAMA ADMIN (YANG LOGIN)
            PreparedStatement psAdmin = conn.prepareStatement("SELECT nama FROM tbl_user WHERE id_user = ?");
            psAdmin.setString(1, idAdmin);
            ResultSet rsAdmin = psAdmin.executeQuery();
            if(rsAdmin.next()) this.namaAdmin = rsAdmin.getString("nama");
            else this.namaAdmin = "Admin";

            // 2. AMBIL DATA SERVIS & PELANGGAN
            String sqlServis = "SELECT s.*, p.nama_pelanggan, p.no_hp " +
                               "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                               "WHERE s.id_servis = ?";
            PreparedStatement psServis = conn.prepareStatement(sqlServis);
            psServis.setString(1, idServis);
            ResultSet rsServis = psServis.executeQuery();
            
            if (rsServis.next()) {
                this.namaPelanggan = rsServis.getString("nama_pelanggan");
                this.noHp = rsServis.getString("no_hp");
                this.jenis = rsServis.getString("jenis_barang");
                this.merek = rsServis.getString("merek");
                this.model = rsServis.getString("model");
                this.seri = rsServis.getString("no_seri");
                this.keluhan = rsServis.getString("keluhan_awal");
                this.status = rsServis.getString("status");
                
                // Cek apakah data model/seri kosong
                if(model == null) model = "-";
                if(seri == null) seri = "-";
            }

            // 3. AMBIL DATA PERBAIKAN & TEKNISI (LEFT JOIN KARENA BISA JADI BELUM ADA)
            String sqlPerbaikan = "SELECT p.*, u.nama AS nama_teknisi " +
                                  "FROM perbaikan p LEFT JOIN tbl_user u ON p.id_teknisi = u.id_user " +
                                  "WHERE p.id_servis = ?";
            PreparedStatement psPerbaikan = conn.prepareStatement(sqlPerbaikan);
            psPerbaikan.setString(1, idServis);
            ResultSet rsPerbaikan = psPerbaikan.executeQuery();
            
            if (rsPerbaikan.next()) {
                this.namaTeknisi = rsPerbaikan.getString("nama_teknisi");
                this.kerusakanTeknisi = rsPerbaikan.getString("kerusakan");
                this.tindakan = rsPerbaikan.getString("tindakan");
                this.biayaJasa = rsPerbaikan.getInt("biaya_jasa");
            } else {
                this.namaTeknisi = "-";
            }

            // 4. AMBIL DATA SPAREPART (Hanya jika ada perbaikan)
            String sqlPart = "SELECT s.qty, s.harga, s.subtotal, b.nama_barang " +
                             "FROM servis_sparepart s JOIN tbl_barang b ON s.id_sparepart = b.kode_barang " +
                             "WHERE s.id_servis = ?";
            PreparedStatement psPart = conn.prepareStatement(sqlPart);
            psPart.setString(1, idServis);
            ResultSet rsPart = psPart.executeQuery();
            
            while(rsPart.next()){
                String[] part = {
                    rsPart.getString("nama_barang"),
                    rsPart.getString("qty"),
                    rsPart.getString("harga"),
                    rsPart.getString("subtotal")
                };
                listSparepart.add(part);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- FUNGSI UTAMA MENGGAMBAR STRUK ---
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        int y = 10; // Posisi Y awal
        int x = 5;  // Margin kiri
        int width = (int) pageFormat.getImageableWidth() - 10;
        int lineHeight = 10;

        // 1. HEADER TOKO
        g2d.setFont(fontHeader);
        drawCenter(g2d, "MS COMPUTER SERVICE", width, y); y += 12;
        g2d.setFont(fontRegular);
        drawCenter(g2d, "Jl. Contoh No. 123, Kota Anda", width, y); y += 10;
        drawCenter(g2d, "Telp: 0812-3456-7890", width, y); y += 15;
        drawLine(g2d, x, width, y); y += 12;

        // 2. INFO TRANSAKSI (Admin & Teknisi Dipisah)
        g2d.setFont(fontRegular);
        g2d.drawString("No. Servis : " + idServis, x, y); y += lineHeight;
        g2d.drawString("Tanggal    : " + tglCetak, x, y); y += lineHeight;
        g2d.drawString("Admin      : " + namaAdmin, x, y); y += lineHeight; // FITUR BARU
        g2d.drawString("Teknisi    : " + namaTeknisi, x, y); y += lineHeight;
        g2d.drawString("Pelanggan  : " + namaPelanggan, x, y); y += lineHeight;
        drawLine(g2d, x, width, y); y += 12;

        // 3. DETAIL PERANGKAT
        g2d.setFont(fontBold);
        g2d.drawString("DETAIL PERANGKAT", x, y); y += lineHeight;
        g2d.setFont(fontRegular);
        g2d.drawString("Perangkat : " + merek + " " + model, x, y); y += lineHeight;
        g2d.drawString("No. Seri  : " + seri, x, y); y += lineHeight;
        
        g2d.drawString("Keluhan Awal:", x, y); y += lineHeight;
        y = drawMultiLine(g2d, keluhan, x + 5, width - 5, y); // Multi-line text
        y += 5;

        // 4. LOGIKA KONDISIONAL BERDASARKAN STATUS
        // Jika status Menunggu, Dibatalkan, atau Data Perbaikan kosong
        boolean isProcessed = (status.equalsIgnoreCase("Selesai") || status.equalsIgnoreCase("Proses")) && biayaJasa > 0;
        
        if (!isProcessed) {
            // TAMPILAN JIKA BELUM DIPERBAIKI
            drawLine(g2d, x, width, y); y += 15;
            g2d.setFont(fontBold);
            drawCenter(g2d, "*** STATUS: " + status.toUpperCase() + " ***", width, y); y += 12;
            g2d.setFont(fontRegular);
            drawCenter(g2d, "Perangkat Masih Menunggu /", width, y); y += 10;
            drawCenter(g2d, "Belum Ada Tindakan Perbaikan", width, y); y += 15;
            
        } else {
            // TAMPILAN JIKA SUDAH DIPERBAIKI
            drawLine(g2d, x, width, y); y += 12;
            
            // A. HASIL PERBAIKAN (FITUR BARU: Kerusakan & Tindakan)
            g2d.setFont(fontBold);
            g2d.drawString("HASIL PERBAIKAN", x, y); y += lineHeight;
            g2d.setFont(fontRegular);
            
            g2d.drawString("Kerusakan (Teknisi):", x, y); y += lineHeight;
            y = drawMultiLine(g2d, kerusakanTeknisi, x + 5, width - 5, y);
            
            g2d.drawString("Tindakan:", x, y); y += lineHeight;
            y = drawMultiLine(g2d, tindakan, x + 5, width - 5, y);
            y += 5;
            
            drawLine(g2d, x, width, y); y += 12;

            // B. RINCIAN BIAYA
            g2d.setFont(fontBold);
            g2d.drawString("RINCIAN BIAYA", x, y); y += lineHeight;
            g2d.setFont(fontRegular);
            
            int total = 0;
            DecimalFormat df = new DecimalFormat("#,###");

            // Loop Sparepart
            for (String[] item : listSparepart) {
                String nama = item[0];
                int qty = Integer.parseInt(item[1]);
                int hrg = Integer.parseInt(item[2]);
                int sub = Integer.parseInt(item[3]);
                total += sub;
                
                g2d.drawString("- " + nama, x, y); y += lineHeight;
                // Format: 2 x 50.000      100.000
                String rincian = qty + " x " + df.format(hrg);
                String subText = df.format(sub);
                
                g2d.drawString("  " + rincian, x, y);
                drawRight(g2d, subText, width, y); 
                y += lineHeight;
            }

            // Biaya Jasa
            g2d.drawString("- Jasa Servis", x, y); 
            drawRight(g2d, df.format(biayaJasa), width, y); 
            y += lineHeight;
            total += biayaJasa;

            drawLine(g2d, x, width, y); y += 12;
            
            // C. TOTAL
            g2d.setFont(fontBold);
            g2d.drawString("TOTAL TAGIHAN", x, y);
            drawRight(g2d, "Rp " + df.format(total), width, y); 
            y += lineHeight + 5;
        }
        
        // 5. FOOTER
        drawLine(g2d, x, width, y); y += 12;
        g2d.setFont(fontRegular);
        drawCenter(g2d, "GARANSI SERVIS 1 MINGGU", width, y); y += 10;
        drawCenter(g2d, "(Syarat & Ketentuan Berlaku)", width, y); y += 15;
        drawCenter(g2d, "Terima Kasih", width, y);

        return PAGE_EXISTS;
    }
    
    // --- METHOD BANTUAN UNTUK GAMBAR ---
    
    private void drawCenter(Graphics2D g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
    
    private void drawRight(Graphics2D g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = width - fm.stringWidth(text);
        g.drawString(text, x, y);
    }
    
    private void drawLine(Graphics2D g, int x, int width, int y) {
        g.drawLine(x, y, width, y); // Garis horizontal sederhana
    }
    
    // Method untuk memecah teks panjang menjadi beberapa baris (Word Wrap)
    private int drawMultiLine(Graphics2D g, String text, int x, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        String currentLine = "";
        int lineHeight = fm.getHeight();
        
        for (String word : words) {
            if (fm.stringWidth(currentLine + word) < width) {
                currentLine += word + " ";
            } else {
                g.drawString(currentLine, x, y);
                y += lineHeight;
                currentLine = word + " ";
            }
        }
        if (!currentLine.isEmpty()) {
            g.drawString(currentLine, x, y);
            y += lineHeight;
        }
        return y;
    }
    
    // Fungsi Static untuk Memanggil Print dari Luar
    public static void cetakStruk(String idServis, String idAdmin) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk Servis " + idServis);
        
        job.setPrintable(new CetakStruk(idServis, idAdmin));
        
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Gagal Mencetak: " + e.getMessage());
            }
        }
    }
}