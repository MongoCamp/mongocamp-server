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
  The list is configured with [`AGGREGATION_STAGES_DENIED`](../config/environment-db.md#aggregation), the default is `$function`, `$accumulator`, `$where`, `$out`, `$merge`, `$currentOp`, `$listSessions`, `$collStats` and `$indexStats`. The statistics stages are denied because their result contains the address of the database server (`host`) and the namespace (`ns`); the usage of the indexes is available from `GET .../index/stats`. A deployment that saved its own list keeps it, add the two stages to it.
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

## Update and delete by filter string

`PATCH .../documents/many/update` and `DELETE .../documents/many/delete` accept the Lucene filter of the list as query parameter `filter`, as an alternative to the MongoDB filter of the request body (send `{}` as the body of a delete). Use only one of them: both together answer 400 with code `4004`, because a filter that is silently ignored would touch more documents than intended. A filter string that matches no document changes nothing, an empty filter still needs `matchAll=true` (code `4003`). Both operations load the matching documents for the events, so very large matches use memory in proportion.

## Missing grants

A user who is logged in but has no grant for a collection, a bucket or an admin route is refused with the status of `AUTH_GRANT_DENIED_STATUS`, `401` by default. A client such as the UI clears its session on `401`, so a user who only lacks one grant is logged out; set the value to `403` to avoid that. The error code `4011` and the message (`user not authorized for collection`, `... for bucket`, `... for request`) are the same for both. A missing or invalid token is always `401`. The default changes to `403` in the next major version.

## Errors of the database

A request that the database refuses because of its content is answered with the status that fits, the `code` is the error code of the database (also in the header `x-error-code`) and the message does not contain the address of the server:

| Status | Cases |
|---|---|
| `400` | an invalid pipeline (unknown stage, `$unwind` without `$`, `$limit` that is not a number, `$group` without `_id`, unknown operator), an empty field name in `sort` or `projection`, an index without keys, dropping the index of `_id` |
| `404` | the database reports that an index or a collection does not exist (whether it does that for dropping an index that does not exist depends on the database, some versions answer with success) |
| `409` | a duplicate key, also when a unique index is created over duplicate data, an index that conflicts with an existing one |
| `500` | everything else, for example a database that shuts down or a lost connection |

The status is derived from the error code of the database, so a case that is not listed stays a `500` until its code is known to be a mistake of the client.

The messages of these errors do not name the database, the collection, the id of an index build or the values of a duplicate key: `E11000 duplicate key error collection: shop.customers index: email_1 dup key: { email: "a@b.c" }` is answered as `E11000 duplicate key error, index: email_1`.

## Index usage

`GET /mongodb/collections/{name}/index/stats` (administrate grant) returns how often every index was used since the counting started, without the address of the database server:

```json
[{ "name": "email_1", "key": { "email": 1 }, "ops": 42, "since": "2026-11-05T08:00:00Z" }]
```

The details of the collection (`GET /mongodb/collections/{name}?includeDetails=true`) do not contain `host`, `ns` and `localTime` any more.

## Search in the request body

`POST /mongodb/collections/{name}/documents/search` is the list of documents with the parameters in the request body, for filters that do not fit in a URL: the server refuses a URL of more than 2048 characters before the request is handled (`414`, without CORS headers, so a browser reports a network error).

```json
{ "filter": "name:John AND age:[18 TO 65]", "sort": ["-age"], "projection": ["name", "age"] }
```

`filter` is a Lucene query like in the list, `sort` and `projection` are lists of field names like the query parameters (a leading `-` sorts descending), `rowsPerPage` and `page` are query parameters. The answer and the `x-pagination-*` headers are the same as for the list, an invalid filter is a `400` with code `4001` and the reason in `x-error-additional-info`. The route `POST .../documents` with a MongoDB filter in the body stays as it is.

## Aggregation with a filter

`POST /mongodb/collections/{name}/aggregate?filter=` takes the Lucene filter of the list and puts it in front of the pipeline as a `$match` stage, so the aggregation works on exactly the documents that the list shows (and the database can use an index for it). The filter is converted like the filter of the list (`_id`, dates), an invalid filter is a `400`, the read grant of the collection applies as before.

A pipeline that has to start with a special stage (`$geoNear`, `$search`, `$searchMeta`, `$vectorSearch`, `$documents`) cannot get a stage in front of it: with a filter it is refused with `400` and code `4005`, put the conditions into the stage itself (`query` of `$geoNear`). The count of the pagination headers is the count of the result of the filtered pipeline.

## Insert many without stopping at the first error

`PUT /mongodb/collections/{name}/documents/many/insert?ordered=false` stores every document that can be stored and lists the others, so that a retry can send exactly these:

```json
{
  "wasAcknowledged": true,
  "insertedIds": ["u1", "u3", "u5"],
  "insertedCount": 3,
  "errors": [{ "index": 1, "code": 11000, "message": "E11000 duplicate key error, index: _id_" }]
}
```

`index` is the position of the document in the request, `code` the error code of the database. Documents without an `_id` get an ObjectId from the server, so that `insertedIds` is complete. With `ordered=true` (the default) nothing changes: the insert stops at the first document that fails, a duplicate key is a `409`, the documents before it are stored. `insertedCount` and `errors` (empty) are part of that answer too.

## Information of all collections

`GET /mongodb/collections/info` returns the numbers of all collections of the default database that the user may read in one request, instead of one `GET /mongodb/collections/{name}` per collection:

```json
[{ "name": "accounts", "collectionType": "Standard", "count": 1000, "size": 480000, "storageSize": 215000, "avgObjSize": 480, "nindexes": 1, "totalIndexSize": 36000, "available": true }]
```

With `?name=a&name=b` only these collections are given (a collection that the user may not read is never given). A collection without statistics, for example a view, is listed with `available: false` and zeros. The database is asked once per collection, so on a server with very many collections restrict the answer with `name`. A collection with the name `info` is shadowed for `GET /mongodb/collections/info`, its information is available with the other routes of the collection.

## Fields with types

`GET /mongodb/collections/{name}/fields/detail?sampleSize=&depth=` returns all fields as a flat list, one entry per path, with the types of the values and how often every type was found in the analysed documents:

```json
[
  { "path": "address", "types": [{ "type": "object", "count": 20 }] },
  { "path": "address.city", "types": [{ "type": "string", "count": 20 }] },
  { "path": "age", "types": [{ "type": "long", "count": 15 }, { "type": "string", "count": 5 }] },
  { "path": "tags[]", "types": [{ "type": "string", "count": 40 }] }
]
```

The elements of an array are the path with `[]` (`tags[]`), the type names are the ones of the database (`$type`). `sampleSize` is 1000 by default, the analysis of a big collection with a greater sample is more exact and slower. `depth` (default 3) limits how deep objects are followed. `GET .../schema/analysis` returned `percentageOfAnalysed` as 0 for every sample that is smaller than the collection (an integer division), it is the share now (`0.25` for 5 of 20 documents).

## Indices with details

`GET /mongodb/collections/{name}/index/details` (administrate grant) lists the indices as the database reports them. The older `GET .../index` returns the keys as a map without a reliable order and a text index with the internal keys `_fts` and `_ftsx`.

```json
[
  { "name": "compound", "keys": [{ "field": "c", "direction": "1" }, { "field": "b", "direction": "-1" }], "unique": false, "sparse": false, "hidden": false, "version": 2 },
  { "name": "texts", "keys": [{ "field": "title", "direction": "text" }, { "field": "body", "direction": "text" }], "weights": { "title": 10, "body": 1 }, "defaultLanguage": "german" }
]
```

`keys` is in the order of the index (the order of a compound index matters), `direction` is `1` or `-1` for a sorted key, otherwise the type of the key (`text`, `2dsphere`, `2d`, `hashed`). A text index lists its fields, `weights` has their weight. Also given when the index has them: `partialFilterExpression`, `collation`, `defaultLanguage`, `expireAfterSeconds`. An index with the name `details` is not reachable by name with `GET .../index/{indexName}`.

## Creating indices

`PUT /mongodb/collections/{name}/index` takes the keys of the index as `orderedKeys`, a list in the order of the index. `direction` is `1`, `-1` or the type of the key (`text`, `2dsphere`, `2d`, `hashed`), so a compound index with several text fields is possible:

```json
{
  "orderedKeys": [{ "field": "category", "direction": "1" }, { "field": "title", "direction": "text" }, { "field": "body", "direction": "text" }],
  "indexOptionsRequest": { "name": "texts", "weights": { "title": 5, "body": 1 }, "defaultLanguage": "german" }
}
```

The map `keys` (`{"a": 1, "b": -1}`) of the older versions still works, send one of both (both is a `400`). The options have `sparse`, `hidden`, `partialFilterExpression`, `weights` and `collation` (`locale` is required, also `strength`, `caseLevel`, `caseFirst`, `numericOrdering`, `alternate`, `maxVariable`, `backwards`, `normalization`) besides the older ones. A request that cannot be an index is a `400` with code `4006` and nothing is created: no key, a direction that is not allowed (`{"z": 5}` used to create the index `z_5`), a field twice, an unknown collation option. A combination that only the database knows to be wrong (`sparse` together with `partialFilterExpression`) is answered by the database, also as `400`.

## Renaming a collection

`POST /mongodb/collections/{name}/rename` renames a collection in the database, with its documents and indices:

```json
{ "target": "customers2024", "dropTarget": false }
```

- The target is a name in the database of the collection (`geodata:places` for the collection `geodata:locations`), another database is refused: the database cannot rename into another one.
- The administrate grant for the collection and the write grant for the target are needed. An existing target is a `409` (code `4091`) and nothing changes, with `dropTarget` it is replaced; that needs the administrate grant for the target, too.
- A bad name is a `400` with code `4007`: empty, longer than 120 characters, with `$` or control characters, starting with `system.` or with the prefix of the server (`mc_`), ending with `.files` or `.chunks` (the collections of a bucket have their own routes), or the collection itself. The collections of the server and of a bucket cannot be renamed, either.
- A collection that does not exist is a `404`. The event `RenameCollectionEvent` is published.
