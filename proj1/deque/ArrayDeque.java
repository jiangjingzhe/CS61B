package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    private T[] data;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    public ArrayDeque(){
        data = (T[]) new Object[8];
        front = 4;
        rear = 5;
        size = 0;
        capacity = 8;
    }
//     public ArrayDeque(T t){
//         data = (T[]) new Object[8];
//         front = 4;
//         data[front--] = t;
//         rear = 5;
//         size = 1;
//         capacity = 8;
//     }

    private void resize(int num){
        T[] newData = (T[]) new Object[num];
        for(int i = 0; i < size; i++)
        {
            front = (front + 1) % capacity;
            newData[i] = data[front];
        }
        data = newData;
        front = num - 1;
        rear = size;
        capacity = num;
    }
    @Override
    public void addFirst(T item) {
        if(size == capacity)
            resize(capacity*2);
        size++;
        data[front] = item;
        front = (front + capacity - 1) % capacity;

    }

    @Override
    public void addLast(T item) {
        if(size == capacity)
            resize(capacity*2);
        size++;
        data[rear] = item;
        rear = (rear + 1) % capacity;
    }

//    @Override
//    public boolean isEmpty() {
//        return size == 0;
//    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for(int i = 1; i <= size; i++)
        {
            System.out.print(data[(front + i) % capacity].toString() + ' ');
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        T t;
        if (isEmpty())
            return null;
        else {
            front = (front + 1) % capacity;
            t = data[front];
            size--;
        }
        if(size < capacity/4 && capacity >32)
            resize(capacity/4);
        return t;
    }

    @Override
    public T removeLast() {
        T t;
        if (isEmpty())
            return null;
        else {
            rear = (rear + capacity - 1) % capacity;
            t = data[rear];
            size--;
        }
        if(size < capacity/4 && capacity >32)
            resize(capacity/4);
        return t;
    }

    @Override
    public T get(int index) {
        if(index < 0 || index >= size)
            return null;
        int x = (front + index +1) % capacity;
        return data[x];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Deque) || ((Deque<?>) o).size() != size()) {
            return false;
        }
        if (o == this) {
            return true;
        }

        for (int i = 0; i < size; i++) {
            Object item = ((Deque<?>) o).get(i);
            if (!item.equals(get(i))) {
                return false;
            }
        }
        return true;
    }

    private class ArrayDequeIterator implements Iterator<T>{
        private int index = 0;
        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public T next() {
            return get(index++);
        }
    }
}
