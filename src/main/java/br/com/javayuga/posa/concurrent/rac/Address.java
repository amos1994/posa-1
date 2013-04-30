package br.com.javayuga.posa.concurrent.rac;

public class Address {

    private String host;
    private Integer port;
    
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

}
