package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

/**
 * the abstract class which represents the Acceptor role
 *
 */
public abstract class Acceptor extends EventHandler{
    
    protected Reactor reactor;
    protected TransportHandle acceptorHandle;
    
    protected ServiceHandler serviceHandler;
    
    public Acceptor(Reactor reactor){
        this.reactor = reactor;
        
    }

    public abstract void open(Address address) throws IOException;

    protected abstract void makeServiceHandler() throws IOException;
    
    public void accept() throws IOException{
        makeServiceHandler();
    }
    
    public void handleEvent() throws IOException{
        accept();
    }
    
}
