package br.com.javayuga.posa.concurrent.netty;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class DiscardServerDaemon {
    
    private ServerBootstrap bootstrap;
    private ChannelFactory factory;

    private Address address;

    public DiscardServerDaemon(Address addr){
        address = addr;
    }
    
    public void start() throws IOException{

        factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new DiscardServerHandler());
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(address.toInetSocketAddress());
        
        
        System.out.println("Server up @ " + address + '\n');

    }

}
