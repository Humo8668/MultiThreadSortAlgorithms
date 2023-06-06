package Threads.SortAlgorithms;

import java.util.concurrent.Phaser;

import Threads.SortAlgorithms.util.IntArraysUtil;
import Threads.SortAlgorithms.util.Timer;

class QuickSortJob implements Runnable {
    int[] arr;
    int leftBound;
    int rightBound;
    boolean isAscendingOrder;
    Phaser phaser;

    public QuickSortJob(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder, Phaser phaser) {
        this.arr = arr;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.isAscendingOrder = isAscendingOrder;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        QuickSort.QuickSort(arr, leftBound, rightBound, isAscendingOrder);
        this.phaser.arriveAndDeregister();
    }
}

public class QuickSort {

    public static void QuickSort(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder) {
        if(rightBound - leftBound + 1 < 2) {
            return;
        } else if (rightBound - leftBound + 1 == 2) {
            if(isAscendingOrder) {
                if(arr[leftBound] > arr[rightBound]) {
                    IntArraysUtil.swap(arr, leftBound, rightBound);
                }
            } else {
                if(arr[leftBound] < arr[rightBound]) {
                    IntArraysUtil.swap(arr, leftBound, rightBound);
                }
            }
            return;
        }
        //int pivotIndex = (rightBound + leftBound + 1) / 2;
        float pivot = (arr[leftBound] + arr[(rightBound + leftBound) / 2] + arr[rightBound]) / 3.0f; // if Pivot value contains in array, may occur stack overflow.
        int l = leftBound;
        int r = rightBound;
        while(l < r) {
            if(isAscendingOrder) {
                if(arr[r] >= pivot) {
                    r--;
                    continue;
                }
                if(arr[l] < pivot) { // equals to pivot must be to the right from pivot
                    l++;
                    continue;
                }
                
            } else {
                if(arr[r] <= pivot) {
                    r--;
                    continue;
                }
                if(arr[l] > pivot) { // equals to pivot must be to the right from pivot
                    l++;
                    continue;
                }
            }
            IntArraysUtil.swap(arr, l, r);
            l++; r--;
        }
        /*if(r < leftBound) {
            r = leftBound;
        }*/
        QuickSort(arr, leftBound, r, isAscendingOrder);
        QuickSort(arr, r+1, rightBound, isAscendingOrder);
    }

    public static void QuickSortMultiThread(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder, int threadsCount) {
        Phaser phaser = new Phaser(threadsCount + 1);
        Thread[] threads = new Thread[threadsCount];
        int itemsPerThread = (rightBound - leftBound + 1) / threadsCount;
        for(int i = 0; i < threadsCount - 1; i++) {
            threads[i] = new Thread(new QuickSortJob(arr, leftBound + i * itemsPerThread, leftBound + (i+1) * itemsPerThread - 1, isAscendingOrder, phaser));
        }
        threads[threadsCount - 1] = new Thread(new QuickSortJob(arr, leftBound + (threadsCount - 1) * itemsPerThread, rightBound, isAscendingOrder, phaser));

        for(int i = 0; i < threadsCount; i++) {
            threads[i].start();
        }
        phaser.arriveAndAwaitAdvance();
    }

    public static void main(String[] args) {
        final int ELEMENTS_COUNT = 20_000_000;
        final int THREADS_COUNT = 4;
        final boolean PRINT_ARRAY = false;
        Timer timer = new Timer();
        int[] arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        //arr = new int[] {5, 2, 7, 6, 1, 5};
        if(PRINT_ARRAY) {
            System.out.print("Initial array: ");
            IntArraysUtil.printArray(arr);
        }
        
        timer.start();
        QuickSort(arr, 0, arr.length - 1, true);
        timer.stop();
        System.out.printf("One-thread sort for %d elements took execution time: %.3f s. \n", ELEMENTS_COUNT, Timer.getInSeconds(timer.getTimeInMs()));

        if(PRINT_ARRAY) {
            System.out.print("Sorted array: ");
            IntArraysUtil.printArray(arr);
        }

        timer.reset();
        arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        timer.start();
        QuickSortMultiThread(arr, 0, arr.length - 1, true, THREADS_COUNT);
        timer.stop();
        System.out.printf("Multi-thread sort for %d elements with threads count of %d took execution time: %.3f s. \n", 
            ELEMENTS_COUNT, 
            THREADS_COUNT, 
            Timer.getInSeconds(timer.getTimeInMs())
        );
    }
}
