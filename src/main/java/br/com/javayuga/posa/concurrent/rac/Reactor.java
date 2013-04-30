package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * An implementation of the Reactor pattern
 */
public class Reactor implements Runnable {
    
    private Selector selector;
    
    private int WAIT_INTERVAL = 1000;

    private Address address;
    private boolean keepRunning = true;

    public Reactor(Address addr) throws IOException {

        selector = Selector.open();
        address = addr;

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
    
}