/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    // Menambahkan parameter bulanText dan tahun untuk keperluan Judul & Nama File
    public static void exportJTableToExcel(JTable table, String bulanText, int tahun) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Laporan Excel");
            
            // --- 1. FORMAT NAMA FILE DEFAULT ---
            // Format: Laporan_Servis_Bulan_Tahun.xlsx
            String defaultFileName = "Laporan_Servis_" + bulanText + "_" + tahun + ".xlsx";
            chooser.setSelectedFile(new File(defaultFileName));
            
            // Filter hanya file Excel
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx");
            chooser.setFileFilter(filter);

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                
                // --- 2. LOGIKA PENAMAAN OTOMATIS (ANTI TIMPA) ---
                File file = chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                
                // Pastikan ekstensi .xlsx
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    file = new File(filePath + ".xlsx");
                }

                // Cek jika file sudah ada, tambahkan angka (1), (2), dst
                String parent = file.getParent();
                String name = file.getName();
                String baseName = name.substring(0, name.lastIndexOf("."));
                String extension = ".xlsx";
                
                int counter = 1;
                while (file.exists()) {
                    String newName = baseName + "(" + counter + ")" + extension;
                    file = new File(parent, newName);
                    counter++;
                }

                // --- MULAI MEMBUAT EXCEL ---
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Data Servis");

                // --- 3. MEMBUAT JUDUL LAPORAN (Baris 0) ---
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("LAPORAN SERVIS BULAN " + bulanText.toUpperCase() + " TAHUN " + tahun);
                
                // Style Judul (Tengah, Bold, Font Besar)
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleCell.setCellStyle(titleStyle);
                
                // Merge Cells untuk Judul (Dari kolom 0 sampai kolom 14)
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 14));

                // --- 4. HEADER TABEL (Baris 2 - Baris 1 dikosongkan untuk spasi) ---
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

                // --- 5. DATA DATABASE & LOOPING ---
                Connection conn = Koneksi.configDB();
                String sqlTambahan = "SELECT s.model, s.no_seri, u.nama AS nama_teknisi " +
                                     "FROM servis s " +
                                     "LEFT JOIN perbaikan p ON s.id_servis = p.id_servis " +
                                     "LEFT JOIN tbl_user u ON p.id_teknisi = u.id_user " +
                                     "WHERE s.id_servis = ?";
                PreparedStatement ps = conn.prepareStatement(sqlTambahan);

                // Looping Data (Mulai baris ke-3 Excel karena 0=Judul, 1=Kosong, 2=Header)
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
                    
                    // Isi Data
                    createCell(excelRow, 0, table.getValueAt(row, 0)); 
                    createCell(excelRow, 1, table.getValueAt(row, 1)); 
                    createCell(excelRow, 2, table.getValueAt(row, 2)); 
                    createCell(excelRow, 3, table.getValueAt(row, 3)); 
                    createCell(excelRow, 4, table.getValueAt(row, 4)); 
                    createCell(excelRow, 5, table.getValueAt(row, 5)); 
                    createCell(excelRow, 6, table.getValueAt(row, 6)); 
                    createCell(excelRow, 7, table.getValueAt(row, 7)); 
                    
                    createCell(excelRow, 8, dbModel);   
                    createCell(excelRow, 9, dbNoSeri);  
                    
                    createCell(excelRow, 10, table.getValueAt(row, 8)); 
                    createCell(excelRow, 11, table.getValueAt(row, 9)); 
                    
                    createCell(excelRow, 12, dbTeknisi); 
                    
                    createCell(excelRow, 13, table.getValueAt(row, 10)); 
                    createCell(excelRow, 14, table.getValueAt(row, 11)); 
                }

                // Auto size kolom
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Tulis file ke 'file' yang sudah divalidasi/rename tadi
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