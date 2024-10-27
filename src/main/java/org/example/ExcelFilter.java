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

    // Метод для чтения данных из Excel и проверки условий
    public static String InputChange(String input) throws IOException {
        FileInputStream file = new FileInputStream("C:/Users/Алексей/IdeaProjects/Airport/TryThis.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Pattern pattern = Pattern.compile("column\\[(\\d+)\\] = '([^']+)'");

        List<Integer> matchingRowNumbers = new ArrayList<>();

        // Проходим по каждому условию
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            int columnIndex = Integer.parseInt(matcher.group(1)) - 1; // Преобразуем индекс
            String expectedValue = matcher.group(2);
            int matchingRowNumber = 0;
            // Проверяем совпадения в таблице
            boolean matchFound = false;
            for (Row row : sheet) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    System.out.println("Значение ячейки: " + cellValue);
                    System.out.println("Значение из фильтра: " + expectedValue);
                    if (cellValue.equals(expectedValue)) {
                        matchFound = true;
                        matchingRowNumbers.add(row.getRowNum() + 1);
                        break;
                    }
                }
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    int cellValue = (int) cell.getNumericCellValue();
                    System.out.println("Значение ячейки: " + cellValue);
                    System.out.println("Значение из фильтра: " + expectedValue);
                    if (cellValue == Integer.parseInt(expectedValue)) {
                        System.out.println("working!!!!!!!!!");
                        matchFound = true;
                        matchingRowNumbers.add(row.getRowNum() + 1);
                        break;
                    }
                }
            }
            // Заменяем условие в строке на 1 или 0
            input = input.replace(matcher.group(0), matchFound ? "1" : "0");


        }

        workbook.close();
        file.close();
        if (!matchingRowNumbers.isEmpty()) {
            System.out.println("Строки, соответствующие условиям: " + matchingRowNumbers);
        } else {
            System.out.println("Совпадений не найдено.");
        }
        return input;
    }

    // Оставшийся код остается без изменений
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

        String processedInput = InputChange(input);
        System.out.println("Преобразованная строка: " + processedInput);
        boolean result = parseFilter(processedInput);
        System.out.println("Результат: " + result);

        scanner.close();
    }
}
