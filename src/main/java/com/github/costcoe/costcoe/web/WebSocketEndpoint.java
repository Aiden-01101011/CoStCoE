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
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.jcraft.jsch.JSchException;


@ServerEndpoint("/ws")
public class WebSocketEndpoint extends Endpoint {

    private SSHInterface ssh = new SSHInterface();

    private static final String NODE_IP_MANAGER = System.getenv("NODE_IP_MANAGER");
    private static final String NODE_IP_1 = System.getenv("NODE_IP_1");
    private static final String NODE_IP_2 = System.getenv("NODE_IP_2");
    private static final String NODE_ID_MANAGER = System.getenv("NODE_ID_MANAGER");
    private static final String NODE_ID_1 = System.getenv("NODE_ID_1");
    private static final String NODE_ID_2 = System.getenv("NODE_ID_2");

    private Timer timer;
    @Override
    public void onOpen(Session session, EndpointConfig config) {

        // maybe change availablePorts() to return int

        CSVManager.init();
        String port;
        String response = null;
        try{
            port = CSVManager.availablePort();
            StringBuilder commandBuilder = new StringBuilder();
            ssh.open();

            // Execute command to create docker service
            commandBuilder.append("sudo docker service create --name ")
                .append(session.getId())
                .append(" -d --publish published=")
                .append(port)
                .append(",target=5901,mode=host accetto/ubuntu-vnc-xfce");
            response = ssh.execute(commandBuilder.toString());
            System.out.println(response);

            // Execute command to get service node
            commandBuilder.setLength(0); // Clear StringBuilder
            commandBuilder.append("sudo docker service ps --format '{{.Node}}' ")
                .append(response);
            response = ssh.execute(commandBuilder.toString());
            System.out.println(response);

            // Execute command to get node ID
            commandBuilder.setLength(0); // Clear StringBuilder
            commandBuilder.append("sudo docker node inspect --format '{{.ID}}' ")
                .append(response);
            response = ssh.execute(commandBuilder.toString());
            System.out.println(response);

            ssh.disconnect();

            String nodeID = response.trim();
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

            CSVManager.addSession(session.getId(), nodeIP, port);

            System.out.println(String.format("GO%s%s%s", nodeIP, port, session.getId()));
            session.getBasicRemote().sendText(String.format("GO%s%s%s", nodeIP, port, session.getId()));

            // Register a periodic task to check client activity
            timer = new Timer();
            timer.scheduleAtFixedRate(new CheckClientActivity(session), 0, 5000); // check every 5 secs
        }
        catch (IOException | JSchException e){
            e.printStackTrace();
        }
        catch (OutOfPortsException e){
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

