package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    public int hashing(K key) {
        int hashCode = key.hashCode();
        return (hashCode & 0x7fffffff) & (this.buckets.length - 1);
    }

    @Override
    public void clear() {
        for (int i = 0 ; i < buckets.length ; i++) {
            buckets[i].clear();
        }
        buckets = null;
        itemsCount = 0;
        keySet.clear();
    }

    @Override
    public boolean containsKey(K key) {
        return keySet().contains(key);
    }

    @Override
    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = hashing(key);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return keySet.size();
    }

    @Override
    public void put(K key, V value) {
        int index = hashing(key);
        if (containsKey(key)) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                    return;
                }
            }
        } else {
            double load = (double) (itemsCount + 1) / buckets.length;
            if (load > loadFactor) {
                Expand();
            }
            index = hashing(key);
            buckets[index].add(createNode(key, value));
            keySet.add(key);
            itemsCount++;
        }
    }

    @Override
    public Set<K> keySet() {
        return this.keySet;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = hashing(key);
        Iterator<Node> iter = buckets[index].iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node.key.equals(key)) {
                iter.remove();
                keySet.remove(key);
                itemsCount--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        int index = hashing(key);
        Iterator<Node> iter = buckets[index].iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node.key.equals(key) && node.value.equals(value)) {
                iter.remove();
                keySet.remove(key);
                itemsCount--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet.iterator();
    }

    public void Expand(){
        int newSize = buckets.length * 2;
        Collection<Node>[] newBuckets = createTable(newSize);
        for (int i = 0; i < size(); i++) {
            for (Node node : buckets[i]) {
                int index = hashing(node.key) ;
                newBuckets[index].add(node);
            }
        }
        buckets = newBuckets;
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int basicSize = 16;
    private double loadFactor = 0.75;
    private int itemsCount = 0;
    private Set<K> keySet = new HashSet<>();
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(basicSize);
    }

    public MyHashMap(int initialSize) {
        this.basicSize = initialSize;
        buckets = createTable(basicSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.basicSize = initialSize;
        this.loadFactor = maxLoad;
        buckets = createTable(basicSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new java.util.LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = (Collection<Node>[]) new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

}
