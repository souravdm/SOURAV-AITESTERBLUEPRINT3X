package com.restfulbooker.api.utils;

import com.restfulbooker.api.exceptions.FrameworkException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads test data from Excel (.xlsx) files using Apache POI.
 * The first row is treated as the header row.
 */
public final class ExcelDataProvider {

    private ExcelDataProvider() {}

    /**
     * Reads all rows from the specified sheet and returns a list of maps.
     * Keys are column headers; values are cell values as strings.
     *
     * @param filePath   absolute or relative path to the .xlsx file
     * @param sheetName  name of the worksheet to read
     * @return list of row data maps
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new FrameworkException("Sheet not found: " + sheetName + " in " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            int colCount  = headerRow.getLastCellNum();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int c = 0; c < colCount; c++) {
                    String header = getCellValue(headerRow.getCell(c));
                    String value  = getCellValue(row.getCell(c));
                    rowData.put(header, value);
                }
                rows.add(rowData);
            }
        } catch (IOException e) {
            throw new FrameworkException("Failed to read Excel file: " + filePath, e);
        }
        return rows;
    }

    /**
     * Converts the sheet data to a 2-D Object array suitable for TestNG @DataProvider.
     */
    public static Object[][] toDataProviderArray(String filePath, String sheetName) {
        List<Map<String, String>> rows = readSheet(filePath, sheetName);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default      -> "";
        };
    }
}
