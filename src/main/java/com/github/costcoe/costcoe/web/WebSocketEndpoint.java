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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.jcraft.jsch.JSchException;


@ServerEndpoint("/ws")
public class WebSocketEndpoint extends Endpoint {

    // Define the variables without initialization
    private static final String NODE_IP_MANAGER;
    private static final String NODE_IP_1;
    private static final String NODE_IP_2;
    private static final String NODE_ID_MANAGER;
    private static final String NODE_ID_1;
    private static final String NODE_ID_2;
    private static final String SSH_USER;
    private static final String SSH_PASSWORD;
    private static final String SSH_HOST;

    private static final String VNC_PORT_1;
    private static final String VNC_IMAGE_1;

    private static final String VNC_PORT_2;
    private static final String VNC_IMAGE_2;

    private static final String VNC_PORT_3;
    private static final String VNC_IMAGE_3;

    private static final String VNC_PORT_4;
    private static final String VNC_IMAGE_4;

    // Static initialization block to initialize the variables
    static {
        NODE_IP_MANAGER = System.getProperty("NODE_IP_MANAGER");
        NODE_IP_1 = System.getProperty("NODE_IP_1");
        NODE_IP_2 = System.getProperty("NODE_IP_2");
        NODE_ID_MANAGER = System.getProperty("NODE_ID_MANAGER");
        NODE_ID_1 = System.getProperty("NODE_ID_1");
        NODE_ID_2 = System.getProperty("NODE_ID_2");
        SSH_USER = System.getProperty("SSH_USER");
        SSH_PASSWORD = System.getProperty("SSH_PASSWORD");
        SSH_HOST = System.getProperty("SSH_HOST");

        VNC_PORT_1 = System.getProperty("VNC_PORT_1");
        VNC_IMAGE_1 = System.getProperty("VNC_IMAGE_1");

        VNC_PORT_2 = System.getProperty("VNC_PORT_2");
        VNC_IMAGE_2 = System.getProperty("VNC_IMAGE_2");

        VNC_PORT_3 = System.getProperty("VNC_PORT_3");
        VNC_IMAGE_3 = System.getProperty("VNC_IMAGE_3");

        VNC_PORT_4 = System.getProperty("VNC_PORT_4");
        VNC_IMAGE_4 = System.getProperty("VNC_IMAGE_4");
    }

    private SSHInterface ssh = new SSHInterface(SSH_HOST, SSH_USER, SSH_PASSWORD);

    private Timer timer;
    @Override
    public void onOpen(Session session, EndpointConfig config) {

        CSVManager.init();
        // Register a periodic task to check client activity
        timer = new Timer();
        timer.scheduleAtFixedRate(new CheckClientActivity(session), 60000, 5000); // check every 5 secs

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                System.out.println("MESSAGE RECIEVED");
                int image = Integer.parseInt(message);
                    if (image < 1 || image > 4) {
                        try {
                            session.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        throw new IllegalArgumentException("requested image out of bounds");
                    }
                    try{
                        String port;
                        try {
                            port = CSVManager.availablePort();
                        } catch (OutOfPortsException e) {
                            e.printStackTrace();
                            session.close();
                            return;
                        }
                        String response = startContainer(image, port, session.getId());
                        System.out.println(response);
                        String nodeID = response;
                        String nodeIP;
                        if (nodeID.equals(NODE_ID_MANAGER)) {
                            nodeIP = NODE_IP_MANAGER;
                        } else if (nodeID.equals(NODE_ID_1)) {
                            nodeIP = NODE_IP_1;
                        } else if (nodeID.equals(NODE_ID_2)) {
                            nodeIP = NODE_IP_2;
                        }
                        else{
                            throw new RuntimeException("returned node ID does not match any ID's defined in config");
                        }
                        
                        System.out.println(String.format("%s %s %s", session.getId(), nodeIP, port));
                        CSVManager.addSession(session.getId(), nodeIP, port);
        
                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append("GO")
                                      .append(nodeIP)
                                      .append(port)
                                      .append(image)
                                      .append(session.getId());
        
                        session.getBasicRemote().sendText(messageBuilder.toString());
        
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
            }
        });

        try {
            session.getBasicRemote().sendText("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        // Cancel the timer when the session is closed
        timer.cancel();
        System.out.println("Session Closed - Performing actions...");
        CSVManager.deleteSessionEntry(session.getId());

        try {
            ssh.open();
            System.out.println(ssh.execute(String.format("sudo docker service rm %s", session.getId())));
            ssh.disconnect();
        }
        catch (JSchException | IOException e){
            e.printStackTrace();
        }
    }

    private String startContainer(int image, String port, String sessionID) {
        String response = null;
        try {
            port = CSVManager.availablePort();
            StringBuilder commandBuilder = new StringBuilder();
            ssh.open();

            // Execute command to create docker service
            switch (image){
                case 1:
                    commandBuilder.append("sudo docker service create --name ")
                        .append(sessionID)
                        .append(" -d --publish published=")
                        .append(port)
                        .append(",target=")
                        .append(VNC_PORT_1)
                        .append(",mode=host ")
                        .append(VNC_IMAGE_1);
                    response = ssh.execute(commandBuilder.toString());
                    break;
                
                case 2:
                    commandBuilder.append("sudo docker service create -t --name ")
                        .append(sessionID)
                        .append(" -d --publish published=")
                        .append(port)
                        .append(",target=")
                        .append(VNC_PORT_2)
                        .append(",mode=host ")
                        .append(VNC_IMAGE_2);
                    response = ssh.execute(commandBuilder.toString());
                    break;

                case 3:
                    commandBuilder.append("sudo docker service create --name ")
                        .append(sessionID)
                        .append(" -d --publish published=")
                        .append(port)
                        .append(",target=")
                        .append(VNC_PORT_3)
                        .append(",mode=host ")
                        .append(VNC_IMAGE_3);
                    response = ssh.execute(commandBuilder.toString());
                    break;

                case 4:
                    commandBuilder.append("sudo docker service create --name ")
                        .append(sessionID)
                        .append(" -d --publish published=")
                        .append(port)
                        .append(",target=")
                        .append(VNC_PORT_4)
                        .append(",mode=host ")
                        .append(VNC_IMAGE_4);
                    response = ssh.execute(commandBuilder.toString());
                    break;
            }
            
            // Execute command to get service node
            commandBuilder.setLength(0); // Clear StringBuilder
            commandBuilder.append("sudo docker service ps --format '{{.Node}}' ")
                .append(response);
            response = ssh.execute(commandBuilder.toString());

            // Execute command to get node ID
            commandBuilder.setLength(0); // Clear StringBuilder
            commandBuilder.append("sudo docker node inspect --format '{{.ID}}' ")
                .append(response);
            response = ssh.execute(commandBuilder.toString()).trim();

            ssh.disconnect();
        } catch (OutOfPortsException | JSchException | IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static class CheckClientActivity extends TimerTask {

        private final Session session;

        public CheckClientActivity(Session session) {
            this.session = session;
        }

        @Override
        public void run() {
            try {
                // Send a ping to check client responsiveness
                System.out.println("Sending ping!");
                session.getBasicRemote().sendPing(ByteBuffer.wrap("Ping".getBytes()));
            } catch (IOException e) {
                System.out.println("Ping failed to send!");
                try {
                    session.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

