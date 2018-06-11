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
	docker run -v "${VOLUME}:/data" ${OCRD_VERSION} "java -ea -jar /apps/${JAR} -l DEBUG -m m -w /data -I I -O O -c train --parameter /data/ocr/defaultConfiguration.json ocrd-train /data/ocr/1841-DieGrenzboten-gt.zip /data/ocr/1841-DieGrenzboten-abbyy.zip /data/ocr/1841-DieGrenzboten-ocropus.zip /data/ocr/1841-DieGrenzboten-tesseract.zip"

.PHONY: docker
