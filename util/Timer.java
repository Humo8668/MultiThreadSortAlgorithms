package Threads.SortAlgorithms.util;

public class Timer {
    long startTime;
    long endTime;
    boolean hasStarted = false;
    
    public void start() {
        if(hasStarted)
            return;
        this.startTime = System.currentTimeMillis();
        this.hasStarted = true;
    }

    /***
     * Stops the timer
     * @return milliseconds missed since <code>start()</code> method have been called
     */
    public long stop() {
        if(!hasStarted) 
            return 0;
        this.endTime = System.currentTimeMillis();
        this.hasStarted = false;
        return this.endTime - this.startTime;
    }

    public long getTimeInMs() {
        return this.endTime - this.startTime;
    }

    public void reset() {
        this.hasStarted = false;
        this.startTime = 0;
        this.endTime = 0;
    }

    public static double getInSeconds(long milliseconds) {
        return ((double)(milliseconds))/1000.0;
    }
}
