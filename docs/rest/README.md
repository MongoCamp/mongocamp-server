# Documentation for mongocamp

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AdminApi* | [**getUser**](Apis/AdminApi.md#getuser) | **GET** /admin/users/{userId} | UserProfile for userId
*AdminApi* | [**getUserRoles**](Apis/AdminApi.md#getuserroles) | **GET** /admin/userroles/{userRoleName} | Get UserRole
*AdminApi* | [**listUserRoles**](Apis/AdminApi.md#listuserroles) | **GET** /admin/userroles | List UserRoles
*AdminApi* | [**listUsers**](Apis/AdminApi.md#listusers) | **GET** /admin/users | List Users
*AuthApi* | [**login**](Apis/AuthApi.md#login) | **POST** /auth/login | Login User
*AuthApi* | [**logout**](Apis/AuthApi.md#logout) | **POST** /auth/logout | Logout User
*AuthApi* | [**logoutByDelete**](Apis/AuthApi.md#logoutbydelete) | **DELETE** /auth/logout | Logout User
*AuthApi* | [**refreshToken**](Apis/AuthApi.md#refreshtoken) | **GET** /auth/token/refresh | Refresh User
*AuthApi* | [**userProfile**](Apis/AuthApi.md#userprofile) | **GET** /auth/profile | User Profile
*CreateApi* | [**insert**](Apis/CreateApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/insert | Collection Insert
*CreateApi* | [**insertMany**](Apis/CreateApi.md#insertmany) | **PUT** /mongodb/collections/{collectionName}/insert/many | Collection Insert many
*DeleteApi* | [**delete**](Apis/DeleteApi.md#delete) | **DELETE** /mongodb/collections/{collectionName}/delete | Delete one in Collection
*DeleteApi* | [**deleteAll**](Apis/DeleteApi.md#deleteall) | **DELETE** /mongodb/collections/{collectionName}/delete/all | Delete all in Collection
*DeleteApi* | [**deleteMany**](Apis/DeleteApi.md#deletemany) | **DELETE** /mongodb/collections/{collectionName}/delete/many | Delete Many in Collection
*IndexApi* | [**createExpiringIndexForField**](Apis/IndexApi.md#createexpiringindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/{duration}/expiring | Create Index by Field for Collection
*IndexApi* | [**createIndex**](Apis/IndexApi.md#createindex) | **PUT** /mongodb/collections/{collectionName}/index | Create Index for Collection
*IndexApi* | [**createIndexForField**](Apis/IndexApi.md#createindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName} | Create Index by Field for Collection
*IndexApi* | [**createTextIndexForField**](Apis/IndexApi.md#createtextindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/text | Create Index by Field for Collection
*IndexApi* | [**createUniqueIndexForField**](Apis/IndexApi.md#createuniqueindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/unique | Create Index by Field for Collection
*IndexApi* | [**deleteIndex**](Apis/IndexApi.md#deleteindex) | **DELETE** /mongodb/collections/{collectionName}/index/{indexName} | Delete Index
*IndexApi* | [**index**](Apis/IndexApi.md#index) | **GET** /mongodb/collections/{collectionName}/index/{indexName} | Index for Collection
*IndexApi* | [**indexList**](Apis/IndexApi.md#indexlist) | **GET** /mongodb/collections/{collectionName}/index | List Indices for Collection
*InformationApi* | [**collectionList**](Apis/InformationApi.md#collectionlist) | **GET** /mongodb/collections | List of Collections
*InformationApi* | [**collectionStatus**](Apis/InformationApi.md#collectionstatus) | **GET** /mongodb/collections/{collectionName}/status | Status of Collection
*InformationApi* | [**databaseInfos**](Apis/InformationApi.md#databaseinfos) | **GET** /mongodb/databases/infos | List of Database Infos
*InformationApi* | [**databaseList**](Apis/InformationApi.md#databaselist) | **GET** /mongodb/databases | List of Databases
*InformationApi* | [**version**](Apis/InformationApi.md#version) | **GET** /version | Version Information
*ReadApi* | [**aggregate**](Apis/ReadApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection
*ReadApi* | [**distinct**](Apis/ReadApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection
*ReadApi* | [**find**](Apis/ReadApi.md#find) | **POST** /mongodb/collections/{collectionName}/find | Search in Collection
*UpdateApi* | [**replace**](Apis/UpdateApi.md#replace) | **PATCH** /mongodb/collections/{collectionName}/replace | ReplaceOne in Collection
*UpdateApi* | [**update**](Apis/UpdateApi.md#update) | **PATCH** /mongodb/collections/{collectionName}/update | Update One in Collection
*UpdateApi* | [**updateMany**](Apis/UpdateApi.md#updatemany) | **PATCH** /mongodb/collections/{collectionName}/update/many | Update many in Collection


<a name="documentation-for-models"></a>
## Documentation for Models

 - [CollectionGrant](./Models/CollectionGrant.md)
 - [CollectionStatus](./Models/CollectionStatus.md)
 - [DatabaseInfo](./Models/DatabaseInfo.md)
 - [DeleteResponse](./Models/DeleteResponse.md)
 - [ErrorDescription](./Models/ErrorDescription.md)
 - [IndexCreateRequest](./Models/IndexCreateRequest.md)
 - [IndexCreateResponse](./Models/IndexCreateResponse.md)
 - [IndexDropResponse](./Models/IndexDropResponse.md)
 - [IndexOptionsRequest](./Models/IndexOptionsRequest.md)
 - [InsertResponse](./Models/InsertResponse.md)
 - [JsonResult_Boolean](./Models/JsonResult_Boolean.md)
 - [Login](./Models/Login.md)
 - [LoginResult](./Models/LoginResult.md)
 - [MongoAggregateRequest](./Models/MongoAggregateRequest.md)
 - [MongoFindRequest](./Models/MongoFindRequest.md)
 - [MongoIndex](./Models/MongoIndex.md)
 - [PipelineStage](./Models/PipelineStage.md)
 - [ReplaceOrUpdateRequest](./Models/ReplaceOrUpdateRequest.md)
 - [ReplaceResponse](./Models/ReplaceResponse.md)
 - [UpdateResponse](./Models/UpdateResponse.md)
 - [UserProfile](./Models/UserProfile.md)
 - [UserRole](./Models/UserRole.md)
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

