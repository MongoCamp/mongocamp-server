# IndexApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createExpiringIndex**](IndexApi.md#createExpiringIndex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/{duration}/expiring | Create expiring Index by Field for Collection
[**createIndex**](IndexApi.md#createIndex) | **PUT** /mongodb/collections/{collectionName}/index | Create Index for Collection
[**createIndexForField**](IndexApi.md#createIndexForField) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName} | Create Index by Field for Collection
[**createTextIndex**](IndexApi.md#createTextIndex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/text | Create text index by field for collection
[**createUniqueIndex**](IndexApi.md#createUniqueIndex) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/unique | Create Unique Index
[**deleteIndex**](IndexApi.md#deleteIndex) | **DELETE** /mongodb/collections/{collectionName}/index/{indexName} | Delete Index
[**index**](IndexApi.md#index) | **GET** /mongodb/collections/{collectionName}/index/{indexName} | Index for Collection
[**listIndices**](IndexApi.md#listIndices) | **GET** /mongodb/collections/{collectionName}/index | List Indices for Collection


<a name="createExpiringIndex"></a>
# **createExpiringIndex**
> IndexCreateResponse createExpiringIndex(collectionName, fieldName, duration, sortAscending, name)

Create expiring Index by Field for Collection

    Create expiring Index by Field for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **fieldName** | **String**| The field Name for your index | [default to null]
 **duration** | **String**| Expiring Duration in format 15d (https://www.scala-lang.org/api/2.13.7/scala/concurrent/duration/Duration.html) | [default to 15d]
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]
 **name** | **String**| Name for your index | [optional] [default to null]

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="createIndex"></a>
# **createIndex**
> IndexCreateResponse createIndex(collectionName, IndexCreateRequest)

Create Index for Collection

    Create Index for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **IndexCreateRequest** | [**IndexCreateRequest**](../Models/IndexCreateRequest.md)|  |

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="createIndexForField"></a>
# **createIndexForField**
> IndexCreateResponse createIndexForField(collectionName, fieldName, sortAscending, IndexOptionsRequest)

Create Index by Field for Collection

    Create Index by Field for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **fieldName** | **String**| The field Name for your index | [default to null]
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]
 **IndexOptionsRequest** | [**IndexOptionsRequest**](../Models/IndexOptionsRequest.md)|  | [optional]

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="createTextIndex"></a>
# **createTextIndex**
> IndexCreateResponse createTextIndex(collectionName, fieldName, IndexOptionsRequest)

Create text index by field for collection

    Create text index by field for given collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **fieldName** | **String**| The field Name for your index | [default to null]
 **IndexOptionsRequest** | [**IndexOptionsRequest**](../Models/IndexOptionsRequest.md)|  | [optional]

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="createUniqueIndex"></a>
# **createUniqueIndex**
> IndexCreateResponse createUniqueIndex(collectionName, fieldName, sortAscending, name)

Create Unique Index

    Create Unique Index by Field for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **fieldName** | **String**| The field Name for your index | [default to null]
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]
 **name** | **String**| Name for your index | [optional] [default to null]

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="deleteIndex"></a>
# **deleteIndex**
> IndexDropResponse deleteIndex(collectionName, indexName)

Delete Index

    Delete Index by Name for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **indexName** | **String**| The name of your Index | [default to null]

### Return type

[**IndexDropResponse**](../Models/IndexDropResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="index"></a>
# **index**
> MongoIndex index(collectionName, indexName)

Index for Collection

    Index by Name for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **indexName** | **String**| The name of your Index | [default to null]

### Return type

[**MongoIndex**](../Models/MongoIndex.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listIndices"></a>
# **listIndices**
> List listIndices(collectionName)

List Indices for Collection

    List all Indices for given Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**List**](../Models/MongoIndex.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

