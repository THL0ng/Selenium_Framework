package com.project.utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static Façade quản lý toàn bộ việc đọc dữ liệu trong Framework.
 * Đã bọc toàn bộ Interface và Implementation vào trong để hợp lệ hóa quy tắc biên dịch của Java.
 */
public final class FileHelper {

    private static final CachingFileReader INSTANCE = new CachingFileReader();

    private FileHelper() {} // Pure static façade

    // =============================================================================
    // API CHÍNH (Giữ nguyên 100% backward compatibility)
    // =============================================================================

    public static JsonNode readJson(String filePath) {
        return INSTANCE.readJson(filePath);
    }

    public static List<List<String>> readCsv(String filePath) {
        return INSTANCE.readCsv(filePath);
    }

    public static void clearAllCaches() {
        INSTANCE.clearAllCaches();
    }

    public static void invalidate(String filePath) {
        INSTANCE.invalidate(filePath);
    }

    public static FileReader getInstance() {
        return INSTANCE;
    }

    public static void logCacheStats() {
        INSTANCE.logCacheStats();
    }

    // =============================================================================
    // NESTED INTERFACE — Định nghĩa Contract công khai để Mock dễ dàng ở các package khác
    // =============================================================================
    public interface FileReader {
        JsonNode readJson(String filePath);
        List<List<String>> readCsv(String filePath);
        void clearAllCaches();
        void invalidate(String filePath);
    }

    // =============================================================================
    // NESTED IMPLEMENTATION — Bộ engine xử lý ngầm mạnh mẽ
    // =============================================================================
    private static class CachingFileReader implements FileReader {
        private static final Logger LOG = LogManager.getLogger(CachingFileReader.class);
        private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
        private static final int DEFAULT_MAX_SIZE = 500;

        private final Cache<String, JsonNode> jsonCache;
        private final Cache<String, List<List<String>>> csvCache;

        public CachingFileReader() {
            this(
                    Caffeine.newBuilder().maximumSize(DEFAULT_MAX_SIZE).recordStats().build(),
                    Caffeine.newBuilder().maximumSize(DEFAULT_MAX_SIZE).recordStats().build()
            );
        }

        public CachingFileReader(Cache<String, JsonNode> jsonCache, Cache<String, List<List<String>>> csvCache) {
            this.jsonCache = jsonCache;
            this.csvCache  = csvCache;
        }

        @Override
        public JsonNode readJson(String filePath) {
            validatePath(filePath);
            String abs = toAbsolute(filePath);
            return jsonCache.get(abs, this::loadJsonFromDisk).deepCopy();
        }

        @Override
        public List<List<String>> readCsv(String filePath) {
            validatePath(filePath);
            String abs = toAbsolute(filePath);
            return new ArrayList<>(csvCache.get(abs, this::loadCsvFromDisk));
        }

        @Override
        public void clearAllCaches() {
            jsonCache.invalidateAll();
            csvCache.invalidateAll();
            LOG.info("=== CachingFileReader: all caches cleared ===");
        }

        @Override
        public void invalidate(String filePath) {
            // ✅ FIX ĐIỂM 3: Khôi phục lại cảnh báo WARNING khi path bị null hoặc blank, không nuốt lỗi ngầm
            if (filePath == null || filePath.isBlank()) {
                LOG.warn("invalidate() called with null/blank path — ignored");
                return;
            }

            String abs = toAbsolute(filePath);

            // ✅ FIX ĐIỂM 1: Kiểm tra trạng thái dữ liệu trước khi xóa để phân biệt Log Hit/Miss rõ ràng

            boolean hadJson = jsonCache.getIfPresent(abs) != null;
            boolean hadCsv  = csvCache.getIfPresent(abs)  != null;

            jsonCache.invalidate(abs);
            csvCache.invalidate(abs);

            if (hadJson || hadCsv) {
                LOG.info("Cache invalidated for: {}", abs);
            } else {
                LOG.debug("invalidate() called but key was not in cache: {}", abs);
            }
        }

        public void logCacheStats() {
            LOG.info("JSON cache stats : {}", jsonCache.stats());
            LOG.info("CSV  cache stats : {}", csvCache.stats());
        }

        // ✅ FIX ĐIỂM 2: Chuyển các hàm helper thành private static chuẩn mực kiến trúc
        // ✅ FIX: Xử lý thông minh dấu gạch chéo / và \ trước khi tạo Path
        private static String toAbsolute(String filePath) {
            // Thay thế tất cả dấu / thành dấu gạch chéo chuẩn của hệ điều hành hiện tại
            // Sau đó dùng Paths.get để nó tự chuẩn hóa theo Windows/Linux
            String normalizedPath = filePath.replace("/", java.io.File.separator)
                    .replace("\\", java.io.File.separator);

            return Paths.get(normalizedPath).toAbsolutePath().toString();
        }

        // ✅ FIX ĐIỂM 2: Chuyển thành private static giúp tối ưu vùng nhớ tĩnh
        private static void validatePath(String filePath) {
            if (filePath == null || filePath.isBlank())
                throw new IllegalArgumentException("filePath must not be null or blank");
        }

        private JsonNode loadJsonFromDisk(String absolutePath) {
            LOG.info("Cache MISS — reading JSON from disk: {}", absolutePath);
            Path path = Paths.get(absolutePath);

            try (InputStream rawIn = Files.newInputStream(path);
                 BOMInputStream bomIn = new BOMInputStream(rawIn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, StandardCharsets.UTF_8))) {
                return JSON_MAPPER.readTree(reader);
            } catch (NoSuchFileException e) {
                throw new IllegalArgumentException("JSON file not found: " + absolutePath, e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse JSON: " + absolutePath, e);
            }
        }

        private List<List<String>> loadCsvFromDisk(String absolutePath) {
            LOG.info("Cache MISS — reading CSV from disk: {}", absolutePath);
            Path path = Paths.get(absolutePath);

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .build();

            List<List<String>> tempContainer = new ArrayList<>();

            try (InputStream rawIn = Files.newInputStream(path);
                 BOMInputStream bomIn = new BOMInputStream(rawIn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, StandardCharsets.UTF_8));
                 CSVParser csvParser = new CSVParser(reader, csvFormat)) {

                for (CSVRecord record : csvParser) {
                    List<String> row = new ArrayList<>(record.size());
                    for (int i = 0; i < record.size(); i++) {
                        String val = record.get(i);
                        row.add(val != null ? val.trim().replaceAll("\\r?\\n", " ") : "");
                    }
                    tempContainer.add(Collections.unmodifiableList(row));
                }

                return Collections.unmodifiableList(tempContainer);

            } catch (NoSuchFileException e) {
                throw new IllegalArgumentException("CSV file not found: " + absolutePath, e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse CSV: " + absolutePath, e);
            }
        }
    }
}