package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SyncLayerMessageQueueTest {

    final static int MAX_MESSAGE = 5;
    final static int SLEEP = 20000;
    
    final static Logger logger = Logger.getLogger(SyncLayerMessageQueueTest.class);

    final static SyncLayerMessageQueue messageQueue = new SyncLayerMessageQueue(MAX_MESSAGE);

    @Test
    public void testSyncLayerFullLocking() {

        Thread consumerThread = new Thread((new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(SLEEP);
                    
                    TransportHandle m = messageQueue.getMessage();
                    
                } catch (InterruptedException e) {
                    logger.error(e);
                    
                }
                
            }}));
        
        consumerThread.start();

        try {
            for (int i = 0; i < MAX_MESSAGE + 1; i++) {
                messageQueue.putMessage(new TransportHandle());
            }

        } catch (InterruptedException e) {
            logger.error(e);
            
        } catch (IOException e) {
            logger.error(e);
            
        }

    }

}
