# DocumentsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDocument**](DocumentsApi.md#deleteDocument) | **DELETE** /mongodb/collections/{collectionName}/documents/{documentId} | Delete Document from Collection
[**deleteMany**](DocumentsApi.md#deleteMany) | **DELETE** /mongodb/collections/{collectionName}/documents/many/delete | Delete Many in Collection
[**documentsList**](DocumentsApi.md#documentsList) | **GET** /mongodb/collections/{collectionName}/documents | Documents in Collection
[**find**](DocumentsApi.md#find) | **POST** /mongodb/collections/{collectionName}/documents | Documents in Collection
[**getDocument**](DocumentsApi.md#getDocument) | **GET** /mongodb/collections/{collectionName}/documents/{documentId} | Document from Collection
[**insert**](DocumentsApi.md#insert) | **PUT** /mongodb/collections/{collectionName}/documents | Insert Document
[**insertMany**](DocumentsApi.md#insertMany) | **PUT** /mongodb/collections/{collectionName}/documents/many/insert | Insert many Documents
[**updateDocument**](DocumentsApi.md#updateDocument) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId} | Update Document in Collection
[**updateDocumentPartitial**](DocumentsApi.md#updateDocumentPartitial) | **PATCH** /mongodb/collections/{collectionName}/documents/{documentId}/partitial | Update Document Parts in Collection
[**updateMany**](DocumentsApi.md#updateMany) | **PATCH** /mongodb/collections/{collectionName}/documents/many/update | Update many in Collection


<a name="deleteDocument"></a>
# **deleteDocument**
> DeleteResponse deleteDocument(collectionName, documentId)

Delete Document from Collection

    Delete one Document from Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **documentId** | **String**| DocumentId to delete | 

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
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **request\_body** | [**Map**](../Models/string.md)|  |

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="documentsList"></a>
# **documentsList**
> List documentsList(collectionName, filter, sort, projection, rowsPerPage, page)

Documents in Collection

    Get Documents paginated from MongoDatabase Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **filter** | **String**| MongoDB Filter Query by Default all filter | [optional] 
 **sort** | **String**| MongoDB sorting | [optional] 
 **projection** | **String**| MongoDB projection | [optional] 
 **rowsPerPage** | **Long**| Count elements per page | [optional] 
 **page** | **Long**| Requested page of the ResultSets | [optional] 

### Return type

[**List**](../Models/map.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="find"></a>
# **find**
> List find(collectionName, MongoFindRequest, rowsPerPage, page)

Documents in Collection

    Alternative to GET Route for more complex queries and URL max. Length

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **MongoFindRequest** | [**MongoFindRequest**](../Models/MongoFindRequest.md)|  |
 **rowsPerPage** | **Long**| Count elements per page | [optional] 
 **page** | **Long**| Requested page of the ResultSets | [optional] 

### Return type

[**List**](../Models/map.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="getDocument"></a>
# **getDocument**
> Map getDocument(collectionName, documentId)

Document from Collection

    Get one Document from Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **documentId** | **String**| DocumentId to read | 

### Return type

**Map**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="insert"></a>
# **insert**
> InsertResponse insert(collectionName, request\_body)

Insert Document

    Insert one Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
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

Insert many Documents

    Insert many documents in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **request\_body** | [**List**](../Models/map.md)|  | [optional]

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateDocument"></a>
# **updateDocument**
> UpdateResponse updateDocument(collectionName, documentId, request\_body)

Update Document in Collection

    &#39;Replace&#39; one Document with the new document from Request in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **documentId** | **String**| DocumentId to update | 
 **request\_body** | [**Map**](../Models/string.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateDocumentPartitial"></a>
# **updateDocumentPartitial**
> UpdateResponse updateDocumentPartitial(collectionName, documentId, request\_body)

Update Document Parts in Collection

    Update the document Parts with the values from the Request

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **documentId** | **String**| DocumentId to update | 
 **request\_body** | [**Map**](../Models/string.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateMany"></a>
# **updateMany**
> UpdateResponse updateMany(collectionName, UpdateRequest)

Update many in Collection

    Update many Document in Collection

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **collectionName** | **String**| The name of your MongoDb Collection | 
 **UpdateRequest** | [**UpdateRequest**](../Models/UpdateRequest.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

