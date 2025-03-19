package deque;

import java.util.Comparator;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>,Iterable<T> {

    private int front;
    private int back;
    private int capacity;
    private T[] ADeque;
    private int size;
    public ArrayDeque(){
        ADeque=(T[])new Object[8];
        this.capacity = ADeque.length;
        front = capacity - 1;
        //front,back指的是下一位置
        back = 0;
        size = 0;
    }
    private void resize(int capacity){
        T[]a = (T[])new Object[capacity];
        for (int i = 1;i <= size;i++){
            a[i] = ADeque[(++front) % this.capacity];
        }
        this.capacity = capacity;
        front = 0;
        back = size + 1;
        ADeque = a;
    }

    public void addFirst(T item) {
        if (size == capacity){
            resize(capacity * 2);
        }
        ADeque[front] = item;
        size++;
        if (front == 0){
            front = capacity - 1;
        }else {
            front--;
        }
    }

    public void addLast(T item) {
        if (size == capacity){
            resize(capacity * 2);
        }
        ADeque[back] = item;
        size++;
        back = (back + 1) % capacity;
    }

    public int size() {
        return size;
    }

    public T Traversal(int flag, Comparator<T> c){
        if(size == 0){
            return null;
        }
        if(flag == 1){
            for (int i = (front + 1) % capacity; i != back - 1; i = (i + 1) % capacity){
                System.out.print(ADeque[i] + " ");
            }
            System.out.print(ADeque[back - 1]);
            return null;
        }else{
            T mx = ADeque[0];
            for (int i = (front + 1) % capacity; i != back - 1; i = (i + 1) % capacity){
                if(c.compare(mx,ADeque[i]) < 0){
                    mx = ADeque[i];
                }
            }
            if(c.compare(mx,ADeque[back - 1]) < 0){
                mx = ADeque[back - 1];
            }
            return mx;
        }
    }

    public void printDeque() {
        Traversal(1,null);
    }

    public T removeFirst() {
        if (size == 0){
            return null;
        }
        front = (front + 1) % capacity;
        T temp = ADeque[front];
        ADeque[front] = null;
        size--;
        if (capacity >= 16 && size < capacity / 4) {
            resize(capacity / 2);
        }
        return temp;
    }

    public T removeLast() {
        if (size == 0){
            return null;
        }
        back = back ==0?capacity-1: back -1;
        T temp = ADeque[back];
        ADeque[back] = null;
        size--;
        if (capacity >= 16 && size < capacity / 4) {
            resize(capacity / 2);
        }
        return temp;
    }

    public T get(int index) {
        if (index >= size){
            return null;
        }
        return ADeque[(front + 1 + index) % capacity];
    }

    @Override
    public Iterator<T> iterator(){
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T>{
        int siz;

        public ArrayDequeIterator(){
            siz = 0;
        }

        @Override
        public boolean hasNext() {
            return siz != size;
        }

        @Override
        public T next() {
            T item = ADeque[(front + 1 + siz) % capacity];
            siz++;
            return item;

        }
    }


    public boolean equals(Object o){
        return false;
    }
}