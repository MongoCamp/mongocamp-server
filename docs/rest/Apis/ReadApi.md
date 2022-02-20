# ReadApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**aggregate**](ReadApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection
[**distinct**](ReadApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection
[**find**](ReadApi.md#find) | **POST** /mongodb/collections/{collectionName}/find | Search in Collection


<a name="aggregate"></a>
# **aggregate**
> List aggregate(collectionName, MongoAggregateRequest)

Aggregate in Collection

    Aggregate in your MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **MongoAggregateRequest** | [**MongoAggregateRequest**](../Models/MongoAggregateRequest.md)|  |

### Return type

[**List**](../Models/map.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="distinct"></a>
# **distinct**
> List distinct(collectionName, field)

Distinct in Collection

    Distinct for Field in your MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **field** | **String**| The field for your distinct Request. | [default to null]

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="find"></a>
# **find**
> List find(collectionName, MongoFindRequest)

Search in Collection

    Search in your MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **MongoFindRequest** | [**MongoFindRequest**](../Models/MongoFindRequest.md)|  |

### Return type

[**List**](../Models/map.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

