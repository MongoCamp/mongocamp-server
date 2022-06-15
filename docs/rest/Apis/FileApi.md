# FileApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteFile**](FileApi.md#deleteFile) | **DELETE** /mongodb/buckets/{bucketName}/files/{fileId} | Delete File from Bucket
[**findFiles**](FileApi.md#findFiles) | **POST** /mongodb/buckets/{bucketName}/files | Files in Bucket
[**getFile**](FileApi.md#getFile) | **GET** /mongodb/buckets/{bucketName}/files/{fileId}/file | File from Bucket
[**getFileInformation**](FileApi.md#getFileInformation) | **GET** /mongodb/buckets/{bucketName}/files/{fileId} | FileInformation from Bucket
[**insertFile**](FileApi.md#insertFile) | **PUT** /mongodb/buckets/{bucketName}/files | Insert File
[**listFiles**](FileApi.md#listFiles) | **GET** /mongodb/buckets/{bucketName}/files | Files in Bucket
[**updateFileInformation**](FileApi.md#updateFileInformation) | **PATCH** /mongodb/buckets/{bucketName}/files/{fileId} | Update FileInformation in Bucket


<a name="deleteFile"></a>
# **deleteFile**
> DeleteResponse deleteFile(bucketName, fileId)

Delete File from Bucket

    Delete one File from given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **fileId** | **String**| fileId to delete | [default to null]

### Return type

[**DeleteResponse**](../Models/DeleteResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="findFiles"></a>
# **findFiles**
> List findFiles(bucketName, MongoFindRequest, rowsPerPage, page)

Files in Bucket

    Alternative to GET Route for more complex queries and URL max. Length

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **MongoFindRequest** | [**MongoFindRequest**](../Models/MongoFindRequest.md)|  |
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

[**List**](../Models/FileInformation.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="getFile"></a>
# **getFile**
> File getFile(bucketName, fileId)

File from Bucket

    Get File from given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **fileId** | **String**| FileId to read | [default to null]

### Return type

**File**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/octet-stream, application/json

<a name="getFileInformation"></a>
# **getFileInformation**
> FileInformation getFileInformation(bucketName, fileId)

FileInformation from Bucket

    Get one FileInformation from given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **fileId** | **String**| fileId to read | [default to null]

### Return type

[**FileInformation**](../Models/FileInformation.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="insertFile"></a>
# **insertFile**
> InsertResponse insertFile(bucketName, file, metaData, fileName)

Insert File

    Insert one File in given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **file** | **File**|  | [default to null]
 **metaData** | **String**|  | [default to null]
 **fileName** | **String**| override filename of uploaded file | [optional] [default to null]

### Return type

[**InsertResponse**](../Models/InsertResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: multipart/form-data
- **Accept**: application/json, text/plain

<a name="listFiles"></a>
# **listFiles**
> List listFiles(bucketName, filter, sort, projection, rowsPerPage, page)

Files in Bucket

    Get Files paginated from given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **filter** | **String**| MongoDB Filter Query by Default all filter | [optional] [default to null]
 **sort** | **String**| MongoDB sorting | [optional] [default to null]
 **projection** | **String**| MongoDB projection | [optional] [default to null]
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

[**List**](../Models/FileInformation.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="updateFileInformation"></a>
# **updateFileInformation**
> UpdateResponse updateFileInformation(bucketName, fileId, UpdateFileInformationRequest)

Update FileInformation in Bucket

    Replace MetaData and potential update FileName

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]
 **fileId** | **String**| fileId to update | [default to null]
 **UpdateFileInformationRequest** | [**UpdateFileInformationRequest**](../Models/UpdateFileInformationRequest.md)|  |

### Return type

[**UpdateResponse**](../Models/UpdateResponse.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

