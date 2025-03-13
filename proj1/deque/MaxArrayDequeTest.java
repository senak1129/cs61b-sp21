package deque;

import org.junit.Test;
import java.util.Comparator;
import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {

    @Test
    public void testMaxArrayDeque() {
        // 创建一个 Comparator 用于比较整数
        Comparator<Integer> comparator = Integer::compareTo;

        // 创建 MaxArrayDeque 实例
        MaxArrayDeque<Integer> deque = new MaxArrayDeque<>(comparator);

        // 向队列中添加元素
        deque.addFirst(1);
        deque.addLast(2);
        deque.addLast(3);
        deque.addLast(4);
        deque.addLast(5);

        // 获取最大值并验证
        Integer max = deque.max();
        assertEquals(5, (int) max); // 断言最大值是 5
    }
}