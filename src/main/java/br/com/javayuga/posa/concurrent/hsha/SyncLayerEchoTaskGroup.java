package br.com.javayuga.posa.concurrent.hsha;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class encapsulates
 * 
 * an instance of the SyncLayerMessageQueue
 * 
 * a threadPool of SyncLayerEchoTasks
 *
 */
public class SyncLayerEchoTaskGroup {

    private final SyncLayerMessageQueue messageQueue;

    private final ExecutorService threadPool;

    public SyncLayerEchoTaskGroup(int maxEchoWorkerPoolSize,
            int maxMessageQueueSize, boolean verbose) {

        messageQueue = new SyncLayerMessageQueue(maxMessageQueueSize);

        threadPool = Executors.newFixedThreadPool(maxEchoWorkerPoolSize);

        for (int i = 0; i < maxEchoWorkerPoolSize; i++)
            threadPool.execute(new SyncLayerEchoTask(messageQueue, i, verbose));

    }

    public SyncLayerMessageQueue getMessageQueue() {
        return messageQueue;
    }
    
}
