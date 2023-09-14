# Documentation for mongocamp-server

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost*

| Class | Method | HTTP request | Description |
|------------ | ------------- | ------------- | -------------|
| *AdminApi* | [**addRole**](Apis/AdminApi.md#addrole) | **PUT** /admin/roles | Add Role |
*AdminApi* | [**addUser**](Apis/AdminApi.md#adduser) | **PUT** /admin/users | Add User |
*AdminApi* | [**deleteRole**](Apis/AdminApi.md#deleterole) | **DELETE** /admin/roles/{roleName} | Delete Role |
*AdminApi* | [**deleteUser**](Apis/AdminApi.md#deleteuser) | **DELETE** /admin/users/{userId} | Delete User |
*AdminApi* | [**getRole**](Apis/AdminApi.md#getrole) | **GET** /admin/roles/{roleName} | Get Role |
*AdminApi* | [**getUser**](Apis/AdminApi.md#getuser) | **GET** /admin/users/{userId} | UserProfile for userId |
*AdminApi* | [**gnerateNewApiKeyForUser**](Apis/AdminApi.md#gneratenewapikeyforuser) | **PATCH** /admin/users/{userId}/apikey | Update ApiKey |
*AdminApi* | [**listRoles**](Apis/AdminApi.md#listroles) | **GET** /admin/roles | List Roles |
*AdminApi* | [**listUsers**](Apis/AdminApi.md#listusers) | **GET** /admin/users | List Users |
*AdminApi* | [**updatePasswordForUser**](Apis/AdminApi.md#updatepasswordforuser) | **PATCH** /admin/users/{userId}/password | Update Password |
*AdminApi* | [**updateRole**](Apis/AdminApi.md#updaterole) | **PATCH** /admin/roles/{roleName} | Update Role |
*AdminApi* | [**updateRolesForUser**](Apis/AdminApi.md#updaterolesforuser) | **PATCH** /admin/users/{userId}/roles | Update User Roles |
| *ApplicationApi* | [**eventMetrics**](Apis/ApplicationApi.md#eventmetrics) | **GET** /system/monitoring/events | Event Metrics |
*ApplicationApi* | [**getConfig**](Apis/ApplicationApi.md#getconfig) | **GET** /system/configurations/{configurationKey} | Configuration for configurationKey |
*ApplicationApi* | [**jvmMetrics**](Apis/ApplicationApi.md#jvmmetrics) | **GET** /system/monitoring/jvm | JVM Metrics |
*ApplicationApi* | [**listConfigurations**](Apis/ApplicationApi.md#listconfigurations) | **GET** /system/configurations | List Configurations |
*ApplicationApi* | [**mongoDbMetrics**](Apis/ApplicationApi.md#mongodbmetrics) | **GET** /system/monitoring/mongodb | MongoDb Metrics |
*ApplicationApi* | [**settings**](Apis/ApplicationApi.md#settings) | **GET** /system/settings | System Settings |
*ApplicationApi* | [**systemMetrics**](Apis/ApplicationApi.md#systemmetrics) | **GET** /system/monitoring/system | System Metrics |
*ApplicationApi* | [**updateConfiguration**](Apis/ApplicationApi.md#updateconfiguration) | **PATCH** /system/configurations/{configurationKey} | Update Configuration |
| *AuthApi* | [**generateNewApiKey**](Apis/AuthApi.md#generatenewapikey) | **PATCH** /auth/profile/apikey | Update ApiKey |
*AuthApi* | [**isAuthenticated**](Apis/AuthApi.md#isauthenticated) | **GET** /auth/authenticated | Check Authentication |
*AuthApi* | [**login**](Apis/AuthApi.md#login) | **POST** /auth/login | Login User |
*AuthApi* | [**logout**](Apis/AuthApi.md#logout) | **POST** /auth/logout | Logout User |
*AuthApi* | [**logoutByDelete**](Apis/AuthApi.md#logoutbydelete) | **DELETE** /auth/logout | Logout User |
*AuthApi* | [**refreshToken**](Apis/AuthApi.md#refreshtoken) | **GET** /auth/token/refresh | Refresh User |
*AuthApi* | [**updatePassword**](Apis/AuthApi.md#updatepassword) | **PATCH** /auth/profile/password | Update Password |
*AuthApi* | [**userProfile**](Apis/AuthApi.md#userprofile) | **GET** /auth/profile | User Profile |
| *BucketApi* | [**clearBucket**](Apis/BucketApi.md#clearbucket) | **DELETE** /mongodb/buckets/{bucketName}/clear | Clear Bucket |
*BucketApi* | [**deleteBucket**](Apis/BucketApi.md#deletebucket) | **DELETE** /mongodb/buckets/{bucketName} | Delete Bucket |
*BucketApi* | [**getBucket**](Apis/BucketApi.md#getbucket) | **GET** /mongodb/buckets/{bucketName} | Bucket Information |
*BucketApi* | [**listBuckets**](Apis/BucketApi.md#listbuckets) | **GET** /mongodb/buckets | List of Buckets |
| *CollectionApi* | [**aggregate**](Apis/CollectionApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection |
*CollectionApi* | [**clearCollection**](Apis/CollectionApi.md#clearcollection) | **DELETE** /mongodb/collections/{collectionName}/clear | Clear Collection |
*CollectionApi* | [**deleteCollection**](Apis/CollectionApi.md#deletecollection) | **DELETE** /mongodb/collections/{collectionName} | Delete Collection |
*CollectionApi* | [**distinct**](Apis/CollectionApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection |
*CollectionApi* | [**getCollectionFields**](Apis/CollectionApi.md#getcollectionfields) | **GET** /mongodb/collections/{collectionName}/fields | Collection Fields |
*CollectionApi* | [**getCollectionInformation**](Apis/CollectionApi.md#getcollectioninformation) | **GET** /mongodb/collections/{collectionName} | Collection Information |
*CollectionApi* | [**getJsonSchema**](Apis/CollectionApi.md#getjsonschema) | **GET** /mongodb/collections/{collectionName}/schema | Collection Fields |
*CollectionApi* | [**getSchemaAnalysis**](Apis/CollectionApi.md#getschemaanalysis) | **GET** /mongodb/collections/{collectionName}/schema/analysis | Collection Fields |
*CollectionApi* | [**listCollections**](Apis/CollectionApi.md#listcollections) | **GET** /mongodb/collections | List of Collections |
*CollectionApi* | [**listCollectionsByDatabase**](Apis/CollectionApi.md#listcollectionsbydatabase) | **GET** /mongodb/databases/{databaseName}/collections | List of Collections |
| *DatabaseApi* | [**databaseInfos**](Apis/DatabaseApi.md#databaseinfos) | **GET** /mongodb/databases/infos | List of Database Infos |
*DatabaseApi* | [**deleteDatabase**](Apis/DatabaseApi.md#deletedatabase) | **DELETE** /mongodb/databases/{databaseName} | Delete Database |
*DatabaseApi* | [**getDatabaseInfo**](Apis/DatabaseApi.md#getdatabaseinfo) | **GET** /mongodb/databases/{databaseName} | Database Infos of selected Database |
*DatabaseApi* | [**listCollectionsByDatabase**](Apis/DatabaseApi.md#listcollectionsbydatabase) | **GET** /mongodb/databases/{databaseName}/collections | List of Collections |
*DatabaseApi* | [**listDatabases**](Apis/DatabaseApi.md#listdatabases) | **GET** /mongodb/databases | List of Databases |
| *DocumentApi* | [**delete**](Apis/DocumentApi.md#delete) | **DELETE** /mongodb/collections/{collectionName}/documents/{documentId} | Delete Document from Collection |
*DocumentApi* | [**deleteMany**](Apis/DocumentApi.md#deletemany) | **DELETE** /mongodb/collections/{collectionName}/documents/many/delete | Delete Many in Collection |
*DocumentApi* | [**find**](Apis/DocumentApi.md#find) | **POST** /mongodb/collections/{collectionName}/documents | Documents in Collection |
*DocumentApi* | [**getDocument**](Apis/DocumentApi.md#getdocument) | **GET** /mongodb/collections/{collectionName}/documents/{documentId} | Document from Collection |
*DocumentApi* | [**insert**](Apis/DocumentApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/documents | Insert Document |
*DocumentApi* | [**insertMany**](Apis/DocumentApi.md#insertmany) | **PUT** /mongodb/collections/{collectionName}/documents/many/insert | Insert many Documents |
*DocumentApi* | [**listDocuments**](Apis/DocumentApi.md#listdocuments) | **GET** /mongodb/collections/{collectionName}/documents | Documents in Collection |
*DocumentApi* | [**update**](Apis/DocumentApi.md#update) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId} | Update Document in Collection |
*DocumentApi* | [**updateDocumentPartial**](Apis/DocumentApi.md#updatedocumentpartial) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId}/partial | Update Document Parts in Collection |
*DocumentApi* | [**updateMany**](Apis/DocumentApi.md#updatemany) | **PATCH** /mongodb/collections/{collectionName}/documents/many/update | Update many in Collection |
| *FileApi* | [**deleteFile**](Apis/FileApi.md#deletefile) | **DELETE** /mongodb/buckets/{bucketName}/files/{fileId} | Delete File from Bucket |
*FileApi* | [**findFiles**](Apis/FileApi.md#findfiles) | **POST** /mongodb/buckets/{bucketName}/files | Files in Bucket |
*FileApi* | [**getFile**](Apis/FileApi.md#getfile) | **GET** /mongodb/buckets/{bucketName}/files/{fileId}/file | File from Bucket |
*FileApi* | [**getFileInformation**](Apis/FileApi.md#getfileinformation) | **GET** /mongodb/buckets/{bucketName}/files/{fileId} | FileInformation from Bucket |
*FileApi* | [**insertFile**](Apis/FileApi.md#insertfile) | **PUT** /mongodb/buckets/{bucketName}/files | Insert File |
*FileApi* | [**listFiles**](Apis/FileApi.md#listfiles) | **GET** /mongodb/buckets/{bucketName}/files | Files in Bucket |
*FileApi* | [**updateFileInformation**](Apis/FileApi.md#updatefileinformation) | **PATCH** /mongodb/buckets/{bucketName}/files/{fileId} | Update FileInformation in Bucket |
| *IndexApi* | [**createExpiringIndex**](Apis/IndexApi.md#createexpiringindex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/{duration}/expiring | Create expiring Index by Field for Collection |
*IndexApi* | [**createIndex**](Apis/IndexApi.md#createindex) | **PUT** /mongodb/collections/{collectionName}/index | Create Index for Collection |
*IndexApi* | [**createIndexForField**](Apis/IndexApi.md#createindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName} | Create Index by Field for Collection |
*IndexApi* | [**createTextIndex**](Apis/IndexApi.md#createtextindex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/text | Create text index by field for collection |
*IndexApi* | [**createUniqueIndex**](Apis/IndexApi.md#createuniqueindex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/unique | Create Unique Index |
*IndexApi* | [**deleteIndex**](Apis/IndexApi.md#deleteindex) | **DELETE** /mongodb/collections/{collectionName}/index/{indexName} | Delete Index |
*IndexApi* | [**index**](Apis/IndexApi.md#index) | **GET** /mongodb/collections/{collectionName}/index/{indexName} | Index for Collection |
*IndexApi* | [**listIndices**](Apis/IndexApi.md#listindices) | **GET** /mongodb/collections/{collectionName}/index | List Indices for Collection |
| *InformationApi* | [**version**](Apis/InformationApi.md#version) | **GET** /version | Version Information |
| *JobsApi* | [**deleteJob**](Apis/JobsApi.md#deletejob) | **DELETE** /system/jobs/{jobGroup}/{jobName} | Delete Job |
*JobsApi* | [**executeJob**](Apis/JobsApi.md#executejob) | **POST** /system/jobs/{jobGroup}/{jobName} | Execute Job |
*JobsApi* | [**jobsList**](Apis/JobsApi.md#jobslist) | **GET** /system/jobs | Registered Jobs |
*JobsApi* | [**possibleJobsList**](Apis/JobsApi.md#possiblejobslist) | **GET** /system/jobs/classes | Possible Jobs |
*JobsApi* | [**registerJob**](Apis/JobsApi.md#registerjob) | **PUT** /system/jobs | Register Job |
*JobsApi* | [**updateJob**](Apis/JobsApi.md#updatejob) | **PATCH** /system/jobs/{jobGroup}/{jobName} | Update Job |


<a name="documentation-for-models"></a>
## Documentation for Models

 - [BucketInformation](./Models/BucketInformation.md)
 - [CollectionStatus](./Models/CollectionStatus.md)
 - [DatabaseInfo](./Models/DatabaseInfo.md)
 - [DeleteResponse](./Models/DeleteResponse.md)
 - [ErrorDescription](./Models/ErrorDescription.md)
 - [FileInformation](./Models/FileInformation.md)
 - [Grant](./Models/Grant.md)
 - [IndexCreateRequest](./Models/IndexCreateRequest.md)
 - [IndexCreateResponse](./Models/IndexCreateResponse.md)
 - [IndexDropResponse](./Models/IndexDropResponse.md)
 - [IndexOptionsRequest](./Models/IndexOptionsRequest.md)
 - [InsertResponse](./Models/InsertResponse.md)
 - [JobConfig](./Models/JobConfig.md)
 - [JobInformation](./Models/JobInformation.md)
 - [JsonSchema](./Models/JsonSchema.md)
 - [JsonSchemaDefinition](./Models/JsonSchemaDefinition.md)
 - [JsonValue_Any](./Models/JsonValue_Any.md)
 - [JsonValue_Boolean](./Models/JsonValue_Boolean.md)
 - [JsonValue_String](./Models/JsonValue_String.md)
 - [Login](./Models/Login.md)
 - [LoginResult](./Models/LoginResult.md)
 - [Measurement](./Models/Measurement.md)
 - [Metric](./Models/Metric.md)
 - [MongoAggregateRequest](./Models/MongoAggregateRequest.md)
 - [MongoCampConfiguration](./Models/MongoCampConfiguration.md)
 - [MongoFindRequest](./Models/MongoFindRequest.md)
 - [MongoIndex](./Models/MongoIndex.md)
 - [PasswordUpdateRequest](./Models/PasswordUpdateRequest.md)
 - [PipelineStage](./Models/PipelineStage.md)
 - [Role](./Models/Role.md)
 - [SchemaAnalysis](./Models/SchemaAnalysis.md)
 - [SchemaAnalysisField](./Models/SchemaAnalysisField.md)
 - [SchemaAnalysisFieldType](./Models/SchemaAnalysisFieldType.md)
 - [SettingsResponse](./Models/SettingsResponse.md)
 - [UpdateFileInformationRequest](./Models/UpdateFileInformationRequest.md)
 - [UpdateRequest](./Models/UpdateRequest.md)
 - [UpdateResponse](./Models/UpdateResponse.md)
 - [UpdateRoleRequest](./Models/UpdateRoleRequest.md)
 - [UserInformation](./Models/UserInformation.md)
 - [UserProfile](./Models/UserProfile.md)
 - [Version](./Models/Version.md)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="apiKeyAuth"></a>
### apiKeyAuth

- **Type**: API key
- **API key parameter name**: X-AUTH-APIKEY
- **Location**: HTTP header

<a name="httpAuth"></a>
### httpAuth

- **Type**: HTTP basic authentication

<a name="httpAuth1"></a>
### httpAuth1

- **Type**: HTTP basic authentication

