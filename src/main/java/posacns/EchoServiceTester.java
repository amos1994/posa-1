package posacns;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import static org.junit.Assert.*;

public class EchoServiceTester 
{

    private static int portNumber = 2048;
    private static String hostName = "127.0.0.1";
    private static int serverReactionTime = 10; 
    private static String EOL = System.getProperty("line.separator"); 

    public static void main(String[] args) {
		
        System.out.println("Running a stand alone textui JUnit test.");
        System.out.println("Usage: [portNumber [hostName [serverReactionTime]]]");
		
        if (args.length >= 1) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println(String.format("Invalid argument: %s. Should be integer.", args[0]));
                System.exit(1);
            }
        }
		
        if (args.length >= 2) {
            hostName = args[1];
        }
		
        if (args.length == 3) {
            try {
                serverReactionTime = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println(String.format("Invalid argument: %s. Should be integer.", args[2]));
                System.exit(2);
            }
        }
		
        System.out.println("Port number : " + portNumber);
        System.out.println("Host name : " + hostName);
        System.out.println("Server reaction time : " + serverReactionTime);
		
        junit.textui.TestRunner.run(new JUnit4TestAdapter(EchoServiceTester.class));
		
    }
	
    @Test
	public void connectionToServer() throws UnknownHostException, IOException {
		
        /*
         * Description
         */
		
        System.out.println("Checking if it is possible to connect to the server...");
		
        /*
         * Test
         */
		
        Socket socket = new Socket(hostName, portNumber);
        assertTrue(socket.isConnected());
        socket.close();
        assertTrue(socket.isClosed());
		
    }
	
    @Test
	public void echoFromServer() throws UnknownHostException, IOException, InterruptedException {
		
        /*
         * Description
         */
		
        System.out.println("Checking if the server echoes a short message...");
		
        /*
         * Test
         */
		
        Socket socket = new Socket(hostName, portNumber);
        byte[] bytes = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + EOL).getBytes();
        socket.getOutputStream().write(bytes);
        // Waits for the server to respond
        Thread.sleep(serverReactionTime);
        int available = socket.getInputStream().available();
        byte[] actual = new byte[available];
        socket.getInputStream().read(actual);
        socket.close();
		
        assertEquals(bytes.length, available);
        assertArrayEquals(bytes, actual);
		
    }
	
    @Test
	public void randomLongEchoFromServer() throws UnknownHostException, IOException, InterruptedException {
		
        /*
         * Test parameter
         */
        int messageSize = 1024;
		
        /*
         * Description
         */
		
        System.out.println("Checking if the server echoes a long random message...");
		
        /*
         * Test
         */
		
        byte[] bytes0 = new byte[messageSize];
        for (int i = 0; i < messageSize; i++) {
            bytes0[i] = (byte)(32 + Math.random()* (126 - 32));
        }

        byte[] bytes = (new String(bytes0) + EOL).getBytes(); 
		
        Socket socket = new Socket(hostName, portNumber);
        socket.getOutputStream().write(bytes);
        // Waits for the server to respond
        Thread.sleep(serverReactionTime);
        int available = socket.getInputStream().available();
        byte[] actual = new byte[available];
        socket.getInputStream().read(actual);
        socket.close();
		
        assertEquals(bytes.length, available);
        assertArrayEquals(bytes, actual);
		
    }
	
    @Test
	public void multipleEchoesFromServer() throws UnknownHostException, IOException, InterruptedException {
		
        /*
         * Test parameter
         */
		
        int messages = 300;
		
        /*
         * Description
         */
		
        System.out.println("Checking if the server echoes several messages from the same client...");
		
        /*
         * Test
         */
		
        Socket socket = new Socket(hostName, portNumber);
		
        for (int i = 0; i < messages; i++) {
			
            byte[] bytes = ("Message" + i + EOL).getBytes();
            socket.getOutputStream().write(bytes);
            // Waits for the server to respond
            Thread.sleep(serverReactionTime);
            int available = socket.getInputStream().available();
            byte[] actual = new byte[available];
            socket.getInputStream().read(actual);
            assertEquals(bytes.length, available);
            assertArrayEquals(bytes, actual);
			
        }
		
        socket.close();
		
    }
	
    @Test
	public void concurrentClients() throws UnknownHostException, IOException, InterruptedException {
		
        /*
         * Test parameter
         */
		
        int numConcurrentClients = 40;
		
        /*
         * Description
         */
		
        System.out.println("Checking if the server echoes several messages from several clients simultaneously connected (although the server is NOT required to pass this test)...");
		
        /*
         * Test
         */
		
        ArrayList<Socket> sockets = new ArrayList<Socket>(numConcurrentClients);
		
        for (int i = 0; i < numConcurrentClients; i++) {
            sockets.add(new Socket(hostName, portNumber));
        }
		
        // Random strings
        ArrayList<String> testData = new ArrayList<String>();
        testData.add("Rio");
        testData.add("Tokyo");
        testData.add("NYC");
        testData.add("Madrid");
        testData.add("Cairo");
		
        for (String testDatum : testData) {
            for (Socket socket : sockets) {
                byte[] bytes = (testDatum + EOL).getBytes();
                socket.getOutputStream().write(bytes);
                // Waits for the server to respond
                Thread.sleep(serverReactionTime);
                int available = socket.getInputStream().available();
                byte[] actual = new byte[available];
                socket.getInputStream().read(actual);
				
                assertEquals(bytes.length, available);
                assertArrayEquals(bytes, actual);
				
                System.out.print(".");
            }
        }
		
        for (Socket socket : sockets) {
            socket.close();
        }
    }
	
}
