package com.nhom7.quiz.quizapp.service.AdminService;

import com.nhom7.quiz.quizapp.repository.AnalyticsRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    private final AnalyticsRepo analyticsRepo;

    public AnalyticsService(AnalyticsRepo analyticsRepo) {
        this.analyticsRepo = analyticsRepo;
    }

    private LocalDateTime[] rangeFor(String from, String to, String tz) {
        ZoneId zoneId = (tz != null && !tz.isBlank()) ? ZoneId.of(tz) : ZoneId.systemDefault();
        LocalDate startDate = LocalDate.parse(from);
        LocalDate endDate = LocalDate.parse(to);
        ZonedDateTime start = startDate.atStartOfDay(zoneId);
        ZonedDateTime end = endDate.plusDays(1).atStartOfDay(zoneId);
        return new LocalDateTime[]{start.toLocalDateTime(), end.toLocalDateTime()};
    }

    public Map<String, Object> attemptsSeries(String from, String to, String tz) {
        LocalDateTime[] r = rangeFor(from, to, tz);
        var rows = analyticsRepo.attemptsSeries(r[0], r[1]);
        return buildDailySeries(from, to, rows);
    }

    public Map<String, Object> usersSeries(String from, String to, String tz) {
        LocalDateTime[] r = rangeFor(from, to, tz);
        var active = analyticsRepo.activeUsersSeries(r[0], r[1]);
        var news = analyticsRepo.newUsersSeries(r[0], r[1]);
        Map<String, Object> base = buildEmptySeries(from, to);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) base.get("labels");
        int n = labels.size();
        int[] activeArr = new int[n];
        int[] newArr = new int[n];
        fillSeries(active, labels, activeArr);
        fillSeries(news, labels, newArr);
        base.put("activeUsers", activeArr);
        base.put("newUsers", newArr);
        return base;
    }

    public Map<String, Object> qualitySeries(String from, String to, String tz) {
        LocalDateTime[] r = rangeFor(from, to, tz);
        var rows = analyticsRepo.avgScoreSeries(r[0], r[1]);
        Map<String, Object> base = buildEmptySeries(from, to);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) base.get("labels");
        int[] avg = new int[labels.size()];
        for (Object[] row : rows) {
            String d = String.valueOf(row[0]);
            int idx = labels.indexOf(d);
            if (idx >= 0) {
                double val = ((Number) row[1]).doubleValue();
                avg[idx] = (int) Math.round(val);
            }
        }
        base.put("avgScore", avg);
        return base;
    }

    public Map<String, Object> scoreHistogram(String from, String to, int bins) {
        LocalDateTime[] r = rangeFor(from, to, null);
        var counts = analyticsRepo.scoreCounts(r[0], r[1]);
        // Build 0..100 histogram with given bins
        int width = Math.max(1, 100 / Math.max(1, bins));
        int[] hist = new int[bins];
        for (Object[] row : counts) {
            int score = ((Number) row[0]).intValue();
            int c = ((Number) row[1]).intValue();
            int bin = Math.min(bins - 1, score / width);
            hist[bin] += c;
        }
        List<Map<String, Object>> binObjs = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            int min = i * width;
            int max = i == bins - 1 ? 100 : (i + 1) * width;
            Map<String, Object> b = new HashMap<>();
            b.put("min", min);
            b.put("max", max);
            b.put("count", hist[i]);
            binObjs.add(b);
        }
        return Map.of("bins", binObjs);
    }

    public Map<String, Object> completion(String from, String to) {
        LocalDateTime[] r = rangeFor(from, to, null);
        long attempts = analyticsRepo.countAttempts(r[0], r[1]);
        long completed = analyticsRepo.countCompleted(r[0], r[1]);
        return Map.of(
                "attempts", attempts,
                "completed", completed,
                "completionRate", attempts == 0 ? 0 : (double) completed / attempts
        );
    }

    public Map<String, Object> categoryDistribution(String from, String to, int limit) {
        LocalDateTime[] r = rangeFor(from, to, null);
        var rows = analyticsRepo.categoryDistributionTop(r[0], r[1], limit);
        long total = rows.stream().mapToLong(o -> ((Number) ((Object[]) o)[2]).longValue()).sum();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object obj : rows) {
            Object[] row = (Object[]) obj;
            long id = ((Number) row[0]).longValue();
            String name = String.valueOf(row[1]);
            long cnt = ((Number) row[2]).longValue();
            double ratio = total == 0 ? 0 : (double) cnt / total;
            items.add(Map.of("categoryId", id, "name", name, "count", cnt, "ratio", ratio));
        }
        return Map.of("items", items);
    }

    public Map<String, Object> heatmap(String from, String to, String tz) {
        LocalDateTime[] r = rangeFor(from, to, tz);
        var rows = analyticsRepo.heatmap(r[0], r[1]);
        int[][] matrix = new int[7][24];
        for (Object[] row : rows) {
            int wd = ((Number) row[0]).intValue(); // SQLServer: 1=Sunday..7=Saturday
            int hour = ((Number) row[1]).intValue();
            int count = ((Number) row[2]).intValue();
            int rIdx = (wd + 5) % 7; // convert to 0=Mon..6=Sun
            matrix[rIdx][hour] = count;
        }
        return Map.of("matrix", matrix);
    }

    public Map<String, Object> topQuizzes(String from, String to, int limit) {
        LocalDateTime[] r = rangeFor(from, to, null);
        var attempts = analyticsRepo.topQuizzesByAttempts(r[0], r[1], limit);
        var completed = analyticsRepo.completedAndAvgScorePerQuiz(r[0], r[1]);
        Map<Long, long[]> compMap = new HashMap<>(); // quizId -> [completed, avgScore]
        for (Object[] row : completed) {
            long qid = ((Number) row[0]).longValue();
            long comp = ((Number) row[1]).longValue();
            int avg = (int) Math.round(((Number) row[2]).doubleValue());
            compMap.put(qid, new long[]{comp, avg});
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : attempts) {
            long qid = ((Number) row[0]).longValue();
            String title = String.valueOf(row[1]);
            long att = ((Number) row[2]).longValue();
            long comp = compMap.getOrDefault(qid, new long[]{0, 0})[0];
            long avg = compMap.getOrDefault(qid, new long[]{0, 0})[1];
            double completionRate = att == 0 ? 0 : (double) comp / att;
            items.add(Map.of("quizId", qid, "title", title, "attempts", att, "completionRate", completionRate, "avgScore", avg));
        }
        return Map.of("items", items);
    }

    public Map<String, Object> topPerformers(String from, String to, int limit, int minAttempts) {
        LocalDateTime[] r = rangeFor(from, to, null);
        var rows = analyticsRepo.topPerformers(r[0], r[1], limit, minAttempts);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : rows) {
            long userId = ((Number) row[0]).longValue();
            String username = String.valueOf(row[1]);
            String fullName = String.valueOf(row[2]);
            long attempts = ((Number) row[3]).longValue();
            int avg = (int) Math.round(((Number) row[4]).doubleValue());
            items.add(Map.of("userId", userId, "username", username, "fullName", fullName, "attempts", attempts, "avgScore", avg));
        }
        return Map.of("items", items);
    }

    private Map<String, Object> buildDailySeries(String from, String to, List<Object[]> rows) {
        Map<String, Object> base = buildEmptySeries(from, to);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) base.get("labels");
        int[] values = new int[labels.size()];
        fillSeries(rows, labels, values);
        base.put("data", values);
        return base;
    }

    private Map<String, Object> buildEmptySeries(String from, String to) {
        List<String> labels = new ArrayList<>();
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            labels.add(d.toString());
        }
        Map<String, Object> base = new HashMap<>();
        base.put("labels", labels);
        return base;
    }

    private void fillSeries(List<Object[]> rows, List<String> labels, int[] values) {
        for (Object[] row : rows) {
            String d = String.valueOf(row[0]);
            int c = ((Number) row[1]).intValue();
            int idx = labels.indexOf(d);
            if (idx >= 0) values[idx] = c;
        }
    }
}


