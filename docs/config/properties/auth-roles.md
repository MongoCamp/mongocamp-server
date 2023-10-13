# Auth Users
The list of roles for the users in your MongoCamp Server Instance, if you have configured as Static Login Handler.

::: tip
This Configuration is only needed for `AUTH_HANDLER` is `static`.
:::

Type: List[String]

## Sample
```json
[
  "{\"name\": \"admin\", \"isAdmin\": true,  \"collectionGrants\": []}",
  "{\"name\": \"test\", \"isAdmin\": false, \"collectionGrants\" : [{\"collection\": \"test\", \"read\": true, \"write\": false, \"administrate\": false}, {\"collection\": \"test\", \"read\": false, \"write\": true, \"administrate\": true}]}"
]
```