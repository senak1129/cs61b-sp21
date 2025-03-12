package deque;

import org.junit.Test;

public class ArrayDequeTest {
    @Test
    public void Test() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(1);
        lld1.addFirst(2);
        lld1.addFirst(3);
        lld1.addFirst(4);
        lld1.addLast(5);
        lld1.addLast(6);
        lld1.addLast(7);
        lld1.addLast(8);
        lld1.addLast(9);
        lld1.printDeque();
    }
}
