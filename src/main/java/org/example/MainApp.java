package org.example;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        try (ExcelReader ExcelReader = new ExcelReader("C:\\Users\\Алексей\\IdeaProjects\\Airport\\dataset.xlsx")) {
            ProductFilter productFilter = new ProductFilter(ExcelReader);
            Scanner scanner = new Scanner(System.in);

            boolean running = true;
            while (running) {
                System.out.println("Введите наименование товара (или 'exit' для выхода):");
                String productName = scanner.nextLine().trim();
                if (productName.equalsIgnoreCase("exit")) {
                    running = false;
                    continue;
                }

                System.out.println("Введите дополнительные фильтры (или 'exit' для выхода):");
                String filter = scanner.nextLine().trim();
                if (filter.equalsIgnoreCase("exit")) {
                    running = false;
                    continue;
                }

                List<String> results = productFilter.findMatchingProducts(productName, filter);
                System.out.println("Строки, подходящие под условия:");
                results.forEach(System.out::println);
            }
            scanner.close();
            System.out.println("Программа завершена.");
        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом Excel: " + e.getMessage());
        }
    }
}
