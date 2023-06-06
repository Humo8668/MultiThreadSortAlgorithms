package Threads.SortAlgorithms.util;

import java.util.Random;

public class IntArraysUtil {
    public static int[] getRandomArray(int ELEMENTS_COUNT) {
        final int HIGH_BOUND_OF_RANDOM = 10000;
        Random rand = new Random();
        int[] arr = new int[ELEMENTS_COUNT];
        for(int i = 0; i < ELEMENTS_COUNT; i++) {
            arr[i] = rand.nextInt(HIGH_BOUND_OF_RANDOM);
        }

        return arr;
    }

    public static void printArray(int[] arr) {
        System.out.println(" ");
        for(int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            System.out.print(" ");
        }
        System.out.println("");
    }

    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static int roundUpOnDivide(int N, int M) {
        return (N - 1) / M + 1;
    }
}
