package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Handles messages from clients
 * 
 */
public class EchoTestServiceHandler extends ServiceHandler {

    public EchoTestServiceHandler(Reactor r) throws IOException {
        super(r);
        
    }

    @Override
    public void open() throws IOException {
        
        serviceHandle.setSelectionKey(reactor.registerHandler(
                serviceHandle.getsChannel(), this,
                SelectionKey.OP_READ));
        
    }

    @Override
    public void handleEvent() throws IOException {
        serviceHandle.processMessageFromServer();

    }

}