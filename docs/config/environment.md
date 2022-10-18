# Environment Configuration

The configuration settings for the applications are loaded from docker container by environment variables and in normal cases you have to override this.

## MongoDb Connection
| Configuration       |  Default  |      Type      |
|---------------------|:---------:|:--------------:|
| CONNECTION_HOST     | localhost |     String     |
| CONNECTION_PORT     |   27017   |      Long      |
| CONNECTION_DATABASE | mongocamp |     String     |
| CONNECTION_USERNAME |           | Option[String] |
| CONNECTION_PASSWORD |           | Option[String] |
| CONNECTION_AUTHDB   |   admin   |     String     |

## Auth
| Configuration                          | Description                                  |  Default   |     Type     |
|----------------------------------------|----------------------------------------------|:----------:|:------------:|
| AUTH_PREFIX                            | Prefix for the MongoCamp needed Collections. |    mc_     |    String    |
| [AUTH_USERS](properties/auth-users.md) | only needed for static auth handler          | Empty List | List[String] |
| [AUTH_ROLES](properties/auth-roles.md) | only needed for static auth handler          | Empty List | List[String] |
