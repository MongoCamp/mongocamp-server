# CollectionApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**aggregate**](CollectionApi.md#aggregate) | **POST** /mongodb/collections/{collectionName}/aggregate | Aggregate in Collection
[**clearCollection**](CollectionApi.md#clearCollection) | **DELETE** /mongodb/collections/{collectionName}/clear | Clear Collection
[**deleteCollection**](CollectionApi.md#deleteCollection) | **DELETE** /mongodb/collections/{collectionName} | Delete Collection
[**distinct**](CollectionApi.md#distinct) | **POST** /mongodb/collections/{collectionName}/distinct/{field} | Distinct in Collection
[**getCollectionFields**](CollectionApi.md#getCollectionFields) | **GET** /mongodb/collections/{collectionName}/fields | Collection Fields
[**getCollectionInformation**](CollectionApi.md#getCollectionInformation) | **GET** /mongodb/collections/{collectionName} | Collection Information
[**listCollections**](CollectionApi.md#listCollections) | **GET** /mongodb/collections | List of Collections


<a name="aggregate"></a>
# **aggregate**
> List aggregate(collectionName, MongoAggregateRequest, rowsPerPage, page)

Aggregate in Collection

    Aggregate in a given Collection

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
> JsonResult_Boolean clearCollection(collectionName)

Clear Collection

    Delete all Document in Collection

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

<a name="deleteCollection"></a>
# **deleteCollection**
> JsonResult_Boolean deleteCollection(collectionName)

Delete Collection

    Delete a given Collection

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

    Distinct for Field in a given Collection

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

<a name="getCollectionFields"></a>
# **getCollectionFields**
> List getCollectionFields(collectionName, sample size)

Collection Fields

    List the Fields in a given collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | [default to null]
 **sample size** | **Integer**| Use sample size greater 0 (e.g. 1000) for better performance on big collections | [optional] [default to null]

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

    All Information about a single Collection

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

<a name="listCollections"></a>
# **listCollections**
> List listCollections()

List of Collections

    List of all Collections of the default database

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

