OCRD_VERSION ?= "ocrd-0.1"

default: docker

src := ${shell find src/main/java -type f -iname '*.java'}
target/${OCRD_VERSION}-cli.jar: ${src}
	mvn compile

docker: target/${OCRD_VERSION}-cli.jar Dockerfile
	docker build -t ${OCRD_VERSION} .

train:
	docker run -v "${HOME}/data/ocrd-volume:/data" ${OCRD_VERSION} "java -jar /apps/${OCRD_VERSION}-cli.jar -m m -w /data -I I -O O -c train --parameter /data/defaultConfiguration.json ocrd-train /data/ocr/1841-DieGrenzboten-gt.zip /data/ocr/1841-DieGrenzboten-abbyy.zip /data/ocr/1841-DieGrenzboten-ocropus.zip /data/ocr/1841-DieGrenzboten-tesseract.zip"

.PHONY: docker
