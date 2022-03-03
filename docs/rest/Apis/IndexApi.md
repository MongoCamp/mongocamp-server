# IndexApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createExpiringIndexForField**](IndexApi.md#createExpiringIndexForField) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/{duration}/expiring | Create Index by Field for Collection
[**createIndex**](IndexApi.md#createIndex) | **PUT** /mongodb/collections/{collectionName}/index | Create Index for Collection
[**createIndexForField**](IndexApi.md#createIndexForField) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName} | Create Index by Field for Collection
[**createTextIndexForField**](IndexApi.md#createTextIndexForField) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/text | Create Index by Field for Collection
[**createUniqueIndexForField**](IndexApi.md#createUniqueIndexForField) | **PUT** /mongodb/collections/{collectionName}/index/field/{fieldName}/unique | Create Index by Field for Collection
[**deleteIndex**](IndexApi.md#deleteIndex) | **DELETE** /mongodb/collections/{collectionName}/index/{indexName} | Delete Index
[**index**](IndexApi.md#index) | **GET** /mongodb/collections/{collectionName}/index/{indexName} | Index for Collection
[**indexList**](IndexApi.md#indexList) | **GET** /mongodb/collections/{collectionName}/index | List Indices for Collection


<a name="createExpiringIndexForField"></a>
# **createExpiringIndexForField**
> IndexCreateResponse createExpiringIndexForField(collectionName, fieldName, duration, sortAscending, name)

Create Index by Field for Collection

    Create Index by Field for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **fieldName** | **String**| The field Name for your index | 
 **duration** | **String**| Expiring Duration in format 15d (https://www.scala-lang.org/api/2.13.7/scala/concurrent/duration/Duration.html) | [default to 15d]
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]
 **name** | **String**| Name for your index | [optional] 

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

    Create Index for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
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
> IndexCreateResponse createIndexForField(collectionName, fieldName, IndexOptionsRequest, sortAscending)

Create Index by Field for Collection

    Create Index by Field for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **fieldName** | **String**| The field Name for your index | 
 **IndexOptionsRequest** | [**IndexOptionsRequest**](../Models/IndexOptionsRequest.md)|  |
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="createTextIndexForField"></a>
# **createTextIndexForField**
> IndexCreateResponse createTextIndexForField(collectionName, fieldName, IndexOptionsRequest)

Create Index by Field for Collection

    Create Index by Field for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **fieldName** | **String**| The field Name for your index | 
 **IndexOptionsRequest** | [**IndexOptionsRequest**](../Models/IndexOptionsRequest.md)|  |

### Return type

[**IndexCreateResponse**](../Models/IndexCreateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="createUniqueIndexForField"></a>
# **createUniqueIndexForField**
> IndexCreateResponse createUniqueIndexForField(collectionName, fieldName, sortAscending, name)

Create Index by Field for Collection

    Create Index by Field for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **fieldName** | **String**| The field Name for your index | 
 **sortAscending** | **Boolean**| Sort your index ascending | [optional] [default to true]
 **name** | **String**| Name for your index | [optional] 

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

    Delete Index by Name for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **indexName** | **String**| The name of your Index | 

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

    Index by Name for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **indexName** | **String**| The name of your Index | 

### Return type

[**MongoIndex**](../Models/MongoIndex.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="indexList"></a>
# **indexList**
> List indexList(collectionName)

List Indices for Collection

    List all Indices for Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 

### Return type

[**List**](../Models/MongoIndex.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

