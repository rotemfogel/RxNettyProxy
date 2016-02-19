#!/bin/bash
mvn clean package
java -Xmx1024m -XX:+UseConcMarkSweepGC -jar ./target/proxy-server-1.0.0-jar-with-dependencies.jar
