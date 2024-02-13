# MongoDB Micrometer Statistics

This is Plugin adds Micrometer Functionality to a [MongoCamp Server](../../index.md). So other Plugins can use Micrometer or you can get your Micrometer Statistics to Database or call them by HTTP.


## Configuration

### Dependency Setup
Add this to your [Plugin Modules](../../config/properties/plugins-module.md) to download the plugin from Maven Central.


<DependencyGroup organization="dev.mongocamp" name="mongocamp-plugin-micrometer" version="$$MC_VERSION$$" />

### MongoCamp Configuration Settings
#### Persistence Plugin

| Configuration                  | Description                                 | Default |   Type   |
|--------------------------------|---------------------------------------------|:-------:|:--------:|
| LOGGING_METRICS_MONGODB_STEP   | Duration between persist Metrics to MongoDB |   60m   | Duration |
| LOGGING_METRICS_MONGODB_JVM    | Should persist JVM Metrics to MongoDB       |  false  | Boolean  |
| LOGGING_METRICS_MONGODB_SYSTEM | Should persist SYSTEM Metrics to MongoDB    |  false  | Boolean  |
| LOGGING_METRICS_MONGODB_MONGO  | Should persist MONGO Metrics to MongoDB     |  false  | Boolean  |
| LOGGING_METRICS_MONGODB_EVENT  | Should persist EVENT Metrics to MongoDB     |  false  | Boolean  |

#### MongoDB Metrics Plugin

Plugin to monitor your MongoDB with your Application

| Configuration                  | Description                                     | Default |   Type   |
|--------------------------------|-------------------------------------------------|:-------:|:--------:|
| METRICS_MONGODB_DATABASE       | Should monitor all collections in Database      |  false  | Boolean  |
| METRICS_MONGODB_COLLECTIONS    | Should monitor explicit collections in Database |   []    | [String] |
| METRICS_MONGODB_CONNECTIONS    | Should monitor CONNECTIONS of your MongoDB      |  false  | Boolean  |
| METRICS_MONGODB_NETWORK        | Should monitor NETWORK of your MongoDB          |  false  | Boolean  |
| METRICS_MONGODB_OPERATION      | Should monitor OPERATION of your MongoDB        |  false  | Boolean  |
| METRICS_MONGODB_SERVER         | Should monitor SERVER of your MongoDB           |  false  | Boolean  |
| METRICS_MONGODB_COMMAND        | Should monitor COMMAND of your MongoDB          |  false  | Boolean  |
| METRICS_MONGODB_CONNECTIONPOOL | Should monitor CONNECTIONPOOL of your MongoDB   |  false  | Boolean  |

