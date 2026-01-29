/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mscompapp;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import config.Koneksi;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ExportPDF {

    public static void exportToPDF(JTable table, String bulanText, int tahun, String statusFilter) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Laporan PDF");

            // 1. Format Nama File Default
            String defaultFileName = "Laporan_Servis_" + bulanText + "_" + tahun + ".pdf";
            chooser.setSelectedFile(new File(defaultFileName));

            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents", "pdf");
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                
                // --- 2. LOGIKA AUTO RENAME (ANTI TIMPA) ---
                File file = chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    file = new File(filePath + ".pdf");
                }
                
                // Cek duplikat dan rename otomatis (misal: Laporan(1).pdf)
                String parent = file.getParent();
                String name = file.getName();
                
                // Handle jika file tidak punya ekstensi atau titik
                String baseName;
                if (name.contains(".")) {
                    baseName = name.substring(0, name.lastIndexOf("."));
                } else {
                    baseName = name;
                }
                
                String extension = ".pdf";
                int counter = 1;
                while (file.exists()) {
                    String newName = baseName + "(" + counter + ")" + extension;
                    file = new File(parent, newName);
                    counter++;
                }

                // --- 3. MULAI MEMBUAT DOKUMEN PDF ---
                // Menggunakan Landscape agar muat banyak kolom
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // --- 4. JUDUL & SUB-JUDUL ---
                Font fontJudul = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                Font fontSub = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                
                Paragraph pJudul = new Paragraph("LAPORAN SERVIS BULAN " + bulanText.toUpperCase() + " TAHUN " + tahun, fontJudul);
                pJudul.setAlignment(Element.ALIGN_CENTER);
                document.add(pJudul);
                
                // Keterangan Status (Sesuai Permintaan)
                Paragraph pStatus = new Paragraph(statusFilter, fontSub); // Misal: "Menampilkan Data: Semua Status"
                pStatus.setAlignment(Element.ALIGN_CENTER);
                document.add(pStatus);
                
                document.add(new Paragraph(" ")); // Spasi Kosong

                // --- 5. PERSIAPAN TABEL PDF ---
                // Total 15 Kolom
                PdfPTable pdfTable = new PdfPTable(15);
                pdfTable.setWidthPercentage(100); 
                // Lebar relatif kolom (Agar rapi)
                float[] columnWidths = {3f, 6f, 7f, 8f, 7f, 8f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 7f, 6f};
                pdfTable.setWidths(columnWidths);

                // --- 6. HEADER TABEL ---
                String[] headers = {
                    "No", "ID", "Tgl Masuk", "Pelanggan", "HP", 
                    "Alamat", "Jenis", "Merek", "Model", "Seri", 
                    "Keluhan", "Kelengkapan", "Teknisi", "Biaya", "Status"
                };
                
                Font fontHeader = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }

                // --- 7. ISI DATA (HYBRID: TABLE GUI + DATABASE) ---
                Connection conn = Koneksi.configDB();
                String sqlTambahan = "SELECT s.model, s.no_seri, u.nama AS nama_teknisi " +
                                     "FROM servis s " +
                                     "LEFT JOIN perbaikan p ON s.id_servis = p.id_servis " +
                                     "LEFT JOIN tbl_user u ON p.id_teknisi = u.id_user " +
                                     "WHERE s.id_servis = ?";
                PreparedStatement ps = conn.prepareStatement(sqlTambahan);
                
                Font fontIsi = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

                for (int row = 0; row < table.getRowCount(); row++) {
                    String idServis = table.getValueAt(row, 1).toString();
                    
                    // Ambil Data DB
                    String dbModel = "-";
                    String dbNoSeri = "-";
                    String dbTeknisi = "Belum diperbaiki";
                    
                    ps.setString(1, idServis);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()){
                        dbModel = rs.getString("model");
                        dbNoSeri = rs.getString("no_seri");
                        String teknisi = rs.getString("nama_teknisi");
                        if(teknisi != null && !teknisi.isEmpty()) dbTeknisi = teknisi;
                    }
                    
                    // Masukkan ke Sel PDF
                    addCell(pdfTable, table.getValueAt(row, 0), fontIsi); // No
                    addCell(pdfTable, table.getValueAt(row, 1), fontIsi); // ID
                    addCell(pdfTable, table.getValueAt(row, 2), fontIsi); // Tgl (GUI)
                    addCell(pdfTable, table.getValueAt(row, 3), fontIsi); // Nama
                    addCell(pdfTable, table.getValueAt(row, 4), fontIsi); // HP
                    addCell(pdfTable, table.getValueAt(row, 5), fontIsi); // Alamat
                    addCell(pdfTable, table.getValueAt(row, 6), fontIsi); // Jenis
                    addCell(pdfTable, table.getValueAt(row, 7), fontIsi); // Merek
                    
                    addCell(pdfTable, dbModel, fontIsi);   // DB Model
                    addCell(pdfTable, dbNoSeri, fontIsi);  // DB Seri
                    
                    addCell(pdfTable, table.getValueAt(row, 8), fontIsi); // Keluhan
                    addCell(pdfTable, table.getValueAt(row, 9), fontIsi); // Kelengkapan
                    
                    addCell(pdfTable, dbTeknisi, fontIsi); // DB Teknisi
                    
                    addCell(pdfTable, table.getValueAt(row, 10), fontIsi); // Biaya
                    addCell(pdfTable, table.getValueAt(row, 11), fontIsi); // Status
                }
                
                document.add(pdfTable);
                
                // --- 8. FOOTER: TANGGAL CETAK ---
                document.add(new Paragraph(" ")); // Spasi
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                String tglCetak = sdf.format(new Date());
                
                Font fontFooter = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                Paragraph footer = new Paragraph("Laporan ini dicetak pada tanggal: " + tglCetak, fontFooter);
                footer.setAlignment(Element.ALIGN_RIGHT);
                document.add(footer);

                document.close();
                JOptionPane.showMessageDialog(null, "PDF Berhasil Disimpan:\n" + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal export PDF: " + e.getMessage());
        }
    }
    
    // Helper agar tidak error saat data null
    private static void addCell(PdfPTable table, Object text, Font font) {
        String val = (text == null) ? "" : text.toString();
        PdfPCell cell = new PdfPCell(new Phrase(val, font));
        cell.setPadding(3);
        table.addCell(cell);
    }
}