package br.com.javayuga.posa.concurrent;

import java.io.IOException;

import br.com.javayuga.posa.concurrent.hsha.Address;
import br.com.javayuga.posa.concurrent.hsha.HSHAServerDaemon;

/**
 * 
 * Programming Assignment for Week 6
 * 
 * Server with Wrapper, Reactor and Acceptor-Connector
 * 
 * this solution uses plain Java classes
 * 
 * 
 * there are two modes of execution, one as as Server, another as a Client
 * 
 * the client portion has been purposefully omitted from the assignment
 * 
 * 
 * usage: java -cp .:netty-3.2.6.Final.jar ProgramW6c [[PORT] [HOST]]
 * 
 * DO NOT forget to use the -classpath parameter pointing to correct JAR if
 * using a command line
 * 
 * 
 * when the program is running, the server echoes back any input sent by putty
 * or by using a plain telnet (e.g. telnet localhost 8088)
 * 
 * IMPORTANT: putty should be in RAW mode to avoid displaying terminal
 * negotiation characters
 * 
 * 
 * successfully tested with PA#3 Testing utility (adjust server and port
 * accordingly):
 * http://www.dre.vanderbilt.edu/~schmidt/Coursera/assignments/PA3/Java/
 * 
 * 
 * comments used to communicate with peer graders, not production style
 * 
 * See http://en.wikipedia.org/wiki/Comment_%28computer_programming%29 # Need
 * for comments
 * 
 * http://www.codeodor.com/index.cfm/2008/6/18/Common-Excuses-Used-To-Comment-
 * Code-and-What-To-Do-About-Them/2293
 * 
 * 
 */
public class ProgramW7 {

    /**
     * For the Wrapper Façade portion of the assignment, please check
     * 
     * https://class.coursera.org/posa-001/forum/thread?thread_id=934
     * 
     * quoting prof.Doug:
     * 
     * Java already implements the Wrapper Facade pattern, so you simply need to
     * apply it, i.e., by using the appropriate Java classes for networking and
     * concurrency.
     * 
     * 
     * package java.nio should take care of the Wrapper Façade role
     * 
     */

    
    /**
     * this implementation is very simplistic,
     * and little care was taken into parsing the command line 
     * 
     * trying to concentrate on the HSHA pattern
     * 
     * most of HSHAServerDaemon parameters are hard coded instead
     * 
     */
    final static String host = "localhost";

    final static int maxWorkerThreadPool = 8;
    final static int maxMessageQueueSize = 10;

    static Integer port = 8088;
    static boolean verbose = false;
    
    /**
     * 
     * @param args
     *            [0] the port of the host (defaults to 8088)
     *
     * @param args
     *            [1] activates the verbose mode
     * 
     */
    public static void main(String[] args) {

        try {
            if (args.length != 0) {
                try {
                    if (args.length == 1) {
                        parsePort(args[0]);

                        if (port < 1 || port > 65535) {
                            throw new Exception("invalid port " + port);

                        }

                    } else if (args.length == 2) {
                        parsePort(args[0]);

                        if (port < 1 || port > 65535) {
                            throw new Exception("invalid port " + port);

                        }
                        
                        if (args[1].equalsIgnoreCase("-verbose")) {
                            verbose = true;

                        }

                    } else {
                        throw new Exception(
                                "usage: java ProgramW6 [[PORT] [-verbose]]");
                    }

                } catch (NumberFormatException e) {
                    throw new Exception(
                            String.format(
                                    "Invalid argument: %s. Should be integer.",
                                    args[0]));
                }

            }

            HSHAServerDaemon server = new HSHAServerDaemon(new Address(host,
                    port), maxWorkerThreadPool, maxMessageQueueSize, verbose);

            server.start();

        } catch (IOException e) {
            System.err.println(e);

        } catch (Exception e) {
            System.err.println(e);

        }

    }
    
    private static void parsePort(String arg) throws Exception{
        port = Integer.parseInt(arg);

        if (port < 1 || port > 65535) {
            throw new Exception("invalid port " + port);

        }

    }

}
