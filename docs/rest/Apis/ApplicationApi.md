# ApplicationApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**eventMetrics**](ApplicationApi.md#eventMetrics) | **GET** /system/monitoring/events | Event Metrics
[**jvmMetrics**](ApplicationApi.md#jvmMetrics) | **GET** /system/monitoring/jvm | JVM Metrics
[**mongoDbMetrics**](ApplicationApi.md#mongoDbMetrics) | **GET** /system/monitoring/mongodb | MongoDb Metrics
[**settings**](ApplicationApi.md#settings) | **GET** /system/settings | System Settings
[**systemMetrics**](ApplicationApi.md#systemMetrics) | **GET** /system/monitoring/system | System Metrics


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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

