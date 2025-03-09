package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import net.sf.saxon.om.Item;
import org.junit.Test;
import timingtest.AList;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */

public class TestBuggyAList {
  // YOUR TESTS HERE
  @Test
  public void testThreeAddThreeRemove(){
    AListNoResizing<Integer> correct = new AListNoResizing<>();
    BuggyAList<Integer> broken = new BuggyAList<>();
    correct.addLast(1);
    correct.addLast(2);
    correct.addLast(3);

    broken.addLast(1);
    broken.addLast(2);
    broken.addLast(3);
    assertEquals(correct.size(), broken.size());

    assertEquals(correct.removeLast(), broken.removeLast());
    assertEquals(correct.removeLast(), broken.removeLast());
    assertEquals(correct.removeLast(), broken.removeLast());
  }
  @Test
  public void randomizedTest(){
    AListNoResizing<Integer> correct = new AListNoResizing<>();
    BuggyAList<Integer> broken = new BuggyAList<>();

    int N = 5000;
    for (int i = 0; i < N; i += 1) {
      int operationNumber = StdRandom.uniform(0, 4);
      if (operationNumber == 0) {
        // addLast
        int randVal = StdRandom.uniform(0, 100);
        correct.addLast(randVal);
        broken.addLast(randVal);
        assertEquals(correct.size(), broken.size());
      } else if (operationNumber == 1) {
        // size
        assertEquals(correct.size(), broken.size());
      }else if(operationNumber == 2 && correct.size() > 0){
        //getlast
        assertEquals(correct.getLast(), broken.getLast());
      }else if(operationNumber == 3 && correct.size() > 0){
        //removelast
        assertEquals(correct.removeLast(), broken.removeLast());
      }
    }
  }

}
