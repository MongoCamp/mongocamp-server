# UpdateApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**replace**](UpdateApi.md#replace) | **PATCH** /mongodb/collections/{collectionName}/replace | ReplaceOne in Collection
[**update**](UpdateApi.md#update) | **PATCH** /mongodb/collections/{collectionName}/update | Update One in Collection
[**updateMany**](UpdateApi.md#updateMany) | **PATCH** /mongodb/collections/{collectionName}/update/many | Update many in Collection


<a name="replace"></a>
# **replace**
> ReplaceResponse replace(collectionName, ReplaceOrUpdateRequest)

ReplaceOne in Collection

    Replace one Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **ReplaceOrUpdateRequest** | [**ReplaceOrUpdateRequest**](../Models/ReplaceOrUpdateRequest.md)|  |

### Return type

[**ReplaceResponse**](../Models/ReplaceResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="update"></a>
# **update**
> UpdateResponse update(collectionName, ReplaceOrUpdateRequest)

Update One in Collection

    Update one Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **ReplaceOrUpdateRequest** | [**ReplaceOrUpdateRequest**](../Models/ReplaceOrUpdateRequest.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateMany"></a>
# **updateMany**
> UpdateResponse updateMany(collectionName, ReplaceOrUpdateRequest)

Update many in Collection

    Update many Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **ReplaceOrUpdateRequest** | [**ReplaceOrUpdateRequest**](../Models/ReplaceOrUpdateRequest.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

