package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.comparator = c;
    }

    public T max() {
        return Traversal(0, comparator);
    }

    public T max(Comparator<T> c) {
        return Traversal(0, c);
    }
}
