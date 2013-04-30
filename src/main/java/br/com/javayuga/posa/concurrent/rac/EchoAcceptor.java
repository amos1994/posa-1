package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class EchoAcceptor extends Acceptor {

    public EchoAcceptor(Reactor reactor) throws IOException {
        super(reactor);

    }

    @Override
    public void open(Address address) throws IOException {
        
        acceptorHandle = new Handle(address);
        
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