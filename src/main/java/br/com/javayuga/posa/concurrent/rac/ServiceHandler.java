package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;

public abstract class ServiceHandler extends EventHandler {

    protected Reactor reactor;
    protected Handle serviceHandle;
    
    public ServiceHandler(Reactor r, Handle acceptorHandle) throws IOException{
        reactor = r;
        
        serviceHandle = new Handle(acceptorHandle);
        
        setPeerHandle(serviceHandle);

    }
    
    /**
     * constructor used only for client portion
     * 
     */

    public ServiceHandler(Reactor r) throws IOException{
        reactor = r;
        
        serviceHandle = new Handle();
        
        setPeerHandle(serviceHandle);

    }

    
    public Handle getHandle(){
        return serviceHandle;
        
    }
    
    public void setHandle(Handle h){
        serviceHandle = h;
        
    }
    
    public abstract void open() throws IOException;

}
