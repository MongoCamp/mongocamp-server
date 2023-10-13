# HTTP Client Headers
The HTTP Client that is used for downloading JARs from remote systems can have headers. That are stored in your database as Json String.

Type: String

## Sample
```json
{
    "github.com": {
        "hello": "world",
    },
    "maven.pkg.github.com": {
        "hello": "world",
        "Authorization": "Bearer YOUR-GITHUB-TOKEN"
    }
}
```

## What is needed?
If you what to use the Header Parameter you have to add the host of your target to the map with a key value map of your header parameter.
So if you url is `https://maven.pkg.github.com/MongoCamp/mongocamp-sample-plugin/com.quadstingray/mongocamp-sample-plugin_2.13/0.5.0/mongocamp-sample-plugin_2.13-0.5.0.jar` you have to add the host `maven.pkg.github.com`.