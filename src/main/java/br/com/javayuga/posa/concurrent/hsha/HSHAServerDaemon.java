package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

/**
 * The server daemon holds an instance of
 * 
 * a Reactor implementation of InitializationDispatcher
 * 
 * a SyncLayerEchoTaskGroup, a wrapper for the Sync layer
 *
 */
public class HSHAServerDaemon {

    final Reactor reactor;
    final SyncLayerEchoTaskGroup syncLayerEchoTaskGroup;
    
    
    public HSHAServerDaemon(Address addr, int maxEchoWorkerPoolSize,
            int maxMessageQueueSize, boolean verbose) throws IOException{
        
        syncLayerEchoTaskGroup = new SyncLayerEchoTaskGroup(maxEchoWorkerPoolSize,
                maxMessageQueueSize, verbose);
        
        reactor = new Reactor(addr, syncLayerEchoTaskGroup.getMessageQueue());
        reactor.initializeAcceptors();
        
        System.out.println("Server up @ " + addr + '\n');
        
    }
    
    public void start(){
        Thread singleReactorThread = new Thread(reactor);
        
        singleReactorThread.start();
    }
    
}
