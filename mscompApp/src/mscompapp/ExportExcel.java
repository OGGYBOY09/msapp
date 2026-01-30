package mscompapp;

import config.Koneksi;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportExcel {

    // --- METHOD LAMA (UNTUK LAPORAN BULANAN) - TETAP ADA AGAR LAP BULANAN TIDAK ERROR ---
    public static void exportJTableToExcel(JTable table, String bulanText, int tahun) {
        String judul = "LAPORAN SERVIS BULAN " + bulanText.toUpperCase() + " TAHUN " + tahun;
        String filename = "Laporan_Servis_" + bulanText + "_" + tahun;
        generateExcel(table, judul, filename);
    }

    // --- METHOD BARU (UNTUK LAPORAN MINGGUAN / CUSTOM) ---
    public static void exportJTableToExcelCustom(JTable table, String judulLaporan, String suffixFilename) {
        // suffixFilename misal: "Mingguan_01-01-2026_sd_07-01-2026"
        String filename = "Laporan_Servis_" + suffixFilename;
        generateExcel(table, judulLaporan, filename);
    }

    // --- LOGIKA INTI PEMBUATAN EXCEL (DIPISAH AGAR LEBIH RAPI) ---
    private static void generateExcel(JTable table, String judulLaporan, String defaultFileName) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Laporan Excel");
            
            chooser.setSelectedFile(new File(defaultFileName + ".xlsx"));
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx");
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                
                File file = chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    file = new File(filePath + ".xlsx");
                }

                // Auto Rename jika file ada
                String parent = file.getParent();
                String name = file.getName();
                String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
                String extension = ".xlsx";
                
                int counter = 1;
                while (file.exists()) {
                    file = new File(parent, baseName + "(" + counter + ")" + extension);
                    counter++;
                }

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Data Servis");

                // --- HEADER JUDUL ---
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(judulLaporan.toUpperCase()); // Judul dari Parameter
                
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleCell.setCellStyle(titleStyle);
                
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 14));

                // --- HEADER TABEL ---
                Row headerRow = sheet.createRow(2);
                String[] headers = {
                    "No", "ID Servis", "Tanggal Masuk", "Nama Pelanggan", "Nomor HP", 
                    "Alamat", "Jenis Barang", "Merek", "Model/Tipe", "Nomor Seri", 
                    "Keluhan", "Kelengkapan", "Nama Teknisi", "Total Biaya", "Status"
                };

                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // --- ISI DATA (DATABASE) ---
                Connection conn = Koneksi.configDB();
                String sqlTambahan = "SELECT s.model, s.no_seri, u.nama AS nama_teknisi " +
                                     "FROM servis s " +
                                     "LEFT JOIN perbaikan p ON s.id_servis = p.id_servis " +
                                     "LEFT JOIN tbl_user u ON p.id_teknisi = u.id_user " +
                                     "WHERE s.id_servis = ?";
                PreparedStatement ps = conn.prepareStatement(sqlTambahan);

                int startRow = 3; 
                
                for (int row = 0; row < table.getRowCount(); row++) {
                    Row excelRow = sheet.createRow(startRow + row);
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
                        if (teknisi != null && !teknisi.isEmpty()) dbTeknisi = teknisi;
                    }
                    
                    // Mapping Data (Pastikan index kolom tabel GUI sesuai)
                    createCell(excelRow, 0, table.getValueAt(row, 0)); 
                    createCell(excelRow, 1, table.getValueAt(row, 1)); 
                    createCell(excelRow, 2, table.getValueAt(row, 2)); // Tgl Masuk
                    createCell(excelRow, 3, table.getValueAt(row, 3)); 
                    createCell(excelRow, 4, table.getValueAt(row, 4)); 
                    createCell(excelRow, 5, table.getValueAt(row, 5)); 
                    createCell(excelRow, 6, table.getValueAt(row, 6)); 
                    createCell(excelRow, 7, table.getValueAt(row, 7)); // Merek
                    
                    createCell(excelRow, 8, dbModel);   
                    createCell(excelRow, 9, dbNoSeri);  
                    
                    createCell(excelRow, 10, table.getValueAt(row, 8)); // Keluhan
                    createCell(excelRow, 11, table.getValueAt(row, 9)); // Kelengkapan
                    
                    createCell(excelRow, 12, dbTeknisi); 
                    
                    createCell(excelRow, 13, table.getValueAt(row, 10)); // Total Biaya
                    createCell(excelRow, 14, table.getValueAt(row, 11)); // Status
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                workbook.close();

                JOptionPane.showMessageDialog(null, "Laporan berhasil disimpan di:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal export: " + e.getMessage());
        }
    }
    
    private static void createCell(Row row, int colIndex, Object value) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue("");
        }
    }
}