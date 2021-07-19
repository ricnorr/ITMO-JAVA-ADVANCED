package info.kgeorgiy.ja.korobejnikov.i18n.tests;


import info.kgeorgiy.ja.korobejnikov.i18n.OutputFormatter;
import info.kgeorgiy.ja.korobejnikov.i18n.Statistics;
import info.kgeorgiy.ja.korobejnikov.i18n.TextStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MyTest {

    private static final Locale RUSSIAN = new Locale("ru", "RU");

    private static final List<String> subjects = List.of("Sentence", "Word", "Number", "Date", "Money");
    private final String path = MyTest.class.getPackage().getName().replace(".", File.separator) + File.separator;


    @Test
    @DisplayName("Tests words, numbers, dates in US locale")
    public void simpleUSTest() throws IOException {
        getStat("test_1", Locale.US, RUSSIAN);
    }

    @Test
    @DisplayName("Tests words, numbers, dates in RU locale")
    public void simpleRUTest() throws IOException {
        getStat("test_2", RUSSIAN, RUSSIAN);
    }

    @Test
    @DisplayName("Arabic words, numbers")
    public void simpleArabicTest() throws IOException {
        getStat("test_3", new Locale("ar", "ae"), RUSSIAN);
    }

    @Test
    @DisplayName("English-US long text")
    public void longUSTest() throws IOException {
        getStat("test_4", Locale.US, Locale.US);
    }

    @Test
    @DisplayName("Russian long text")
    public void longRUTest() throws IOException {
        getStat("test_5", RUSSIAN, RUSSIAN);
    }

    @Test
    @DisplayName("Russian no dates and money")
    public void noDatesMoneyRuTest() throws IOException {
        getStat("test_6", RUSSIAN, RUSSIAN);
    }

    @Test
    @DisplayName("English (US) no dates and money")
    public void noDatesMoneyUSTest() throws IOException {
        getStat("test_7", RUSSIAN, RUSSIAN);
    }


    private void validateStatistics(final Map<String, Statistics<?>> statistics, final String testName, final Locale locale) {
        final ResourceBundle bundle = ResourceBundle.getBundle(MyTest.class.getPackage().getName() + "." + testName);
        final Map<String, Map<String, String>> stringMapMap = new OutputFormatter(locale).parseStatistics(statistics);
        for (final String type : subjects) {
            for (final String fieldName : stringMapMap.get(type).keySet()) {
                final String gotString = stringMapMap.get(type).get(fieldName);
                final String expectedString = bundle.containsKey(fieldName + type) ? bundle.getString(fieldName + type) : null;
                assertEquals(expectedString, gotString);
            }
        }
    }

    private void getStat(final String testName, final Locale inLocale, final Locale outLocale) throws IOException {
        validateStatistics(
                new TextStatistics(inLocale).getStatistics(
                        Files.newBufferedReader(Path.of(path + testName + ".in")).lines().collect(Collectors.joining())),
                testName,
                outLocale
        );
    }
}
