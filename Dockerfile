FROM ocrd/core
MAINTAINER Florian Fink <finkf@cis.lmu.de>
ENV OCRD_VERSION "ocrd-0.1"
ENV PROFILER_GIT https://github.com/cisocrgroup/Profiler
ENV LC_ALL C.UTF-8
ENV LANG C.UTF-8

VOLUME ["/data"]
# update
RUN apt-get update && \
    apt-get install -y git cmake g++ libxerces-c-dev libcppunit-dev openjdk-8-jre

ENV VERSION "2018-06-06"
# install profiler
RUN mkdir /src && \
    cd /src && \
    git clone -b ocrd ${PROFILER_GIT} && \
    cd Profiler && mkdir build && cd build && \
    cmake -DCMAKE_BUILD_TYPE=release .. && \
    make -j 4 profiler && \
    mkdir /apps && \
    cp bin/profiler /apps/ && \
    cd / && \
		rm -rf /src/Profiler
COPY target/${OCRD_VERSION}-cli.jar /apps/

ENTRYPOINT ["/bin/sh", "-c"]
#COPY target/${OCRD_VERSION}.war /usr/local/tomcat/webapps
#COPY tomcat-users.xml ${CATALINA_HOME}/conf/tomcat-users.xml
#COPY context.xml ${CATALINA_HOME}/webapps/manager/META-INF/context.xml
#COPY context.xml ${CATALINA_HOME}/webapps/host-manager/META-INF/context.xml
