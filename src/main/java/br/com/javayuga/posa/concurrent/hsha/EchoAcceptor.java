package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * The concrete implementation of the Acceptor role 
 *
 */
public class EchoAcceptor extends Acceptor {

    public EchoAcceptor(Reactor reactor) throws IOException {
        super(reactor);

    }

    @Override
    public void open(Address address) throws IOException {
        
        acceptorHandle = new TransportHandle(address);
        
        acceptorHandle.setSelectionKey(reactor.registerHandler(
                acceptorHandle.getSsChannel(), this, SelectionKey.OP_ACCEPT));
        
    }

    @Override
    protected void makeServiceHandler() throws IOException {
        serviceHandler = new EchoServiceHandler(reactor, acceptorHandle);
        
        serviceHandler.getHandle().acceptSocketChannel();

        serviceHandler.open();

    }


}