package br.com.javayuga.posa.concurrent;
import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

/**
 *
 * Programming Assignment for Week 6
 *
 * Server with Wrapper, Reactor and Acceptor-Connector
 *
 * this solution uses the Netty Framework (netty-3.2.6.Final.jar)
 * 
 *
 * there are two modes of execution, one as as Server, another as a Client
 *
 * the client portion has been purposefully omitted from the assignment
 * 
 * 
 * usage: java -cp .:netty-3.2.6.Final.jar ProgramW6c [[PORT] [HOST]]
 *
 * DO NOT forget to use the -classpath parameter pointing to correct JAR if using a command line
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
public class ProgramW6c {

    /**
     * For the Wrapper portion of the assignment, please check
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
     * Netty uses classes from the package java.nio extensively
     *
     */

    /**
     *
     * @param args
     *            [0] the port of the host (defaults to 8088)
     * @param args
     *            [1] the name of the host (defaults to localhost)
     *
     */
    public static void main(String[] args) {
        String host = "localhost";
        Integer port = 8088;

        try {
            if (args.length != 0) {
                if (args.length == 1) {
                    try {
                        port = Integer.parseInt(args[0]);

                    } catch (NumberFormatException e) {
                        throw new Exception(String.format(
                                "Invalid argument: %s. Should be integer.",
                                args[0]));
                    }

                } else if (args.length == 2) {
                    host = args[1];

                } else {
                    throw new Exception("usage: java ProgramW6 [[PORT] [HOST]]");
                }

            }

            EchoServerDaemon server = new EchoServerDaemon(new Address(host, port));

            server.start();

        } catch (IOException e) {
            System.err.println(e);

        } catch (Exception e) {
            System.err.println(e);

        }

    }
    
    /**
     * 
     * POJO for Address (host:port)
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
        
        public String toString(){
            return host + ":" + port;
        }
        
        public InetSocketAddress toInetSocketAddress() {
            inetSocketAddress = new InetSocketAddress(host, port); 
            return inetSocketAddress;
        }
        

    }
    
    /**
     * 
     * The EchoServerDaemon encapsulates
     * 
     *  a ServerBootstrap instance
     *  a NioServerSocketChannelFactory 
     *
     *  after binding, a new thread performs the Reactor role
     *
     */
    public static class EchoServerDaemon {
        
        private ServerBootstrap bootstrap;
        private ChannelFactory factory;

        private Address address;

        public EchoServerDaemon(Address addr){
            address = addr;
        }
        
        public void start() throws IOException{

            factory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());

            bootstrap = new ServerBootstrap(factory);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    ChannelPipeline pipeline = pipeline();
                      
                    // Add the text line codec combination first,
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
                    8192, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
  
                    // and then business logic.
                    pipeline.addLast("handler", new EchoServerHandler());
                    
                    return pipeline;
                }
            });

            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);
            
            /**
             * after binding, there is a boss channel bound to the address
             * this channel is running on a separate thread, according to:
             * 
             * http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/socket/nio/NioServerSocketChannelFactory.html
             * 
             * the implementation of the boss channel has its own Selector
             * 
             * For Netty 3, AbstractNioSelector (and its subclasses)
             * corresponds to the Reactor role of the pattern
             * 
             */

            bootstrap.bind(address.toInetSocketAddress());
            
            System.out.println("Server up @ " + address + '\n');

        }

    }

    /**
     * 
     * The EchoServerHandler is responsible for the business logic
     * (i.e. echoing the received message) 
     *
     *
     */
    public static class EchoServerHandler extends SimpleChannelUpstreamHandler {

        
        /**
         * when this method gets finally called there is another thread running
         * (one for each client)
         * 
         * In this variation of the pattern, the Acceptor role is played
         * by ChannelFactory in conjunction with the original Selector running in the boss thread (see above)
         * 
         * A new Selector is registered for each connection and treats it independently
         * from other I/O events, so that multiple clients are supported
         * 
         * the model is one-thread-per-connection, or better:
         * one thread per client connection + one thread for each server socket
         * 
         */
        @Override
        public void channelConnected(
                ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                System.out.println("connection accepted for channel " + e.getChannel());
            
        }
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
             Channel ch = e.getChannel();
             
             if (ch.isReadable()){
                 
                 // Cast to a String first.
                 // We know it is a String because we put some codec in TelnetPipelineFactory.
                 String request = (String) e.getMessage();
                 
                 System.out.println("received from channel " + e.getChannel() + " : " + request);
                 
                 // not really part of the assignment
                 // but it is nice to have a way to kill channel client connection 
                 //
                 if (request.equalsIgnoreCase("bye")){
                     e.getChannel().close();
                     
                 }else{
                     // a bit overkill to use StringBuilder here, but it will be useful for PA#5
                     // when multiple threads will be waiting for synchronous messages
                     // to do the incredibly lengthy process of echoing
                     //
                     // appending a newline to make for a neat display at the client
                     //
                     StringBuilder echo = new StringBuilder(request);
                     echo.append('\n');
                 
                     ch.write(echo.toString());
                     
                 }
                 
             }
             
             
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();

            Channel ch = e.getChannel();
            ch.close();
        }
    }


}
