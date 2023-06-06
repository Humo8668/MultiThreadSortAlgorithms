package Threads.SortAlgorithms;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

import Threads.SortAlgorithms.util.BlockingArray;
import Threads.SortAlgorithms.util.IntArraysUtil;
import Threads.SortAlgorithms.util.Timer;

class SortingMetaData {
    private Integer[] arr;
    private Integer upperBound;

    public SortingMetaData(Integer[] arr) {
        this.arr = arr;
        this.upperBound = arr.length;
    }

    public void decreaseUpperBound() {
        synchronized(upperBound) {
            this.upperBound--;
        }
    }

    /**
     * Not including!
     * @return
     */
    public int getUpperBound() {
        synchronized(upperBound) {
            return this.upperBound;
        }
    }
}

class BubbleSortJob implements Runnable {
    private boolean isAscendingOrder;
    private SortingMetaData md;
    private BlockingArray<Integer> arr;
    private Phaser phaser;
    
    public BubbleSortJob(BlockingArray<Integer> arr, Boolean isAscendingOrder, SortingMetaData md, Phaser phaser) {
        this.arr = arr;
        this.isAscendingOrder = isAscendingOrder;
        this.md = md;
        this.phaser = phaser;
        this.phaser.register();
    }

    @Override
    public void run() {
        for(int j = 0; j < arr.length(); j++) {
        //while(md.getUpperBound() > 1) {
            //for(int i = 0; i < md.getUpperBound() - 1; i++) {
            for(int i = 0; i < arr.length() - 1; i++) {
                if(arr.tryBlock(i, i + 1)) {
                    if(isAscendingOrder) {
                        if(arr.get(i) > arr.get(i+1)) {
                            arr.swap(i, i+1);
                        }
                    } else {
                        if(arr.get(i) < arr.get(i+1)) {
                            arr.swap(i, i+1);
                        }
                    }
                    
                    /*if(i >= md.getUpperBound() - 2) // swapped last pair of elements
                    {
                        md.decreaseUpperBound();
                    }*/
                    arr.release(i, i + 1);
                } else {
                    continue;
                }
            }
        //}       
        }
        phaser.arriveAndDeregister();
    }
}


class JobsFinishIndicator {
    /**
     * Firstly, it's true. Then if one of threads declares that it's not finished, then isFinished becones false.
     * */ 
    private boolean isFinished = true;
    public synchronized void setFinished(boolean isFinished) {
        this.isFinished = (this.isFinished && isFinished);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void reset() {
        this.isFinished = true;
    }
}

class SequentialBubbleSortJob implements Runnable {
    private int[] arr;
    private boolean isAscendingOrder;
    private Phaser phaser;
    private CountDownLatch countDownLatch;
    private int from;
    private int to;
    private boolean flagOfEnding;
    private JobsFinishIndicator finishIndicator;

    public SequentialBubbleSortJob(int[] arr, int from, int to, boolean isAscendingOrder, Phaser phaser, CountDownLatch countDownLatch, JobsFinishIndicator finishIndicator) {
        this.phaser = phaser;
        this.arr = arr;
        this.isAscendingOrder = isAscendingOrder;
        this.from = from;
        this.to = to;
        this.flagOfEnding = false;
        this.finishIndicator = finishIndicator;
        this.countDownLatch = countDownLatch;
        phaser.register();
    }

    @Override
    public void run() {
        // first, ODD-phase; second = EVEN-phase
        while(!flagOfEnding)
        {
            flagOfEnding = true;
            // ODD-phase
            for(int i = from; i + 1 <= to; i=i+2) {
                if(isAscendingOrder) {
                    if(arr[i] > arr[i+1]) {
                        IntArraysUtil.swap(arr, i, i + 1);
                        flagOfEnding = false;
                    }
                } else {
                    if(arr[i] < arr[i+1]) {
                        IntArraysUtil.swap(arr, i, i + 1);
                        flagOfEnding = false;
                    }
                }
            }
            phaser.arriveAndAwaitAdvance();
            // EVEN-phase
            for(int i = from + 1; i + 1 <= to; i=i+2) {
                if(isAscendingOrder) {
                    if(arr[i] > arr[i+1]) {
                        IntArraysUtil.swap(arr, i, i + 1);
                        flagOfEnding = false;
                    }
                } else {
                    if(arr[i] < arr[i+1]) {
                        IntArraysUtil.swap(arr, i, i + 1);
                        flagOfEnding = false;
                    }
                }
            }
            finishIndicator.setFinished(flagOfEnding);
            phaser.arriveAndAwaitAdvance();
            if(!finishIndicator.isFinished()){
                flagOfEnding = false;
            }
            phaser.arriveAndAwaitAdvance();
            finishIndicator.reset();
        }
        countDownLatch.countDown();
    }
}

public class BubbleSort {

