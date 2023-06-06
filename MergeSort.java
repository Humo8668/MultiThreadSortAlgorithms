package Threads.SortAlgorithms;

import java.util.Random;
import java.util.concurrent.Phaser;

import Threads.SortAlgorithms.util.IntArraysUtil;
import Threads.SortAlgorithms.util.Timer;


public class MergeSort {
    static void mergeSortMonoThread(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder) {
        if(leftBound >= rightBound)
            return;
        if(rightBound - leftBound + 1 == 1) { // count of elements in sub-array is 1
            return;
        } else if(rightBound - leftBound + 1 == 2) { // count of elements in sub-array is 2, then sort them 
            if(isAscendingOrder){
                if(arr[leftBound] > arr[rightBound]) {
                    int left = arr[leftBound];
                    arr[leftBound] = arr[rightBound];
                    arr[rightBound] = left;
                }
            } else {
                if(arr[leftBound] < arr[rightBound]) {
                    int left = arr[leftBound];
                    arr[leftBound] = arr[rightBound];
                    arr[rightBound] = left;
                }
            }
        } else {
            int middle = (rightBound + leftBound) / 2;
            mergeSortMonoThread(arr, leftBound, middle, isAscendingOrder);
            mergeSortMonoThread(arr, middle + 1, rightBound, isAscendingOrder);
            int[] arr_copy = new int[rightBound - leftBound + 1];
            int t = 0;
            int i = leftBound;
            int j = middle + 1;

            while(i <= middle || j <= rightBound) {
                if(i > middle) {
                    arr_copy[t] = arr[j];
                    j++;
                    t++;
                    continue;
                } else if(j > rightBound) {
                    arr_copy[t] = arr[i];
                    i++;
                    t++;
                    continue;
                }
                if(isAscendingOrder){
                    if(arr[i] < arr[j]) {
                        arr_copy[t] = arr[i];
                        i++;
                    } else {
                        arr_copy[t] = arr[j];
                        j++;
                    }
                } else {
                    if(arr[i] > arr[j]) {
                        arr_copy[t] = arr[i];
                        i++;
                    } else {
                        arr_copy[t] = arr[j];
                        j++;
                    }
                }
                t++;
            }
            for(int index = 0; index < arr_copy.length; index++) {
                arr[index + leftBound] = arr_copy[index];
            }
        }
        return;
    }

    static class MergeSortJob implements Runnable {
        private int[] arr;
        private int leftBound;
        private int rightBound;
        private boolean isAscendingOrder;
        private Phaser phaser;

        public MergeSortJob(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder, Phaser phaser) {
            this.arr = arr;
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.isAscendingOrder = isAscendingOrder;
            this.phaser = phaser;
            this.phaser.register();
        }

        @Override
        public void run() {
            mergeSortMonoThread(arr, leftBound, rightBound, isAscendingOrder);
            this.phaser.arriveAndDeregister();
        }
    } 

    static void mergeSortMultiThread_2(int[] arr, int leftBound, int rightBound, boolean isAscendingOrder, int threadsCount) {
        Phaser phaser = new Phaser(1);
        Thread[] threads = new Thread[threadsCount];
        int elementsNumberPerThread = ((rightBound - leftBound + 1) / threadsCount);
        for(int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new MergeSortJob(arr, leftBound + elementsNumberPerThread * i, leftBound + elementsNumberPerThread * (i+1) - 1, isAscendingOrder, phaser));
        }
        for(int i = 0; i < threadsCount; i++) {
            threads[i].start();
        }
        phaser.arriveAndAwaitAdvance();
    }

    public static void main(String[] args) {
        final int ELEMENTS_COUNT = 20_000_000;
        final int THREADS_COUNT = 4;
        //final int ELEMENTS_COUNT = 10;
        final boolean PRINT_ARRAY = false;
        Timer timer = new Timer();
        int[] arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        if(PRINT_ARRAY) {
            System.out.print("Initial array: ");
            IntArraysUtil.printArray(arr);
        }

        timer.start();
        mergeSortMonoThread(arr, 0, arr.length - 1, true);
        timer.stop();
        System.out.printf("Mono-thread sort for %d elements took execution time: %.3f s. \n", ELEMENTS_COUNT, Timer.getInSeconds(timer.getTimeInMs()));

        if(PRINT_ARRAY) {
            System.out.print("Sorted array: ");
            IntArraysUtil.printArray(arr);
        }

        timer.reset();
        arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        timer.start();
        mergeSortMultiThread_2(arr, 0, arr.length - 1, true, THREADS_COUNT);
        timer.stop();
        System.out.printf("Multi-thread sort for %d elements with threads count of %d took execution time: %.3f s. \n", 
            ELEMENTS_COUNT, 
            THREADS_COUNT, 
            Timer.getInSeconds(timer.getTimeInMs())
        );
    }
}
