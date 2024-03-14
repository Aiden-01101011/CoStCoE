/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.costcoe.costcoe.web;

import java.util.ArrayList;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

public class SSHInterface {

    private String user;
    private String host;
    private String password;
    private int port;
    private Session session;

    // Constructor with mandatory parameters
    public SSHInterface(String host, String user, String password) {
        try {
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
        catch (JSchException e){
            e.printStackTrace();
        }
    }

    // Constructor with optional port parameter
    public SSHInterface(String host, String user, String password, int port){
        try {
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
        catch (JSchException e){
            e.printStackTrace();
        }
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

    public String[] execute(String command) { // deal with exception handling later
        try{
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
        catch (JSchException e){
            e.printStackTrace();
            return null;
        }
        catch (IOException e){
            //e.printStackTrace();
            return null;
        }
    }
}


