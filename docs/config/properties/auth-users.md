# Auth Users
The list of users to log in to your MongoCamp Server Instance, if you have configured as Static Login Handler.  

::: tip
This Configuration is only needed for `AUTH_HANDLER` is `static`.
:::

Type: List[String]

## Sample
```json
[
  "{\"userId\":\"test\", \"password\":\"test1234\", \"apiKey\":\"apiKey\", \"roles\" : [\"admin\",  \"test\"]}",
  "{\"userId\":\"admin\", \"password\":\"test1234\", \"apiKey\":\"apiKey\", \"roles\" : [\"admin\",  \"test\"]}"
]
```