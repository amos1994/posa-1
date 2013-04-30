package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;

public abstract class Connector extends EventHandler{

    protected Reactor reactor;
    
    protected ServiceHandler serviceHandler;
    
    protected boolean synchronousMode;
    
    public Connector(Reactor r, ServiceHandler sh, boolean sm){
        reactor = r;
        serviceHandler = sh;
        synchronousMode = sm;
    }
    
    public abstract void connect(ServiceHandler sh, Address address) throws IOException;
    
    
    

}
