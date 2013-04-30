package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The TransportHandle implements the Comparable interface
 * to illustrate how messages could be
 * prioratized in SyncLayerMessageQueue
 * 
 * Higher priorities could be given to individual clients
 * whose messages would move up to the head of the queue
 * 
 * In this implementation, however, all messages have the same priority
 * See EchoServiceHandler where a dummy 
 * commonPriority parameter is always set to 0 
 * 
 */
public class TransportHandle implements Comparable<TransportHandle> {

    private ServerSocketChannel ssChannel;

    private SocketChannel sChannel;
    private SelectionKey selectionKey;

    private int transportPriority;

    public static final int BUFFER_SIZE = 256;
    public static final char MESSAGE_END = '\n';

    private String incomingData;
    private String message;
    
    private SyncLayerMessageQueue messageQueue;

    public TransportHandle() throws IOException {
        incomingData = "";
        transportPriority = 0;

    }

    public TransportHandle(Address address) throws IOException {
        incomingData = "";
        transportPriority = 0;
        initializeHandleServerChannel(address);

    }

    public TransportHandle(TransportHandle originalServerHandle, SyncLayerMessageQueue queue, int priority)
            throws IOException {
        incomingData = "";
        transportPriority = priority;
        messageQueue = queue;

        ssChannel = originalServerHandle.getSsChannel();

        if (ssChannel == null) {
            throw new IOException(
                    "trying to create a client socket channel without a corresponding server socket channel");
        }

    }

    /**
     * 
     * 
     */
    public void initializeHandleServerChannel(Address address)
            throws IOException {

        if (ssChannel == null) {
            ssChannel = ServerSocketChannel.open();
            ssChannel.configureBlocking(false);
            ssChannel.socket()
                    .bind(new InetSocketAddress(address.getHost(), address
                            .getPort()));

        }

    }

    /**
     * Accessors
     * 
     * 
     */
    public ServerSocketChannel getSsChannel() {
        return ssChannel;

    }

    public SocketChannel getsChannel() {
        return sChannel;
    }

    public int getTransportPriority() {
        return transportPriority;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    /**
     *
     * 
     * 
     */
    public SocketChannel acceptSocketChannel() throws IOException {
        sChannel = ssChannel.accept();

        if (sChannel != null) {
            SocketAddress address = sChannel.socket().getRemoteSocketAddress();
            System.out.println("Accepting connection from " + address);

            sChannel.configureBlocking(false);
        }

        return sChannel;

    }

    /**
     * @throws IOException 
     * @throws InterruptedException 
    *
    * 
    * 
    */
    public void enqueMessage() throws IOException, InterruptedException {
        
        SocketAddress address = sChannel.socket().getLocalSocketAddress();
        
        readBuffer(address);

        message = parseMessage();

        if (messageQueue!=null){
            messageQueue.putMessage(this);
        }
        
    }

    /**
     * 
     * @param workerName
     * @param verbose attaches the workerThreadName to the output
     * @throws IOException
     */
    public void echoMessage(String workerName, boolean verbose) throws IOException {
        System.out.println(workerName);
        
        StringBuilder echoMessage = new StringBuilder(message);
        
        if (verbose){
            echoMessage.deleteCharAt(echoMessage.length()-1);
            echoMessage.append(" [");
            echoMessage.append(workerName);
            echoMessage.append("]");
        }
        
        echoMessage.append("\n");
        
        sendOverChannel(echoMessage.toString());
        
        
    }

    private String parseMessage() throws IOException{
        
        StringBuilder message = new StringBuilder();
        
        // Parse the incoming data into buffer separate messages
        // and handle them
        while (true) {
            int pos = incomingData.indexOf(MESSAGE_END);

            // No message end mark in the incoming data buffer
            if (pos == -1) {
                break;
            }

            // Extracts message
            message = new StringBuilder(incomingData.substring(0,
                    pos));
            
            incomingData = pos == incomingData.length() - 1 ? "" : incomingData
                    .substring(pos + 1);


        }

        return message.toString();
    }

    private void readBuffer(SocketAddress address) throws IOException{
        
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

        // Read the entire content of the socket
        while (true) {
            buf.clear();
            int numBytesRead = sChannel.read(buf);

            // Closed channel
            if (numBytesRead == -1) {
                // No more bytes can be read from the channel
                System.out
                        .println("client on " + address + " has disconnected");

                sChannel.close();

                // freeing up the select() look by cancelling this client key
                //
                selectionKey.cancel();

                break;
            }

            // Read the buffer
            if (numBytesRead > 0) {
                buf.flip();
                String str = new String(buf.array(), 0, numBytesRead);
                incomingData = incomingData + str;
            }

            // end of message
            if (numBytesRead < BUFFER_SIZE) {
                break;
            }
        }
        
    }

    private void sendOverChannel(String message) throws IOException{
        if (sChannel.isOpen()) {
            sChannel.write(ByteBuffer.wrap(message.toString().getBytes()));

        }
        
    }

    
    
    @Override
    public int compareTo(TransportHandle o) {
        int oPriority = o.getTransportPriority();
        
        if (oPriority != transportPriority) {
            return oPriority > transportPriority ? 1 : -1;
            
        }
            
        return 0;
    }

}
