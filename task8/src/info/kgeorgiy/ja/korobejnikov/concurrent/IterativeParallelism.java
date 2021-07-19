package info.kgeorgiy.ja.korobejnikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {

    private final ParallelMapper mapper;

    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        validateEmpty(values);
        // :NOTE: optional
        final Function<Stream<? extends T>, T> max = stream -> stream.max(comparator).orElseThrow(IllegalArgumentException::new);
        return start(threads, values, max, max);
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: min = max(reverseOrder)
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return start(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
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
        final List<Stream<T>> subLists = splitList(threads, list);
        final List<Thread> threadsList = new ArrayList<>();
        final List<P> result = new ArrayList<>();
        for (final Stream<T> subStream : subLists) {
            result.add(null);
            final int index = result.size() - 1;
            threadsList.add(new Thread(() -> result.set(index, functionForThread.apply(subStream))));
            threadsList.get(threadsList.size() - 1).start();
        }
        for (final Thread thread : threadsList) {
            thread.join();
        }
        return collectResult.apply(result.stream());
    }

    public <T> List<Stream<T>> splitList(final int threads, final List<T> list) {
        final List<Stream<T>> result = new ArrayList<>();
        final int forEach = (list.size() / threads);
        int extraAdding = list.size() - threads * forEach;
        int leftBorder = 0;
        for (int i = 0; i < threads && forEach + extraAdding != 0; i++) {
            final int extraAddingForThread = extraAdding > 0 ? 1 : 0;
            final int left = leftBorder;
            extraAdding -= extraAddingForThread;
            result.add(list.subList(left, left + forEach + extraAddingForThread).stream());
            leftBorder += forEach + extraAddingForThread;
        }
        return result;
    }

    private <T, P> P calcByMapper(final int threads,
                                  final List<T> list,
                                  final Function<? super Stream<? extends T>, ? extends P> functionForThread,
                                  final Function<? super Stream<? extends P>, ? extends P> collectResult) throws InterruptedException {
        if (mapper == null) {
            throw new IllegalStateException("Mapper can't be null there");
        }
        final List<Stream<T>> subLists = splitList(threads, list);
        return collectResult.apply(mapper.map(functionForThread, subLists).stream());
    }

    private <T, P> P start(final int threads,
                           final List<T> list,
                           final Function<? super Stream<? extends T>, ? extends P> functionForThread,
                           final Function<? super Stream<? extends P>, ? extends P> collectResult) throws InterruptedException {
        if (mapper == null) {
            return startThreads(threads, list, functionForThread, collectResult);
        } else {
            return calcByMapper(threads, list, functionForThread, collectResult);
        }
    }
}


