package com.nhom7.quiz.quizapp.service.AdminService;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsExportService {

    private final AnalyticsService analyticsService;

    public AnalyticsExportService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public byte[] exportAnalyticsXlsx(String from, String to, String tz, int bins, int topLimit, int minAttempts) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(wb);
            createCoverSheet(wb, styles, from, to, tz);

            // Tên sheet tiếng Việt
            final String SH_ATTEMPTS = "Lượt làm";
            final String SH_USERS = "Người dùng";
            final String SH_AVG_SCORE = "Điểm trung bình";
            final String SH_SCORE_HIST = "Phân phối điểm";
            final String SH_COMPLETION = "Tỷ lệ hoàn thành";
            final String SH_CATEGORY_TOP = "Danh mục Top";
            final String SH_HEATMAP = "Bản đồ nhiệt";
            final String SH_TOP_QUIZZES = "Top quizzes";
            final String SH_TOP_PERFORMERS = "Top người dùng";

            // 1) Attempts series
            Map<String, Object> attempts = analyticsService.attemptsSeries(from, to, tz);
            writeSeriesSheet(wb, styles, SH_ATTEMPTS, attempts);

            // 2) Users series
            Map<String, Object> users = analyticsService.usersSeries(from, to, tz);
            writeUsersSeriesSheet(wb, styles, SH_USERS, users);

            // 3) Quality series
            Map<String, Object> quality = analyticsService.qualitySeries(from, to, tz);
            writeSeriesSheetWithTitle(wb, styles, SH_AVG_SCORE, quality, "Điểm trung bình %");

            // 4) Score histogram
            Map<String, Object> histogram = analyticsService.scoreHistogram(from, to, bins);
            writeHistogramSheet(wb, styles, SH_SCORE_HIST, histogram);

            // 5) Completion
            Map<String, Object> completion = analyticsService.completion(from, to);
            writeCompletionSheet(wb, styles, SH_COMPLETION, completion);

            // 6) Category distribution
            Map<String, Object> catDist = analyticsService.categoryDistribution(from, to, topLimit);
            writeCategorySheet(wb, styles, SH_CATEGORY_TOP, catDist);

            // 7) Heatmap
            Map<String, Object> heatmap = analyticsService.heatmap(from, to, tz);
            writeHeatmapSheet(wb, styles, SH_HEATMAP, heatmap);

            // 8) Top lists
            Map<String, Object> topQuizzes = analyticsService.topQuizzes(from, to, topLimit);
            Map<String, Object> topPerformers = analyticsService.topPerformers(from, to, topLimit, minAttempts);
            writeTopQuizzesSheet(wb, styles, SH_TOP_QUIZZES, topQuizzes);
            writeTopPerformersSheet(wb, styles, SH_TOP_PERFORMERS, topPerformers);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Export analytics failed", e);
        }
    }

    private void writeSeriesSheet(Workbook wb, Styles styles, String name, Map<String, Object> series) {
        writeSeriesSheetWithTitle(wb, styles, name, series, "Số lượt");
    }

    @SuppressWarnings("unchecked")
    private void writeSeriesSheetWithTitle(Workbook wb, Styles styles, String name, Map<String, Object> series, String valueTitle) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Nguồn: hệ thống Analytics • Đơn vị: " + valueTitle, 1);

        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Ngày", valueTitle});

        List<String> labels = (List<String>) series.get("labels");
        int[] data = (int[]) series.get("data");
        if (data == null) data = (int[]) series.get("avgScore"); // fallback cho sheet Điểm trung bình
        if (labels != null && data != null) {
            for (int i = 0; i < labels.size(); i++) {
                Row r = sh.createRow(start + 1 + i);
                createTextCell(r, 0, labels.get(i), styles.bodyText);
                createNumberCell(r, 1, data.length > i ? data[i] : 0, styles.intNumber);
                applyZebra(r, styles, i);
            }
        }
        int last = labels != null ? (start + labels.size()) : start + 1;
        addLineChart((XSSFSheet) sh, "Xu hướng " + name, start, last, 0, new int[]{1}, new String[]{valueTitle}, 4, start, 14, start + 16);
        finishTable(sh, 2, start);
    }

    @SuppressWarnings("unchecked")
    private void writeUsersSeriesSheet(Workbook wb, Styles styles, String name, Map<String, Object> users) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Người dùng hoạt động vs người dùng mới theo thời gian", 2);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Ngày", "Người dùng hoạt động", "Người dùng mới"});

        List<String> labels = (List<String>) users.get("labels");
        int[] active = (int[]) users.get("activeUsers");
        int[] news = (int[]) users.get("newUsers");
        if (labels != null) {
            for (int i = 0; i < labels.size(); i++) {
                Row r = sh.createRow(start + 1 + i);
                createTextCell(r, 0, labels.get(i), styles.bodyText);
                createNumberCell(r, 1, active != null && active.length > i ? active[i] : 0, styles.intNumber);
                createNumberCell(r, 2, news != null && news.length > i ? news[i] : 0, styles.intNumber);
                applyZebra(r, styles, i);
            }
        }
        int last = labels != null ? (start + labels.size()) : start + 1;
        addLineChart((XSSFSheet) sh, "DAU & New Users", start, last, 0, new int[]{1,2}, new String[]{"Hoạt động","Mới"}, 4, start, 14, start + 16);
        finishTable(sh, 3, start);
    }

    @SuppressWarnings("unchecked")
    private void writeHistogramSheet(Workbook wb, Styles styles, String name, Map<String, Object> histogram) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Phân bố điểm số theo bins", 1);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Khoảng điểm", "Số lượng"});

        List<Map<String, Object>> bins = (List<Map<String, Object>>) histogram.get("bins");
        if (bins != null) {
            int rowIdx = 0;
            for (Map<String, Object> b : bins) {
                Row r = sh.createRow(start + 1 + rowIdx);
                String label = b.get("min") + "-" + b.get("max");
                createTextCell(r, 0, label, styles.bodyText);
                createNumberCell(r, 1, ((Number) b.getOrDefault("count", 0)).intValue(), styles.intNumber);
                applyZebra(r, styles, rowIdx);
                rowIdx++;
            }
        }
        int last = bins != null ? (start + bins.size()) : start + 1;
        addBarChart((XSSFSheet) sh, "Phân phối điểm", start, last, 0, new int[]{1}, new String[]{"Số lượng"}, 4, start, 14, start + 16, true);
        finishTable(sh, 2, start);
    }

    private void writeCompletionSheet(Workbook wb, Styles styles, String name, Map<String, Object> completion) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Tỷ lệ hoàn thành tổng quan", 2);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Số lượt", "Đã hoàn thành", "Tỷ lệ hoàn thành"});

        Row r = sh.createRow(start + 1);
        createNumberCell(r, 0, ((Number) completion.getOrDefault("attempts", 0)).longValue(), styles.intNumber);
        createNumberCell(r, 1, ((Number) completion.getOrDefault("completed", 0)).longValue(), styles.intNumber);
        createPercentCell(r, 2, ((Number) completion.getOrDefault("completionRate", 0)).doubleValue(), styles.percent);

        long attempts = ((Number) completion.getOrDefault("attempts", 0)).longValue();
        long completed = ((Number) completion.getOrDefault("completed", 0)).longValue();
        long notCompleted = Math.max(0, attempts - completed);
        Row pHeader = sh.createRow(start + 3);
        createHeaderRow(pHeader, styles, new String[]{"Trạng thái", "Số lượng"});
        Row p1 = sh.createRow(start + 4); createTextCell(p1, 0, "Đã hoàn thành", styles.bodyText); createNumberCell(p1, 1, (int) completed, styles.intNumber);
        Row p2 = sh.createRow(start + 5); createTextCell(p2, 0, "Chưa hoàn thành", styles.bodyText); createNumberCell(p2, 1, (int) notCompleted, styles.intNumber);
        addPieChart((XSSFSheet) sh, "Tỷ lệ hoàn thành", new CellRangeAddress(start + 4, start + 5, 0, 0), new CellRangeAddress(start + 4, start + 5, 1, 1), 8, start, 16, start + 14);
        finishTable(sh, 3, start);
    }

    @SuppressWarnings("unchecked")
    private void writeCategorySheet(Workbook wb, Styles styles, String name, Map<String, Object> dist) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Phân bố theo danh mục (Top)", 2);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Danh mục", "Số lượt", "Tỷ lệ"});

        List<Map<String, Object>> items = (List<Map<String, Object>>) dist.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> it = items.get(i);
                Row r = sh.createRow(start + 1 + i);
                createTextCell(r, 0, String.valueOf(it.get("name")), styles.bodyText);
                createNumberCell(r, 1, ((Number) it.getOrDefault("count", 0)).longValue(), styles.intNumber);
                createPercentCell(r, 2, ((Number) it.getOrDefault("ratio", 0)).doubleValue(), styles.percent);
                applyZebra(r, styles, i);
            }
        }
        int last = items != null ? (start + items.size()) : start + 1;
        addBarChart((XSSFSheet) sh, "Top danh mục", start, last, 0, new int[]{1}, new String[]{"Số lượt"}, 6, start, 16, start + 18, false);
        finishTable(sh, 3, start);
    }

    @SuppressWarnings("unchecked")
    private void writeHeatmapSheet(Workbook wb, Styles styles, String name, Map<String, Object> heatmap) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Mật độ theo Day x Hour (0-23h)", 24);
        Row header = sh.createRow(start);
        String[] titles = new String[25];
        titles[0] = "Day/Hour";
        for (int h = 0; h < 24; h++) titles[h + 1] = h + "h";
        createHeaderRow(header, styles, titles);

        Object matrixObj = heatmap.get("matrix");
        int rowsWritten = 0;

        if (matrixObj instanceof int[][] mArr) {
            for (int d = 0; d < mArr.length; d++) {
                Row r = sh.createRow(start + 1 + d);
                createTextCell(r, 0, "D" + (d + 1), styles.bodyText);
                int[] row = mArr[d];
                for (int h = 0; h < Math.min(24, row.length); h++) {
                    createNumberCell(r, h + 1, row[h], styles.intCenter);
                }
                rowsWritten++;
            }
        } else if (matrixObj instanceof List<?> list) {
            List<List<Integer>> matrix = (List<List<Integer>>) (Object) list;
            for (int d = 0; d < matrix.size(); d++) {
                Row r = sh.createRow(start + 1 + d);
                createTextCell(r, 0, "D" + (d + 1), styles.bodyText);
                List<Integer> row = matrix.get(d);
                for (int h = 0; h < Math.min(24, row.size()); h++) {
                    createNumberCell(r, h + 1, row.get(h), styles.intCenter);
                }
                rowsWritten++;
            }
        }

        // Color scale formatting
        if (sh instanceof XSSFSheet xs && rowsWritten > 0) {
            var scf = xs.getSheetConditionalFormatting();
            var rule = scf.createConditionalFormattingColorScaleRule();
            var fmt = rule.getColorScaleFormatting();
            fmt.setNumControlPoints(3);
            // middle threshold value
            fmt.getThresholds()[1].setValue(50d);
            fmt.getColors()[0].setARGBHex("FFE5EFFF");
            fmt.getColors()[1].setARGBHex("FF7DB4FF");
            fmt.getColors()[2].setARGBHex("FF2563EB");
            scf.addConditionalFormatting(
                new CellRangeAddress[]{ new CellRangeAddress(start + 1, start + rowsWritten, 1, 24) }, rule
            );
        }

        finishTable(sh, 25, start);
    }

    @SuppressWarnings("unchecked")
    private void writeTopQuizzesSheet(Workbook wb, Styles styles, String name, Map<String, Object> data) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Top quizzes theo số lượt/hoàn thành/điểm TB", 3);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Bài Quiz", "Số lượt", "Tỷ lệ hoàn thành", "Điểm TB"});

        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> it = items.get(i);
                Row r = sh.createRow(start + 1 + i);
                createTextCell(r, 0, String.valueOf(it.get("title")), styles.bodyText);
                createNumberCell(r, 1, ((Number) it.getOrDefault("attempts", 0)).longValue(), styles.intNumber);
                createPercentCell(r, 2, ((Number) it.getOrDefault("completionRate", 0)).doubleValue(), styles.percent);
                createPercentCell(r, 3, ((Number) it.getOrDefault("avgScore", 0)).doubleValue() / 100.0, styles.percent);
                applyZebra(r, styles, i);
            }
        }
        int last = items != null ? (start + items.size()) : start + 1;
        addBarChart((XSSFSheet) sh, "Top quizzes", start, last, 0, new int[]{1}, new String[]{"Số lượt"}, 6, start, 16, start + 18, false);
        finishTable(sh, 4, start);
    }

    @SuppressWarnings("unchecked")
    private void writeTopPerformersSheet(Workbook wb, Styles styles, String name, Map<String, Object> data) {
        Sheet sh = wb.createSheet(name);
        setTabColorIfXSSF(sh, IndexedColors.LIGHT_TURQUOISE.getIndex());

        int start = writeSheetBanner(sh, styles, name, "Top người dùng theo số lượt/điểm TB", 2);
        Row header = sh.createRow(start);
        createHeaderRow(header, styles, new String[]{"Người dùng", "Số lượt", "Điểm TB"});

        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> it = items.get(i);
                Row r = sh.createRow(start + 1 + i);
                createTextCell(r, 0, String.valueOf(it.get("fullName")), styles.bodyText);
                createNumberCell(r, 1, ((Number) it.getOrDefault("attempts", 0)).longValue(), styles.intNumber);
                createPercentCell(r, 2, ((Number) it.getOrDefault("avgScore", 0)).doubleValue() / 100.0, styles.percent);
                applyZebra(r, styles, i);
            }
        }
        int last2 = items != null ? (start + items.size()) : start + 1;
        addBarChart((XSSFSheet) sh, "Top người dùng", start, last2, 0, new int[]{1}, new String[]{"Số lượt"}, 6, start, 16, start + 18, false);
        finishTable(sh, 3, start);
    }

    private void autosize(Sheet sh, int cols) {
        for (int i = 0; i < cols; i++) sh.autoSizeColumn(i);
    }

    // --------- Styling + helpers (đã tinh gọn & chống "style explosion") ----------

    // Add line chart: categories in column catCol, values in valueCols, data rows [start+1..end]
    private void addLineChart(XSSFSheet sheet, String title, int start, int end, int catCol, int[] valueCols, String[] seriesNames,
                              int x1, int y1, int x2, int y2) {
        var drawing = sheet.createDrawingPatriarch();
        var anchor = new XSSFClientAnchor(); anchor.setCol1(x1); anchor.setRow1(y1); anchor.setCol2(x2); anchor.setRow2(y2);
        XSSFChart chart = drawing.createChart(anchor); chart.setTitleText(title); chart.setTitleOverlay(false);
        XDDFCategoryDataSource cat = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(start + 1, end, catCol, catCol));
        XDDFChartData data = chart.createData(ChartTypes.LINE, chart.createCategoryAxis(AxisPosition.BOTTOM), chart.createValueAxis(AxisPosition.LEFT));
        for (int i = 0; i < valueCols.length; i++) {
            XDDFNumericalDataSource<Double> vals = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(start + 1, end, valueCols[i], valueCols[i]));
            XDDFChartData.Series s = data.addSeries(cat, vals); s.setTitle(seriesNames[i], null);
        }
        chart.plot(data);
    }

    // Add bar chart; horizontal if isHorizontal
    private void addBarChart(XSSFSheet sheet, String title, int start, int end, int catCol, int[] valueCols, String[] seriesNames,
                             int x1, int y1, int x2, int y2, boolean isHorizontal) {
        var drawing = sheet.createDrawingPatriarch();
        var anchor = new XSSFClientAnchor(); anchor.setCol1(x1); anchor.setRow1(y1); anchor.setCol2(x2); anchor.setRow2(y2);
        XSSFChart chart = drawing.createChart(anchor); chart.setTitleText(title); chart.setTitleOverlay(false);
        XDDFCategoryAxis catAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis valAxis = chart.createValueAxis(AxisPosition.LEFT);
        XDDFCategoryDataSource cat = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(start + 1, end, catCol, catCol));
        XDDFChartData data = chart.createData(ChartTypes.BAR, catAxis, valAxis);
        for (int i = 0; i < valueCols.length; i++) {
            XDDFNumericalDataSource<Double> vals = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(start + 1, end, valueCols[i], valueCols[i]));
            XDDFChartData.Series s = data.addSeries(cat, vals); s.setTitle(seriesNames[i], null);
        }
        chart.plot(data);
        XDDFBarChartData bar = (XDDFBarChartData) data; bar.setBarDirection(isHorizontal ? BarDirection.BAR : BarDirection.COL);
    }

    // Add pie chart from two ranges (labels and values)
    private void addPieChart(XSSFSheet sheet, String title, CellRangeAddress labelRange, CellRangeAddress valueRange,
                             int x1, int y1, int x2, int y2) {
        var drawing = sheet.createDrawingPatriarch();
        var anchor = new XSSFClientAnchor(); anchor.setCol1(x1); anchor.setRow1(y1); anchor.setCol2(x2); anchor.setRow2(y2);
        XSSFChart chart = drawing.createChart(anchor); chart.setTitleText(title); chart.setTitleOverlay(false);
        XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
        var cat = XDDFDataSourcesFactory.fromStringCellRange(sheet, labelRange);
        var val = XDDFDataSourcesFactory.fromNumericCellRange(sheet, valueRange);
        data.addSeries(cat, val); chart.plot(data);
    }

    private static class Styles {
        final DataFormat df;

        // fonts
        final XSSFFont fontBase;
        final XSSFFont fontHeader;
        final XSSFFont fontTitle;

        // core styles
        final XSSFCellStyle header;
        final XSSFCellStyle bodyText;
        final XSSFCellStyle intNumber;
        final XSSFCellStyle intCenter;
        final XSSFCellStyle percent;

        // zebra variants
        final XSSFCellStyle bodyTextZebra;
        final XSSFCellStyle intNumberZebra;
        final XSSFCellStyle intCenterZebra;
        final XSSFCellStyle percentZebra;

        // banner styles
        final XSSFCellStyle bannerTitle;
        final XSSFCellStyle bannerSubtle;

        Styles(Workbook wb) {
            df = wb.createDataFormat();

            // fonts
            fontBase = (XSSFFont) wb.createFont();
            fontBase.setFontName("Calibri");
            fontBase.setFontHeightInPoints((short) 11);

            fontHeader = (XSSFFont) wb.createFont();
            fontHeader.setFontName("Calibri");
            fontHeader.setBold(true);
            fontHeader.setFontHeightInPoints((short) 11);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            fontTitle = (XSSFFont) wb.createFont();
            fontTitle.setFontName("Calibri");
            fontTitle.setBold(true);
            fontTitle.setFontHeightInPoints((short) 16);
            fontTitle.setColor(IndexedColors.WHITE.getIndex());

            // header
            header = (XSSFCellStyle) wb.createCellStyle();
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            header.setWrapText(true);
            header.setBorderBottom(BorderStyle.THIN);
            header.setBorderTop(BorderStyle.THIN);
            header.setBorderLeft(BorderStyle.THIN);
            header.setBorderRight(BorderStyle.THIN);
            header.setFont(fontHeader);

            // base cell
            bodyText = (XSSFCellStyle) wb.createCellStyle();
            bodyText.setBorderBottom(BorderStyle.THIN);
            bodyText.setBorderTop(BorderStyle.THIN);
            bodyText.setBorderLeft(BorderStyle.THIN);
            bodyText.setBorderRight(BorderStyle.THIN);
            bodyText.setVerticalAlignment(VerticalAlignment.CENTER);
            bodyText.setFont(fontBase);

            // numbers
            intNumber = (XSSFCellStyle) wb.createCellStyle();
            intNumber.cloneStyleFrom(bodyText);
            intNumber.setDataFormat(df.getFormat("0"));

            intCenter = (XSSFCellStyle) wb.createCellStyle();
            intCenter.cloneStyleFrom(intNumber);
            intCenter.setAlignment(HorizontalAlignment.CENTER);

            percent = (XSSFCellStyle) wb.createCellStyle();
            percent.cloneStyleFrom(bodyText);
            percent.setDataFormat(df.getFormat("0.0%"));

            // zebra variants (very light gray)
            short zebra = IndexedColors.GREY_25_PERCENT.getIndex();

            bodyTextZebra = (XSSFCellStyle) wb.createCellStyle();
            bodyTextZebra.cloneStyleFrom(bodyText);
            bodyTextZebra.setFillForegroundColor(zebra);
            bodyTextZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            intNumberZebra = (XSSFCellStyle) wb.createCellStyle();
            intNumberZebra.cloneStyleFrom(intNumber);
            intNumberZebra.setFillForegroundColor(zebra);
            intNumberZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            intCenterZebra = (XSSFCellStyle) wb.createCellStyle();
            intCenterZebra.cloneStyleFrom(intCenter);
            intCenterZebra.setFillForegroundColor(zebra);
            intCenterZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            percentZebra = (XSSFCellStyle) wb.createCellStyle();
            percentZebra.cloneStyleFrom(percent);
            percentZebra.setFillForegroundColor(zebra);
            percentZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // banner
            bannerTitle = (XSSFCellStyle) wb.createCellStyle();
            bannerTitle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            bannerTitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bannerTitle.setAlignment(HorizontalAlignment.LEFT);
            bannerTitle.setVerticalAlignment(VerticalAlignment.CENTER);
            bannerTitle.setFont(fontTitle);

            bannerSubtle = (XSSFCellStyle) wb.createCellStyle();
            bannerSubtle.cloneStyleFrom(bodyText);
            bannerSubtle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            bannerSubtle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
    }

    private int writeSheetBanner(Sheet sh, Styles styles, String title, String subtitle, int lastCol) {
        // Row 0: title (merged)
        Row r0 = sh.createRow(0);
        r0.setHeightInPoints(28f);
        Cell c0 = r0.createCell(0);
        c0.setCellValue(title);
        c0.setCellStyle(styles.bannerTitle);
        sh.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(0, lastCol)));

        // Row 1: subtitle (merged)
        Row r1 = sh.createRow(1);
        r1.setHeightInPoints(20f);
        Cell c1 = r1.createCell(0);
        c1.setCellValue(subtitle);
        c1.setCellStyle(styles.bannerSubtle);
        sh.addMergedRegion(new CellRangeAddress(1, 1, 0, Math.max(0, lastCol)));

        return 2; // vị trí bắt đầu header bảng
    }

    private void createHeaderRow(Row header, Styles styles, String[] titles) {
        for (int i = 0; i < titles.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(titles[i]);
            c.setCellStyle(styles.header);
        }
        header.setHeightInPoints(22f);
    }

    private void createTextCell(Row r, int col, String text, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(text);
        c.setCellStyle(style);
    }

    private void createNumberCell(Row r, int col, long value, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void createNumberCell(Row r, int col, int value, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void createPercentCell(Row r, int col, double value, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void applyZebra(Row r, Styles styles, int rowIndexZeroBased) {
        if (rowIndexZeroBased % 2 != 1) return; // chỉ tô các hàng lẻ (1,3,5,...)
        for (int i = 0; i < r.getLastCellNum(); i++) {
            Cell c = r.getCell(i);
            if (c == null) continue;
            CellStyle s = c.getCellStyle();
            if (s == styles.bodyText) c.setCellStyle(styles.bodyTextZebra);
            else if (s == styles.intNumber) c.setCellStyle(styles.intNumberZebra);
            else if (s == styles.intCenter) c.setCellStyle(styles.intCenterZebra);
            else if (s == styles.percent) c.setCellStyle(styles.percentZebra);
            else c.setCellStyle(styles.bodyTextZebra);
        }
    }

    private void finishTable(Sheet sh, int cols, int firstDataRowIndex) {
        autosize(sh, cols);
        // Freeze ngay dưới header (giữ banner + header cố định)
        sh.createFreezePane(0, firstDataRowIndex + 1);
        if (sh.getLastRowNum() >= 0) {
            sh.setAutoFilter(new CellRangeAddress(firstDataRowIndex, sh.getLastRowNum(), 0, cols - 1));
        }
    }

    private void setTabColorIfXSSF(Sheet sh, short colorIdx) {
        if (sh instanceof XSSFSheet xs) {
            java.awt.Color awt = mapIndexedColor(colorIdx);
            if (awt != null) {
                xs.setTabColor(new XSSFColor(awt, new DefaultIndexedColorMap()));
            }
        }
    }

    private java.awt.Color mapIndexedColor(short idx) {
        if (idx == IndexedColors.BLUE.getIndex()) return new java.awt.Color(0, 102, 204);
        if (idx == IndexedColors.LIGHT_TURQUOISE.getIndex()) return new java.awt.Color(176, 224, 230);
        return new java.awt.Color(120, 144, 156); // fallback (blue gray)
    }

    private void createCoverSheet(Workbook wb, Styles styles, String from, String to, String tz) {
        Sheet sh = wb.createSheet("Summary");
        setTabColorIfXSSF(sh, IndexedColors.BLUE.getIndex());

        int start = writeSheetBanner(sh, styles, "Báo cáo Analytics", "Khoảng thời gian: " + from + " → " + to, 3);

        Row r2 = sh.createRow(start);
        r2.createCell(0).setCellValue("Múi giờ");
        r2.createCell(1).setCellValue("Asia/Ho_Chi_Minh (Hà Nội)");
        r2.getCell(0).setCellStyle(styles.bodyText);
        r2.getCell(1).setCellStyle(styles.bodyText);

        Row r3 = sh.createRow(start + 1);
        r3.createCell(0).setCellValue("Tạo lúc");
        r3.createCell(1).setCellValue(java.time.ZonedDateTime.now().toString());
        r3.getCell(0).setCellStyle(styles.bodyText);
        r3.getCell(1).setCellStyle(styles.bodyText);

        // TOC
        CreationHelper helper = wb.getCreationHelper();
        String[] sheets = new String[]{
            "Lượt làm","Người dùng","Điểm trung bình","Phân phối điểm","Tỷ lệ hoàn thành","Danh mục Top","Bản đồ nhiệt","Top quizzes","Top người dùng"
        };
        Row tocHeader = sh.createRow(start + 3);
        createHeaderRow(tocHeader, styles, new String[]{"Mục lục", "Đi tới"});

        for (int i = 0; i < sheets.length; i++) {
            Row r = sh.createRow(start + 4 + i);
            createTextCell(r, 0, sheets[i], styles.bodyText);
            Cell linkCell = r.createCell(1);
            linkCell.setCellValue("Đi tới " + sheets[i]);
            Hyperlink link = helper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress("'" + sheets[i] + "'!A1");
            linkCell.setHyperlink(link);
            linkCell.setCellStyle(styles.bodyText);
            applyZebra(r, styles, i);
        }

        autosize(sh, 4);
        sh.createFreezePane(0, start + 4);
    }
}
