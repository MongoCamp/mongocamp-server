---
title: Routes Plugin
---
# {{ $frontmatter.title }}

To add your own routes you have to implement the trait `dev.mongocamp.server.plugin.RoutesPlugin`.

Routes are implemented with [Tapir Endpoints](https://tapir.softwaremill.com/en/latest/endpoint/basics.html) and activated by Reflection. 

## Sample
<<< @/external/files/SampleRoutes.scala{scala}