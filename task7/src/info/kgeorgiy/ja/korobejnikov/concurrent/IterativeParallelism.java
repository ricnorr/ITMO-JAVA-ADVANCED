package info.kgeorgiy.ja.korobejnikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {


    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        validateEmpty(values);
        // :NOTE: optional
        Function<Stream<? extends T>, T> max = stream -> stream.max(comparator).orElseThrow(IllegalArgumentException::new);
        return startThreads(threads, values, max, max);
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: min = max(reverseOrder)
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return startThreads(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T> void validateEmpty(final List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List of values can't be empty");
        }
    }

    private <T, P> P startThreads(final int threads,
                                  final List<T> list,
                                  final Function<? super Stream<? extends T>, ? extends P> functionForThread,
                                  final Function<? super Stream<? extends P>, ? extends P> collectResult
    ) throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads must be > 0");
        }
        final int forEach = (list.size() / threads);
        final List<Thread> threadsList = new ArrayList<>(threads);
        final List<P> result = new ArrayList<>();
        int extraAdding = list.size() - threads * forEach;
        int leftBorder = 0;
        for (int i = 0; i < threads && forEach + extraAdding != 0; i++) {
            final int extraAddingForThread = extraAdding > 0 ? 1 : 0;
            extraAdding -= extraAddingForThread;
            final int index = i;
            result.add(null);
            final int left = leftBorder;
            threadsList.add(new Thread(() -> result.set(
                    index,
                    functionForThread.apply(list.subList(left, left + forEach + extraAddingForThread).stream()))));
            threadsList.get(i).start();
            leftBorder += forEach + extraAddingForThread;
        }
        for (final Thread thread : threadsList) {
            thread.join();
        }
        return collectResult.apply(result.stream());
    }
}


