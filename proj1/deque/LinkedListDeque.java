package deque;

import java.util.Iterator;

public class LinkedListDeque <T> implements Deque<T>, Iterable<T>{

    private LinkedNode<T> sentinel = new LinkedNode<>(null, null, null);
    private int size;

    LinkedListDeque()
    {
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    LinkedListDeque(T item)
    {
        sentinel.next = new LinkedNode<>(item, sentinel, sentinel);
        sentinel.prev = sentinel.next;
        size = 1;
    }


    @Override
    public void addFirst(T item) {
        sentinel.next = new LinkedNode<>(item, sentinel, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        size++;
    }

    @Override
    public void addLast(T item) {
        sentinel.prev.next = new LinkedNode<>(item, sentinel.prev, sentinel);
        sentinel.prev = sentinel.prev.next;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for(T t : this)
        {
            System.out.print(t.toString() + ' ');
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if(size < 1)
            return null;

        LinkedNode<T> p = sentinel.next;
        sentinel.next = p.next;
        p.next.prev = sentinel;
        size--;
        return p.item;
    }

    @Override
    public T removeLast() {
        if(size < 1)
            return null;

        LinkedNode<T> p = sentinel.prev;
        sentinel.prev = p.prev;
        p.prev.next = sentinel;
        size--;
        return p.item;
    }

    @Override
    public T get(int index) {
        if(index < 0 ||index > size - 1)
            return null;

        LinkedNode<T> current = sentinel.next;
        for(int i = 0; i < index; i++)
        {
            current = current.next;
        }
        return current.item;
    }

    public T getRecursive(int index){
        if(index < 0 ||index > size - 1)
            return null;

        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, LinkedNode<T> current){
        if(index == 0)
            return current.item;
        else
            return getRecursiveHelper((index - 1), current.next);
    }
    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(!(o instanceof LinkedListDeque))
            return false;
        LinkedListDeque<?> head = (LinkedListDeque<?>) o;
        if(head.size != size)
            return false;
        for(int i = 0; i < size; i++)
        {
            if(head.get(i) != this.get(i))
                return false;
        }
        return true;
    }
    public Iterator<T> iterator(){
        return new LinkListDequeIterator();
    }
    public static class LinkedNode <N>{
        private N item;
        private LinkedNode<N> prev;
        private LinkedNode<N> next;

        LinkedNode(N i,LinkedNode<N> p,LinkedNode<N> n)
        {
            item = i;
            prev = p;
            next = n;
        }

        @Override
        public String toString(){
            if(item == null)
                return null;
            else
                return item.toString();
        }
    }

    private class LinkListDequeIterator implements Iterator<T>{

        private LinkedNode<T> current = sentinel.next;
        @Override
        public boolean hasNext() {
            return current == sentinel;
        }

        @Override
        public T next() {
            T iterm = current.item;
            current = current.next;
            return iterm;
        }
    }
}
