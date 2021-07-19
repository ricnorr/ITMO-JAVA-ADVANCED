package info.kgeorgiy.ja.korobejnikov.i18n;


import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;


public class OutputFormatter {
    private final ResourceBundle bundle;
    private static final String bundlePath = OutputFormatter.class.getPackage().getName() + ".UsageResourceBundle";

    private static final String SENTENCE = "Sentence";
    private static final String WORD = "Word";
    private static final String NUMBER = "Number";
    private static final String DATE = "Date";
    private static final String MONEY = "Money";


    private static final String AMOUNT = "amount";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String AVERAGE = "average";
    private static final String VISITED = "visited";
    private static final String MIN_LEN = "minLen";
    private static final String MAX_LEN = "maxLen";
    private static final String MAX_LEN_CNT = "maxLenCnt";
    private static final String MIN_LEN_CNT = "minLenCnt";
    private static final String TITLE = "title";
    private static final String FORMAT1 = "format1";
    private static final String FORMAT2 = "format2";
    private static final String FORMAT3 = "format3";


    private final String formatOne;
    private final String formatTwo;
    private final String formatThree;
    private static final List<String> subjects = List.of(SENTENCE, WORD, NUMBER, MONEY, DATE);


    private final Map<String, Format> formatMap;


    private String toStatLine(final String format, final Object... objects) {
        if (Arrays.stream(objects).anyMatch(Objects::isNull)) {
            return "";
        }
        return System.lineSeparator() + "\t" + MessageFormat.format(format, objects);
    }


    private String getFromBundle(final String s) {
        if (bundle.containsKey(s)) {
            return bundle.getString(s);
        }
        return null;
    }


    private String parseInFormat(final String mode, final Object object) {
        if (object instanceof String) {
            return String.join("", "\"", (String) object, "\"");
        }
        return formatMap.get(mode) == null || object == null ? bundle.getString("absent") : formatMap.get(mode).format(object);
    }

    private String printLongStat2(final Map<String, String> statistics, final String suf) {
        final String count = statistics.get(AMOUNT);
        final String min = statistics.get(MIN);
        final String max = statistics.get(MAX);
        final String average = statistics.get(AVERAGE);

        final String minLen = statistics.get(MIN_LEN);
        final String maxLen = statistics.get(MAX_LEN);
        final String minLenCnt = statistics.get(MIN_LEN_CNT);
        final String maxLenCnt = statistics.get(MAX_LEN_CNT);
        final String visited = statistics.get(VISITED);


        return String.join("",
                getFromBundle(TITLE + suf),
                toStatLine(formatThree, getFromBundle(AMOUNT + suf), count, visited, bundle.getString("different")),
                toStatLine(formatOne, getFromBundle(MIN + suf), min),
                toStatLine(formatOne, getFromBundle(MAX + suf), max),
                toStatLine(formatTwo, getFromBundle(MIN_LEN + suf), minLenCnt, minLen),
                toStatLine(formatTwo, getFromBundle(MAX_LEN + suf), maxLenCnt, maxLen),
                toStatLine(formatOne, getFromBundle(AVERAGE + suf), average));
    }

    private String printStats(final Map<String, Map<String, String>> map) {
        return OutputFormatter.subjects.stream()
                .map(x -> printLongStat2(map.get(x), x)).collect(Collectors.joining(System.lineSeparator()));

    }

    private String printSummaryStats(final Map<String, Map<String, String>> map) {
        return OutputFormatter.subjects.stream()
                .map(x -> MessageFormat.format(
                        formatOne,
                        bundle.getString(AMOUNT + x), map.get(x).get(AMOUNT))
                )
                .collect(Collectors.joining(System.lineSeparator() + "\t", "\t", ""));
    }

    public Map<String, Map<String, String>> parseStatistics(final Map<String, Statistics<?>> map) {
        final Map<String, Map<String, String>> result = new HashMap<>();
        subjects.forEach(x -> {
            result.put(x, new HashMap<>());
            final Statistics<?> stat = map.get(x);
            final Map<String, String> current = result.get(x);
            current.put(AMOUNT, parseInFormat(NUMBER, stat.getCount()));
            current.put(MIN, parseInFormat(x, stat.getMin()));
            current.put(MAX, parseInFormat(x, stat.getMax()));
            current.put(VISITED, parseInFormat(NUMBER, stat.getVisited().size()));
            if (x.equals(DATE) || x.equals(MONEY)) {
                current.put(AVERAGE, parseInFormat(x, stat.getSum()));
            } else {
                current.put(AVERAGE, parseInFormat(NUMBER, stat.getSum()));
            }
            if (x.equals(WORD) || x.equals(SENTENCE)) {
                current.put(MIN_LEN, parseInFormat(WORD, stat.getMinLengthString()));
                current.put(MAX_LEN, parseInFormat(WORD, stat.getMaxLengthString()));
                current.put(MIN_LEN_CNT, parseInFormat(NUMBER, stat.getMinLength()));
                current.put(MAX_LEN_CNT, parseInFormat(NUMBER, stat.getMaxLength()));
            }
        });
        return result;
    }


    public OutputFormatter(final Locale locale) {
        bundle = ResourceBundle.getBundle(bundlePath, locale);
        formatMap = Map.of(
                NUMBER, NumberFormat.getNumberInstance(locale),
                DATE, DateFormat.getDateInstance(DateFormat.LONG, locale),
                MONEY, NumberFormat.getCurrencyInstance(locale));
        formatOne = bundle.getString(FORMAT1);
        formatTwo = bundle.getString(FORMAT2);
        formatThree = bundle.getString(FORMAT3);
    }

    public void writeInFile(final BufferedWriter writer, final String inputFilename, final Map<String, Statistics<?>> map)
            throws IOException {
        final Map<String, Map<String, String>> parsedStat = parseStatistics(map);
        writer.write(String.join(
                System.lineSeparator(),
                MessageFormat.format(bundle.getString("analyzedFile"), inputFilename),
                bundle.getString("summary"),
                printSummaryStats(parsedStat),
                printStats(parsedStat)
        ));
    }
}
