---
title: Files Plugin
---
# {{ $frontmatter.title }}

To add your own FileHandler you have to implement the trait `dev.mongocamp.server.plugin.FilePlugin`.

Every MongoCamp Server Instance can have only active File Handler by setting the [Config AUTH_HANDLER](../../config/environment-db.md).

## Sample
<<< @../../src/main/scala/dev/mongocamp/server/file/GridFsFileAdapter.scala{scala}