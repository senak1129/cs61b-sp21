package deque;

import org.junit.Test;

import java.util.Iterator;

public class ArrayDequeTest {
    @Test
    public void Test() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addLast(1);
        lld1.addLast(2);
        lld1.addLast(3);
        lld1.addLast(4);
        lld1.addLast(5);
        lld1.addLast(6);
        lld1.addLast(7);
        lld1.addLast(8);
        lld1.addLast(9);
        lld1.addLast(10);
        lld1.addLast(11);
        lld1.removeLast();
        lld1.removeFirst();
        Iterator<Integer> i1 = lld1.iterator();
        while(i1.hasNext()) {
            System.out.println(i1.next());
        }
    }
}
