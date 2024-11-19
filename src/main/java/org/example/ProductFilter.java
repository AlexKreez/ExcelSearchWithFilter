package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductFilter {
    private final ExcelReader ExcelReader;
    private final FilterParser filterParser;

    public ProductFilter(ExcelReader handler) {
        this.ExcelReader = handler;
        this.filterParser = new FilterParser();
    }

    public List<String> findMatchingProducts(String productName, String filter) throws IOException {
        List<String> columnData = ExcelReader.getColumnData(7);
        List<Integer> matchingRows = new ArrayList<>();

        for (int i = 0; i < columnData.size(); i++) {
            if (columnData.get(i).equalsIgnoreCase(productName)) {
                matchingRows.add(i + 2);
            }
        }

        List<String> matchingRowContents = new ArrayList<>();
        for (int rowIndex : matchingRows) {
            if (filterParser.isMatch(ExcelReader.getRow(rowIndex - 1), filter)) {
                matchingRowContents.add(rowIndex + " || " + ExcelReader.getRowContent(rowIndex - 1));
            }
        }
        return matchingRowContents;
    }
}
