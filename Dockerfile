#Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

# Use the Tomcat 9.0 base image
FROM tomcat:9.0

# Label for maintainer
LABEL maintainer="me"

# Install OpenSSL
RUN apt-get update && apt-get install -y openssl

# Create directories to store certificates
RUN mkdir -p /usr/local/tomcat/conf/ssl

# Generate a self-signed SSL certificate
RUN openssl req -x509 -nodes -newkey rsa:2048 -keyout /usr/local/tomcat/conf/ssl/server.key -out /usr/local/tomcat/conf/ssl/server.crt -days 365 -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"

# Set environment variables for keystore and truststore passwords
ENV KEYSTORE_PASSWORD=password
ENV TRUSTSTORE_PASSWORD=password

# Add SSL configuration to Tomcat's setenv.sh using environment variables
RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -Djavax.net.ssl.keyStore=/usr/local/tomcat/conf/ssl/server.crt -Djavax.net.ssl.keyStorePassword=$KEYSTORE_PASSWORD -Djavax.net.ssl.trustStore=/usr/local/tomcat/conf/ssl/server.crt -Djavax.net.ssl.trustStorePassword=$TRUSTSTORE_PASSWORD"' >> /usr/local/tomcat/bin/setenv.sh

# Add your application WAR file to Tomcat's webapps directory
ADD target/*.war /usr/local/tomcat/webapps/

# Expose port 8080 (default Tomcat HTTP port)
EXPOSE 8080

# Expose port 8443 for HTTPS (SSL)
EXPOSE 8443

# Start Tomcat
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
