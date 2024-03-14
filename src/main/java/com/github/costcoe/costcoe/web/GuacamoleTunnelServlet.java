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

import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

//import com.jcraft.jsch.ChannelExec;
//import com.jcraft.jsch.JSch;

//import com.jcraft.jsch.Session;
import java.util.Arrays;

/**
 * Simple tunnel example with hard-coded configuration parameters.
 */
public class GuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException{

        // guacd connection information
        String hostname = System.getenv("GUACD_HOST");
        int GUACD_PORT = Integer.parseInt(System.getenv("GUACD_PORT"));
        // Data from JS
        int user = Integer.parseInt(request.getParameter("USERNUM"));
        //int image = Integer.parseInt(request.getParameter("IMAGE"));
        // SSH connection information
        String SSH_HOST = System.getenv("SSH_HOST");
        String SSH_USER = System.getenv("SSH_USER");
        String SSH_PASSWORD = System.getenv("SSH_PASSWORD");

        SSHInterface ssh = new SSHInterface(SSH_HOST, SSH_USER, SSH_PASSWORD);
        String name = "";
        int port = 34000;
        // String[] response = ssh.execute(String.format("sudo docker service create --name %s --port %d:5901 b3c0056806ac", name, port)).clone();
        // response = ssh.execute(String.format("sudo docker service ps --format '{{.Node}}' %s", response[0])).clone();
        // System.out.println(response[0]);
        //System.out.println(Arrays.toString(ssh.execute("echo testing")));
        ssh.disconnect();

        String vncIp;
        switch (user) {
            case 1:
                vncIp = "172.17.0.3";
                break;

            case 2:
                vncIp = "172.17.0.5";
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
                new InetGuacamoleSocket(hostname, GUACD_PORT),
                config
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        return tunnel;


    }

}
