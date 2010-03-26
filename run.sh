#!/bin/sh
java -Xms128m -Xmx1024m -cp bin/:lib/commons-codec-1.4.jar:lib/commons-compress-1.0.jar -Djava.util.logging.config.file=logging.properties Main

