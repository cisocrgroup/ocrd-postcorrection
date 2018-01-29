FROM tomcat:alpine
MAINTAINER Florian Fink <finkf@cis.lmu.de>
ENV OCRD_VERSION "ocrd-0.1"
COPY target/${OCRD_VERSION}.war /usr/local/tomcat/webapps
COPY tomcat-users.xml ${CATALINA_HOME}/conf/tomcat-users.xml
COPY context.xml ${CATALINA_HOME}/webapps/manager/META-INF/context.xml
COPY context.xml ${CATALINA_HOME}/webapps/host-manager/META-INF/context.xml
