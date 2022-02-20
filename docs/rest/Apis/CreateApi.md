# CreateApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**insert**](CreateApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/insert | Collection Insert
[**insertMany**](CreateApi.md#insertMany) | **PUT** /mongodb/collections/{collectionName}/insert/many | Collection Insert many


<a name="insert"></a>
# **insert**
> InsertResponse insert(collectionName, request\_body)

Collection Insert

    Insert one Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **request\_body** | [**Map**](../Models/string.md)| JSON Representation for your Document. |

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="insertMany"></a>
# **insertMany**
> InsertResponse insertMany(collectionName, request\_body)

Collection Insert many

    Insert many documents in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **request\_body** | [**List**](../Models/map.md)|  | [optional]

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

