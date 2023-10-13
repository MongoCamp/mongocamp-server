# BucketApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**clearBucket**](BucketApi.md#clearBucket) | **DELETE** /mongodb/buckets/{bucketName}/clear | Clear Bucket |
| [**deleteBucket**](BucketApi.md#deleteBucket) | **DELETE** /mongodb/buckets/{bucketName} | Delete Bucket |
| [**getBucket**](BucketApi.md#getBucket) | **GET** /mongodb/buckets/{bucketName} | Bucket Information |
| [**listBuckets**](BucketApi.md#listBuckets) | **GET** /mongodb/buckets | List of Buckets |


<a name="clearBucket"></a>
# **clearBucket**
> JsonValue_Boolean clearBucket(bucketName)

Clear Bucket

    Delete all Files in Bucket

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **bucketName** | **String**| The name of your MongoDb Collection | [default to null] |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteBucket"></a>
# **deleteBucket**
> JsonValue_Boolean deleteBucket(bucketName)

Delete Bucket

    Delete a given Bucket

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **bucketName** | **String**| The name of your MongoDb Collection | [default to null] |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getBucket"></a>
# **getBucket**
> BucketInformation getBucket(bucketName)

Bucket Information

    All Information about a single Bucket

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **bucketName** | **String**| The name of your MongoDb Collection | [default to null] |

### Return type

[**BucketInformation**](../Models/BucketInformation.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

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

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

