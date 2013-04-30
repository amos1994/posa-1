import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Programming Assignment for Week 7
 * 
 * Server with Wrapper, Reactor and Acceptor-Connector
 * 
 * this solution uses only plain Java classes
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
 * As a side feature, a secondary Acceptor is configured at port 10001
 * for a graceful shutdown of Reactor and Worker threads
 * 
 * 
 */
public class ProgramW7b {

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
     * this implementation is very simplistic, and little care was taken into
     * parsing the command line
     * 
     * an alternative would be using Apache Commons CLI
     * http://commons.apache.org/proper/commons-cli/
     * 
     * trying to concentrate on the HSHA pattern
     * 
     * most of HSHAServerDaemon parameters are hard coded instead
     * 
     */
    final static String host = "localhost";

    final static int maxWorkerThreadPool = 8;
    final static int maxMessageQueueSize = 1000;
    final static int disconnectPort = 10001;

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

    private static void parsePort(String arg) throws Exception {
        port = Integer.parseInt(arg);

        if (port < 1 || port > 65535) {
            throw new Exception("invalid port " + port);

        }

    }

    /**
     * The server daemon holds an instance of
     * 
     * a Reactor implementation of InitializationDispatcher
     * 
     * a SyncLayerEchoTaskGroup, a wrapper for the Sync layer
     * 
     */
    public static class HSHAServerDaemon {

        final Reactor reactor;
        final SyncLayerEchoTaskGroup syncLayerEchoTaskGroup;

        public HSHAServerDaemon(Address addr, int maxEchoWorkerPoolSize,
                int maxMessageQueueSize, boolean verbose) throws IOException {

            syncLayerEchoTaskGroup = new SyncLayerEchoTaskGroup(
                    maxEchoWorkerPoolSize, maxMessageQueueSize, verbose);

            reactor = new Reactor(addr, syncLayerEchoTaskGroup);
            reactor.initializeAcceptors();

            System.out.println("Server up @ " + addr + '\n');

        }

        public void start() {
            Thread singleReactorThread = new Thread(reactor);

            singleReactorThread.start();
        }

    }

    /**
     * convenience POJO for dealing with Addresses
     * 
     */
    public static class Address {

        private String host;
        private Integer port;

        private InetSocketAddress inetSocketAddress;

        public Address(String host, Integer port) {
            super();
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String toString() {
            return host + ":" + port;
        }

        public InetSocketAddress toInetSocketAddress() {
            inetSocketAddress = new InetSocketAddress(host, port);
            return inetSocketAddress;
        }

    }

    /**
     * An implementation of the InitializationDispatcher role
     * 
     * The Reactor takes care of the half async portion of the HAHS pattern
     * 
     * it reads messages from clients and enqueues them in the
     * SyncLayerMessageQueue
     * 
     */
    public static class Reactor implements Runnable {

        private Selector selector;

        private int WAIT_INTERVAL = 1000;

        private Address address;
        private boolean keepRunning = true;

        private SyncLayerEchoTaskGroup echoTaskGroup;
        private SyncLayerMessageQueue queue;

        public Reactor(Address addr, SyncLayerEchoTaskGroup tg)
                throws IOException {

            selector = Selector.open();
            address = addr;
            echoTaskGroup = tg;
            queue = echoTaskGroup.getMessageQueue();

        }

        public void initializeAcceptors() throws IOException {
            EchoAcceptor acceptor = new EchoAcceptor(this);

            acceptor.open(address);

            // this is a hack which demonstrates how different acceptors
            // can be handled concurrently
            DisconnectAcceptor disconnectAcceptor = new DisconnectAcceptor(this);

            disconnectAcceptor.open(new Address(host, disconnectPort));

        }

        public void handleEvents() throws IOException {
            while (keepRunning) {

                // Wait for an event for some time
                // but does not block forever, because someone might
                // have have asked
                //
                selector.select(WAIT_INTERVAL);

                // Get list of selection keys
                // with pending events
                //
                Iterator<SelectionKey> events = selector.selectedKeys()
                        .iterator();

                while (events.hasNext()) {
                    SelectionKey selKey = (SelectionKey) events.next();

                    // Remove event from the iterator to indicate
                    // that it is being processed
                    //
                    events.remove();

                    if ((selKey.isValid())
                            && (selKey.attachment() instanceof EventHandler)) {
                        EventHandler handler = (EventHandler) selKey
                                .attachment();

                        handler.handleEvent();

                    }

                }
            }

        }

        public SelectionKey registerHandler(SelectableChannel channel,
                EventHandler handler, int eventType) throws IOException {
            return channel.register(selector, eventType, handler);

        }

        public void run() {
            try {
                handleEvents();

            } catch (IOException e) {
                System.err.println(e);

            }

        }

        public void stopReactor() throws IOException {
            keepRunning = false;
            echoTaskGroup.stopWorkers();
        }

        public Address getAddress() {
            return address;
        }

        public SyncLayerMessageQueue getSyncLayerMessageQueue() {
            return queue;
        }

    }

