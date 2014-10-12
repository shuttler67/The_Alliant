package net.shuttler.alliant.client;

public class Timer {
    private double lastFrame;
    private double averageDelta = 0;
    private double delta = 0.016;

    private int timeSinceLastPrint = 0;

    public double getTime() {
        return (((double)System.nanoTime()) / 1000000000);
    }
    public double getDelta() {
        return delta;
    }

    public void update() {
        double currentTime = getTime();
        delta = currentTime - lastFrame;
        lastFrame = getTime();
        averageDelta = (averageDelta + delta)/2;
    }

    public int getFPS() {
        return (int)(1/averageDelta);
    }

    public void printFPS() {
        if (timeSinceLastPrint > 20) {
            timeSinceLastPrint = 0;
            System.out.println("CurrentFPS = " + getFPS());
        }
        timeSinceLastPrint += delta;
    }
}
