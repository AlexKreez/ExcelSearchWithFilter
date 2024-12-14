package org.example;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CsvToDatabase {
    public static void main(String[] args) {
        String csvFile = "dataset.csv";
        String jdbcURL = "jdbc:postgresql://localhost:5432/postgres";
        String username = "postgres";
        String password = "123";
        String tableName = "invtable";

        try {
            List<String[]> data = readCsv(csvFile);

            try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
                System.out.println("Connected to database.");

                insertDataIntoTable(connection, tableName, data);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    public static List<String[]> readCsv(String csvFile) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "windows-1251"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(";");
                data.add(values);
            }
        }
        return data;
    }

    public static void insertDataIntoTable(Connection connection, String tableName, List<String[]> data) throws SQLException {
        int header = 0;
        String[] columns = data.get(header);
        data.remove(header);

        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append("\"").append(columns[i].trim()).append("\"");
            if (i < columns.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append("?");//placeholder
            if (i < columns.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");
        String query = queryBuilder.toString();
        System.out.println("Составление запроса: " + query);

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] == null || row[i].trim().isEmpty()) {
                        //preparedStatement.setNull(i + 1, Types.NULL);
                        preparedStatement.setNull(i + 1, Types.VARCHAR);
                        //preparedStatement.setString(i + 1, "");
                    } else {
                        preparedStatement.setString(i + 1, row[i].trim());
                    }
                }
                preparedStatement.addBatch();
            }
            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println("Внесено " + updateCounts.length + " строк");
        }
    }
}
