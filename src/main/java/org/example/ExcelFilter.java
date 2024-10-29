package org.example;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFilter {

    public static List<Integer> InputChange(String input) throws IOException {
        FileInputStream file = new FileInputStream("C:\\Users\\Алексей\\IdeaProjects\\Airport\\TryThis.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Pattern pattern = Pattern.compile("column\\[(\\d+)\\] = '([^']+)'");
        List<Integer> matchingRowNumbers = new ArrayList<>();

        for (Row row : sheet) {
            StringBuilder expressionForRow = new StringBuilder(input); // Копия строки

            // Перемещение условиям из инпута
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                int columnIndex = Integer.parseInt(matcher.group(1)) - 1; // Индекс столбца
                String expectedValue = matcher.group(2);

                Cell cell = row.getCell(columnIndex);
                boolean matchFound = false;

                // Проверка
                if (cell != null) {
                    if (cell.getCellType() == CellType.STRING) {
                        matchFound = cell.getStringCellValue().equals(expectedValue);
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        matchFound = Integer.toString((int) cell.getNumericCellValue()).equals(expectedValue);
                    }
                }

                // Заменяем на "1" или "0" в строке
                expressionForRow = new StringBuilder(expressionForRow.toString().replace(matcher.group(0), matchFound ? "1" : "0"));
            }

            // Если выражение для строки вычисляется как true, добавляем номер строки
            if (parseFilter(expressionForRow.toString())) {
                matchingRowNumbers.add(row.getRowNum() + 1);
            }
        }

        workbook.close();
        file.close();

        return matchingRowNumbers;
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
        switch (operator) {
            case "&": return a && b;
            case "||": return a || b;
            default: return false;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите значение для проверки:");
        String input = scanner.nextLine();

        List<Integer> matchingRows = InputChange(input);
        System.out.println("Строки, подходящие под условия: " + matchingRows);

        scanner.close();
    }
}
