package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class BSTMap <K extends Comparable<K>, V> implements Map61B<K,V>{

    private Node root;
    private int size = 0;
    private class Node{
        public final K key;
        public V value;
        public Node left;
        public Node right;
         public Node(K key, V value){
             this.key = key;
             this.value = value;
         }
    }

    private Node findNode(Node node, K key){
        if(node == null)
            return null;
        int cmp = key.compareTo(node.key);
        if(cmp < 0)
            return findNode(node.left, key);
        else if(cmp > 0)
            return findNode(node.right, key);
        else
            return node;
    }
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if(findNode(root, key) != null)
            return true;
        else
            return false;
    }

    @Override
    public V get(K key) {
        Node node = findNode(root, key);
        return node == null ? null:node.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node put(Node node, K key, V value){
        if(node == null){
            size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if(cmp < 0){
            node.left = put(node.left, key, value);
        }else if(cmp > 0){
            node.right = put(node.right, key, value);
        }else {
            node.value = value;
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        addKeys(root, set);
        return set;
    }
    private void addKeys(Node node, Set<K> set){
        if(node == null)
            return;
        set.add(node.key);
        addKeys(node.left, set);
        addKeys(node.right, set);
    }

    @Override
    public V remove(K key) {
        V targetValue = get(key);
        if(targetValue != null){
            root = remove(root, key);
        }
        return targetValue;
    }

    @Override
    public V remove(K key, V value) {
        if(get(key).equals(value)){
            remove(root, key);
            return value;
        }
        return null;
    }

    private Node remove(Node node, K key){
        if(node == null)
            return null;
        int cmp = key.compareTo(node.key);
        if(cmp < 0){
            node.left = remove(node.left, key);
        }else if(cmp > 0){
            node.right = remove(node.right, key);
        }else {
            size--;
            if(node.left == null)
                return node.right;
            if(node.right == null)
                return node.left;

            Node tmp = node;
            node = findNext(node.right);
            node.right = removeNext(tmp.right);
            node.left = tmp.left;
        }
        return node;
    }
    private Node findNext(Node node){
        if(node.left == null)
            return node;
        else
            return findNext(node.left);
    }
    private Node removeNext(Node node){
        if(node.left == null)
            return node.right;
        node.left = removeNext(node.left);
        return node;
    }

    public void printInOrder(){
        printInOrder(root);
    }
    private void printInOrder(Node node){
        if(node == null)
            return;
        printInOrder(node.left);
        System.out.print(node.key+",");
        printInOrder(node.right);
    }
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    private class BSTMapIter implements Iterator<K>{
        LinkedList<Node> list;
        public BSTMapIter(){
            list = new LinkedList<>();
            list.addLast(root);
        }
        @Override
        public boolean hasNext() {
            return !list.isEmpty();
        }

        @Override
        public K next() {
            Node cur = list.removeLast();
            list.addLast(cur.left);
            list.addLast(cur.right);
            return cur.key;
        }
    }
}
