package org.example;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class FilterParser {

    public boolean parseExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        Stack<Boolean> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == '(') {
                operators.push(String.valueOf(ch));
            } else if (ch == ')') {
                while (!operators.peek().equals("(")) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop();
            } else if (ch == '&') {
                operators.push("&");
            } else if (ch == '|' && i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
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

    private boolean applyOperator(String operator, boolean b, boolean a) {
        return switch (operator) {
            case "&" -> a && b;
            case "||" -> a || b;
            default -> false;
        };
    }

    public boolean isMatch(Row row, String filter) {
        StringBuilder expressionForRow = new StringBuilder(filter);
        Pattern pattern = Pattern.compile("column\\[(\\d+)] = '([^']+)'");
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
        return parseExpression(expressionForRow.toString());
    }
}
