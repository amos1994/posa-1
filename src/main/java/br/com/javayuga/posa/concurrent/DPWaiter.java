package br.com.javayuga.posa.concurrent;

import java.util.Random;

/**
 * This is the main class of the solution. The solution implements the Conductor
 * solutions of the Dining Philosopher problem.
 * 
 * In this solution, philosophers (Threads) requests each time one chopstick to
 * the Waiter (Monitor)
 * 
 */
public class DPWaiter {

    public static final int MAX_PHILOSOPHERS = 5;
    public static final int MAX_ITERATIONS = 10;

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Thread philosophers[] = new Thread[MAX_PHILOSOPHERS];
        final Waiter waiter = new Waiter();
        for (int i = 0; i < MAX_PHILOSOPHERS; i++) {
            philosophers[i] = new Thread(new Philosopher(i + 1, waiter),
                    "Philosopher" + i);
        }

        System.out.println("Dinner is starting!");
        for (Thread philosopher : philosophers) {
            philosopher.start();
        }

        // Join threads here.
        try {
            for (Thread philosopher : philosophers) {
                philosopher.join();
            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException caught joining threads!");
        }
        System.out.println("Dinner is over!");

    }

}

/**
 * Implement the philosophers.
 * 
 * This version of the solution make use of the State pattern to keep track of
 * the states of philosophers. Since modelling only two states (thinking,
 * eating) was two trivial, I added the hungry state to indicate philosophers
 * intention to takes the chopsticks and eat.
 * 
 */
class Philosopher implements Runnable {

    private final Waiter waiter;
    private final int id;

    private PhilosopherState state = PhilosopherState.THINKING;

    public Philosopher(int id, Waiter waiter) {
        super();
        this.waiter = waiter;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        for (int i = 0; i < DPWaiter.MAX_ITERATIONS; i++) {
            think();
            hungry();
            takeLeftChopstick();
            takeRightChopstick();
            eat();
            putLeftChopstick();
            putRightChopstick();
        }
    }

    private void putLeftChopstick() {
        waiter.putLeftChopstick(id);
    }

    private void putRightChopstick() {
        waiter.putRightChopstick(id);
    }

    private void takeRightChopstick() {
        waiter.takeRightChopstick(id);
    }

    private void takeLeftChopstick() {
        waiter.takeLeftChopstick(id);
    }

    private void eat() {
        state.eat(this);
        printState();
        // Spend sometime eating.
        doSomeThing();
    }

    private void printState() {
        System.out.println("Philosopher " + id + " " + state);
    }

    private void think() {
        state.think(this);
        printState();
        // Spend sometime thinking.
        doSomeThing();
    }

    private void hungry() {
        state.hungry(this);
        printState();
        // Spend sometime hungry.
        doSomeThing();
    }

    /**
     * This function do something so the eat or think behave differently. Either
     * wait (1/4), relinquish control (1/4) or simply do nothing (1/2).
     * 
     * Wait up to 10sec so the method
     */
    private void doSomeThing() {
        Random randomGenerator = new Random();
        int todo = randomGenerator.nextInt(4);
        // Relinquish control with probability 1/4
        if (todo == 3) {
            Thread.yield();
        }
        // Wait 2 * 200ms with probability 1/4
        if (todo == 2) {
            try {
                Thread.sleep(todo * 100);
            } catch (InterruptedException e) {
            }
        }
    }

    public PhilosopherState state() {
        return state;
    }

    public void setState(PhilosopherState state) {
        this.state = state;
    }

}

/**
 * The Waiter implements the Monitor pattern using native Java Monitor Patterns.
 * 
 * This Monitor manages the Chopsticks which are Resources for the philosophers.
 * 
 */
class Waiter {

    private Chopstick[] chopsticks = new Chopstick[DPWaiter.MAX_PHILOSOPHERS];

    public Waiter() {
        super();
        for (int i = 0; i < DPWaiter.MAX_PHILOSOPHERS; i++) {
            chopsticks[i] = new Chopstick();
        }
    }

    public synchronized void takeRightChopstick(int id) {
        while (!chopsticks[Waiter.rightChopsitck(id)].isAvailable()) {
            waitChopStick();
        }
        chopsticks[Waiter.rightChopsitck(id)].take();
        System.out.println("Philosopher " + id + " takes RIGHT chopstick");
    }

    public synchronized void takeLeftChopstick(int id) {
        while (!(chopsticks[Waiter.leftChopsitck(id)].isAvailable() && chopsticks[Waiter
                .rightChopsitck(id)].isAvailable())) {
            waitChopStick();
        }
        chopsticks[Waiter.leftChopsitck(id)].take();
        System.out.println("Philosopher " + id + " takes LEFT chopstick");
    }

    public synchronized void putLeftChopstick(int id) {
        chopsticks[Waiter.leftChopsitck(id)].put();
        notifyAll();
        System.out.println("Philosopher " + id + " puts down LEFT chopstick");
    }

    public synchronized void putRightChopstick(int id) {
        chopsticks[Waiter.rightChopsitck(id)].put();
        notifyAll();
        System.out.println("Philosopher " + id + " puts down RIGHT chopstick");
    }

    private void waitChopStick() {
        try {
            wait();
        } catch (InterruptedException e) {
            System.out
                    .println("InterruptedException caught awaiting Chopstick!");
        }
    }

    private static int rightChopsitck(int id) {
        return ((id - 1) + 1) % DPWaiter.MAX_PHILOSOPHERS;
    }

    private static int leftChopsitck(int id) {
        return (id - 1);
    }
}

/**
 * Chopstick resource.
 * 
 * This resource is instantiated and managed by the Waiter which is the Monitor.
 * 
 */
class Chopstick {

    private boolean available = true;

    public boolean isAvailable() {
        return available;
    }

    protected void setAvailable(boolean available) {
        this.available = available;
    }

    public void take() {
        setAvailable(false);
    }

    public void put() {
        setAvailable(true);
    }
}

abstract class PhilosopherState {

    public static final PhilosopherState THINKING = new ThinkingState();
    public static final PhilosopherState EATING = new EatingState();
    public static final PhilosopherState HUNGRY = new HungryState();
    private final String name;

    protected PhilosopherState(String name) {
        super();
        this.name = name;
    }

    public void eat(Philosopher philophoser) {

    }

    public void think(Philosopher philophoser) {

    }

    public void hungry(Philosopher philophoser) {

    }

    @Override
    public String toString() {
        return name;
    }

}

class EatingState extends PhilosopherState {

    public EatingState() {
        super("eats");
    }

    @Override
    public void think(Philosopher philophoser) {
        philophoser.setState(THINKING);
    }

}

class HungryState extends PhilosopherState {

    public HungryState() {
        super("is hungry");
    }

    @Override
    public void eat(Philosopher philophoser) {
        philophoser.setState(EATING);
    }

}

class ThinkingState extends PhilosopherState {

    public ThinkingState() {
        super("thinks");
    }

    @Override
    public void hungry(Philosopher philophoser) {
        philophoser.setState(HUNGRY);
    }

}