# CollectionApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**aggregate**](CollectionApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection
[**clearCollection**](CollectionApi.md#clearCollection) | **DELETE** /mongodb/collections/{collectionName}/clear | Clear Collection
[**collectionList**](CollectionApi.md#collectionList) | **GET** /mongodb/collections | List of Collections
[**deleteCollection**](CollectionApi.md#deleteCollection) | **DELETE** /mongodb/collections/{collectionName} | Delete Collection
[**distinct**](CollectionApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection
[**getCollectionInformation**](CollectionApi.md#getCollectionInformation) | **GET** /mongodb/collections/{collectionName} | Collection Information


<a name="aggregate"></a>
# **aggregate**
> List aggregate(collectionName, MongoAggregateRequest, rowsPerPage, page)

Aggregate in Collection

    Aggregate in your MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **MongoAggregateRequest** | [**MongoAggregateRequest**](../Models/MongoAggregateRequest.md)|  |
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

[**List**](../Models/map.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="clearCollection"></a>
# **clearCollection**
> DeleteResponse clearCollection(collectionName)

Clear Collection

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

<a name="deleteCollection"></a>
# **deleteCollection**
> JsonResult_Boolean deleteCollection(collectionName)

Delete Collection

    Delete Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="distinct"></a>
# **distinct**
> List distinct(collectionName, field, rowsPerPage, page)

Distinct in Collection

    Distinct for Field in your MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **field** | **String**| The field for your distinct Request. | [default to null]
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="getCollectionInformation"></a>
# **getCollectionInformation**
> CollectionStatus getCollectionInformation(collectionName, includeDetails)

Collection Information

    All Informations about a single Collection

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

