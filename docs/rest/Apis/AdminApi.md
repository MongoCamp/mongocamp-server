# AdminApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getUser**](AdminApi.md#getUser) | **GET** /admin/users/{userId} | UserProfile for userId
[**getUserRoles**](AdminApi.md#getUserRoles) | **GET** /admin/userroles/{userRoleName} | Get UserRole
[**listUserRoles**](AdminApi.md#listUserRoles) | **GET** /admin/userroles | List UserRoles
[**listUsers**](AdminApi.md#listUsers) | **GET** /admin/users | List Users


<a name="getUser"></a>
# **getUser**
> UserProfile getUser(userId)

UserProfile for userId

    Get UserProfile for user

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | [default to null]

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getUserRoles"></a>
# **getUserRoles**
> UserRole getUserRoles(userRoleName)

Get UserRole

    Get UserRole

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userRoleName** | **String**| UserRoleKey | [default to null]

### Return type

[**UserRole**](../Models/UserRole.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listUserRoles"></a>
# **listUserRoles**
> List listUserRoles(filter)

List UserRoles

    List all UserRolss or filtered

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | **String**| filter after userId by contains | [optional] [default to null]

### Return type

[**List**](../Models/UserRole.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="listUsers"></a>
# **listUsers**
> List listUsers(filter)

List Users

    List all Users or filtered

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | **String**| filter after userId by contains | [optional] [default to null]

### Return type

[**List**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

