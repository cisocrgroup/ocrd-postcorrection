src folder structure
==========
### `enmap`.rest.raml
* package enmap.rest.raml
* java classes holding handlers & api implementation classes
* api mapped to `<context-root>`/api
* subfolders:
      * `handler`
        * jdbcHandler
        * proposal-handler (methods for db-communication, which are needed by implementation classes under `enmap.rest.raml.impl`)
        * requestHandler for OR-SPC'
    	* all other handlers...
      * `impl`
        * Implementation of API-Interfaces
            * all implementations of auto-generated Interface-Classes created by raml-jaxrs plugin
        * other servlets
        * AppConfig
            * registers all needed implementation-classes to tomcat
            * used by `webapp` deployment descriptor
        * RunLocalGrizzly
            * main-class runs simple grizzly http server at port 8181
                * http://localhost:8181/ -> `<context-root>` showing UI
                * http://localhost:8181/api/ -> all `api` endpoints
      * `utils`
        * custom classes needed for api

### `resources`
* raml objects holding api-spec(=non java)
* java jax-rs classes derived via raml-jaxrs-plugin

### `webapp`
* holds compiled frontend code (html, js)
* contains deployment descriptor
* mapped to `<context-root>`/

### `webapp-src`
* holds webapp js & html source
* use build-script (e.g. node.js build script) which auto-sync's build-results to `webapp`