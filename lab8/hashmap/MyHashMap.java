package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {


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
    // You should probably define some more!
    private int size = 0;
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        this(16,0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        size = 0;
        loadFactor =maxLoad;
        buckets = new Collection[initialSize];
        for(int i = 0; i < initialSize; i++){
            buckets[i] = createBucket();
        }
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
        return new ArrayList<>();
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
        return null;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        buckets = null;
        size = 0;
        loadFactor = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) == null ? false : true;
    }

    @Override
    public V get(K key) {
        return getNode(key) == null ? null : getNode(key).value;
    }
    private Node getNode(K key){
        if(size == 0)
            return null;
        int index = Math.floorMod(key.hashCode(), buckets.length);
        for(Node node : buckets[index]){
            if(node.key.equals(key))
                return node;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if(getNode(key) != null)
        {
            getNode(key).value = value;
            return;
        }
        int index = Math.floorMod(key.hashCode(), buckets.length);
        buckets[index].add(createNode(key, value));
        size++;
        if(size / buckets.length > loadFactor){
            resize(buckets.length*2);
        }

    }
    private void resize(int newSize){
        Collection<Node>[] resized = new Collection[newSize];
        for(int i = 0; i < newSize; i++)
        {
            resized[i] = createBucket();
        }
        for(int i = 0; i < buckets.length; i++)
        {
            for(Node node : buckets[i])
            {
                int index = Math.floorMod(node.key.hashCode(), newSize);
                resized[index].add(node);
            }
        }
        buckets = resized;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (Collection<Node> items : buckets) {
            for (Node node : items) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        Node node = getNode(key);
        if(node == null)
            return null;
        else {
            buckets[index].remove(node);
            size--;
            return node.value;
        }
    }

    @Override
    public V remove(K key, V value) {
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new HMIterator();
    }

    private class HMIterator implements Iterator{
        ArrayDeque<Node> queue;
        private HMIterator(){
            queue = new ArrayDeque<>();
            for(Collection<Node> items : buckets){
                for(Node node : items){
                    queue.addFirst(node);
                }
            }
        }
        @Override
        public boolean hasNext() {
            return queue.isEmpty();
        }

        @Override
        public Object next() {
            return queue.removeLast();
        }
    }
}
