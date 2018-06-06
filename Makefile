OCRD_VERSION ?= "ocrd-0.1"
VOLUME ?= "${HOME}/data/ocrd-volume"
JAR = ${OCRD_VERSION}-cli.jar

default: docker

src := ${shell find src/main/java -type f -iname '*.java'}
target/${JAR}: ${src}
	mvn -q -Dmaven.test.skip=true package
test:
	mvn -q test

docker: target/${JAR} Dockerfile
	docker build -t ${OCRD_VERSION} .

train: docker
	docker run -v "${VOLUME}:/data" ${OCRD_VERSION} "java -jar /apps/${JAR} -m m -w /data -I I -O O -c train --parameter /data/defaultConfiguration.json ocrd-train /data/ocr/1841-DieGrenzboten-gt-small.zip /data/ocr/1841-DieGrenzboten-abbyy-small.zip /data/ocr/1841-DieGrenzboten-ocropus-small.zip /data/ocr/1841-DieGrenzboten-tesseract-small.zip"

.PHONY: docker
