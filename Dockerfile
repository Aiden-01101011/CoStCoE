#Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

# Use the Tomcat 9.0 base image
FROM tomcat:9.0

# Label for maintainer
LABEL maintainer="me"

# Copy SSL certificate and key into the container
COPY ssl.crt /usr/local/tomcat/conf/ssl.crt
COPY ssl.key /usr/local/tomcat/conf/ssl.key

# Set environment variables for SSL keystore password and key alias
ENV SSL_KEYSTORE_PASS changeit
ENV SSL_KEY_ALIAS tomcat

# Update Tomcat server.xml to enable SSL with environment variables
RUN sed -i 's/<Connector port="8080"/<Connector port="8443" scheme="https" secure="true" SSLEnabled="true" keystoreFile="\/usr\/local\/tomcat\/conf\/ssl.crt" keystorePass="'"$SSL_KEYSTORE_PASS"'" keyAlias="'"$SSL_KEY_ALIAS"'" keystoreType="PKCS12"/' /usr/local/tomcat/conf/server.xml

# Add your application WAR file to Tomcat's webapps directory
ADD target/*.war /usr/local/tomcat/webapps/

# Expose port 8080 (default Tomcat HTTP port)
EXPOSE 8080

# Expose port 8443 for HTTPS (SSL)
EXPOSE 8443

# Start Tomcat
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
