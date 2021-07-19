package info.kgeorgiy.ja.korobejnikov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> storage;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(final Comparator<? super E> comparator) {
        this(List.of(), comparator);
    }

    public ArraySet(final Collection<E> collection) {
        this(collection, null);
    }

    public ArraySet(final Collection<E> collection, final Comparator<? super E> comparator) {
        this.comparator = comparator;
        final SortedSet<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        storage = List.copyOf(set);
    }

    private ArraySet(final List<E> list, final Comparator<? super E> comparator) {
        storage = list;
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return storage.size();
    }

    private void validateEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("Can't get element: ArraySet is empty");
        }
    }

    @SuppressWarnings("unchecked")
    private boolean checkInvalidFromToElements(final E fromElement, final E toElement) {
        if (comparator != null) {
            return comparator.compare(fromElement, toElement) > 0;
        } else {
            return ((Comparable<E>) fromElement).compareTo(toElement) > 0;
        }
    }

    @Override
    public E first() {
        return getElement(0);
    }

    @Override
    public E last() {
        return getElement(storage.size() - 1);
    }

    private E getElement(final int index) {
        validateEmpty();
        return storage.get(index);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return storage.iterator();
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
        if (checkInvalidFromToElements(fromElement, toElement)) {
            throw new IllegalArgumentException("fromElement can't be more than toElement in subset");
        }
        return subsetImpl(getIndex(comparator, fromElement), getIndex(comparator, toElement));
    }

    private SortedSet<E> subsetImpl(final int left, final int right) {
        return new ArraySet<>(storage.subList(left, right), comparator);
    }

    private int getIndex(final Comparator<? super E> comparator, final E element) {
        final int index = Collections.binarySearch(storage, element, comparator);
        return index < 0 ? -1 - index : index;
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
        return subsetImpl(0, getIndex(comparator, toElement));
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
        return subsetImpl(getIndex(comparator, fromElement), size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object o) {
        return (Collections.binarySearch(storage, (E) o, comparator) >= 0);
    }
}
