package Threads.SortAlgorithms.util;

public class BlockingArray<T> {
    T[] arr;
    boolean[] blockMapping;

    public BlockingArray(T[] arr) {
        this.arr = arr;
        this.blockMapping = new boolean[arr.length];
    }

    public void block(int from, int to) throws InterruptedException {
        for(int i = from; i <= to; i++) {
            this.block(i);
        }
    }

    public synchronized void block(int i) throws InterruptedException {
        if(i >= arr.length || i < 0)
            throw new IndexOutOfBoundsException();
        while(isBlocked(i))
            wait();

        blockMapping[i] = true;
    }

    public synchronized void release(int from, int to) {
        for(int i = from; i <= to; i++) {
            this.release(i);
        }
    }

    public synchronized void release(int i) {
        blockMapping[i] = false;
        this.notifyAll();
    }

    public boolean isBlocked(int i) {
        return blockMapping[i];
    }

    public synchronized boolean tryBlock(int i) {
        if(!isBlocked(i)) {
            try {
                block(i);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean tryBlock(int from, int to) {
        boolean hasBlockedOne = false;
        for(int i = from; i <= to; i++) {
            if(isBlocked(i)) {
                hasBlockedOne = true;
                break;
            }
        }
        if(hasBlockedOne) {
            return false;
        }
        try {
            block(from, to);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }

    public synchronized T get(int i) {
        return arr[i];
    }

    public synchronized void set(int i, T obj) {
        arr[i] = obj;
    }

    public int length() {
        return arr.length;
    }

    public void swap(int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public void copyTo(T[] otherArr) {
        
    }
}