    /**
     * The concrete implementation of the Acceptor role for the Echo operation
     * 
     */
    public static class EchoAcceptor extends Acceptor {

        public EchoAcceptor(Reactor reactor) throws IOException {
            super(reactor);

        }

        @Override
        public void open(Address address) throws IOException {

            acceptorHandle = new TransportHandle(address);

            acceptorHandle
                    .setSelectionKey(reactor.registerHandler(
                            acceptorHandle.getSsChannel(), this,
                            SelectionKey.OP_ACCEPT));

        }

        @Override
        protected void makeServiceHandler() throws IOException {
            serviceHandler = new EchoServiceHandler(reactor, acceptorHandle);

            serviceHandler.getHandle().acceptSocketChannel();

            serviceHandler.open();

        }

    }

    /**
     * The concrete implementation of the Acceptor role for the Disconnect
     * operation
     * 
     */
    public static class DisconnectAcceptor extends Acceptor {

        public DisconnectAcceptor(Reactor reactor) throws IOException {
            super(reactor);

        }

        @Override
        public void open(Address address) throws IOException {

            acceptorHandle = new TransportHandle(address);

            acceptorHandle
                    .setSelectionKey(reactor.registerHandler(
                            acceptorHandle.getSsChannel(), this,
                            SelectionKey.OP_ACCEPT));

        }

        // this is kind of a hack to stop server upon connection
        // to the port in which it was open
        // see disconnectPort which is hardcoded close to main()
        //
        @Override
        protected void makeServiceHandler() throws IOException {
            reactor.stopReactor();

        }

    }

    /**
     * the abstract class which represents the Acceptor role
     * 
     */
    public static abstract class Acceptor extends EventHandler {

        protected Reactor reactor;
        protected TransportHandle acceptorHandle;

        protected ServiceHandler serviceHandler;

        public Acceptor(Reactor reactor) {
            this.reactor = reactor;

        }

        public abstract void open(Address address) throws IOException;

        protected abstract void makeServiceHandler() throws IOException;

        public void accept() throws IOException {
            makeServiceHandler();
        }

        public void handleEvent() throws IOException {
            accept();
        }

    }

    /**
     * an abstract class representation of an EventHandler
     * 
     */
    public static abstract class EventHandler {

        private TransportHandle peerHandle;

        public TransportHandle getHandle() {
            return peerHandle;

        }

        public void setPeerHandle(TransportHandle peerHandle) {
            this.peerHandle = peerHandle;

        }

        public abstract void handleEvent() throws IOException;

    }

    /**
     * An abstract representation of a ServiceHandler
     * 
     */
    public static abstract class ServiceHandler extends EventHandler {

        protected Reactor reactor;
        protected TransportHandle serviceHandle;

        public ServiceHandler(Reactor r, TransportHandle acceptorServerHandle,
                SyncLayerMessageQueue queue, int priority) throws IOException {
            reactor = r;

            serviceHandle = new TransportHandle(acceptorServerHandle, queue,
                    priority);

            setPeerHandle(serviceHandle);

        }

        /**
         * constructor used only for client portion
         * 
         */

        public ServiceHandler(Reactor r) throws IOException {
            reactor = r;

            serviceHandle = new TransportHandle();

            setPeerHandle(serviceHandle);

        }

        public TransportHandle getHandle() {
            return serviceHandle;

        }

        public void setHandle(TransportHandle h) {
            serviceHandle = h;

        }

        public abstract void open() throws IOException;

    }

    /**
     * Handles messages from clients and enques them in SyncLayerMessageQueue
     * 
     */
    public static class EchoServiceHandler extends ServiceHandler {

        final static int commonPriority = 0;

        public EchoServiceHandler(Reactor reactor,
                TransportHandle originalHandle) throws IOException {
            super(reactor, originalHandle, reactor.getSyncLayerMessageQueue(),
                    commonPriority);

        }

        @Override
        public void open() throws IOException {

            serviceHandle.setSelectionKey(reactor.registerHandler(
                    serviceHandle.getsChannel(), this, SelectionKey.OP_READ));

        }

        @Override
        public void handleEvent() throws IOException {
            try {
                serviceHandle.enqueMessage();

            } catch (InterruptedException e) {
                System.err.println(e);

            }

        }

    }

    /**
     * The TransportHandle implements the Comparable interface to illustrate how
     * messages could be prioratized in SyncLayerMessageQueue
     * 
     * Higher priorities could be given to individual clients whose messages
     * would move up to the head of the queue
     * 
     * In this implementation, however, all messages have the same priority See
     * EchoServiceHandler where a dummy commonPriority parameter is always set
     * to 0
     * 
     */
    public static class TransportHandle implements Comparable<TransportHandle> {

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

