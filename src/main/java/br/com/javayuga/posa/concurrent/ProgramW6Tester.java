package br.com.javayuga.posa.concurrent;

import java.io.IOException;

import br.com.javayuga.posa.concurrent.rac.Address;
import br.com.javayuga.posa.concurrent.rac.Client;

public class ProgramW6Tester {
    
    static Client interactiveClient;
    
    static String host = "localhost";
    static Integer port = 8088;
    static boolean synchronous = true;

    public static void main(String[] args) {
        
        Address address = new Address(host, port);

        try {
            initializeInteractive(address); 
            
        } catch (IOException e) {
            System.err.println(e);
        }

    }
    
    
    private static void initializeInteractive(Address address) throws IOException{
        interactiveClient = new Client(address, synchronous );
        
        interactiveClient.start();
        
        interactiveClient.enterInteractiveMode();
        
    }

}
