mvn = mvn
java = java

default: all

.PHONY: all
all: build build-webapp

.PHONY: build
build: generate
	$(mvn) package

.PHONY: generate
generate:
	$(mvn) generate-sources

.PHONY: build-webapp
build-webapp: build src/webapp-src/build.sh
	cd src/webapp-src && bash build.sh

.PHONY: run
run: all
	java -jar target/ocrd-0.1-jar-with-dependencies.jar
