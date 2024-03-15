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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/ws")
public class WebSocketEndpoint extends Endpoint {

    private static final String SSH_HOST = System.getenv("SSH_HOST");
    private static final String SSH_USER = System.getenv("SSH_USER");
    private static final String SSH_PASSWORD = System.getenv("SSH_PASSWORD");

    private static final String NODE_IP_MANAGER = System.getenv("NODE_IP_MANAGER");
    private static final String NODE_IP_1 = System.getenv("NODE_IP_1");
    private static final String NODE_IP_2 = System.getenv("NODE_IP_2");
    private static final String NODE_ID_MANAGER = System.getenv("NODE_ID_MANAGER");
    private static final String NODE_ID_1 = System.getenv("NODE_ID_1");
    private static final String NODE_ID_2 = System.getenv("NODE_ID_2");

    private Timer timer;
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Register a periodic task to check client activity
        timer = new Timer();
        timer.scheduleAtFixedRate(new CheckClientActivity(session), 0, 5000); // Check every 5 seconds

        SSHInterface ssh = new SSHInterface(SSH_HOST, SSH_USER, SSH_PASSWORD);
        // maybe change availablePorts() to return int

        CSVManager.init();

        //String[] response = ssh.execute(String.format("sudo docker service create --name %s --port %s:5901 accetto/ubuntu-vnc-xfce", session.getId(), CSVManager.availablePort())).clone();
        String[] response = ssh.execute("echo poopy").clone();
        System.out.println(response[0]);
        //response = ssh.execute(String.format("sudo docker service ps --format '{{.Node}}' %s", response[0])).clone();
        //response = ssh.execute(String.format("sudo docker node inspect --format '{{.ID}}' %s", response[0])).clone();
        String nodeID = response[0];
        ssh.disconnect();

        String nodeIP;
        if (nodeID == NODE_ID_MANAGER) {
            nodeIP = NODE_IP_MANAGER;
        } else if (nodeID == NODE_ID_1) {
            nodeIP = NODE_IP_1;
        } else if (nodeID == NODE_ID_2) {
            nodeIP = NODE_IP_2;
        }
        else{
            throw new RuntimeException("returned node ID does not match any ID's defined in config");
        }

        CSVManager.addSession(session.getId(), nodeIP, CSVManager.availablePort());
        try {
            session.getBasicRemote().sendText("GO");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        // Cancel the timer when the session is closed
        timer.cancel();
        System.out.println("Client not responding. Performing actions...");

        CSVManager.deleteSessionEntry(session.getId());

        SSHInterface ssh = new SSHInterface(SSH_HOST, SSH_USER, SSH_PASSWORD);
        System.out.println(Arrays.toString(ssh.execute(String.format("sudo docker service rm %s", session.getId()))));
        ssh.disconnect();
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
                System.err.println("Ping failed to send!");
                e.printStackTrace();
            }
        }
    }
}

