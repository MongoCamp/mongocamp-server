# Environment or Database Configuration

The configuration settings for the applications are loaded from docker container by environment or from your database.
If you have a configuration in you environment variables that one ALWAYS wins. The System checks the variable in the database und update them with the value from your System Env

The configurations can also change by using the configuration routes.

## Auth

| Configuration          | Description                                                                   |  Default   |
|------------------------|-------------------------------------------------------------------------------|:----------:|
| AUTH_HANDLER           | Handler for managing User and Role Data. Possible Values: `mongo` or `static` |   mongo    |
| AUTH_APIKEYLENGTH      | Length of the generated API Key                                               |     32     |
| AUTH_SECRET            | Private Key for JWT                                                           | secret_key |
| AUTH_CACHE_DB          | Should the JWT cached to the DB to keep it after application restart.         |    true    |
| AUTH_EXPIRING_DURATION | Lifetime of generated JWT                                                     |     6h     |
| AUTH_BEARER            | Enable Bearer Header Token for secured routes.                                |    true    |
| AUTH_BASIC             | Enable HttpBasicAuth for secured routes.                                      |    true    |
| AUTH_TOKEN             | Enable Api Key Token for secured routes.                                      |    true    |

## Server

| Configuration          | Description | Default |
|------------------------|-------------|:-------:|
| SERVER_INTERFACE       |             | 0.0.0.0 |
| SERVER_PORT            |             |  8080   |

## CORS

| Configuration        | Description                                                |                                                               Default                                                                |
|----------------------|------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------:|
| CORS_HEADERS_ALLOWED | Used as Access-Control-Allow-Origin                        |                               `["Authorization", "Content-Type", "X-Requested-With", "X-AUTH-APIKEY"]`                               |
| CORS_HEADERS_EXPOSED | Used as Access-Control-Expose-Headers                      | `["Content-Type", "x-pagination-rows-per-page", "x-pagination-current-page", "x-pagination-count-rows", "x-pagination-count-pages"]` |
| CORS_ORIGINS_ALLOWED | Access-Control-Expose-HeadersAccess-Control-Expose-Headers |                                                     `["http://localhost:8080"]`                                                      |

## File

| Configuration  | Description                                                      | Default |
|----------------|------------------------------------------------------------------|:-------:|
| FILE_HANDLER   | Default only supported `gridfs`. Possible to extend with Plugin. | gridfs  |
| FILE_CACHE_AGE | Used at File Header `cache-control` with value `max-age=`        | 7776000 |

## Plugins
::: info
All plugins in Folder and Modules will be activated automatically. If your want to exclude one of this plugins use `PLUGINS_IGNORED`, for example if you don't want to expose Metrics you can add `dev.mongocamp.server.route.MetricsRoutes$` to ignored plugins. 
:::

| Configuration                                           | Description                                             |         Default          |
|---------------------------------------------------------|---------------------------------------------------------|:------------------------:|
| PLUGINS_IGNORED                                         | List of ignored Plugins.                                |           `[]`           |
| [PLUGINS_MODULES](properties/plugins-module.md)         | List of your used Plugins / Maven Dependencies.         |           `[]`           |
| [PLUGINS_MAVEN_REPOSITORIES](properties/plugins-mvn.md) | List of your special MVN Repository (maybe private).    |           `[]`           |
| PLUGINS_DIRECTORY                                       | Directory where the app search the plugins.             | `/opt/mongocamp/plugins` |
| [PLUGINS_URLS](properties/plugins-urls.md)              | Urls of JARs the Server should download before startup. |           `[]`           |

## HttpClient

| Configuration                                            | Description                                                                                            | Default |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------|:-------:|
| [HTTP_CLIENT_HEADERS](properties/http-client-headers.md) | Additional headers for hosts saved in the Database as String containing a Json with Headers. Sample at |  `{}`   |

## Docs

| Configuration | Description                               | Default |
|---------------|-------------------------------------------|:-------:|
| DOCS_SWAGGER  | Should the SwaggerUI served by server.    |  false  |
| DOCS_OPENAPI  | Should the OpenApi Yaml served by server. |  true   |