    public static void Sort(int[] arr, boolean isAscendingOrder) {
        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr.length - i - 1; j++) {
                if(isAscendingOrder) {
                    if(arr[j] > arr[j + 1]) {
                        IntArraysUtil.swap(arr, j, j+1);
                    }
                } else {
                    if(arr[j] < arr[j+1]) {
                        IntArraysUtil.swap(arr, j, j+1);
                    }
                }
            }
        }
    }

    /**
     * Each thread chaotically selects pair of elements and tries to block. If blocking successed, compares and swaps the elements.
     * If couldn't block the pair of elements at the same time, then continues to iterate through elements.
     * @param arr
     * @param isAscendingOrder
     * @param threadsCount
     */
    public static void SortChaoticMultiThread(Integer[] arr, boolean isAscendingOrder, int threadsCount) {
        Phaser phaser = new Phaser(1);
        SortingMetaData md = new SortingMetaData(arr);
        BlockingArray<Integer> blockingArray = new BlockingArray<>(arr);
        Thread[] threads = new Thread[threadsCount];
        for(int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new BubbleSortJob(blockingArray, true, md, phaser));
        }
        for(int i = 0; i < threadsCount; i++) {
            threads[i].start();
        }
        phaser.arriveAndAwaitAdvance();
    }

    public static void SortMultiThread(int[] arr, boolean isAscendingOrder, int threadsCount) throws InterruptedException {
        if(IntArraysUtil.roundUpOnDivide(arr.length, threadsCount) % 2 != 0)
        {
            throw new RuntimeException("Insufficient number of threads for such length of array.");
        }
        if(threadsCount < 2) {
            throw new RuntimeException("Number of threads must be greater or equal to 2");
        }
        Phaser phaser = new Phaser();
        JobsFinishIndicator indicator = new JobsFinishIndicator();
        CountDownLatch countDownLatch = new CountDownLatch(threadsCount);
        int itemsPerThread = IntArraysUtil.roundUpOnDivide(arr.length, threadsCount) + 1;
        Thread[] threads = new Thread[threadsCount];
        //threads[0] = new Thread(new SequentialBubbleSortJob(arr, 0, itemsPerThread, isAscendingOrder, phaser, countDownLatch, indicator));
        for(int i = 0; i < threadsCount - 1; i++) {
            threads[i] = new Thread(
                new SequentialBubbleSortJob(arr, i * itemsPerThread - i, (i+1) * itemsPerThread - i - 1, isAscendingOrder, phaser, countDownLatch, indicator)
            );
        }
        // The last thread will include all remaining items.
        threads[threadsCount - 1] = new Thread(
            new SequentialBubbleSortJob(arr, (threadsCount - 1) * itemsPerThread - (threadsCount - 1), arr.length - 1, isAscendingOrder, phaser, countDownLatch, indicator)
        );

        for(int i = 0; i < threadsCount; i++) {
            threads[i].start();
        }
        countDownLatch.await();
    }

    public static void main(String[] args) {
        final int ELEMENTS_COUNT = 100_000;
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
        //Sort(arr, true);
        timer.stop();
        System.out.printf("One-thread sort for %d elements took execution time: %.3f s. \n", ELEMENTS_COUNT, Timer.getInSeconds(timer.getTimeInMs()));

        if(PRINT_ARRAY) {
            System.out.print("Sorted array: ");
            IntArraysUtil.printArray(arr);
        }
        System.out.println("");
        // ************************************************************************
        timer.reset();
        arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        Integer[] wrapperArray = new Integer[arr.length];
        for(int i = 0; i < arr.length; i++) {
            wrapperArray[i] = arr[i];
        }
        timer.start();
        //SortChaoticMultiThread(wrapperArray, true, THREADS_COUNT);
        timer.stop();
        System.out.printf("Chaotic multi-thread sort for %d elements with threads count of %d took execution time: %.3f s. \n", 
            ELEMENTS_COUNT, 
            THREADS_COUNT, 
            Timer.getInSeconds(timer.getTimeInMs())
        );
        if(PRINT_ARRAY) {
            System.out.print("Sorted array: ");
            for(int i = 0; i < wrapperArray.length; i++) {
                System.out.print(wrapperArray[i] + " ");
            }
            System.out.println("");
        }
        System.out.println("");
        // ************************************************************************
        timer.reset();
        arr = IntArraysUtil.getRandomArray(ELEMENTS_COUNT);
        timer.start();
        try {
            SortMultiThread(arr, true, THREADS_COUNT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        System.out.printf("Sequential multi-thread sort for %d elements with threads count of %d took execution time: %.3f s. \n", 
            ELEMENTS_COUNT, 
            THREADS_COUNT, 
            Timer.getInSeconds(timer.getTimeInMs())
        );
        if(PRINT_ARRAY) {
            System.out.print("Sorted array: ");
            IntArraysUtil.printArray(arr);
        }
        System.out.println("");
        // ************************************************************************
        
        // ************************************************************************
    }
}
