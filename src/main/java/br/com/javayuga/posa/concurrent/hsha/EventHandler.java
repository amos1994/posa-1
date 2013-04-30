package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

/**
 * an abstract class representation of an EventHandler
 *
 */
public abstract class EventHandler {

    private TransportHandle peerHandle; 
    
    public TransportHandle getHandle(){
        return peerHandle;
        
    }
    
    public void setPeerHandle(TransportHandle peerHandle){
        this.peerHandle = peerHandle;
        
    }
    
    public abstract void handleEvent()  throws IOException ;
    
}
