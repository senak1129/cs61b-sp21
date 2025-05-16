package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private static final Object NOT_FOUND = new Object();


    private Node root = null;
    private int size = 0;
    private class Node {
        K key;
        V val;
        Node left, right;

        public Node(K key, V val, Node left, Node right) {
            this.key = key;
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    private V get(Node x, K key) {
        if (key == null) throw new IllegalArgumentException("calls get() with a null key");
        if (x == null) return (V) NOT_FOUND;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return get(x.left, key);
        else if (cmp > 0) return get(x.right, key);
        else              return x.val;
    }

    @Override
    public V get(K key){
        return get(root, key);
    }

    @Override
    public void put(K key, V value){
        size++;
        Node x = new Node(key, value, null, null);
        if (root == null) {
            root = x;
        }
        else {
            Node cur = root;
            while (cur != null) {
                int cmp = key.compareTo(cur.key);
                if (cmp == 0) {
                    return;
                }
                else if (cmp < 0) {
                    if(cur.left == null) {
                        cur.left = x;
                        return;
                    } else {
                        cur = cur.left;
                    }
                }
                else {
                    if(cur.right == null) {
                        cur.right = x;
                        return;
                    } else {
                        cur = cur.right;
                    }
                }
            }
        }
    }

    @Override
    public boolean containsKey(K key){
        return get(root, key) != NOT_FOUND;
    }

    @Override
    public void clear(){
        size = 0;
        root = null;
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public Set<K> keySet(){
        Iterator<K> it = this.iterator();
        Set<K> set = new HashSet<K>();
        while (it.hasNext()) {
            set.add(it.next());
        }
        return set;
    }

    @Override
    public V remove(K key){
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value){
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
    }

    private class BSTIterator implements Iterator<K> {

        Stack<Node> stack = new Stack<Node>();

        BSTIterator() {
            inorderTraversal(root);
        }

        public void inorderTraversal(Node root){
            if (root == null) {
                return;
            }
            inorderTraversal(root.left);
            stack.add(root);
            inorderTraversal(root.right);
        }


        @Override
        public boolean hasNext() {
            return ! stack.empty();
        }

        @Override
        public K next() {
            return stack.pop().key;
        }


    }
    public void printsInOrder(){
        Stack<V> res = new Stack<V>();
        Iterator<K> it = this.iterator();
        while (it.hasNext()) {
            res.push(get(it.next()));
        }
        while (!res.isEmpty()) {
            System.out.println(res.pop());
        }
    }

}
