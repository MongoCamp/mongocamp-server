# Plugins Module
The best and recommended way to ad your dependencies and plugins to your MongoCamp instance is using a [Maven Repository](plugins-mvn.md) and Maven Modules.      

## Format
`{groupId}:{artifactId}:{version}`

The `artifactId` contains the Scala version suffix of the plugin, plugins for MongoCamp Server have to be built with Scala 3, so the artifact ends with `_3` (for example `mongocamp-sample-plugin_3`).

::: tip
When you use https://mvnrepository.com/ to search and copy your dependencies you can copy `Gradle (Short)`. 
:::