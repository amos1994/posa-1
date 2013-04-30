package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;

public class EchoConnector extends Connector {

    public EchoConnector(Reactor r, ServiceHandler sh, boolean sm) {
        super(r, sh, sm);
        
    }

    @Override
    public void connect(ServiceHandler sh, Address address) throws IOException {
        if (synchronousMode){
            
            serviceHandler.getHandle().synchConnect(address);
            
            serviceHandler.open();
           
        }else{
            throw new IOException("still unimplemented");
            
        }
        
    }

    @Override
    public void handleEvent() throws IOException {
        serviceHandler.handleEvent();

    }


}
