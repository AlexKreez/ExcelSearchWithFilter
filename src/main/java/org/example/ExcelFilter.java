package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFilter {


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Введите наименование товара (или 'exit' для выхода):");
            String input = InputChange(scanner.nextLine());

            if (input.equalsIgnoreCase("exit")) {
                running = false;
                continue;
            }

            List<String> columnData = GetColumn();
            List<Integer> matchingRows = ColumnParse(input, columnData);

            System.out.println("Введите дополнительные фильтры (или 'exit' для выхода):");
            String filter = scanner.nextLine();

            if (filter.equalsIgnoreCase("exit")) {
                running = false;
                continue;
            }

            List<String> finalMatchingRows = TableSearch(matchingRows, filter);

            System.out.println("Строки, подходящие под условия: ");
            for (String rowContent : finalMatchingRows) {
                System.out.println(rowContent);
            }
        }

        scanner.close();
        System.out.println("Программа завершена.");
    }


    public static List<String> GetColumn() throws IOException {
        FileInputStream file = new FileInputStream("C:\\Users\\Алексей\\IdeaProjects\\Airport\\dataset.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<String> columnData = new ArrayList<>();

        for (Row row : sheet) {
            Cell cell = row.getCell(7);
            if (cell != null  && cell.getCellType() == CellType.STRING) {
                columnData.add(cell.getStringCellValue());
            }
        }

        workbook.close();
        file.close();
        return columnData;
    }


    public static String InputChange(String input) {

        return input;
    }


    public static List<Integer> ColumnParse(String input, List<String> columnData) {
        List<Integer> matchingRows = new ArrayList<>();
        for (int i = 0; i < columnData.size(); i++) {
            if (columnData.get(i).equalsIgnoreCase(input)) {
                System.out.println(i+2);
                matchingRows.add(i + 2);
            }
        }
        return matchingRows;
    }


    public static List<String> TableSearch(List<Integer> matchingRows, String filter) throws IOException {
        FileInputStream file = new FileInputStream("C:\\Users\\Алексей\\IdeaProjects\\Airport\\dataset.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Pattern pattern = Pattern.compile("column\\[(\\d+)] = '([^']+)'");
        List<String> matchingRowContents = new ArrayList<>();
        for (int rowIndex : matchingRows) {
            Row row = sheet.getRow(rowIndex - 1);
            if (row == null) continue;

            StringBuilder expressionForRow = new StringBuilder(filter);
            Matcher matcher = pattern.matcher(filter);

            while (matcher.find()) {
                int columnIndex = Integer.parseInt(matcher.group(1)) - 1;
                String expectedValue = matcher.group(2);

                Cell cell = row.getCell(columnIndex);
                boolean matchFound = false;

                if (cell != null) {
                    if (cell.getCellType() == CellType.STRING) {
                        matchFound = cell.getStringCellValue().equals(expectedValue);
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        matchFound = Integer.toString((int) cell.getNumericCellValue()).equals(expectedValue);
                    }
                }

                expressionForRow = new StringBuilder(expressionForRow.toString().replace(matcher.group(0), matchFound ? "1" : "0"));
            }
            if (parseFilter(expressionForRow.toString())) {
                matchingRowContents.add(row.getRowNum()+ "||" + getRowContent(row) + "||");
            }
        }

        workbook.close();
        file.close();

        return matchingRowContents;
    }

    private static String getRowContent(Row row) {
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


    public static boolean parseFilter(String input) {
        input = input.replaceAll("\\s+", "");
        Stack<Boolean> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '(') {
                operators.push(String.valueOf(ch));
            } else if (ch == ')') {
                while (!operators.peek().equals("(")) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            } else if (ch == '&') {
                operators.push("&");
            } else if (ch == '|' && i + 1 < input.length() && input.charAt(i + 1) == '|') {
                operators.push("||");
                i++;
            } else if (ch == '1' || ch == '0') {
                values.push(ch == '1');
            }
        }
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }
        return values.pop();
    }
    private static boolean applyOperator(String operator, boolean b, boolean a) {
        return switch (operator) {
            case "&" -> a && b;
            case "||" -> a || b;
            default -> false;
        };
    }



}
