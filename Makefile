DESTDIR ?=
PREFIX ?= /usr/share
LIBDIR ?= ${PREFIX}/lib
SRCS := ${shell find src/main/java/ -type f -name '*.java'}
JAR := target/ocrd-0.1-cli.jar

default: ${JAR}

${JAR}: ${SRCS}
	mvn -q --batch-mode -Dmaven.test.skip=true package

.PHONY: install
install: ${JAR}
	install -D $< ${DESTDIR}${LIBDIR}/ocrd.jar

.PHONY: test
test:
	mvn test
