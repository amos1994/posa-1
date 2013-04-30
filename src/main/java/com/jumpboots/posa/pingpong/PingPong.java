package com.jumpboots.posa.pingpong;

/**
 * Active class that will post its message to the Messenger if the Messenger's
 * message differs
 */
public class PingPong implements Runnable {

    /**
     * The message to post to the Messenger
     */
    private final String message;

    /**
     * The number of messages to send to the Messenger before exiting
     */
    private final int messageCount;

    /**
     * The Messenger to communicate with
     */
    private final Messenger messenger;

    public PingPong(String message, Messenger messenger, int messageCount) {
        this.message = message;
        this.messenger = messenger;
        this.messageCount = messageCount;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            synchronized (messenger) {
                if (messageCount == messenger.getCount()) {
                    return;
                }

                if (!messenger.getMessage().equals(message)) {
                    messenger.setMessageAndPrint(message);
                }
            }
        }
    }

    public static void main(String[] args) {
        final Messenger messenger = new Messenger();
        final Thread[] threads = new Thread[3];
        final int messageCount = threads.length;

        threads[0] = new Thread(new PingPong("Ping!", messenger, messageCount));
        threads[1] = new Thread(new PingPong("Pong!", messenger, messageCount));
        threads[2] = new Thread(new PingPong("Plaft!", messenger, messageCount));

        System.out.println("Ready... Set... Go!");

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        System.out.println("Done!");
    }

    /**
     * The Messenger accepts messages and prints those messages to stdout. The
     * Messenger also keeps track of the last printed message as well as the
     * number of printed messages
     */
    private static class Messenger {

        /**
         * The last printed message
         */
        private String message = "";

        /**
         * The count of printed messages
         */
        private int count = 0;

        public void setMessageAndPrint(String message) {
            this.message = message;
            System.out.println(this.message);
            count++;
        }

        public String getMessage() {
            return message;
        }

        public int getCount() {
            return count;
        }
    }
}