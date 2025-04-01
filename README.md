
# MongoCamp 

![](docs/public/logo_with_text_right.png)

![Docker Pulls](https://img.shields.io/docker/pulls/mongocamp/mongocamp-server) ![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/mongocamp/mongocamp-server?sort=semver) ![Docker Image Version (latest by date)](https://img.shields.io/docker/v/mongocamp/mongocamp-server?sort=semver)![Maven Central](https://img.shields.io/maven-central/v/dev.mongocamp/mongocamp-server_2.13)

## Introduction

Universal REST Handling for MongoDB ....


### Contributing
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/MongoCamp/mongocamp-server)

If you find this library helpfull, so you can see here how you can help:
- Send a pull request with your features and bug fixes
- Help users resolve their [issues](https://github.com/mongocamp/mongocamp-server/issues).


### Issues
Feel free to report your issues to Github's issue tracker. Please use one of the templates for reporting issues. [report your issue](https://github.com/mongocamp/mongocamp-server/issues/new/choose)

## Documentation
[Documentation](https://server.mongocamp.dev/) is still on progress.

## ShowCase
[ShowCase](https://showcase.mongocamp.dev/docs/) is a started is an running Instance of MongoCamp-Server with installed [Sample-Plugin](https://github.com/MongoCamp/mongocamp-sample-plugin). All Data are reverted every 30 minutes. 

### Admin User
User: admin
Password: admin

### Default User
User: test
Password: test

## Licence
[Apache 2 License.](https://github.com/mongocamp/mongocamp-server/blob/master/LICENSE)

## Usage
You can use [Docker Compose](https://server.mongocamp.dev/guide/getting-started.html) or directly use directly docker run command.
```shell
docker run --publish 8080:8080 -e CONNECTION_HOST=your.mongo.db.host -e CONNECTION_DATABASE=test -e CONNECTION_USERNAME=mongodbUser -e CONNECTION_PASSWORD=mongodbPwd mongocamp/mongocamp-server:latest
```

[error] Failed tests:
[error]         dev.mongocamp.server.tests.JobSuite
[error]         dev.mongocamp.server.tests.FileSuite