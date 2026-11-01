---
title: Plugin Development
---
# {{ $frontmatter.title }}

If you need some extra features MongoCamp Server does not offer you, you can extend your instance by write your own plugin. A Sample Implementation you can see in our [ShowCase Sample Plugin](https://github.com/MongoCamp/mongocamp-sample-plugin).

::: warning Scala 3
MongoCamp Server is built with Scala 3. A plugin has to be built with Scala 3 as well: depend on the `_3` artifacts of the server (`"dev.mongocamp" %% "mongocamp-server"` in sbt) and compile with a Scala 3 compiler that is at least as new as the one the server was built with (`scalaVersion` in the `build.sbt` of the server), otherwise sbt can not resolve the dependencies of the server (or the compiler can not read its API). Plugins published for Scala 2.13 (`_2.13` artifacts) are not supported by a Scala 3 server.
:::

<<< @/external/files/sample-build.sbt{scala}

You can add all your extra dependencies to your build.sbt. If you use the [preferred way](../../config/properties/plugins-module.md) to import plugins all dependencies are resolved automatically.

To make your own plugin functional and loaded you have to implement one of this Scala Traits:

- [Server Plugin](plugin-server.md)
- [Routes Plugin](plugin-routes.md)
- [Files Plugin](plugin-files.md)
