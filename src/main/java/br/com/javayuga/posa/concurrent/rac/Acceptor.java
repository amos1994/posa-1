package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;

public abstract class Acceptor extends EventHandler{
    
    protected Reactor reactor;
    protected Handle acceptorHandle;
    
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
