package info.kgeorgiy.ja.korobejnikov.i18n;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Statistics<T> {
    private int count = 0;
    private T min = null;
    private T max = null;
    private String minLengthString = null;
    private String maxLengthString = null;
    private int minLength = 0;
    private int maxLength = 0;
    private BigDecimal sum;
    private final Set<T> visited = new HashSet<>();

    public boolean isNew() {
        return count == 0;
    }

    public void prepareStatistics() {
        if (count != 0) {
            sum = sum.divide(BigDecimal.valueOf(count), MathContext.DECIMAL64);
        }
    }

    public void init(final T initValue) {
        count = 0;
        visited.add(initValue);
        min = initValue;
        max = initValue;
        sum = BigDecimal.ZERO;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public T getMin() {
        return min;
    }

    public void setMin(final T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(final T max) {
        this.max = max;
    }

    public String getMinLengthString() {
        return minLengthString;
    }

    public void setMinLengthString(final String minLengthString) {
        this.minLengthString = minLengthString;
        this.minLength = minLengthString.length();
    }

    public String getMaxLengthString() {
        return maxLengthString;
    }

    public void setMaxLengthString(final String maxLengthString) {
        this.maxLengthString = maxLengthString;
        this.maxLength = maxLengthString.length();
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(final BigDecimal sum) {
        this.sum = sum;
    }

    public Set<T> getVisited() {
        return visited;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, min, max, minLength, maxLength, minLength, maxLength, sum, visited);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }
}