# ApplicationApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**eventMetrics**](ApplicationApi.md#eventMetrics) | **GET** /system/monitoring/events | Event Metrics |
| [**getConfig**](ApplicationApi.md#getConfig) | **GET** /system/configurations/{configurationKey} | Configuration for configurationKey |
| [**jvmMetrics**](ApplicationApi.md#jvmMetrics) | **GET** /system/monitoring/jvm | JVM Metrics |
| [**listConfigurations**](ApplicationApi.md#listConfigurations) | **GET** /system/configurations | List Configurations |
| [**mongoDbMetrics**](ApplicationApi.md#mongoDbMetrics) | **GET** /system/monitoring/mongodb | MongoDb Metrics |
| [**settings**](ApplicationApi.md#settings) | **GET** /system/settings | System Settings |
| [**systemMetrics**](ApplicationApi.md#systemMetrics) | **GET** /system/monitoring/system | System Metrics |
| [**updateConfiguration**](ApplicationApi.md#updateConfiguration) | **PATCH** /system/configurations/{configurationKey} | Update Configuration |


<a name="eventMetrics"></a>
# **eventMetrics**
> List eventMetrics()

Event Metrics

    Returns the Metrics of events of the running MongoCamp Application.

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/Metric.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getConfig"></a>
# **getConfig**
> MongoCampConfiguration getConfig(configurationKey)

Configuration for configurationKey

    Get Configuration for key

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **configurationKey** | **String**| configurationKey to get | [default to null] |

### Return type

[**MongoCampConfiguration**](../Models/MongoCampConfiguration.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="jvmMetrics"></a>
# **jvmMetrics**
> List jvmMetrics()

JVM Metrics

    Returns the JVM Metrics of the running MongoCamp Application

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/Metric.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listConfigurations"></a>
# **listConfigurations**
> List listConfigurations()

List Configurations

    List all Configurations or filtered

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/MongoCampConfiguration.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="mongoDbMetrics"></a>
# **mongoDbMetrics**
> List mongoDbMetrics()

MongoDb Metrics

    Returns the MongoDB Metrics of the running MongoCamp Application

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/Metric.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="settings"></a>
# **settings**
> SettingsResponse settings()

System Settings

    Returns the Settings of the running MongoCamp Application.

### Parameters
This endpoint does not need any parameter.

### Return type

[**SettingsResponse**](../Models/SettingsResponse.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="systemMetrics"></a>
# **systemMetrics**
> List systemMetrics()

System Metrics

    Returns the Metrics of the MongoCamp System

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/Metric.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="updateConfiguration"></a>
# **updateConfiguration**
> JsonValue_Boolean updateConfiguration(configurationKey, JsonValue\_Any)

Update Configuration

    Update Configuration with the value

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **configurationKey** | **String**| configurationKey to edit | [default to null] |
| **JsonValue\_Any** | [**JsonValue_Any**](../Models/JsonValue_Any.md)|  | |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

