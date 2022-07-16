package org.academiadecodigo.shellbys;

import com.sun.xml.internal.stream.StaxErrorReporter;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServer {

    public static final int DEFAULT_PORT = 8080 ;

     private static final Logger logger = Logger.getLogger(WebServer.class.getName());
    private ServerSocket serverSocket;

    public static final String DOCUMENT_ROOT = "resources/";

    private ExecutorService pool;

    public WebServer(int portNumber) throws IOException {
        pool = Executors.newCachedThreadPool();
        listen(portNumber);
    }

    public void listen(int portNumber) {

        try {
            //create server socket
            serverSocket = new ServerSocket(portNumber);
            logger.log(Level.INFO, "server bind to " +getAddress(serverSocket));

            //create client socket and new thread for incoming request
            dispatch(serverSocket);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not bind to port " + portNumber);
            logger.log(Level.SEVERE, e.getMessage());
            System.exit(1);
        }


    }
    public  void dispatch(ServerSocket serverSocket){
        while (true){
            try {
                //waiting for client connection
                System.out.println("waiting for data from clients...");
                // waiting for new connection
                Connection connection = new Connection(serverSocket.accept()) ;

                pool.submit(connection);

            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public String getAddress(ServerSocket socket){
        return socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
    }

    public static void main(String[] args) throws IOException {
       try {
           int port = args.length >  0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

           WebServer webServer = new WebServer(port);
       }
       catch (NumberFormatException e){
           System.out.println("Usage: WebServer [PORT]");
           System.exit(1);
       }

    }
}
