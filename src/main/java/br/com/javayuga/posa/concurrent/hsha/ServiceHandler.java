package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

/**
 * An abstract representation of a ServiceHandler
 *
 */
public abstract class ServiceHandler extends EventHandler {

    protected Reactor reactor;
    protected TransportHandle serviceHandle;
    
    public ServiceHandler(Reactor r, TransportHandle acceptorServerHandle, SyncLayerMessageQueue queue, int priority) throws IOException{
        reactor = r;
        
        serviceHandle = new TransportHandle(acceptorServerHandle, queue, priority);
        
        setPeerHandle(serviceHandle);

    }
    
    /**
     * constructor used only for client portion
     * 
     */

    public ServiceHandler(Reactor r) throws IOException{
        reactor = r;
        
        serviceHandle = new TransportHandle();
        
        setPeerHandle(serviceHandle);

    }

    
    public TransportHandle getHandle(){
        return serviceHandle;
        
    }
    
    public void setHandle(TransportHandle h){
        serviceHandle = h;
        
    }
    
    public abstract void open() throws IOException;

}
