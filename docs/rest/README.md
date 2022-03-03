# Documentation for mongocamp

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AdminApi* | [**addRoles**](Apis/AdminApi.md#addroles) | **PUT** /admin/roles | Add Role
*AdminApi* | [**addUser**](Apis/AdminApi.md#adduser) | **PUT** /admin/users | Add User
*AdminApi* | [**deleteRoles**](Apis/AdminApi.md#deleteroles) | **DELETE** /admin/roles/{roleName} | Delete Role
*AdminApi* | [**deleteUser**](Apis/AdminApi.md#deleteuser) | **DELETE** /admin/users/{userId} | Delete User
*AdminApi* | [**getRoles**](Apis/AdminApi.md#getroles) | **GET** /admin/roles/{roleName} | Get Role
*AdminApi* | [**getUser**](Apis/AdminApi.md#getuser) | **GET** /admin/users/{userId} | UserProfile for userId
*AdminApi* | [**listRoles**](Apis/AdminApi.md#listroles) | **GET** /admin/roles | List Roles
*AdminApi* | [**listUsers**](Apis/AdminApi.md#listusers) | **GET** /admin/users | List Users
*AdminApi* | [**updateApiKeyForUser**](Apis/AdminApi.md#updateapikeyforuser) | **PATCH** /admin/users/{userId}/apikey | Update ApiKey
*AdminApi* | [**updatePasswordForUser**](Apis/AdminApi.md#updatepasswordforuser) | **PATCH** /admin/users/{userId}/password | Update Password
*AdminApi* | [**updateRole**](Apis/AdminApi.md#updaterole) | **PATCH** /admin/roles/{roleName} | Update Role
*AdminApi* | [**updateRolesForUser**](Apis/AdminApi.md#updaterolesforuser) | **PATCH** /admin/users/{userId}/roles | Update User Roles
*AuthApi* | [**login**](Apis/AuthApi.md#login) | **POST** /auth/login | Login User
*AuthApi* | [**logout**](Apis/AuthApi.md#logout) | **POST** /auth/logout | Logout User
*AuthApi* | [**logoutByDelete**](Apis/AuthApi.md#logoutbydelete) | **DELETE** /auth/logout | Logout User
*AuthApi* | [**refreshToken**](Apis/AuthApi.md#refreshtoken) | **GET** /auth/token/refresh | Refresh User
*AuthApi* | [**updateApiKey**](Apis/AuthApi.md#updateapikey) | **PATCH** /auth/profile/apikey | Update ApiKey
*AuthApi* | [**updatePassword**](Apis/AuthApi.md#updatepassword) | **PATCH** /auth/profile/password | Update Password
*AuthApi* | [**userProfile**](Apis/AuthApi.md#userprofile) | **GET** /auth/profile | User Profile
*CollectionApi* | [**aggregate**](Apis/CollectionApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection
*CollectionApi* | [**clearCollection**](Apis/CollectionApi.md#clearcollection) | **DELETE** /mongodb/collections/{collectionName}/clear | Clear Collection
*CollectionApi* | [**collectionList**](Apis/CollectionApi.md#collectionlist) | **GET** /mongodb/collections | List of Collections
*CollectionApi* | [**deleteCollection**](Apis/CollectionApi.md#deletecollection) | **DELETE** /mongodb/collections/{collectionName} | Delete Collection
*CollectionApi* | [**distinct**](Apis/CollectionApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection
*CollectionApi* | [**getCollectionInformation**](Apis/CollectionApi.md#getcollectioninformation) | **GET** /mongodb/collections/{collectionName} | Collection Information
*DatabaseApi* | [**databaseInfos**](Apis/DatabaseApi.md#databaseinfos) | **GET** /mongodb/databases/infos | List of Database Infos
*DatabaseApi* | [**databaseList**](Apis/DatabaseApi.md#databaselist) | **GET** /mongodb/databases | List of Databases
*DatabaseApi* | [**deleteDatabase**](Apis/DatabaseApi.md#deletedatabase) | **DELETE** /mongodb/databases/{databaseName} | Database Infos of selected Database
*DatabaseApi* | [**getDatabaseInfo**](Apis/DatabaseApi.md#getdatabaseinfo) | **GET** /mongodb/databases/{databaseName} | Database Infos of selected Database
*DocumentsApi* | [**deleteDocument**](Apis/DocumentsApi.md#deletedocument) | **DELETE** /mongodb/collections/{collectionName}/documents/{documentId} | Delete Document from Collection
*DocumentsApi* | [**deleteMany**](Apis/DocumentsApi.md#deletemany) | **DELETE** /mongodb/collections/{collectionName}/documents/many/delete | Delete Many in Collection
*DocumentsApi* | [**documentsList**](Apis/DocumentsApi.md#documentslist) | **GET** /mongodb/collections/{collectionName}/documents | Documents in Collection
*DocumentsApi* | [**find**](Apis/DocumentsApi.md#find) | **POST** /mongodb/collections/{collectionName}/documents | Documents in Collection
*DocumentsApi* | [**getDocument**](Apis/DocumentsApi.md#getdocument) | **GET** /mongodb/collections/{collectionName}/documents/{documentId} | Document from Collection
*DocumentsApi* | [**insert**](Apis/DocumentsApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/documents | Insert Document
*DocumentsApi* | [**insertMany**](Apis/DocumentsApi.md#insertmany) | **PUT** /mongodb/collections/{collectionName}/documents/many/insert | Insert many Documents
*DocumentsApi* | [**updateDocument**](Apis/DocumentsApi.md#updatedocument) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId} | Update Document in Collection
*DocumentsApi* | [**updateDocumentPartitial**](Apis/DocumentsApi.md#updatedocumentpartitial) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId}/partitial | Update Document Parts in Collection
*DocumentsApi* | [**updateMany**](Apis/DocumentsApi.md#updatemany) | **PATCH** /mongodb/collections/{collectionName}/documents/many/update | Update many in Collection
*IndexApi* | [**createExpiringIndexForField**](Apis/IndexApi.md#createexpiringindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/{duration}/expiring | Create Index by Field for Collection
*IndexApi* | [**createIndex**](Apis/IndexApi.md#createindex) | **PUT** /mongodb/collections/{collectionName}/index | Create Index for Collection
*IndexApi* | [**createIndexForField**](Apis/IndexApi.md#createindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName} | Create Index by Field for Collection
*IndexApi* | [**createTextIndexForField**](Apis/IndexApi.md#createtextindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/text | Create Index by Field for Collection
*IndexApi* | [**createUniqueIndexForField**](Apis/IndexApi.md#createuniqueindexforfield) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/unique | Create Index by Field for Collection
*IndexApi* | [**deleteIndex**](Apis/IndexApi.md#deleteindex) | **DELETE** /mongodb/collections/{collectionName}/index/{indexName} | Delete Index
*IndexApi* | [**index**](Apis/IndexApi.md#index) | **GET** /mongodb/collections/{collectionName}/index/{indexName} | Index for Collection
*IndexApi* | [**indexList**](Apis/IndexApi.md#indexlist) | **GET** /mongodb/collections/{collectionName}/index | List Indices for Collection
*InformationApi* | [**version**](Apis/InformationApi.md#version) | **GET** /version | Version Information


<a name="documentation-for-models"></a>
## Documentation for Models

 - [CollectionStatus](./Models/CollectionStatus.md)
 - [DatabaseInfo](./Models/DatabaseInfo.md)
 - [DeleteResponse](./Models/DeleteResponse.md)
 - [ErrorDescription](./Models/ErrorDescription.md)
 - [Grant](./Models/Grant.md)
 - [IndexCreateRequest](./Models/IndexCreateRequest.md)
 - [IndexCreateResponse](./Models/IndexCreateResponse.md)
 - [IndexDropResponse](./Models/IndexDropResponse.md)
 - [IndexOptionsRequest](./Models/IndexOptionsRequest.md)
 - [InsertResponse](./Models/InsertResponse.md)
 - [JsonResult_Boolean](./Models/JsonResult_Boolean.md)
 - [JsonResult_String](./Models/JsonResult_String.md)
 - [Login](./Models/Login.md)
 - [LoginResult](./Models/LoginResult.md)
 - [MongoAggregateRequest](./Models/MongoAggregateRequest.md)
 - [MongoFindRequest](./Models/MongoFindRequest.md)
 - [MongoIndex](./Models/MongoIndex.md)
 - [PasswordUpdateRequest](./Models/PasswordUpdateRequest.md)
 - [PipelineStage](./Models/PipelineStage.md)
 - [Role](./Models/Role.md)
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

