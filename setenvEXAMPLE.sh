#Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

# THIS IS AN EXAMPLE DOCUMENT
# ALL VALUES MUST BE FILLED IN
# AND FILE MUST BE RENAMED TO "setenv.sh"

# If the code is already compiled this wont do anything
# to edit these values without recompiling, go to CATALINA_HOME/bin (should be usr/local/tomcat/bin)
# in the tomcat container, and edit setenv.sh
# you will need to restart tomcat server for changes to take effect

export START_PORT=34000 # First port for vnc containers, increments by one for each user

# SSH user info
export SSH_USER=
export SSH_PASSWORD=
export SSH_HOST= # IP address

# guacd connection ifno
export GUACD_HOST= # IP address
export GUACD_PORT=4822
