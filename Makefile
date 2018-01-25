.PHONY: copile test deploy package

default: compile

package:
	mvn package

compile:
	mvn compile

test:
	mvn test

deploy: target/ocrd-0.1.war webapp
	cp target/ocrd-0.1.war ${CATALINA_HOME}/webapps/ocrd-0.1.war

target/ocrd-0.1.war:
	mvn compile war:war

webapp: src/webapp/api.html
	cd src/webapp-src && ./build.sh
