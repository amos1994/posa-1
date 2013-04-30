package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;

public abstract class EventHandler {

    private Handle peerHandle; 
    
    public Handle getHandle(){
        return peerHandle;
        
    }
    
    public void setPeerHandle(Handle peerHandle){
        this.peerHandle = peerHandle;
        
    }
    
    public abstract void handleEvent()  throws IOException ;
    
}
