OCRD_VERSION ?= "ocrd-0.1"
VOLUME ?= "${HOME}/data/ocrd-volume"
JAR = ${OCRD_VERSION}-cli.jar

SRCS := ${find src -type f -name "*.java"}
default: compile

.PHONY: compile
compile: ${SRCS} # Does not work because of .PHONY target.
	mvn -B $@

tags: ${SRCS}
	rm -rf TAGS
	find src -name "*.java" -print | xargs etags -a
	mv TAGS $@

src := ${shell find src/main/java -type f -iname '*.java'}
target/${JAR}: ${src}
	mvn -q -Dmaven.test.skip=true package

.PHONY: test
test: ${SRCS} # Does not work because of .PHONY target.
	mvn -B test

docker: target/${JAR} Dockerfile
	docker build -t ${OCRD_VERSION} .

train: docker
	mkdir -p ${VOLUME}/ocr
	cp src/test/resources/*.zip ${VOLUME}/ocr
	cp src/main/resources/*.json ${VOLUME}/ocr
	docker run -v "${VOLUME}:/data" ${OCRD_VERSION} "java -ea -jar /apps/${JAR} -l DEBUG -m m -w /data -I I -O O -c train --parameter /data/ocr/defaultConfiguration.json ocrd-train /data/ocr/1841-DieGrenzboten-gt.zip /data/ocr/1841-DieGrenzboten-abbyy.zip /data/ocr/1841-DieGrenzboten-ocropus.zip /data/ocr/1841-DieGrenzboten-tesseract.zip"

.PHONY: docker
