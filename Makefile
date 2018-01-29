SUDO ?= "sudo"
TAG ?= "ocrd"
P ?= "8888:8080"

default: compile

package:
	mvn package

compile:
	mvn compile

test:
	mvn test

deploy: Dockerfile target/ocrd-0.1.war
	${SUDO} docker build --tag ${TAG} .
#	${SUDO} docker run -it -p ${P} --entrypoint /bin/bash ${TAG}
	${SUDO} docker run -it -p ${P} ${TAG}

target/ocrd-0.1.war: webapp
	mvn  war:war

webapp: src/webapp/api.html
	cd src/webapp-src && ./build.sh

clean:
	$(RM) -f target

.PHONY: copile test deploy package clean
