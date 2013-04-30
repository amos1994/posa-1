package br.com.javayuga.posa.concurrent.rac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Handle {

    private ServerSocketChannel ssChannel;
    
    private SocketChannel sChannel;
    private SelectionKey selectionKey;

    public static final int BUFFER_SIZE = 256;
    public static final char MESSAGE_END = '\n';

    protected String incomingData;

    /**
     * empty constructor used only for client portion
     * 
     */
    public Handle() {
        incomingData = "";
        
    }

    /**
     * constructor used for creating a server socket channel
     * 
     */
    public Handle(Address address) throws IOException {
        incomingData = "";
        initializeHandleServerChannel(address);

    }

    /**
     * constructor used for creating a client socket channel peered to a server
     * a socket channel
     * 
     */
    public Handle(Handle originalServerHandle) throws IOException {
        incomingData = "";
        ssChannel = originalServerHandle.getSsChannel();

        if (ssChannel == null) {
            throw new IOException(
                    "trying to create a client socket channel without a corresponding server socket channel");
        }

    }

    public ServerSocketChannel getSsChannel() {
        return ssChannel;
    }

    public SocketChannel getsChannel() {
        return sChannel;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

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

    public SocketChannel acceptSocketChannel() throws IOException {
        sChannel = ssChannel.accept();

        if (sChannel != null) {
            SocketAddress address = sChannel.socket().getRemoteSocketAddress();
            System.out.println("Accepting connection from " + address);

            sChannel.configureBlocking(false);
        }

        return sChannel;

    }

    public void synchConnect(Address remoteAddress) throws IOException{
        sChannel = SocketChannel.open();
        sChannel.configureBlocking(false);
        
        sChannel.connect(new InetSocketAddress(remoteAddress.getHost(), remoteAddress
                .getPort())); 
        
        sChannel.finishConnect();
        
    }

    public void echoesMessageToClient() throws IOException {

        SocketAddress address = sChannel.socket().getRemoteSocketAddress();
        System.out.println("Reading from " + address);

        readBuffer(address);

        String message = parseMessage();
        
        sendOverChannel(message);

        System.out.println("echoes to client " + message);

    }
    
    public void processMessageFromServer() throws IOException{
        SocketAddress address = sChannel.socket().getLocalSocketAddress();
        
        readBuffer(address);

        String message = parseMessage();
        
        System.out.println(message);
        
    }
    
    public void sendOverChannel(String message) throws IOException{
        if (sChannel.isOpen()) {
            sChannel.write(ByteBuffer.wrap(message.toString().getBytes()));

        }
        
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

            // Extract one message and add newline
            message = new StringBuilder(incomingData.substring(0,
                    pos)).append('\n');
            
            incomingData = pos == incomingData.length() - 1 ? "" : incomingData
                    .substring(pos + 1);


        }

        return message.toString();
    }

}
