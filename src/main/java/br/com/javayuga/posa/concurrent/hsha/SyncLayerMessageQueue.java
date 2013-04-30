package br.com.javayuga.posa.concurrent.hsha;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * The purpose of creating this wrapper around PriorityBlockingQueue
 * is to illustrate additional control over the message queue
 * 
 * plain PriorityBlockingQueue has no upper bounds,
 * instead it will throw an OutOfMemoryError if no memory can be allocated
 * 
 * 
 */
public class SyncLayerMessageQueue {

    private final PriorityBlockingQueue<TransportHandle> queue;
    private int maxMessageCount;

    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();

    public SyncLayerMessageQueue(int maxCount) {
        queue = new PriorityBlockingQueue<TransportHandle>();
        maxMessageCount = maxCount;
    }

    /**
     * when messageCount reaches a max, the Reactor becomes blocked
     * waiting on the notFull Condition
     * 
     */
    
    public void putMessage(TransportHandle message) throws InterruptedException {
        lock.lock();

        try {
            while (queue.size() == maxMessageCount) {
                notFull.await();
                
            }

        } finally {
            lock.unlock();
        }

        queue.put(message);

    }

    /**
     * take() deals with the lower bound 
     * (implicit notEmpty Condition)
     * 
     */
    public TransportHandle getMessage() throws InterruptedException {
        TransportHandle message = queue.take();

        lock.lock();
        try {
            notFull.signal();

        } finally {
            lock.unlock();
        }
        
        return message;
    }

    public int getMaxMessageCount() {
        return maxMessageCount;
    }

    public void setMaxMessageCount(int maxMessageCount) {
        this.maxMessageCount = maxMessageCount;
    }

}
