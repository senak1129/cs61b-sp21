package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private Node sentinel;
    private int size;

    private class Node {
        public Node prev;
        public Node next;
        public T item;

        public Node(T item) {
            this.item = item;
            prev = null;
            next = null;
        }
    }

    public LinkedListDeque() {
        sentinel = new Node(null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public void addFirst(T item) {
        Node newNode = new Node(item);
        newNode.prev = sentinel;
        newNode.next = sentinel.next;
        if (size == 0) {
            sentinel.prev = newNode;
        } else {
            sentinel.next.prev = newNode;
        }
        sentinel.next = newNode;
        size++;
    }

    public void addLast(T item) {
        Node newNode = new Node(item);
        newNode.prev = sentinel.prev;
        newNode.next = sentinel;
        if (size == 0) {
            sentinel.next = newNode;
        } else {
            sentinel.prev.next = newNode;
        }
        sentinel.prev = newNode;
        size++;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.item + " ");
            current = current.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            T item = sentinel.next.item;
            sentinel.next.next.prev = sentinel;
            sentinel.next = sentinel.next.next;
            size--;
            return item;
        }
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            T item = sentinel.prev.item;
            sentinel.prev.prev.next = sentinel;
            sentinel.prev = sentinel.prev.prev;
            size--;
            return item;
        }
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        } else {
            return getRecursive(index, sentinel.next);
        }
    }

    private T getRecursive(int index, Node current) {
        if (index == 0) {
            return current.item;
        } else {
            return getRecursive(index - 1, current.next);
        }
    }

    public Iterator<T> iterator() {
        return new ListIterator();
    }

    private class ListIterator implements Iterator<T> {
        Node current;

        public ListIterator() {
            current = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return current != sentinel;
        }

        @Override
        public T next() {
            T item = current.item;
            current = current.next;
            return item;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof ArrayDeque)) {
            return false;
        }
        LinkedListDeque<T> other = (LinkedListDeque<T>) o;
        if (other.size() != this.size) {
            return false;
        }
        Node current1 = this.sentinel.next;
        Node current2 = other.sentinel.next;
        while (current1.next != this.sentinel) {
            if (!(current1.item.equals(current2.item))) {
                return false;
            }
        }
        return true;
    }
}
