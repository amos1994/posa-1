package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;


public class ServerDaemon {

    final Reactor reactor;
    
    public ServerDaemon(Address addr) throws IOException{
        reactor = new Reactor(addr);
        reactor.initializeAcceptors();
        
        System.out.println("Server up @ " + addr + '\n');
        
    }
    
    public void start(){
        Thread singleReactorThread = new Thread(reactor);
        
        singleReactorThread.start();
    }
    
}
