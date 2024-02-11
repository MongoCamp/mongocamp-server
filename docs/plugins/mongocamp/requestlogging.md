# MongoDB Request Logging

The MongoDB Request Logging plugin logs all requests to a [MongoCamp Server](../../index.md). This can be useful for debugging and performance analysis.

## Configuration

### Dependency Setup
Add this to your [Plugin Modules](../../config/properties/plugins-module.md) to download the plugin from Maven Central.


<DependencyGroup organization="dev.mongocamp" name="mongocamp-plugin-requestlogging" version="$$MC_VERSION$$" />

### MongoCamp Configuration Settings

| Configuration            | Description                          | Default |   Type   |
|--------------------------|--------------------------------------|:-------:|:--------:|
| REQUESTLOGGING_ENABLED   | Activates Request Logging to MongoDB |  true   | Boolean  |

