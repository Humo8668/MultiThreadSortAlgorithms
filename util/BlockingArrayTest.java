package Threads.SortAlgorithms.util;

import Threads.MyConsoleStream;

public class BlockingArrayTest {
    public static BlockingArray<Integer> blockingArray;
    public static void main(String[] args) {
        System.setOut(new MyConsoleStream());
        Integer[] arr = new Integer[] {1, 2, 3, 4, 5};
        blockingArray = new BlockingArray<Integer>(arr);

        try {
            System.out.println("Main thread: Blocking first 2 elements in array");
            blockingArray.block(0, 1);
            Thread waiterThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Child waiter thread: Trying to block first element in array...");
                        BlockingArrayTest.blockingArray.block(0);
                        System.out.println("Child waiter thread: The first element in array have been successfully blocked");
                        Thread.sleep(1000);
                        System.out.println("Child waiter thread: Now releasing the first element....");
                        BlockingArrayTest.blockingArray.release(0);
                        System.out.println("Child waiter thread: The first element in array have been released");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });

            Thread goOnTread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < blockingArray.length() - 1; i++) {
                        System.out.println("Child goOn thread: Trying to block elements from "+i+" to "+(i+1)+" ....");
                        if(blockingArray.tryBlock(i, i+1)) {
                            System.out.println("Child goOn thread: Have blocked elements from "+i+" to "+(i+1));
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Child goOn thread: Releasing blocked elements ["+i+", "+(i+1)+"]");
                            break;
                        }
                    }
                    
                }
            });

            System.out.println("Main thread: Starting child thread, which will try to get access to first element");
            waiterThread.start();
            goOnTread.start();
            Thread.sleep(1000);
            System.out.println("Main thread: Releasing first two elements");
            blockingArray.release(0, 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
