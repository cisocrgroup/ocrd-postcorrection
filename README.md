![build status](https://travis-ci.org/cisocrgroup/ocrd-postcorrection.svg?branch=dev)
OCRD REST API
====================


Eclipse/Maven project for OCRD Application Server REST API & UIF

## prerequisites
- JDK8 or better
- Maven 3
- Eclipse 4.X or better (installed maven plugin & e.g. Spring STS)


## build, run & dev-steps

### import to eclipse
1. clone repo to your local machine
2. Open Eclipse
3. File->Import->Maven->existing Maven project
4. Run from new projects's context menu Maven->Update project
5. New Java code from RAML is generated in /target/generated-sources/raml-jaxrs


### run local jetty dev-server

#### steps:
- run eclipse project on server: `RunLocalJetty.java`
- Point your browser to http://localhost:8181
    * `/` shows html & js pages which reside in `src/webapp`
    * `/api/` points to all api endpoints


#### API documentation:
- install raml2html module (https://github.com/raml2html/raml2html)
- to generate use: raml2html api.raml > api.html



OCRD WEB APP
====================

- sources in webapp-src folder

### contents
all source files which are needed to build the webapp (e.g. backbone js code & HTML markup code)

### build
use build scripts to create a build. The build-script shall sync the build to `src/webapp`
