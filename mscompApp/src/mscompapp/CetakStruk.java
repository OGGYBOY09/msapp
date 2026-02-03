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
    private String idAdmin; // Simpan ID Admin untuk query nama nanti
    
    // --- DATA-DATA SESUAI PDF ---
    private String tglCetak;
    private String namaAdminStr = "-";
    private String namaTeknisi = "-";
    private String namaPelanggan = "-";
    
    // Detail Perangkat
    private String perangkat = "-";
    private String noSeri = "-";
    private String keluhan = "-";
    
    // Hasil Perbaikan
    private String kerusakan = "-";
    private String tindakan = "-";
    
    // Keuangan
    private int biayaJasa = 0;
    private int diskon = 0;
    
    // List Sparepart
    private List<String[]> listSparepart = new ArrayList<>(); 
    
    public CetakStruk(String idServis, String idAdmin) {
        this.idServis = idServis;
        this.idAdmin = idAdmin;
        
        // Format Tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.tglCetak = sdf.format(new Date());
        
        loadData();
    }
    
    private void loadData() {
        try {
            Connection conn = Koneksi.configDB();
            
            // 1. AMBIL NAMA ADMIN
            PreparedStatement psAdm = conn.prepareStatement("SELECT nama FROM tbl_user WHERE id_user=?");
            psAdm.setString(1, idAdmin);
            ResultSet rsAdm = psAdm.executeQuery();
            if(rsAdm.next()) this.namaAdminStr = rsAdm.getString("nama");
            
            // 2. AMBIL DATA SERVIS & PELANGGAN
            String sqlServis = "SELECT s.*, p.nama_pelanggan, p.no_hp " +
                               "FROM servis s JOIN tbl_pelanggan p ON s.id_pelanggan = p.id_pelanggan " +
                               "WHERE s.id_servis = ?";
            PreparedStatement psSrv = conn.prepareStatement(sqlServis);
            psSrv.setString(1, idServis);
            ResultSet rsSrv = psSrv.executeQuery();
            
            if(rsSrv.next()) {
                this.namaPelanggan = rsSrv.getString("nama_pelanggan");
                this.perangkat = rsSrv.getString("jenis_barang") + " " + rsSrv.getString("merek") + " " + rsSrv.getString("model");
                this.noSeri = rsSrv.getString("no_seri");
                this.keluhan = rsSrv.getString("keluhan_awal");
            }
            
            // 3. AMBIL DATA PERBAIKAN (Teknisi, Kerusakan, Tindakan, Biaya, Diskon)
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
            }
            
            // 4. AMBIL DATA SPAREPART
            String sqlPart = "SELECT b.nama_barang, ss.qty, ss.harga, ss.subtotal " +
                             "FROM servis_sparepart ss " +
                             "JOIN tbl_barang b ON ss.id_sparepart = b.kode_barang " +
                             "WHERE ss.id_servis = ?";
            PreparedStatement psPart = conn.prepareStatement(sqlPart);
            psPart.setString(1, idServis);
            ResultSet rsPart = psPart.executeQuery();
            
            while(rsPart.next()) {
                listSparepart.add(new String[]{
                    rsPart.getString("nama_barang"),
                    rsPart.getString("qty"),
                    rsPart.getString("harga"),
                    rsPart.getString("subtotal")
                });
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g = (Graphics2D) graphics;
        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // --- KONFIGURASI FONT ---
        Font fontHeader = new Font("Monospaced", Font.BOLD, 10);
        Font fontRegular = new Font("Monospaced", Font.PLAIN, 8);
        Font fontBold = new Font("Monospaced", Font.BOLD, 8);
        
        int y = 10;
        int x = 5;
        int width = 190; // Lebar area cetak (sesuaikan dengan kertas thermal)
        
        // --- 1. HEADER UTAMA ---
        g.setFont(fontHeader);
        centerText(g, "MS COMPUTER SERVICE", width, y); y += 10;
        
        g.setFont(fontRegular);
        centerText(g, "Jl. Contoh No. 123, Pekalongan", width, y); y += 10;
        centerText(g, "Telp: 0812-3456-7890", width, y); y += 15;
        
        drawLine(g, x, width, y); y += 10;
        
        // --- 2. INFORMASI DASAR (Sesuai PDF) ---
        g.drawString("No. Servis: " + idServis, x, y); y += 10;
        g.drawString("Tanggal   : " + tglCetak, x, y); y += 10;
        g.drawString("Kasir     : " + namaAdminStr, x, y); y += 10;
        g.drawString("Teknisi   : " + (namaTeknisi!=null?namaTeknisi:"-"), x, y); y += 10;
        g.drawString("Pelanggan : " + namaPelanggan, x, y); y += 15;
        
        drawLine(g, x, width, y); y += 10;
        
        // --- 3. DETAIL PERANGKAT ---
        g.setFont(fontBold);
        g.drawString("DETAIL PERANGKAT", x, y); y += 10;
        g.setFont(fontRegular);
        
        g.drawString("Perangkat : " + perangkat, x, y); y += 10;
        g.drawString("No. Seri  : " + noSeri, x, y); y += 10;
        
        g.drawString("Keluhan Awal :", x, y); y += 10;
        // Gunakan MultiLine agar keluhan panjang tidak terpotong
        y = drawMultiLine(g, keluhan, x + 5, width - 10, y); y += 5;
        
        drawLine(g, x, width, y); y += 10;
        
        // --- 4. HASIL PERBAIKAN ---
        g.setFont(fontBold);
        g.drawString("HASIL PERBAIKAN", x, y); y += 10;
        g.setFont(fontRegular);
        
        g.drawString("Kerusakan (Teknisi) :", x, y); y += 10;
        y = drawMultiLine(g, kerusakan, x + 5, width - 10, y); y += 5;
        
        g.drawString("Tindakan :", x, y); y += 10;
        y = drawMultiLine(g, tindakan, x + 5, width - 10, y); y += 10;
        
        drawLine(g, x, width, y); y += 10;
        
        // --- 5. RINCIAN BIAYA ---
        g.setFont(fontBold);
        g.drawString("RINCIAN BIAYA", x, y); y += 12;
        g.setFont(fontRegular);
        
        int totalSparepart = 0;
        DecimalFormat df = new DecimalFormat("#,###");
        
        // A. SPAREPART
        if(!listSparepart.isEmpty()){
            for(String[] s : listSparepart){
                String nama = s[0];
                int qty = Integer.parseInt(s[1]);
                int hrg = Integer.parseInt(s[2]);
                int sub = Integer.parseInt(s[3]);
                totalSparepart += sub;
                
                // Nama Barang
                g.drawString(nama, x, y); y += 10;
                
                // Qty x Harga ...... Subtotal
                String calc = qty + " x " + df.format(hrg);
                g.drawString(calc, x + 10, y);
                
                String strSub = df.format(sub);
                drawRightAligned(g, strSub, width, y);
                y += 10;
            }
        }
        
        // B. JASA SERVIS
        g.drawString("Jasa Servis", x, y);
        String strJasa = df.format(biayaJasa);
        drawRightAligned(g, strJasa, width, y); 
        y += 10;
        
        // C. DISKON (FITUR BARU)
        if(diskon > 0){
            g.drawString("Diskon", x, y);
            String strDiskon = "-" + df.format(diskon);
            drawRightAligned(g, strDiskon, width, y); 
            y += 10;
        }
        
        drawLine(g, x, width, y); y += 12;
        
        // D. TOTAL TAGIHAN
        int grandTotal = totalSparepart + biayaJasa - diskon;
        g.setFont(fontBold);
        g.drawString("TOTAL TAGIHAN", x, y);
        String strTotal = "Rp " + df.format(grandTotal);
        drawRightAligned(g, strTotal, width, y); 
        y += 20;
        
        // --- 6. FOOTER ---
        g.setFont(fontRegular);
        centerText(g, "GARANSI SERVIS 1 MINGGU", width, y); y += 10;
        centerText(g, "(Syarat & Ketentuan Berlaku)", width, y); y += 15;
        centerText(g, "TERIMA KASIH", width, y);
        
        return PAGE_EXISTS;
    }
    
    // --- HELPER METHODS ---
    
    private void centerText(Graphics2D g, String text, int width, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (width - textWidth) / 2, y);
    }
    
    private void drawRightAligned(Graphics2D g, String text, int width, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, width - textWidth, y);
    }
    
    private void drawLine(Graphics2D g, int x, int width, int y) {
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        Stroke old = g.getStroke();
        g.setStroke(dashed);
        g.drawLine(x, y, width, y);
        g.setStroke(old);
    }
    
    // Fungsi Penting: Memecah teks panjang menjadi beberapa baris
    private int drawMultiLine(Graphics2D g, String text, int x, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        String currentLine = "";
        int lineHeight = fm.getHeight();
        
        for (String word : words) {
            // Cek jika ditambah kata baru apakah melebihi lebar
            if (fm.stringWidth(currentLine + word) < width) {
                currentLine += word + " ";
            } else {
                g.drawString(currentLine, x, y);
                y += lineHeight; // Pindah baris
                currentLine = word + " ";
            }
        }
        // Cetak sisa kata terakhir
        if (!currentLine.isEmpty()) {
            g.drawString(currentLine, x, y);
            y += lineHeight;
        }
        return y; // Kembalikan posisi Y terakhir
    }
    
    // STATIC CALL
    public static void cetakStruk(String idServis, String idAdmin) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk_" + idServis);
        job.setPrintable(new CetakStruk(idServis, idAdmin));
        
        if(job.printDialog()){
            try { job.print(); } 
            catch (PrinterException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        }
    }
}