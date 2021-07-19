package info.kgeorgiy.ja.korobejnikov.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextStatistics {

    final Locale fromLocale;
    final Collator collator;
    final Comparator<Number> numberComparator;
    final Comparator<String> stringComparator;
    final NumberFormat currencyFormat;
    final NumberFormat numberFormat;
    final Comparator<Date> dateComparator;
    final List<DateFormat> dateFormatList;


    public TextStatistics(final Locale fromLocale) {
        this.fromLocale = fromLocale;
        collator = Collator.getInstance(fromLocale);
        stringComparator = collator::compare;
        numberComparator = Comparator.comparingDouble(Number::doubleValue);
        currencyFormat = NumberFormat.getCurrencyInstance(fromLocale);
        numberFormat = NumberFormat.getNumberInstance(fromLocale);
        dateComparator = Comparator.comparingLong(Date::getTime);
        dateFormatList = List.of(
                DateFormat.getDateInstance(DateFormat.FULL, fromLocale),
                DateFormat.getDateInstance(DateFormat.LONG, fromLocale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, fromLocale),
                DateFormat.getDateInstance(DateFormat.SHORT, fromLocale));
    }


    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    private <T> Statistics<T> parseTokens(final BreakIterator breakIterator,
                                          final String text,
                                          final TriFunction<String, String, ParsePosition, T> parser,
                                          final BiConsumer<Statistics<T>, T> statisticsFiller) {
        breakIterator.setText(text);
        int start = breakIterator.first();
        final Statistics<T> statistics = new Statistics<>();
        for (int end = breakIterator.next(), ignore = -1; end != BreakIterator.DONE; start = end, end = breakIterator.next()) {
            if (start < ignore) {
                continue;
            }
            final ParsePosition position = new ParsePosition(start);
            final T object = parser.apply(text, text.substring(start, end), position);
            ignore = position.getIndex();
            if (object != null) {
                statisticsFiller.accept(statistics, object);
            }
        }
        return statistics;
    }

    private String parseSentence(final String text, String word, final ParsePosition position) {
        if (word == null) {
            return null;
        }
        word = word.trim();
        if (!word.isEmpty()) {
            return word;
        }
        return null;
    }

    private String parseWord(final String text, final String word, final ParsePosition position) {
        if (word == null) {
            return null;
        }
        if (!word.isEmpty() && Character.isLetter(word.charAt(0))) {
            return word;
        } else {
            return null;
        }

    }

    private Number parseCurrency(final String text, final String word, final ParsePosition position) {
        if (text == null) {
            return null;
        }
        return currencyFormat.parse(text, position);
    }

    private Number parseNumber(final String text, final String word, final ParsePosition position) {
        if (text == null) {
            return null;
        }
        if (parseDate(text, word, position) == null && parseCurrency(text, word, position) == null) {
            return numberFormat.parse(text, position);
        }
        return null;
    }

    private Date parseDate(final String text, final String word, final ParsePosition position) {
        if (!word.isBlank()) {
            for (final DateFormat dateFormat : dateFormatList) {
                final Date date = dateFormat.parse(text, position);
                if (date != null) {
                    return date;
                }
            }
        }

        return null;
    }

    private <T> void fillStatistics(final Statistics<T> statistics,
                                    final T token,
                                    final Comparator<T> comparator,
                                    final Function<T, BigDecimal> converterForAverage) {
        if (statistics.isNew()) {
            statistics.init(token);
        }
        statistics.setCount(statistics.getCount() + 1);
        statistics.getVisited().add(token);
        if (comparator.compare(token, statistics.getMin()) < 0) {
            statistics.setMin(token);
        }
        if (comparator.compare(token, statistics.getMax()) > 0) {
            statistics.setMax(token);
        }
        statistics.setSum(statistics.getSum().add(converterForAverage.apply(token)));
    }

    private void fillStringStatistics(final Statistics<String> statistics,
                                      final String string,
                                      final Comparator<String> comparator,
                                      final Function<String, BigDecimal> converter) {
        if (string != null) {
            fillStatistics(statistics, string, comparator, converter);
            if (statistics.getMinLengthString() == null || string.length() < statistics.getMaxLengthString().length()) {
                statistics.setMinLengthString(string);
            }
            if (statistics.getMaxLengthString() == null || string.length() > statistics.getMaxLengthString().length()) {
                statistics.setMaxLengthString(string);
            }
        }
    }

    private void fillMoneyAndNumberStatistics(final Statistics<Number> statistics, final Number number) {
        fillStatistics(statistics, number, numberComparator, x -> BigDecimal.valueOf(x.doubleValue()));
    }

    private void fillSentenceStatistics(final Statistics<String> statistics, final String number) {
        fillStringStatistics(statistics, number, stringComparator, x -> BigDecimal.valueOf(x.length()));
    }

    private void fillDateStatistics(final Statistics<Date> statistics, final Date number) {
        fillStatistics(statistics, number, dateComparator, x -> BigDecimal.valueOf(x.getTime()));
    }


    public Map<String, Statistics<?>> getStatistics(final String text) {
        final BreakIterator sentenceBreakIterator = BreakIterator.getSentenceInstance(fromLocale);
        final BreakIterator wordBreakIterator = BreakIterator.getWordInstance(fromLocale);

        final Map<String, Statistics<?>> statisticsMap = new HashMap<>();

        statisticsMap.put("Money", parseTokens(
                wordBreakIterator,
                text,
                this::parseCurrency,
                this::fillMoneyAndNumberStatistics));


        statisticsMap.put("Sentence", parseTokens(
                sentenceBreakIterator,
                text,
                this::parseSentence,
                this::fillSentenceStatistics));

        statisticsMap.put("Number", parseTokens(
                wordBreakIterator,
                text,
                this::parseNumber,
                this::fillMoneyAndNumberStatistics));

        statisticsMap.put("Date", parseTokens(
                wordBreakIterator,
                text,
                this::parseDate,
                this::fillDateStatistics));

        statisticsMap.put("Word", parseTokens(
                wordBreakIterator,
                text,
                this::parseWord,
                this::fillSentenceStatistics));
        for (final Statistics<?> statistics : statisticsMap.values()) {
            statistics.prepareStatistics();
        }
        return statisticsMap;
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Illegal argument format, should be: <input_locale> <output_locale> <input_file> <output_file>");
            return;
        }
        try (final BufferedReader reader = Files.newBufferedReader(Path.of(args[2]))) {
            final Map<String, Statistics<?>> statisticsMap = new TextStatistics(Locale.forLanguageTag(args[0]))
                    .getStatistics(reader.lines().collect(Collectors.joining()));
            final Locale outputLocale = Locale.forLanguageTag(args[1]);
            try (final BufferedWriter writer = Files.newBufferedWriter(Path.of(args[3]))) {
                new OutputFormatter(outputLocale).writeInFile(writer, args[2], statisticsMap);
            } catch (final IOException e) {
                System.err.println("Failed while writing in file");
            } catch (final InvalidPathException pathException) {
                System.err.println("Invalid output path");
            }
        } catch (final IOException e) {
            System.err.println("Failed while reading input file");
        } catch (final InvalidPathException pathException) {
            System.err.println("Invalid input path");
        }
    }
}
