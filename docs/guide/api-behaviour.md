# API behaviour

Rules of the REST API that are not visible in the types of the OpenAPI document.

## Document ids
The `_id` of a document can have any type. A string with 24 hexadecimal characters and `{"$oid": "..."}` are ObjectIds, every other value (strings, numbers) is used as it is.
The id in the path of the single document endpoints is a string: besides the ObjectId or the string itself an id that looks like a number (`12345`, `1.5`) is also searched as number.
A filter can use ids with `{"_id": "abc"}`, `{"_id": {"$in": ["abc", "507f1f77bcf86cd799439011"]}}` and `{"_id": {"$oid": "507f1f77bcf86cd799439011"}}`.

## Dates and numbers in JSON bodies
A string in the body of an insert, update or filter is stored as **date** if it is a full ISO-8601 instant: date, time with seconds, an optional fraction of a second and a zone (`Z` or an offset).

| Value | Stored as |
|---|---|
| `"2024-06-15T12:00:00.000Z"`, `"2024-06-15T12:00:00Z"`, `"2024-06-15T14:00:00+02:00"`, `"2024-06-15T12:00:00.5Z"` | date |
| `"2024-06-15"`, `"2024-06-15T12:00:00"` (no zone), any other text | string |
| `{"$date": "2024-06-15T12:00:00Z"}`, `{"$date": 1718452800000}`, `{"$date": {"$numberLong": "1718452800000"}}` | date |
| `{"$numberLong": "12345678901234567"}` | 64 bit integer |
| `12345678901234567` (JSON number that fits into 64 bit) | 64 bit integer |

The Extended JSON wrappers `$date` and `$numberLong` are read in writes and in filters (`{"d": {"$gt": {"$date": "2024-06-16T00:00:00Z"}}}`), also nested in objects and lists. Responses never contain them: dates are
written as ISO-8601 strings (`2024-06-15T12:00:00Z`, zero milliseconds are omitted) and integers as JSON numbers.

::: warning
JSON numbers above 2^53 lose precision in JavaScript (`JSON.parse`). The server returns the exact value, a JavaScript client has to read such numbers as strings or `BigInt`.
:::

An invalid wrapper (`{"$date": "yesterday"}`, `{"$numberLong": "abc"}`) answers `400`.

## Empty filters
`DELETE .../documents/many/delete` and `PATCH .../documents/many/update` refuse an empty filter (`{}`) with `400` (error code 4003), because it matches every document. Send the query parameter `matchAll=true` if
all documents are meant.

An empty list for `PUT .../documents/many/insert` answers `400`, a duplicate `_id` answers `409`.

## Lucene filters
The `filter` parameter of `GET .../documents` is a Lucene query. A filter that cannot be parsed or converted answers `400` with the reason in the `x-error-additional-info` header.
Open ranges (`n:[5 TO *]`), `>=` / `>`, `_exists_` and `field:*` on fields that are not text are not supported by the query conversion of the driver
([MongoCamp/mongodb-driver#86](https://github.com/MongoCamp/mongodb-driver/issues/86)).

## Aggregation
`POST .../aggregate` takes stages as `{"stage": "match", "value": {...}}`, the value is any JSON value.

- Operators that run JavaScript on the database server or write to collections are refused with `403`, anywhere in the pipeline (also in `$project`, `$addFields`, `$group`, `$match`, `$lookup` and `$facet`).
  The list is configured with [`AGGREGATION_STAGES_DENIED`](../config/environment-db.md#aggregation), the default is `$function`, `$accumulator`, `$where`, `$out`, `$merge`, `$currentOp` and `$listSessions`.
- `$lookup`, `$unionWith` and `$graphLookup` read other collections: the user needs the read grant for each of them, otherwise the answer is `403`. The collection of a stage is in the database of the collection of
  the request (`geodata:locations` with `from: "accounts"` reads `geodata:accounts`). A reference to a collection of another database (`{"db": ..., "coll": ...}`) is not supported.

## Tokens
`GET /auth/token/refresh` issues a new token. The bearer token that was used for the call stays valid for a grace period (`AUTH_REFRESH_GRACE_DURATION`, default 60 seconds, for concurrent requests) and is
invalid afterwards. A call with basic auth or an API key has no bearer token and revokes nothing. The token does not contain the API key of the user, `/auth/profile` returns it.

## Permissions

`GET /auth/permissions` returns what the logged in user may do, resolved by the server with the same matching that protects the routes (case-insensitive names, the `*` wildcard, admins may do everything):

```json
[{ "name": "accounts", "read": true, "write": true, "administrate": false }]
```

Without a parameter only the collections of the default database with at least one right are listed. With `?collection=name` exactly that collection is answered, also when the user has no right (all flags `false`). The collection lists (`GET /mongodb/collections`, `GET /mongodb/databases/{db}/collections`) use the same matching, so a collection that is listed can be opened.

## Counting documents

`GET /mongodb/collections/{name}/documents/count?filter=` returns `{ "count": n }` for the documents that match the Lucene filter (all documents without a filter). It needs the read grant and uses the same filter parser as the list, so an invalid filter is a 400. The path `documents/count` belongs to this route: a document whose `_id` is the string `count` cannot be fetched with `GET .../documents/count` any more, use the list with `filter=_id:count` or the find route.
