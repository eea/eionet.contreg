FROM tomcat:9.0.20-jre8
RUN rm -rf /usr/local/tomcat/conf/logging.properties /usr/local/tomcat/webapps/*
COPY target/cr.war /usr/local/tomcat/webapps/ROOT.war
