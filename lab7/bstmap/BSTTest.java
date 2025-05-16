package bstmap;

import org.w3c.dom.Node;

import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class BSTTest {
    public static void main(String[] args) {
        BSTMap<Integer, Integer> b = new BSTMap<Integer, Integer>();
        b.put(1, 1);
        b.put(2, 2);
        b.put(3, 3);
        b.put(4, 4);
        b.put(5, 5);
        Iterator<Integer> it = b.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
