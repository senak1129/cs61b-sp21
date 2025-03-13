package deque;

import org.junit.Test;

public class ArrayDequeTest {
    @Test
    public void Test() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addLast(1);
        lld1.printDeque();
    }
}