        public TransportHandle(TransportHandle originalServerHandle,
                SyncLayerMessageQueue queue, int priority) throws IOException {
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
                ssChannel.socket().bind(
                        new InetSocketAddress(address.getHost(), address
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
                SocketAddress address = sChannel.socket()
                        .getRemoteSocketAddress();
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

            if (messageQueue != null) {
                messageQueue.putMessage(this);
            }

        }

        /**
         * 
         * @param workerName
         * @param verbose
         *            attaches the workerThreadName to the output
         * @throws IOException
         */
        public void echoMessage(String workerName, boolean verbose)
                throws IOException {
            System.out.println("echo will be handled by " + workerName);

            StringBuilder echoMessage = new StringBuilder(message);

            if (verbose) {
                echoMessage.deleteCharAt(echoMessage.length() - 1);
                echoMessage.append(" [");
                echoMessage.append(workerName);
                echoMessage.append("]");
            }

            echoMessage.append("\n");

            sendOverChannel(echoMessage.toString());

        }

        private String parseMessage() throws IOException {

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
                message = new StringBuilder(incomingData.substring(0, pos));

                incomingData = pos == incomingData.length() - 1 ? ""
                        : incomingData.substring(pos + 1);

            }

            return message.toString();
        }

        private void readBuffer(SocketAddress address) throws IOException {

            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

            // Read the entire content of the socket
            while (true) {
                buf.clear();
                int numBytesRead = sChannel.read(buf);

                // Closed channel
                if (numBytesRead == -1) {
                    // No more bytes can be read from the channel
                    System.out.println("client on " + address
                            + " has disconnected");

                    sChannel.close();

                    // freeing up the select() look by cancelling this client
                    // key
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

        private void sendOverChannel(String message) throws IOException {
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

    /**
     * 
     * The purpose of creating this wrapper around PriorityBlockingQueue is to
     * illustrate additional control over the message queue
     * 
     * plain PriorityBlockingQueue has no upper bounds, instead it will throw an
     * OutOfMemoryError if no memory can be allocated
     * 
     * 
     */
    public static class SyncLayerMessageQueue {

        private final PriorityBlockingQueue<TransportHandle> queue;
        private int maxMessageCount;

        final Lock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();

        public SyncLayerMessageQueue(int maxCount) {
            queue = new PriorityBlockingQueue<TransportHandle>();
            maxMessageCount = maxCount;
        }

        /**
         * when messageCount reaches a max, the Reactor becomes blocked waiting
         * on the notFull Condition
         * 
         */
        public void putMessage(TransportHandle message)
                throws InterruptedException {
            lock.lock();

            try {
                while (queue.size() == maxMessageCount) {
                    notFull.await();

                }

            } finally {
                lock.unlock();
            }

            queue.put(message);

        }

        /**
         * take() deals with the lower bound (implicit notEmpty Condition)
         * 
         */
        public TransportHandle getMessage() throws InterruptedException {
            TransportHandle message = queue.take();

            lock.lock();
            try {
                notFull.signal();

            } finally {
                lock.unlock();
            }

            return message;
        }

        public int getMaxMessageCount() {
            return maxMessageCount;
        }

        public void setMaxMessageCount(int maxMessageCount) {
            this.maxMessageCount = maxMessageCount;
        }

    }

    /**
     * This class encapsulates
     * 
     * an instance of the SyncLayerMessageQueue
     * 
     * a threadPool of SyncLayerEchoTasks
     * 
     */
    public static class SyncLayerEchoTaskGroup {

        private final SyncLayerMessageQueue messageQueue;

        private final ExecutorService threadPool;

        public SyncLayerEchoTaskGroup(int maxEchoWorkerPoolSize,
                int maxMessageQueueSize, boolean verbose) {

            messageQueue = new SyncLayerMessageQueue(maxMessageQueueSize);

            threadPool = Executors.newFixedThreadPool(maxEchoWorkerPoolSize);

            for (int i = 0; i < maxEchoWorkerPoolSize; i++)
                threadPool.execute(new SyncLayerEchoTask(messageQueue, i,
                        verbose));

        }

        public SyncLayerMessageQueue getMessageQueue() {
            return messageQueue;
        }

        public void stopWorkers() {
            threadPool.shutdownNow();
        }

    }

    /**
     * The SyncLayerEchoTask simply calls the echoMessage function in the
     * TransportHandle passing the verbose configuration
     * 
     */
    public static class SyncLayerEchoTask implements Runnable {

        private String thrName;
        private boolean keepWorking;
        private boolean verbose;
        private SyncLayerMessageQueue queue;

        public SyncLayerEchoTask(SyncLayerMessageQueue q, int i, boolean v) {
            thrName = "worker thread " + i;
            keepWorking = true;
            verbose = v;
            queue = q;

        }

        @Override
        public void run() {
            try {
                while (keepWorking) {
                    TransportHandle handle = queue.getMessage();

                    handle.echoMessage(thrName, verbose);
                    
                }

            } catch (InterruptedException e) {
                System.err.println("interrupt " + thrName + " "+ e);

            } catch (IOException e) {
                System.err.println(e);

            }

        }

        public void setKeepWorking(boolean keepWorking) {
            this.keepWorking = keepWorking;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

    }
}
