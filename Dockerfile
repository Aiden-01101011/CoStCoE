#Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.
FROM tomcat:9.0

LABEL maintainer=”me”

ADD target/*.war /usr/local/tomcat/webapps/

ADD setenv.sh /usr/local/tomcat/bin

EXPOSE 8080

CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]