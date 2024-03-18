#!/bin/sh

# Set system properties for Apache Tomcat

# First port for VNC containers
export JAVA_OPTS="$JAVA_OPTS -DSTART_PORT=34000"

# Maximum open ports
export JAVA_OPTS="$JAVA_OPTS -DMAX_OPEN_PORTS=100"

# Path to the CSV file
export JAVA_OPTS="$JAVA_OPTS -DCSV_FILE_PATH=src/main/resources/SessionData.csv"

# SSH user info
export JAVA_OPTS="$JAVA_OPTS -DSSH_USER=sample_user"            # Replace sample_user with actual SSH username
export JAVA_OPTS="$JAVA_OPTS -DSSH_PASSWORD=sample_password"    # Replace sample_password with actual SSH password
export JAVA_OPTS="$JAVA_OPTS -DSSH_HOST=sample_ip_address"      # Replace sample_ip_address with actual SSH host IP

# guacd connection info
export JAVA_OPTS="$JAVA_OPTS -DGUACD_HOST=sample_ip_address"    # Replace sample_ip_address with actual guacd host IP
export JAVA_OPTS="$JAVA_OPTS -DGUACD_PORT=4822"                 # Default guacd port

# Node IP addresses
export JAVA_OPTS="$JAVA_OPTS -DNODE_IP_MANAGER=sample_ip_address"   # Replace sample_ip_address with actual node manager IP
export JAVA_OPTS="$JAVA_OPTS -DNODE_IP_1=sample_ip_address"         # Replace sample_ip_address with actual node 1 IP
export JAVA_OPTS="$JAVA_OPTS -DNODE_IP_2=sample_ip_address"         # Replace sample_ip_address with actual node 2 IP

# Node IDs
export JAVA_OPTS="$JAVA_OPTS -DNODE_ID_MANAGER=sample_node_id"   # Replace sample_node_id with actual node manager ID
export JAVA_OPTS="$JAVA_OPTS -DNODE_ID_1=sample_node_id"         # Replace sample_node_id with actual node 1 ID
export JAVA_OPTS="$JAVA_OPTS -DNODE_ID_2=sample_node_id"         # Replace sample_node_id with actual node 2 ID

# VNC Container Info
export JAVA_OPTS="$JAVA_OPTS -DVNC_PORT_1=5901"               # VNC port for container 1
export JAVA_OPTS="$JAVA_OPTS -DVNC_PASSWORD_1=headless"       # Password for container 1
export JAVA_OPTS="$JAVA_OPTS -DVNC_IMAGE_1=image1"            # Image for container 1

export JAVA_OPTS="$JAVA_OPTS -DVNC_PORT_2=5901"               # VNC port for container 2
export JAVA_OPTS="$JAVA_OPTS -DVNC_PASSWORD_2=headless"       # Password for container 2
export JAVA_OPTS="$JAVA_OPTS -DVNC_IMAGE_2=image2"            # Image for container 2

export JAVA_OPTS="$JAVA_OPTS -DVNC_PORT_3=5901"               # VNC port for container 3
export JAVA_OPTS="$JAVA_OPTS -DVNC_PASSWORD_3=headless"       # Password for container 3
export JAVA_OPTS="$JAVA_OPTS -DVNC_IMAGE_3=image3"            # Image for container 3

export JAVA_OPTS="$JAVA_OPTS -DVNC_PORT_4=5901"               # VNC port for container 4
export JAVA_OPTS="$JAVA_OPTS -DVNC_PASSWORD_4=headless"       # Password for container 4
export JAVA_OPTS="$JAVA_OPTS -DVNC_IMAGE_4=image4"            # Image for container 4