# DatabaseApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**databaseInfos**](DatabaseApi.md#databaseInfos) | **GET** /mongodb/databases/infos | List of Database Infos
[**databaseList**](DatabaseApi.md#databaseList) | **GET** /mongodb/databases | List of Databases
[**deleteDatabase**](DatabaseApi.md#deleteDatabase) | **DELETE** /mongodb/databases/{databaseName} | Database Infos of selected Database
[**getDatabaseInfo**](DatabaseApi.md#getDatabaseInfo) | **GET** /mongodb/databases/{databaseName} | Database Infos of selected Database


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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="databaseList"></a>
# **databaseList**
> List databaseList()

List of Databases

    List of all Databases

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteDatabase"></a>
# **deleteDatabase**
> JsonResult_Boolean deleteDatabase(databaseName)

Database Infos of selected Database

    All Informations about one Database

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **databaseName** | **String**| Name of your Database | 

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getDatabaseInfo"></a>
# **getDatabaseInfo**
> DatabaseInfo getDatabaseInfo(databaseName)

Database Infos of selected Database

    All Informations about one Database

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **databaseName** | **String**| Name of your Database | 

### Return type

[**DatabaseInfo**](../Models/DatabaseInfo.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

