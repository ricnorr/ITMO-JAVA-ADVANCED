package info.kgeorgiy.ja.korobejnikov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Crawls sites recursively.
 */
public class WebCrawler implements Crawler {

    private final static String ARGS_FORMAT = "url [depth [downloads [extractors [perHost]]]]";
    private final static int WAIT_TIMEOUT = 2000;
    private final Downloader downloader;
    private final ExecutorService executorForDownloads;
    private final ExecutorService executorForExtractors;

    /**
     * Constructs {@code WebCrawler}
     *
     * @param downloader  downloads sites
     * @param downloaders limitation of simultaneous downloads
     * @param extractors  limitation of simultaneous extracting links
     * @param perHost     limitation of downloading from host
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.executorForDownloads = Executors.newFixedThreadPool(downloaders);
        this.executorForExtractors = Executors.newFixedThreadPool(extractors);
    }


    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(final String url, final int depth) {
        final Set<String> downloaded = ConcurrentHashMap.newKeySet();
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> exceptionMap = new ConcurrentHashMap<>();
        downloadImpl(url, visited, downloaded, exceptionMap, depth);
        return new Result(new ArrayList<>(downloaded), exceptionMap);
    }


    private void downloadImpl(final String url, final Set<String> visited, final Set<String> downloaded,
                              final Map<String, IOException> errors, int depth) {
        visited.add(url);
        Deque<String> deque = new ConcurrentLinkedDeque<>();
        deque.add(url);
        while (!deque.isEmpty()) {
            int length = deque.size();
            Phaser phaser = new Phaser(1);
            int finalDepth = depth;
            for (int i = 0; i < length; i++) {
                phaser.register();
                executorForDownloads.submit(() -> {
                    String currentUrl = deque.removeFirst();
                    try {
                        Document document = downloader.download(currentUrl);
                        downloaded.add(currentUrl);
                        if (finalDepth > 1) {
                            phaser.register();
                            executorForExtractors.submit(() -> {
                                try {
                                    for (final String link : document.extractLinks()) {
                                        if (visited.add(link)) {
                                            deque.add(link);
                                        }
                                    }
                                } catch (IOException exception) {
                                    errors.put(currentUrl, exception);
                                } finally {
                                    phaser.arrive();
                                }
                            });
                        }
                    } catch (IOException exception) {
                        errors.put(currentUrl, exception);
                    } finally {
                        phaser.arrive();
                    }
                });
            }
            phaser.arriveAndAwaitAdvance();
            depth--;
        }
    }


    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        executorForExtractors.shutdown();
        executorForDownloads.shutdown();
        try {
            executorForDownloads.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            executorForExtractors.awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        }
    }

    /**
     * Console wrap for {@link WebCrawler}.
     * Arguments format is {@code url [depth [downloads [extractors [perHost]]]]}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null || args.length < 1 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Number of arguments should be 1..5: " + ARGS_FORMAT);
            return;
        }
        final int[] parameters = {1, 1, 1, 1};
        final String url;
        url = args[0];
        for (int i = 1; i < args.length; i++) {
            try {
                parameters[i - 1] = Integer.parseInt(args[i]);
                if (parameters[i - 1] <= 0) {
                    System.err.println("Argument " + (i + 1) + " should be a positive integer, but it's <" + args[i] + ">");
                    return;
                }
            } catch (final NumberFormatException e) {
                System.err.println("Argument " + (i + 1) + " should be a positive integer, but it's <" + args[i] + ">");
                return;
            }
        }
        final Downloader downloader;
        try {
            downloader = new CachingDownloader();
        } catch (final IOException e) {
            System.err.println("Creating CachingDownloader is failed");
            return;
        }
        try (final WebCrawler crawler = new WebCrawler(downloader, parameters[1], parameters[2], parameters[3])) {
            crawler.download(url, parameters[0]).getDownloaded().forEach(System.out::println);
        }
    }
}
