#!/bin/sh

# !!!!!!!!!!!!!!!!!! ADJUST THESE !!!!!!!!!!!!!!!!!!
cr=/home/tomcat/apache-tomcat-5.5.25/webapps/cr/WEB-INF
cd $cr/classes
java=/usr/bin/java

libpath=$cr/lib

cp=$libpath/postgresql-8.4-701.jdbc4.jar
cp=$cp:$libpath/commons-logging-1.1.jar
cp=$cp:$libpath/log4j-1.2.13.jar
cp=$cp:$libpath/virtjdbc-3.0.jar:$CLASSPATH

$java -cp $cp eionet.cr.web.action.CopyPostgreSQLTablesToVirtuoso
