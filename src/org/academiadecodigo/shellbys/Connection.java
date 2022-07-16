package org.academiadecodigo.shellbys;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection implements Runnable{

    private static final Logger logger = Logger.getLogger(WebServer.class.getName());

    private Socket clientSocket;

    private String currentVerb;

    private String resource;

    private BufferedReader in;

    private DataOutputStream out;

    private File index;

    private File logo;

    private File favicon;

    public Connection(Socket clientSocket){

        this.clientSocket=clientSocket;

        index = new File("resources/index.html");

        logo = new File("resources/logo.png");

        favicon = new File("resources/favicon.ico");
    }

    @Override
    public void run() {
        try {
            //creates the streams from the client connection
            createStreams();

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error creating streams!");
        }

        //Reads the request from the client
        readRequest();
    }

    public void createStreams() throws IOException {
        //create input stream from client
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //create output stream to client
        out = new DataOutputStream(clientSocket.getOutputStream());

    }

    public void readRequest()  {
        try {
            //read first line of code
            String[] verbs;
            String firstLine = in.readLine();

            //split first line to get verb and resource
            verbs = firstLine.split(" ");
            currentVerb = verbs[0];

            //define current resource
            if (verbs.length > 1) {
                resource = verbs[1];
            }
            //case resource isnt specified
            else {
                resource = null;
                logger.log(Level.WARNING, "resource not specified from: " + getAddress(clientSocket));
                reply(out, HttpHelper.badRequest());
                close(clientSocket);
                return;
            }

            //case current verb isnt GET
            if (!currentVerb.equals("GET")) {
                logger.log(Level.WARNING, "request not supported from " + getAddress(clientSocket));
                reply(out, HttpHelper.notAllowed());
                close(clientSocket);
                return;
            }
            //case resource isnt specified
            if (resource == null) {
                logger.log(Level.WARNING, "resource not specified from " + getAddress(clientSocket));
                reply(out, HttpHelper.badRequest());
                close(clientSocket);
                return;
            }
            String filePath = getPathForResource(resource);
            //case resource file isnt supported
            if(!HttpMedia.isSupported(filePath)){
                logger.log(Level.WARNING, "Request for type not supported from " + getAddress(clientSocket));
                reply(out, HttpHelper.unsupportedMedia());
                close(clientSocket);
                return;
            }

            File file = new File(filePath);
            if(file.exists() && !file.isDirectory()){
                reply(out, HttpHelper.ok());
            }
            else {
                logger.log(Level.WARNING, file.getPath() + " not found!");
                reply(out, HttpHelper.notFound());
                filePath = WebServer.DOCUMENT_ROOT + "404.html";
                file = new File(filePath);
            }

            reply(out, HttpHelper.contentType(filePath));
            reply(out, HttpHelper.contentLength(file.length()));

            //send resources or files to output client stream
            streamFiles(out, file);

            //close client connection
            close(clientSocket);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getPathForResource(String resource) {
        String filePath = resource;

        Pattern pattern = Pattern.compile("(\\.[^.]+)$");
        Matcher matcher = pattern.matcher(filePath);

        if(!matcher.find()){
            filePath += "/index.html";
        }
        filePath = WebServer.DOCUMENT_ROOT + filePath;
        System.out.println(filePath);
        return filePath;
    }

    public void reply(DataOutputStream out, String message) throws IOException {
        out.writeBytes(message);
    }

    public void streamFiles(DataOutputStream out, File file) throws IOException {
        //create array of bytes and read code from pretended resource
        byte[] buffer = new byte[1024];
        FileInputStream inp = new FileInputStream(file);

        //reads from file and sends to client output stream
        int numOfBytes;
        while ((numOfBytes = inp.read(buffer)) != -1){
            out.write(buffer, 0, numOfBytes);
        }
        inp.close();
    }
    public void close(Socket clientSocket){
        try {
            logger.log(Level.INFO, "closing client socket for:" + getAddress(clientSocket));
            clientSocket.close();
        }
        catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
    public String getAddress(Socket socket){
        return socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
    }






}
