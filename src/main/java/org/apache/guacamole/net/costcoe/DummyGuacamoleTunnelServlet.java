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

package org.apache.guacamole.net.costcoe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;


import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.util.Arrays;


/**
 * Simple tunnel example with hard-coded configuration parameters.
 */
public class DummyGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
    
    private static final long serialVersionUID = 1L;
    private static Session sharedSession; // Shared SSH session
    private static int usersCount = 0;    // Number of users using the shared session


    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException{

        // guacd connection information

        String hostname = "172.17.0.2";
        int port = 4822;
        String userNumber = request.getParameter("userNumber");
        int user = Integer.parseInt(userNumber);
        // SSH connection info
        String host = "192.168.56.105";
        String userCMD = "vboxuser";
        String password = "costcoe";
        

        synchronized (DummyGuacamoleTunnelServlet.class) {
            try {
                // If no shared session exists or the session is not connected, create one
                if (sharedSession == null || !sharedSession.isConnected()) {
                    JSch jsch = new JSch();
                    sharedSession = jsch.getSession(userCMD, host, 22);
                    sharedSession.setPassword(password);
                    sharedSession.setConfig("StrictHostKeyChecking", "no");
                    sharedSession.connect();
                }

                // Increment the count of users
                usersCount++;

                // Execute commands or perform SSH actions
                System.out.println(Arrays.toString(executeCommand(sharedSession, "echo dsfgjsf hgsdfj fgdjk sf && echo end")));
                // Create task for image selected by user
                // Get and store IP address of container
                // Get ID of container, associate and store with user data in seperate database

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Decrement the count of users
                usersCount--;

                // If the last user, close the shared session
                if (usersCount == 0 && sharedSession != null && sharedSession.isConnected()) {
                    sharedSession.disconnect();
                    sharedSession = null; // Reset sharedSession
                }
            }
        }
    

        String vncIp;
        switch (user) {
            case 1:
                vncIp = "172.17.0.3";
                break;

            case 2:
                vncIp = "172.17.0.4";
                break;
        
            default:
                vncIp = "172.17.0.3";
                break;
        }

        // VNC connection information
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("vnc");
        config.setParameter("hostname", vncIp);
        config.setParameter("port", "5901");
        config.setParameter("password", "headless");
        config.setParameter("width", "500");
        config.setParameter("height", "500");

        // Connect to guacd, proxying a connection to the VNC server above
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(hostname, port),
                config
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        return tunnel;


    }
    // create doPost method that gets ID of container from database based on user data
    // and uses that ID to kill the docker swarm task that was running that container

    // in the future, this method could just stop the docker container, 
    // and then it could be started again for the user later

    public String[] executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
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
