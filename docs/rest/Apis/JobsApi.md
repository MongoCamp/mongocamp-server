# JobsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteJob**](JobsApi.md#deleteJob) | **DELETE** /system/jobs/{jobGroup}/{jobName} | Delete Job |
| [**executeJob**](JobsApi.md#executeJob) | **POST** /system/jobs/{jobGroup}/{jobName} | Execute Job |
| [**jobsList**](JobsApi.md#jobsList) | **GET** /system/jobs | Registered Jobs |
| [**possibleJobsList**](JobsApi.md#possibleJobsList) | **GET** /system/jobs/classes | Possible Jobs |
| [**registerJob**](JobsApi.md#registerJob) | **PUT** /system/jobs | Register Job |
| [**updateJob**](JobsApi.md#updateJob) | **PATCH** /system/jobs/{jobGroup}/{jobName} | Update Job |


<a name="deleteJob"></a>
# **deleteJob**
> JsonValue_Boolean deleteJob(jobGroup, jobName)

Delete Job

    Delete Job and reload all Job Information

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **jobGroup** | **String**| Group Name of the Job | [default to Default] |
| **jobName** | **String**| Name of the Job | [default to null] |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="executeJob"></a>
# **executeJob**
> JsonValue_Boolean executeJob(jobGroup, jobName)

Execute Job

    Execute scheduled Job manually

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **jobGroup** | **String**| Group Name of the Job | [default to Default] |
| **jobName** | **String**| Name of the Job | [default to null] |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="jobsList"></a>
# **jobsList**
> List jobsList()

Registered Jobs

    Returns the List of all registered Jobs with full information

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/JobInformation.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="possibleJobsList"></a>
# **possibleJobsList**
> List possibleJobsList()

Possible Jobs

    Returns the List of possible job classes

### Parameters
This endpoint does not need any parameter.

### Return type

**List**

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="registerJob"></a>
# **registerJob**
> JobInformation registerJob(JobConfig)

Register Job

    Register an Job and return the JobInformation with next schedule information

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **JobConfig** | [**JobConfig**](../Models/JobConfig.md)|  | |

### Return type

[**JobInformation**](../Models/JobInformation.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateJob"></a>
# **updateJob**
> JobInformation updateJob(jobGroup, jobName, JobConfig)

Update Job

    Add Job and get JobInformation back

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **jobGroup** | **String**| Group Name of the Job | [default to Default] |
| **jobName** | **String**| Name of the Job | [default to null] |
| **JobConfig** | [**JobConfig**](../Models/JobConfig.md)|  | |

### Return type

[**JobInformation**](../Models/JobInformation.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

