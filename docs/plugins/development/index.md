---
title: Plugin Development
---
# {{ $frontmatter.title }}

If you need some extra features MongoCamp Server does not offer you, you can extend your instance by write your own plugin. A Sample Implementation you can see in our [ShowCase Sample Plugin](https://github.com/MongoCamp/mongocamp-sample-plugin).

<<< @/external/files/sample-build.sbt{scala}

You can add all your extra dependencies to your build.sbt. If you use the [preferred way](../../config/properties/plugins-module.md) to import plugins all dependencies are resolved automatically.

To make your own plugin functional and loaded you have to implement one of this Scala Traits:

- [Server Plugin](plugin-server.md)
- [Routes Plugin](plugin-routes.md)
- [Files Plugin](plugin-files.md)
