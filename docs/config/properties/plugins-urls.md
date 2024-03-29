# Plugins Urls

::: warning
Recommended way for loading Plugins is using [PLUGINS_MODULES](plugins-module.md). This loader resolves all dependencies of used plugin. 
:::

The URLs for Plugins or other JARs you need in your individual MongoCamp instance. 
The URLs were resolved as GET-Request to download the file to the folder you configured [PLUGINS_DIRECTORY](../environment-db.md#plugins) in the subfolder `managed`.

::: info
You have to configure all needed dependencies of your used plugins as download url as well. 
:::

## Github Maven Repository
To download an JAR or Plugin from the GitHub Maven Repository you have to configure a url with that pattern:
`https://maven.pkg.github.com/<org>/<repo>/com/<group>/<artifact>/<version>/<file-name>.jar`

Copying a link in the Web UI don`t work. More Information at [this Blog Post](https://josh-ops.com/posts/github-download-from-github-packages/). Your also have to add an [HTTP-Client-Header](http-client-headers.md) in your Configuration with an individual Github Token. 

## Maven Central
Navigate to [Maven Central Repository](https://repo1.maven.org/) and search your JAR to download and copy link to your Database.