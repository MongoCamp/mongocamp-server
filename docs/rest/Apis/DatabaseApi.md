# DatabaseApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**databaseInfos**](DatabaseApi.md#databaseInfos) | **GET** /mongodb/databases/infos | List of Database Infos |
| [**deleteDatabase**](DatabaseApi.md#deleteDatabase) | **DELETE** /mongodb/databases/{databaseName} | Delete Database |
| [**getDatabaseInfo**](DatabaseApi.md#getDatabaseInfo) | **GET** /mongodb/databases/{databaseName} | Database Infos of selected Database |
| [**listCollectionsByDatabase**](DatabaseApi.md#listCollectionsByDatabase) | **GET** /mongodb/databases/{databaseName}/collections | List of Collections |
| [**listDatabases**](DatabaseApi.md#listDatabases) | **GET** /mongodb/databases | List of Databases |


<a name="databaseInfos"></a>
# **databaseInfos**
> List databaseInfos()

List of Database Infos

    List of all Databases Infos

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/DatabaseInfo.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteDatabase"></a>
# **deleteDatabase**
> JsonValue_Boolean deleteDatabase(databaseName)

Delete Database

    Delete given Database

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **databaseName** | **String**| Name of your Database | [default to null] |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getDatabaseInfo"></a>
# **getDatabaseInfo**
> DatabaseInfo getDatabaseInfo(databaseName)

Database Infos of selected Database

    All Information about given Database

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **databaseName** | **String**| Name of your Database | [default to null] |

### Return type

[**DatabaseInfo**](../Models/DatabaseInfo.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listCollectionsByDatabase"></a>
# **listCollectionsByDatabase**
> List listCollectionsByDatabase(databaseName)

List of Collections

    List of all Collections of the given database

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **databaseName** | **String**| Name of your Database | [default to null] |

### Return type

**List**

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listDatabases"></a>
# **listDatabases**
> List listDatabases()

List of Databases

    List of all Databases

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

