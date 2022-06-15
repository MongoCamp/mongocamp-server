# BucketApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**clearBucket**](BucketApi.md#clearBucket) | **DELETE** /mongodb/buckets/{bucketName}/clear | Clear Bucket
[**deleteBucket**](BucketApi.md#deleteBucket) | **DELETE** /mongodb/buckets/{bucketName} | Delete Bucket
[**getBucket**](BucketApi.md#getBucket) | **GET** /mongodb/buckets/{bucketName} | Bucket Information
[**listBuckets**](BucketApi.md#listBuckets) | **GET** /mongodb/buckets | List of Buckets


<a name="clearBucket"></a>
# **clearBucket**
> JsonResult_Boolean clearBucket(bucketName)

Clear Bucket

    Delete all Files in Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteBucket"></a>
# **deleteBucket**
> JsonResult_Boolean deleteBucket(bucketName)

Delete Bucket

    Delete a given Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getBucket"></a>
# **getBucket**
> BucketInformation getBucket(bucketName)

Bucket Information

    All Information about a single Bucket

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bucketName** | **String**| The name of your MongoDb Collection | [default to null]

### Return type

[**BucketInformation**](../Models/BucketInformation.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listBuckets"></a>
# **listBuckets**
> List listBuckets()

List of Buckets

    List of all Buckets of the default database

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

