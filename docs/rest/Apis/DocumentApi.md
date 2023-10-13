# DocumentApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**delete**](DocumentApi.md#delete) | **DELETE** /mongodb/collections/{collectionName}/documents/{documentId} | Delete Document from Collection |
| [**deleteMany**](DocumentApi.md#deleteMany) | **DELETE** /mongodb/collections/{collectionName}/documents/many/delete | Delete Many in Collection |
| [**find**](DocumentApi.md#find) | **POST** /mongodb/collections/{collectionName}/documents | Documents in Collection |
| [**getDocument**](DocumentApi.md#getDocument) | **GET** /mongodb/collections/{collectionName}/documents/{documentId} | Document from Collection |
| [**insert**](DocumentApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/documents | Insert Document |
| [**insertMany**](DocumentApi.md#insertMany) | **PUT** /mongodb/collections/{collectionName}/documents/many/insert | Insert many Documents |
| [**listDocuments**](DocumentApi.md#listDocuments) | **GET** /mongodb/collections/{collectionName}/documents | Documents in Collection |
| [**update**](DocumentApi.md#update) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId} | Update Document in Collection |
| [**updateDocumentPartial**](DocumentApi.md#updateDocumentPartial) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId}/partial | Update Document Parts in Collection |
| [**updateMany**](DocumentApi.md#updateMany) | **PATCH** /mongodb/collections/{collectionName}/documents/many/update | Update many in Collection |


<a name="delete"></a>
# **delete**
> DeleteResponse delete(collectionName, documentId)

Delete Document from Collection

    Delete one Document from given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **documentId** | **String**| DocumentId to delete | [default to null] |

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteMany"></a>
# **deleteMany**
> DeleteResponse deleteMany(collectionName, request\_body)

Delete Many in Collection

    Delete many Document in given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **request\_body** | [**Map**](../Models/string.md)|  | |

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="find"></a>
# **find**
> List find(collectionName, MongoFindRequest, rowsPerPage, page)

Documents in Collection

    Alternative to GET Route for more complex queries and URL max. Length

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **MongoFindRequest** | [**MongoFindRequest**](../Models/MongoFindRequest.md)|  | |
| **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null] |
| **page** | **Long**| Requested page of the ResultSets | [optional] [default to null] |

### Return type

[**List**](../Models/map.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="getDocument"></a>
# **getDocument**
> Map getDocument(collectionName, documentId)

Document from Collection

    Get one Document from given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **documentId** | **String**| DocumentId to read | [default to null] |

### Return type

**Map**

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="insert"></a>
# **insert**
> InsertResponse insert(collectionName, request\_body)

Insert Document

    Insert one Document in given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **request\_body** | [**Map**](../Models/string.md)| JSON Representation for your Document. | |

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="insertMany"></a>
# **insertMany**
> InsertResponse insertMany(collectionName, request\_body)

Insert many Documents

    Insert many documents in given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **request\_body** | [**List**](../Models/map.md)|  | [optional] |

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="listDocuments"></a>
# **listDocuments**
> List listDocuments(collectionName, filter, sort, projection, rowsPerPage, page)

Documents in Collection

    Get Documents paginated from given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **filter** | **String**| MongoDB Filter Query by Default all filter | [optional] [default to null] |
| **sort** | **String**| MongoDB sorting | [optional] [default to null] |
| **projection** | **String**| MongoDB projection | [optional] [default to null] |
| **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null] |
| **page** | **Long**| Requested page of the ResultSets | [optional] [default to null] |

### Return type

[**List**](../Models/map.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="update"></a>
# **update**
> UpdateResponse update(collectionName, documentId, request\_body)

Update Document in Collection

    &#39;Replace&#39; one Document with the new document from Request in Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **documentId** | **String**| DocumentId to update | [default to null] |
| **request\_body** | [**Map**](../Models/string.md)|  | |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateDocumentPartial"></a>
# **updateDocumentPartial**
> UpdateResponse updateDocumentPartial(collectionName, documentId, request\_body)

Update Document Parts in Collection

    Update the document Parts with the values from the Request

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **documentId** | **String**| DocumentId to update | [default to null] |
| **request\_body** | [**Map**](../Models/string.md)|  | |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateMany"></a>
# **updateMany**
> UpdateResponse updateMany(collectionName, UpdateRequest)

Update many in Collection

    Update many Document in given Collection

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **collectionName** | **String**| The name of your MongoDb Collection | [default to null] |
| **UpdateRequest** | [**UpdateRequest**](../Models/UpdateRequest.md)|  | |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

