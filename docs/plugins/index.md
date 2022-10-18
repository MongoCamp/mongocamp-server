---
title: Plugins
---
# {{ $frontmatter.title }}

::: info
Every thing is just a plugin.
::: 

You can extend your own MongoCamp Server Instance by add plugins to have some additional features. 

There are different ways to add your plugin's in MongoCamp Server. The easiest way is to add the URLs for downloading the plugin to the configuration [PLUGINS_URLS](../config/properties/plugins-urls.md), or you can add the Plugin JAR in the Folder you defined in `PLUGINS_DIRECTORY`.

::: info
You need to add all dependencies of you plugin to the folder or the download property. 
:::