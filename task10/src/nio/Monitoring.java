package nio;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Monitoring {
    private Monitoring() {}
    private static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();

    public static void monitor(final Indicator... indicators) {
        final Thread thread = new Thread(() -> {
            final Runtime runtime = Runtime.getRuntime();

            final List<Indicator> inds = new ArrayList<>(Arrays.asList(indicators));
            inds.add(indicator("threads", Thread::activeCount));
            inds.add(indicator("heap", () -> MEMORY.getHeapMemoryUsage().getUsed() >> 20));
            inds.add(indicator("off heap", () -> MEMORY.getNonHeapMemoryUsage().getUsed() >> 20));
            io(() -> {
                while (!Thread.interrupted()) {
                    Thread.sleep(1000);
                    runtime.gc();
                    System.out.println(
                            inds.stream()
                                    .map(p -> String.format("%s = %5d", p.name, p.supplier.getAsLong()))
                                    .collect(Collectors.joining(", "))
                    );
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static Indicator indicator(final String name, final LongSupplier supplier) {
        return new Indicator(name, supplier);
    }

    public static Indicator indicator(final String name, final AtomicInteger value) {
        return indicator(name, value::get);
    }

    public static Indicator indicator(final String name, final AtomicLong value) {
        return indicator(name, () -> value.get() >> 10);
    }

    interface Task {
        void run() throws IOException, InterruptedException;
    }

    public static void io(final Task task) {
        try {
            task.run();
        } catch (final Throwable e) {
            exception(e);
        }
    }

    public static void exception(final Throwable e) {
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }

    public static class Indicator {
        private final String name;
        private final LongSupplier supplier;

        public Indicator(final String name, final LongSupplier supplier) {
            this.name = name;
            this.supplier = supplier;
        }
    }
}
