---
title: Server Plugin
---
# {{ $frontmatter.title }}

To add your own FileHandler you have to implement the trait `dev.mongocamp.server.plugin.ServerPlugin`.

The only method you have to implement is the Method `activate` this is called by activating the plugin. 
