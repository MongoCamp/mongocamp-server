# Configuration

The configuration settings for the applications are loaded from docker container by environment variables.

## MongoDb Connection
| Configuration       | Description |  Default  |
|---------------------|-------------|:---------:|
| CONNECTION_HOST     |             | localhost |
| CONNECTION_PORT     |             |   27017   |
| CONNECTION_DATABASE |             | mongocamp |
| CONNECTION_USERNAME |             |           |
| CONNECTION_PASSWORD |             |           |
| CONNECTION_AUTHDB   |             |  admin    |

## Auth
| Configuration          | Description                                                                   |  Default   |
|------------------------|-------------------------------------------------------------------------------|:----------:|
| AUTH_PREFIX            | Prefix for the MongoCamp needed Collections.                                  |    mc_     |
| AUTH_HANDLER           | Handler for managing User and Role Data. Possible Values: `mongo` or `static` |   mongo    |
| AUTH_USERS             | only needed for static auth handler                                           |  `["{}"]`  |
| AUTH_ROLES             | only needed for static auth handler                                           |  `["{}"]`  |
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
| REQUESTLOGGING_ENABLED |             |  true   |

## CORS
| Configuration         | Description                                                |                                                               Default                                                                |
|-----------------------|------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------:|
| CORS_HEADERS_ALLOWED  | Used as Access-Control-Allow-Origin                        |                               `["Authorization", "Content-Type", "X-Requested-With", "X-AUTH-APIKEY"]`                               |
| CORS_HEADERS_EXPOSED  | Used as Access-Control-Expose-Headers                      | `["Content-Type", "x-pagination-rows-per-page", "x-pagination-current-page", "x-pagination-count-rows", "x-pagination-count-pages"]` |
| CORS_ORIGINS_ALLOWED  | Access-Control-Expose-HeadersAccess-Control-Expose-Headers |                                                     `["http://localhost:8080"]`                                                      |

## File
| Configuration  | Description                                                      | Default  |
|----------------|------------------------------------------------------------------|:--------:|
| FILE_HANDLER   | Default only supported `gridfs`. Possible to extend with Plugin. |  gridfs  |
| FILE_CACHE_AGE | Used at File Header `cache-control` with value `max-age=`        | 7776000  |

## Plugins
| Configuration     | Description                                 |         Default          |
|-------------------|---------------------------------------------|:------------------------:|
| PLUGINS_IGNORED   | List of ignored Plugins.                    |          `[]`            |
| PLUGINS_DIRECTORY | Directory where the app search the plugins. | `/opt/mongocamp/plugins` |

## Docs
| Configuration | Description                               | Default |
|---------------|-------------------------------------------|:-------:|
| DOCS_SWAGGER  | Should the SwaggerUI served by server.    |  false  |
| DOCS_OPENAPI  | Should the OpenApi Yaml served by server. |  true   |