package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Handles messages from clients
 * 
 */
public class EchoServiceHandler extends ServiceHandler {

    public EchoServiceHandler(Reactor reactor, Handle originalHandle)  throws IOException {
        super(reactor, originalHandle);

    }

    @Override
    public void open() throws IOException {
        
        serviceHandle.setSelectionKey(reactor.registerHandler(
                serviceHandle.getsChannel(), this,
                SelectionKey.OP_READ));
        
    }

    @Override
    public void handleEvent() throws IOException {
        serviceHandle.echoesMessageToClient();

    }

}