package deque;

public interface Deque {
    int size();
    default boolean isEmpty(){
        return size() == 0;
    }
}
