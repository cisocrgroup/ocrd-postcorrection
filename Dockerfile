FROM tomcat:alpine
MAINTAINER Florian Fink <finkf@cis.lmu.de>
RUN apk add --no-cache maven openjdk8 make nodejs-npm
# ADD is like COPY but can download URL's.
# prefere COPY
COPY . /app/ocrd
WORKDIR /app/ocrd
COPY tomcat-users.xml /usr/local/tomcat/conf/
RUN make deploy
#RUN cp target/ocrd-0.1.war $CATALINA_HOME/webapps
