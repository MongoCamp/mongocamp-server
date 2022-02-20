# InformationApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**collectionList**](InformationApi.md#collectionList) | **GET** /mongodb/collections | List of Collections
[**collectionStatus**](InformationApi.md#collectionStatus) | **GET** /mongodb/collections/{collectionName}/status | Status of Collection
[**databaseInfos**](InformationApi.md#databaseInfos) | **GET** /mongodb/databases/infos | List of Database Infos
[**databaseList**](InformationApi.md#databaseList) | **GET** /mongodb/databases | List of Databases
[**version**](InformationApi.md#version) | **GET** /version | Version Information


<a name="collectionList"></a>
# **collectionList**
> List collectionList()

List of Collections

    List of all Collections

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="collectionStatus"></a>
# **collectionStatus**
> CollectionStatus collectionStatus(collectionName, includeDetails)

Status of Collection

    Collection Status

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **includeDetails** | **Boolean**| Include all details for the Collection | [optional] [default to false]

### Return type

[**CollectionStatus**](../Models/CollectionStatus.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

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

<a name="version"></a>
# **version**
> Version version()

Version Information

    Version Info of the mongocamp API

### Parameters
This endpoint does not need any parameter.

### Return type

[**Version**](../Models/Version.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

