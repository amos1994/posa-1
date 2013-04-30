package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * An implementation of the InitializationDispatcher role
 * 
 * The Reactor takes care of the half async
 * portion of the HAHS pattern
 * 
 * it reads messages from clients
 * and enqueues them in the SyncLayerMessageQueue
 * 
 */
public class Reactor implements Runnable {
    
    private Selector selector;
    
    private int WAIT_INTERVAL = 1000;

    private Address address;
    private boolean keepRunning = true;
    
    private SyncLayerMessageQueue queue;

    public Reactor(Address addr, SyncLayerMessageQueue q) throws IOException {

        selector = Selector.open();
        address = addr;
        queue = q;

    }
    
    public void initializeAcceptors() throws IOException{
        EchoAcceptor acceptor = new EchoAcceptor(this);

        acceptor.open(address);

    }

    public void handleEvents() throws IOException {
        while (keepRunning) {
            // Wait for an event
            selector.select(WAIT_INTERVAL);

            // Get list of selection keys
            // with pending events
            //
            Iterator<SelectionKey> events = selector.selectedKeys().iterator();

            while (events.hasNext()) {
                SelectionKey selKey = (SelectionKey) events.next();

                // Remove event from the iterator to indicate
                // that it is being processed
                //
                events.remove();

                if ((selKey.isValid())
                        && (selKey.attachment() instanceof EventHandler)) {
                    EventHandler handler = (EventHandler) selKey.attachment();

                    handler.handleEvent();

                }

            }
        }

    }

    public SelectionKey registerHandler(SelectableChannel channel,
            EventHandler handler, int eventType) throws IOException {
        return channel.register(selector, eventType, handler);

    }

    public void run() {
        try {
            handleEvents();

        } catch (IOException e) {
            System.err.println(e);
            
        }

    }

    public void stopReactor() throws IOException {
        keepRunning = false;
        
    }

    public Address getAddress(){
        return address;
    }
    
    public SyncLayerMessageQueue getSyncLayerMessageQueue(){
        return queue;
    }
    
}