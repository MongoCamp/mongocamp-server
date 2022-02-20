# DeleteApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**delete**](DeleteApi.md#delete) | **DELETE** /mongodb/collections/{collectionName}/delete | Delete one in Collection
[**deleteAll**](DeleteApi.md#deleteAll) | **DELETE** /mongodb/collections/{collectionName}/delete/all | Delete all in Collection
[**deleteMany**](DeleteApi.md#deleteMany) | **DELETE** /mongodb/collections/{collectionName}/delete/many | Delete Many in Collection


<a name="delete"></a>
# **delete**
> DeleteResponse delete(collectionName, request\_body)

Delete one in Collection

    Delete one Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **request\_body** | [**Map**](../Models/string.md)|  |

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="deleteAll"></a>
# **deleteAll**
> DeleteResponse deleteAll(collectionName)

Delete all in Collection

    Delete all Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteMany"></a>
# **deleteMany**
> DeleteResponse deleteMany(collectionName, request\_body)

Delete Many in Collection

    Delete many Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **request\_body** | [**Map**](../Models/string.md)|  |

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

