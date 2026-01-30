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

    // --- METHOD LAMA (BULANAN) ---
    public static void exportToPDF(JTable table, String bulanText, int tahun, String statusFilter) {
        String judul = "LAPORAN SERVIS BULAN " + bulanText.toUpperCase() + " TAHUN " + tahun;
        String filename = "Laporan_Servis_" + bulanText + "_" + tahun;
        generatePDF(table, judul, filename, statusFilter);
    }

    // --- METHOD BARU (CUSTOM/MINGGUAN) ---
    public static void exportToPDFCustom(JTable table, String judulLaporan, String statusFilter, String suffixFilename) {
        String filename = "Laporan_Servis_" + suffixFilename;
        generatePDF(table, judulLaporan, filename, statusFilter);
    }

    // --- LOGIKA INTI PDF ---
    private static void generatePDF(JTable table, String judulLaporan, String defaultFileName, String statusFilter) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Laporan PDF");
            chooser.setSelectedFile(new File(defaultFileName + ".pdf"));

            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents", "pdf");
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                
                File file = chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    file = new File(filePath + ".pdf");
                }
                
                // Auto Rename
                String parent = file.getParent();
                String name = file.getName();
                String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
                int counter = 1;
                while (file.exists()) {
                    file = new File(parent, baseName + "(" + counter + ").pdf");
                    counter++;
                }

                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // --- JUDUL ---
                Font fontJudul = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                Font fontSub = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                
                Paragraph pJudul = new Paragraph(judulLaporan.toUpperCase(), fontJudul); // Judul dari parameter
                pJudul.setAlignment(Element.ALIGN_CENTER);
                document.add(pJudul);
                
                Paragraph pStatus = new Paragraph(statusFilter, fontSub);
                pStatus.setAlignment(Element.ALIGN_CENTER);
                document.add(pStatus);
                
                document.add(new Paragraph(" "));

                // --- TABEL ---
                PdfPTable pdfTable = new PdfPTable(15);
                pdfTable.setWidthPercentage(100); 
                float[] columnWidths = {3f, 6f, 7f, 8f, 7f, 8f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 7f, 6f};
                pdfTable.setWidths(columnWidths);

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

                // --- ISI DATA ---
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
                    
                    // Pastikan urutan ini sesuai dengan kolom di tabel GUI
                    addCell(pdfTable, table.getValueAt(row, 0), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 1), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 2), fontIsi); // Tanggal
                    addCell(pdfTable, table.getValueAt(row, 3), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 4), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 5), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 6), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 7), fontIsi); // Merek
                    
                    addCell(pdfTable, dbModel, fontIsi);
                    addCell(pdfTable, dbNoSeri, fontIsi);
                    
                    addCell(pdfTable, table.getValueAt(row, 8), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 9), fontIsi);
                    
                    addCell(pdfTable, dbTeknisi, fontIsi);
                    
                    addCell(pdfTable, table.getValueAt(row, 10), fontIsi);
                    addCell(pdfTable, table.getValueAt(row, 11), fontIsi);
                }
                
                document.add(pdfTable);
                
                // --- FOOTER ---
                document.add(new Paragraph(" "));
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
    
    private static void addCell(PdfPTable table, Object text, Font font) {
        String val = (text == null) ? "" : text.toString();
        PdfPCell cell = new PdfPCell(new Phrase(val, font));
        cell.setPadding(3);
        table.addCell(cell);
    }
}