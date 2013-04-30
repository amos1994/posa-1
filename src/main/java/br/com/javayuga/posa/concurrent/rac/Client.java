package br.com.javayuga.posa.concurrent.rac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    final Reactor reactor;
    final EchoTestServiceHandler echoTestServiceHandler;
    final EchoConnector echoConnector;

    public Client(Address addr, boolean sm) throws IOException {
        reactor = new Reactor(addr);

        echoTestServiceHandler = new EchoTestServiceHandler(reactor);

        echoConnector = new EchoConnector(reactor, echoTestServiceHandler, sm);

        echoConnector.connect(echoTestServiceHandler, addr);

    }

    public void start() {
        Thread singleReactorThread = new Thread(reactor);

        singleReactorThread.start();
    }

    public void enterInteractiveMode() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(System.in));

        System.out
                .println("Write message and press ENTER to be sent to echo server "
                        + reactor.getAddress().getHost()
                        + ":"
                        + reactor.getAddress().getPort());

        System.out.println("type EXIT to finish interactive mode \n");

        while (true) {
            String message = bufferedReader.readLine();
            
            if (message.equalsIgnoreCase("EXIT")){
                break;
            }
            
            echoTestServiceHandler.getHandle().sendOverChannel(message + "\n");

        }

        reactor.stopReactor();

    }

}
