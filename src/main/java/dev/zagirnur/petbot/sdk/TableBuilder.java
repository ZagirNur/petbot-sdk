package dev.zagirnur.petbot.sdk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class TableBuilder {

    List<Column> columns = new ArrayList<>();
    String columnSeparator = " ";
    List<String> header = new ArrayList<>();
    Character headerSeparator = '-';

    public TableBuilder(List<String> header, Character headerSeparator, String columnSeparator) {
        header.addAll(header);
        this.headerSeparator = headerSeparator;
        this.columnSeparator = columnSeparator;
    }

    public TableBuilder(Character headerSeparator, String columnSeparator) {
        this.headerSeparator = headerSeparator;
        this.columnSeparator = columnSeparator;
    }

    public TableBuilder(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public TableBuilder() {
    }

    public TableBuilder column(Alignment alignment, ColumnType columnType, Function<Integer, String> rowSource) {
        columns.add(new Column(alignment, columnType, rowSource));
        return this;
    }

    public TableBuilder column(Alignment alignment, Function<Integer, String> rowSource) {
        columns.add(new Column(alignment, rowSource));
        return this;
    }

    public TableBuilder column(ColumnType columnType, Function<Integer, String> rowSource) {
        columns.add(new Column(columnType, rowSource));
        return this;
    }

    public TableBuilder column(Function<Integer, String> rowSource) {
        columns.add(new Column(rowSource));
        return this;
    }

    public TableBuilder column(String columnHeader, Function<Integer, String> rowSource) {
        header = new ArrayList<>(header);
        header.add(columns.size(), columnHeader);
        columns.add(new Column(rowSource));
        return this;
    }

    public TableBuilder numberColumn(Function<Integer, String> rowSource) {
        columns.add(new Column(Alignment.RIGHT, ColumnType.NUMBER_WITH_TINY_SPACES, rowSource));
        return this;
    }

    public TableBuilder numberColumn(String columnHeader, Function<Integer, String> rowSource) {
        header.add(columns.size(), columnHeader);
        columns.add(new Column(Alignment.RIGHT, ColumnType.NUMBER_WITH_TINY_SPACES, rowSource));
        return this;
    }

    public String build() {
        return textTable(columnSeparator, columns.toArray(Column[]::new));
    }

    @Override
    public String toString() {
        return build();
    }


    public String textTable(
            String columnSeparator,
            Column... columns
    ) {
        List<List<String>> rawData = getRaw(columns);
        List<List<String>> formattedColumns = new ArrayList<>();

        for (int i = 0; i < columns.length; i++) {

            Column column = columns[i];
            List<String> rows = rawData.get(i);
            List<String> formattedRows = switch (column.getColumnType()) {
                case MONOSPACED -> formatMonospaced(rows, column.getAlignment());
                case NUMBER_WITH_TINY_SPACES -> formatNumberWithTinySpaces(rows, column.getAlignment());
                case NOT_MONOSPACED -> formatNotMonospaced(rows, column.getAlignment());
            };
            formattedColumns.add(formattedRows);
        }



        StringBuilder header = new StringBuilder();
        for (int i = 0; i < this.header.size(); i++) {
            int headerLengthWithNoTinySpaces = formattedColumns.get(i).stream()
                    .map(s -> s.replace("</code> <code>", ""))
                    .map(s -> s.replace("<code>", ""))
                    .map(s -> s.replace("</code>", ""))
                    .mapToInt(String::length)
                    .max().orElse(0);
            int headerLength = formattedColumns.get(i).stream()
                    .map(s -> s.replace("</code> <code>", " "))
                    .map(s -> s.replace("<code>", ""))
                    .map(s -> s.replace("</code>", ""))
                    .mapToInt(String::length)
                    .max().orElse(0);
            int tinySpaceCount = headerLength - headerLengthWithNoTinySpaces;

            int length = this.header.get(i).length();
            Alignment alignment = columns[i].getAlignment();
            if (length > headerLengthWithNoTinySpaces) {
                int spaceCount = length - headerLengthWithNoTinySpaces;
                formattedColumns.set(i, formattedColumns.get(i).stream()
                        .map(s -> {
                            if (alignment == Alignment.LEFT) {
                                return s + "</code> <code>".repeat(tinySpaceCount) + " ".repeat(spaceCount);
                            } else {
                                return " ".repeat(spaceCount) + "</code> <code>".repeat(tinySpaceCount) + s;
                            }
                        })
                        .toList());
                headerLengthWithNoTinySpaces = length;
            }


            int spaceCount = headerLengthWithNoTinySpaces - length;

            if (alignment == Alignment.LEFT) {
                header.append(this.header.get(i));
                header.append("</code> <code>".repeat(tinySpaceCount));
                header.append(" ".repeat(spaceCount));
            } else {
                header.append(" ".repeat(spaceCount));
                header.append("</code> <code>".repeat(tinySpaceCount));
                header.append(this.header.get(i));
            }

            if (i != this.header.size() - 1) {
                header.append(columnSeparator);
            }
        }
        if (!header.isEmpty()) {
            int headerLengthWithNoTinySpaces = header.toString().replace("</code> <code>", "").length();
            int tinySpaceCount = header.toString().replace("</code> <code>", " ").length() - headerLengthWithNoTinySpaces;

            header.append("\n")
                    .append(headerSeparator.toString().repeat(headerLengthWithNoTinySpaces))
                    .append(headerSeparator.toString().repeat(tinySpaceCount / 2))
                    .append("\n");
        }

        StringBuilder result = new StringBuilder();
        result.append(header);
        for (int i = 0; i < formattedColumns.get(0).size(); i++) {
            for (int j = 0; j < columns.length; j++) {
                result.append(formattedColumns.get(j).get(i));
                if (j != columns.length - 1) {
                    result.append(columnSeparator);
                }
            }
            result.append("\n");
        }

        return "<code>" + result + "</code>";
    }

    private static List<String> formatNotMonospaced(List<String> rows, Alignment alignment) {
        return formatMonospaced(rows, alignment).stream()
                .map(row -> "</code>" + row + "<code>")
                .toList();
    }

    private static List<String> formatNumberWithTinySpaces(List<String> rows, Alignment alignment) {
        final DecimalFormat formatter = new DecimalFormat("#,##0.00");
        List<String> preFormatted = rows.stream()
                .map(row -> {
                    if (row == null || row.isEmpty()) {
                        return "";
                    } else {
                        return formatter.format(new BigDecimal(row));
                    }
                })
                .toList();
        int maxCommasSize = preFormatted.stream()
                .mapToInt(n -> n.length() - n.replace(",", "").length())
                .max().orElse(0);
        preFormatted = preFormatted.stream()
                .map(row -> {
                    int commasSize = row.length() - row.replace(",", "").length();
                    int spaces = maxCommasSize - commasSize;
                    String result = row.replace(",", "</code> <code>");
                    if (alignment == Alignment.LEFT) {
                        return result + "</code> <code>".repeat(spaces);
                    } else {
                        return "</code> <code>".repeat(spaces) + result;
                    }
                })
                .toList();
        int maxLength = preFormatted.stream().mapToInt(String::length).max().orElse(0);
        return formatMonospaced(preFormatted, alignment);
    }

    private static List<String> formatMonospaced(List<String> rows, Alignment alignment) {
        int maxLength = rows.stream().mapToInt(String::length).max().orElse(0);
        return rows.stream().map(row -> {
            int spaces = maxLength - row.length();
            if (alignment == Alignment.LEFT) {
                return row + " ".repeat(spaces);
            } else {
                return " ".repeat(spaces) + row;
            }
        }).toList();
    }


    private static List<List<String>> getRaw(Column[] column) {
        List<List<String>> rawData = new ArrayList<>();
        for (int i = 0; i < column.length; i++) {
            rawData.add(new ArrayList<>());
        }

        boolean hasNexRow = true;
        while (hasNexRow) {
            hasNexRow = false;
            for (int i = 0; i < column.length; i++) {
                try {
                    String row = column[i].getRowSource().apply(rawData.get(i).size());
                    if (row != null) {
                        hasNexRow = true;
                        rawData.get(i).add(row);
                    }
                } catch (Exception e) {
                }
            }
        }
        return rawData;
    }


    @Data
    @AllArgsConstructor
    public static class Column {
        private Alignment alignment = Alignment.LEFT;
        private ColumnType columnType = ColumnType.MONOSPACED;
        private final Function<Integer, String> rowSource;

        public Column(Alignment alignment, Function<Integer, String> rowSource) {
            this.alignment = alignment;
            this.rowSource = rowSource;
        }

        public Column(ColumnType columnType, Function<Integer, String> rowSource) {
            this.columnType = columnType;
            this.rowSource = rowSource;
        }

        public Column(Function<Integer, String> rowSource) {
            this.rowSource = rowSource;
        }
    }

    public enum Alignment {
        LEFT,
        RIGHT,
    }

    public enum ColumnType {
        MONOSPACED,
        NUMBER_WITH_TINY_SPACES,
        NOT_MONOSPACED,
    }
}
