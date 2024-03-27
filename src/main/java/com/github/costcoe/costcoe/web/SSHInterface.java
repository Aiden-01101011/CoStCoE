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

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;

public class SSHInterface {

    private Session session;
    
    private String username = System.getProperty("SSH_USER");
    private String password = System.getProperty("SSH_PASSWORD");
    private String hostname = System.getProperty("SSH_HOST");

    public SSHInterface() { }

    public SSHInterface(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public void open() throws JSchException {
        open(this.hostname, this.username, this.password);
    }

    public void open(String hostname, String username, String password) throws JSchException{

        JSch jSch = new JSch();

        session = jSch.getSession(username, hostname, 22);
        Properties config = new Properties(); 
        config.put("StrictHostKeyChecking", "no");  // not recommended
        session.setConfig(config);
        session.setPassword(password);

        System.out.println("Connecting SSH to " + hostname + " - Please wait for few seconds... ");
        session.connect();
        System.out.println("Connected!");
    }

    public String execute(String command) throws JSchException, IOException {
        String ret = "";

        if (!session.isConnected())
            throw new RuntimeException("Not connected to an open session.  Call open() first!");

        ChannelExec channel = null;
        channel = (ChannelExec) session.openChannel("exec");

        channel.setCommand(command);
        channel.setInputStream(null);

        InputStream in = channel.getInputStream(); // channel.getInputStream();

        channel.connect();

        ret = getChannelOutput(channel, in);

        channel.disconnect();

        System.out.println("Finished sending commands!");

        return ret;
    }


    private String getChannelOutput(Channel channel, InputStream in) throws IOException{

        byte[] buffer = new byte[1024];
        StringBuilder strBuilder = new StringBuilder();
        int timeout = 30; //timeout in seconds
        String line = "";
        for (int i = 0; i < timeout; i++) {
            while (in.available() > 0) {
                int l = in.read(buffer, 0, 1024);
                if (l < 0) {
                    break;
                }
                strBuilder.append(new String(buffer, 0, l));
                System.out.println(line);
            }

            if(line.contains("logout")){
                break;
            }

            if (channel.isClosed()){
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee){}
        }

        return strBuilder.toString();   
    }

    public void disconnect(){        
        session.disconnect();
        System.out.println("Disconnected channel and session");
    }

}