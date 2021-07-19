package info.kgeorgiy.ja.korobejnikov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final Deque<Runnable> deque;
    private final List<Thread> threadsList;

    public ParallelMapperImpl(final int threads) {
        threadsList = new ArrayList<>();
        deque = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            threadsList.add(new Thread(this::takeTaskAndRun));
            threadsList.get(threadsList.size() - 1).start();
        }
    }

    private void takeTaskAndRun() {
        Runnable task;
        try {
            while (true) {
                synchronized (deque) {
                    while (deque.isEmpty()) {
                        deque.wait();
                    }
                    task = deque.peek();
                    deque.poll();
                    deque.notify();
                }
                task.run();
            }
        } catch (final InterruptedException ignored) {
            //ignored
        }
    }


    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        final ParallelList<R> result = new ParallelList<>(Collections.nCopies(args.size(), null));
        synchronized (deque) {
            for (int i = 0; i < args.size(); i++) {
                final int index = i;
                deque.push(() -> result.set(index, f.apply(args.get(index))));
                deque.notify();
            }
        }
        return result.getList();
    }


    private static class ParallelList<R> {
        private final List<R> list;
        private int alreadyPushed = 0;
        private boolean ready = false;

        private ParallelList(final List<R> list) {
            this.list = new ArrayList<>(list);
        }

        private void set(final int index, final R value) {
            synchronized (this) {
                list.set(index, value);
                alreadyPushed++;
                if (list.size() == alreadyPushed) {
                    ready = true;
                    notify();
                }
            }

        }

        private List<R> getList() throws InterruptedException {
            synchronized (this) {
                while (!ready) {
                    wait();
                }
            }
            return this.list;
        }
    }


    @Override
    public void close() {
        for (final Thread thread : threadsList) {
            thread.interrupt();
            try {
                thread.join();
            } catch (final InterruptedException ignored) {
                // ignored'
            }
        }
    }

}