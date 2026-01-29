/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mscompapp;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportExcel {

    public static void exportJTableToExcel(JTable table) {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan File Excel");
            chooser.setSelectedFile(new File("data_servis.xlsx"));

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Data Servis");

                // ===== Header =====
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(table.getColumnName(col));
                }

                // ===== Data =====
                for (int row = 0; row < table.getRowCount(); row++) {
                    Row excelRow = sheet.createRow(row + 1);
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        Cell cell = excelRow.createCell(col);
                        Object value = table.getValueAt(row, col);
                        cell.setCellValue(value == null ? "" : value.toString());
                    }
                }

                // Auto size kolom
                for (int i = 0; i < table.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile());
                workbook.write(fos);
                fos.close();
                workbook.close();

                JOptionPane.showMessageDialog(null, "Export Excel berhasil!");

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal export: " + e.getMessage());
        }
    }
}
