FROM ocrd/core
MAINTAINER Florian Fink <finkf@cis.lmu.de>
ENV OCRD_VERSION "ocrd-0.1"
ENV PROFILER_GIT https://github.com/cisocrgroup/Profiler
ENV LC_ALL C.UTF-8
ENV LANG C.UTF-8

VOLUME ["/data"]
RUN apt-get update && \
    apt-get install -y git cmake g++ libxerces-c-dev libcppunit-dev && \
    apt-get install -y openjdk-8-jre && \
    mkdir /src && \
    cd /src && \
    git clone -b ocrd ${PROFILER_GIT} && \
    cd Profiler && mkdir build && cd build && \
    cmake -DCMAKE_BUILD_TYPE=release .. && \
    make profiler && \
    mkdir /apps && \
    cp bin/profiler /apps/ && \
    cd / && \
		rm -rf /src/Profiler
COPY target/${OCRD_VERSION}-cli.jar /apps/
COPY src/main/resources/defaultConfiguration.json /apps/${OCRD_VERSION}-config.json

ENTRYPOINT ["/bin/sh", "-c"]
#COPY target/${OCRD_VERSION}.war /usr/local/tomcat/webapps
#COPY tomcat-users.xml ${CATALINA_HOME}/conf/tomcat-users.xml
#COPY context.xml ${CATALINA_HOME}/webapps/manager/META-INF/context.xml
#COPY context.xml ${CATALINA_HOME}/webapps/host-manager/META-INF/context.xml
