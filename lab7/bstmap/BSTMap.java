package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

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
        if (x == null) return null;
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
        Node x = new Node(key, value, null, null);
        if (root == null) {
            root = x;
            size++;
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
                        size++;
                        return;
                    } else {
                        cur = cur.left;
                    }
                }
                else {
                    if(cur.right == null) {
                        cur.right = x;
                        size++;
                        return;
                    } else {
                        cur = cur.right;
                    }
                }
            }
        }
    }

    @Override
    public boolean containsKey(K key) {
        return containsKeyHelper(root, key);
    }

    private boolean containsKeyHelper(Node x, K key) {
        if (key == null) throw new IllegalArgumentException("calls containsKey() with a null key");
        if (x == null) return false;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return containsKeyHelper(x.left, key);
        else if (cmp > 0) return containsKeyHelper(x.right, key);
        else              return true;
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
    public V remove(K key) {
        if (key == null) throw new IllegalArgumentException("argument to remove() is null");
        V removedVal = get(key);
        if (removedVal != null) {
            root = remove(root, key);
            size--;
        }
        return removedVal;
    }

    private Node remove(Node x, K key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            x.left = remove(x.left, key);
        } else if (cmp > 0) {
            x.right = remove(x.right, key);
        } else {
            if (x.left == null) return x.right;
            if (x.right == null) return x.left;

            Node t = x;
            x = min(t.right);
            x.right = deleteMin(t.right);
            x.left = t.left;
        }
        return x;
    }

    private Node min(Node x) {
        if (x.left == null) return x;
        else return min(x.left);
    }

    private Node deleteMin(Node x) {
        if (x.left == null) return x.right;
        x.left = deleteMin(x.left);
        return x;
    }

    private void getSet(Node root,Set<Node>set){
        if (root == null) return;
        set.add(root);
        getSet(root.left, set);
        getSet(root.right, set);
    }



    @Override
    public V remove(K key, V value){
        remove(key);
        return value;
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

    public void printInOrder(){
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
