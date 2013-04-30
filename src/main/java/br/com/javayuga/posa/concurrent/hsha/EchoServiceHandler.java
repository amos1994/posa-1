package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Handles messages from clients and enques them in SyncLayerMessageQueue
 * 
 */
public class EchoServiceHandler extends ServiceHandler {

    final static int commonPriority = 0;
    
    public EchoServiceHandler(Reactor reactor, TransportHandle originalHandle)  throws IOException {
        super(reactor, originalHandle, reactor.getSyncLayerMessageQueue(), commonPriority);

    }

    @Override
    public void open() throws IOException {
        
        serviceHandle.setSelectionKey(reactor.registerHandler(
                serviceHandle.getsChannel(), this,
                SelectionKey.OP_READ));
        
    }

    @Override
    public void handleEvent() throws IOException {
        try {
            serviceHandle.enqueMessage();
            
        } catch (InterruptedException e) {
            System.err.println(e);
            
        }

    }

}