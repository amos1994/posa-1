package br.com.javayuga.posa.concurrent;

/**
 * 
 * Programming Assignment for Week 5 - version b
 * 
 * Dining Philosophers thread
 * 
 * making Monitor Pattern more explicit by getting rid of Semaphore
 * 
 * usage: java ProgramW5b
 * 
 */

public class ProgramW5b {

    /*
     * The original implementation used simple Semaphores from
     * java.util.concurrent
     * 
     * Event though a binary semaphore can be easily viewed as an implementation
     * of a Monitor Object, the new ChopstickMonitor class makes the pattern
     * more explicit
     */

    static class ChopstickMonitor {

        public boolean used;

        public synchronized void pickUp() throws InterruptedException {

            /*
             * the synchronized keyword acquires the lock for each object
             * instance, and halts execution until the chopstick is not used
             * 
             * wait() is put within a while loop to guard against spurious
             * thread wakeup calls which are sometimes reported, especially in
             * some Linux environments
             */
            while (used) {
                wait();

            }

            used = true;

        }

        public synchronized void putDown() {

            used = false;
            notify();

        }

        public boolean isUsed() {
            return used;
        }

    }

    /*
     * the RunnablePhilosopher and ChopstickMonitor class definitions should be
     * kept in separate files
     * 
     * But for this assignment, it is enough for them to remain static
     */

    static class RunnablePhilosopher implements Runnable {

        private static final int PONDERING_INTERVAL = 5;
        private static final int EATING_INTERVAL = 10;
        private static final int MAX_SERVINGS = 5; // try for 500000, intervals
                                                   // adjusted by peer request

        private String name;

        private ChopstickMonitor leftChopstick;
        private ChopstickMonitor rightChopstick;

        private boolean oddSeat;

        public RunnablePhilosopher(String name, ChopstickMonitor leftChopstick,
                ChopstickMonitor rightChopstick, boolean oddSeat) {

            this.name = name;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
            this.oddSeat = oddSeat;

        }

        private boolean isSetup() {
            if (name == null)
                return false;

            if (leftChopstick == null)
                return false;

            if (rightChopstick == null)
                return false;

            return true;

        }

        /*
         * each chopstick is a monitor object which blocks execution until
         * released by a competing philosopher
         * 
         * to avoid deadlock, philosophers sitting on the odd seats pick
         * chopsticks up in the reverse order from the ones sitting on even
         * seats
         * 
         * unfortunately, this approach cannot be easily applied to other cases,
         * but it is kept here because it is a very neat trick =D
         * 
         * A peer rightfully pointed out this is a hack, and I totally agree,
         * but it is a NICE hack and I couldn't resist, sorry =D
         * 
         * a more general approach would be to assign hierarchies to resources
         * and base decisions about the order on those hierarchies
         * 
         * maybe implementing the Comparable interface for ChopstickMonitor,
         * assigning ordinal priorities at the beginning and always start by
         * picking the chopstick with lowest priority would equally avoid
         * deadlock and also avoid starvation
         */

        private synchronized void pickupChopsticks()
                throws InterruptedException {

            if (oddSeat) {
                leftChopstick.pickUp();
                System.out.println(name + " picks up the left chopstick");

                rightChopstick.pickUp();
                System.out.println(name + " picks up the right chopstick");

            } else {
                rightChopstick.pickUp();
                System.out.println(name + " picks up the right chopstick");

                leftChopstick.pickUp();
                System.out.println(name + " picks up the left chopstick");

            }

        }

        @Override
        public void run() {

            try {
                int servingsLeft = MAX_SERVINGS;

                if (!isSetup()) {
                    System.out
                            .println("philosopher setup is incorrect, missing name or a reference to one of the chopsticks. \n\n");

                    System.exit(1); // no need to proceed

                } else {

                    while (servingsLeft != 0) {

                        System.out
                                .println(name
                                        + " holds his chin, and starts pondering... :-?");

                        Thread.sleep((long) (Math.random() * PONDERING_INTERVAL));

                        System.out.println(name
                                + " wakes up from his pondering reverie. =O");

                        pickupChopsticks();

                        System.out
                                .println(name
                                        + " cleans chopsticks with a handkerchief, and starts to eat with gusto. :K");

                        Thread.sleep(EATING_INTERVAL);

                        leftChopstick.putDown();
                        System.out.println(name
                                + " puts down the left chopstick.");

                        rightChopstick.putDown();
                        System.out.println(name
                                + " puts down the right chopstick.");

                        servingsLeft--;
                        System.out.println(name + " smiles, looking at the "
                                + servingsLeft
                                + " servings left on the dish. =)");

                    }

                    System.out
                            .println(name
                                    + " realizes there are no more servings on the dish.  The meal is over!! =D");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private Thread[] thrPhilosopher;
    private RunnablePhilosopher[] runPhilosopher;
    private ChopstickMonitor[] chopsticks;

    // private String[] philosopherNames = { "Democrates", "Plato", "Hypatia",
    // "Ramon Lull", "Kant", "Hegel", "Schopenhauer", "Nietzsche", "Ryle",
    // "Beauvoir", "Dennet" };

    private static final String[] philosopherNames = { "Democrates", "Plato",
            "Ramon Lull", "Kant", "Nietzsche" };

    private ProgramW5b() {
        chopsticks = new ChopstickMonitor[philosopherNames.length];

        for (int i = 0; i < philosopherNames.length; i++) {

            chopsticks[i] = new ChopstickMonitor();

        }

        thrPhilosopher = new Thread[philosopherNames.length];
        runPhilosopher = new RunnablePhilosopher[philosopherNames.length];

        /*
         * (i + 1) % philosopherNames.length is the loopback
         * 
         * i%2!=0 indicates if seat is odd
         */
        for (int i = 0; i < philosopherNames.length; i++) {
            runPhilosopher[i] = new RunnablePhilosopher(philosopherNames[i],
                    chopsticks[i],
                    chopsticks[(i + 1) % philosopherNames.length],
                    (i % 2 != 0) ? true : false);

            thrPhilosopher[i] = new Thread(runPhilosopher[i]);

        }

    }

    public void haveDinner() {
        System.out.println("Dinner is starting! \n");

        for (Thread t : thrPhilosopher) {
            t.start();

        }

        for (Thread t : thrPhilosopher) {
            try {
                t.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Dinner is over!");
    }

    // using the Singleton pattern is not really necessary
    // but it works well for this specific assignment
    //
    private static ProgramW5b _instance = new ProgramW5b();

    public static ProgramW5b get_instance() {
        return _instance;

    }

    public static void main(String[] args) {
        ProgramW5b.get_instance().haveDinner();

    }

}
