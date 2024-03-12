package org.apache.guacamole.net.costcoe;

import java.util.ArrayList;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

public class SSHInterface {

    private String user;
    private String host;
    private String password;
    private int port;
    private Session session;

    // Constructor with mandatory parameters
    public SSHInterface(String user, String host, String password) throws JSchException{
        this.user = user;
        this.host = host;
        this.password = password;
        this.port = 22; // Default port if not specified

        JSch jsch = new JSch();
        this.session = jsch.getSession(this.user, host, 22);
        this.session.setPassword(password);
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.connect();
    }

    // Constructor with optional port parameter
    public SSHInterface(String user, String host, String password, int port) throws JSchException{
        this.user = user;
        this.host = host;
        this.password = password;
        this.port = port;

        JSch jsch = new JSch();
        this.session = jsch.getSession(this.user, host, 22);
        this.session.setPassword(password);
        this.session.setConfig("StrictHostKeyChecking", "no");
        this.session.connect();
    }

    // Getter methods for the class fields (if needed)
    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public void disconnect() {
        this.session.disconnect();
    }

    public String[] execute(String command) throws Exception { // deal with exception handling later
        ChannelExec channel = (ChannelExec) this.session.openChannel("exec");
        channel.setCommand(command);

        // Get input stream to read the command's output
        InputStream inputStream = channel.getInputStream();

        channel.connect();

        // Read the command's output
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        ArrayList<String> outputList = new ArrayList<String>();

        while ((line = reader.readLine()) != null) {
            outputList.add(line);
        }
        // Close resources
        reader.close();
        inputStream.close();

        channel.disconnect();

        String[] output = new String[outputList.size()];
        outputList.toArray(output);

        return output;
    }
}


