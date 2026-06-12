package com.project.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DataProviderHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DataProviderHelper() {}

    // ------------------------------------------------------------------ //
    //  JSON
    // ------------------------------------------------------------------ //

    public static <T> Object[][] fromJson(String filePath, Class<T[]> pojoClass) {
        try {
            T[] items = MAPPER.treeToValue(FileHelper.readJson(filePath), pojoClass);
            return toMatrix(items);
        } catch (Exception e) {
            throw new RuntimeException("Không thể load JSON test data từ: " + filePath, e);
        }
    }

    // ------------------------------------------------------------------ //
    //  CSV
    // ------------------------------------------------------------------ //


    public static <T> Object[][] fromCsv(String filePath, Class<T> pojoClass) {
        try {
            List<List<String>> rows = FileHelper.readCsv(filePath);

            if (rows.size() < 2) {
                throw new IllegalArgumentException(
                        "CSV '" + filePath + "' cần ít nhất 1 header row và 1 data row"
                );
            }

            List<String> headers = rows.get(0);   // row đầu = header
            List<List<String>> dataRows = rows.subList(1, rows.size());

            // Map mỗi row thành Map<fieldName, value> → rồi convert sang POJO
            T[] items = dataRows.stream()
                    .map(row -> rowToMap(headers, row))
                    .map(map -> MAPPER.convertValue(map, pojoClass))
                    .toArray(size -> (T[]) java.lang.reflect.Array.newInstance(pojoClass, size));

            return toMatrix(items);
        } catch (RuntimeException e) {
            throw e; // re-throw RuntimeException trực tiếp, không wrap thêm
        } catch (Exception e) {
            throw new RuntimeException("Không thể load CSV test data từ: " + filePath, e);
        }
    }

    // ------------------------------------------------------------------ //
    //  Private helpers
    // ------------------------------------------------------------------ //


    private static Map<String, String> rowToMap(List<String> headers, List<String> row) {
        return IntStream.range(0, headers.size())
                .boxed()
                .collect(Collectors.toMap(
                        headers::get,
                        i -> i < row.size() ? row.get(i) : "" // phòng row thiếu cột
                ));
    }

    private static <T> Object[][] toMatrix(T[] items) {
        Object[][] result = new Object[items.length][1];
        for (int i = 0; i < items.length; i++) {
            result[i][0] = items[i];
        }
        return result;
    }
}