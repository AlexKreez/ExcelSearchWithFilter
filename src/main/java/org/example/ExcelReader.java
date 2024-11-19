package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader implements AutoCloseable {
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;

    public ExcelReader(String filePath) throws IOException {
        FileInputStream file = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(file);
        sheet = workbook.getSheetAt(0);
    }

    public List<String> getColumnData(int columnIndex) {
        List<String> columnData = new ArrayList<>();
        for (Row row : sheet) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                columnData.add(cell.getStringCellValue());
            }
        }
        return columnData;
    }

    public String getRowContent(int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return "NULL";

        StringBuilder rowContent = new StringBuilder();
        for (Cell cell : row) {
            if (cell.getCellType() == CellType.STRING) {
                rowContent.append(cell.getStringCellValue()).append("\t");
            } else if (cell.getCellType() == CellType.NUMERIC) {
                rowContent.append(cell.getNumericCellValue()).append("\t");
            } else {
                rowContent.append("NULL").append("\t");
            }
        }
        return rowContent.toString().trim();
    }

    public Row getRow(int rowIndex) {
        return sheet.getRow(rowIndex);
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
