# AdminApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addRoles**](AdminApi.md#addRoles) | **PUT** /admin/roles | Add Role
[**addUser**](AdminApi.md#addUser) | **PUT** /admin/users | Add User
[**deleteRoles**](AdminApi.md#deleteRoles) | **DELETE** /admin/roles/{roleName} | Delete Role
[**deleteUser**](AdminApi.md#deleteUser) | **DELETE** /admin/users/{userId} | Delete User
[**getRoles**](AdminApi.md#getRoles) | **GET** /admin/roles/{roleName} | Get Role
[**getUser**](AdminApi.md#getUser) | **GET** /admin/users/{userId} | UserProfile for userId
[**listRoles**](AdminApi.md#listRoles) | **GET** /admin/roles | List Roles
[**listUsers**](AdminApi.md#listUsers) | **GET** /admin/users | List Users
[**updateApiKeyForUser**](AdminApi.md#updateApiKeyForUser) | **PATCH** /admin/users/{userId}/apikey | Update ApiKey
[**updatePasswordForUser**](AdminApi.md#updatePasswordForUser) | **PATCH** /admin/users/{userId}/password | Update Password
[**updateRole**](AdminApi.md#updateRole) | **PATCH** /admin/roles/{roleName} | Update Role
[**updateRolesForUser**](AdminApi.md#updateRolesForUser) | **PATCH** /admin/users/{userId}/roles | Update User Roles


<a name="addRoles"></a>
# **addRoles**
> Role addRoles(Role)

Add Role

    Add a new Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **Role** | [**Role**](../Models/Role.md)|  |

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="addUser"></a>
# **addUser**
> UserProfile addUser(UserInformation)

Add User

    Add a new User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **UserInformation** | [**UserInformation**](../Models/UserInformation.md)|  |

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="deleteRoles"></a>
# **deleteRoles**
> JsonResult_Boolean deleteRoles(roleName)

Delete Role

    Delete Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleName** | **String**| RoleKey | 

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="deleteUser"></a>
# **deleteUser**
> JsonResult_Boolean deleteUser(userId)

Delete User

    Delete User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Delete | 

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getRoles"></a>
# **getRoles**
> Role getRoles(roleName)

Get Role

    Get Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleName** | **String**| UserRoleKey | 

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getUser"></a>
# **getUser**
> UserProfile getUser(userId)

UserProfile for userId

    Get UserProfile for user

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | 

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="listRoles"></a>
# **listRoles**
> List listRoles(filter, rowsPerPage, page)

List Roles

    List all Roles or filtered

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | **String**| filter after userId by contains | [optional] 
 **rowsPerPage** | **Long**| Count elements per page | [optional] 
 **page** | **Long**| Requested page of the ResultSets | [optional] 

### Return type

[**List**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="listUsers"></a>
# **listUsers**
> List listUsers(filter, rowsPerPage, page)

List Users

    List all Users or filtered

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filter** | **String**| filter after userId by contains | [optional] 
 **rowsPerPage** | **Long**| Count elements per page | [optional] 
 **page** | **Long**| Requested page of the ResultSets | [optional] 

### Return type

[**List**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="updateApiKeyForUser"></a>
# **updateApiKeyForUser**
> JsonResult_String updateApiKeyForUser(userId)

Update ApiKey

    Change Password of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | 

### Return type

[**JsonResult_String**](../Models/JsonResult_String.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="updatePasswordForUser"></a>
# **updatePasswordForUser**
> JsonResult_Boolean updatePasswordForUser(userId, PasswordUpdateRequest)

Update Password

    Change Password of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | 
 **PasswordUpdateRequest** | [**PasswordUpdateRequest**](../Models/PasswordUpdateRequest.md)|  |

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateRole"></a>
# **updateRole**
> Role updateRole(roleName, UpdateRoleRequest)

Update Role

    Update Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleName** | **String**| RoleKey | 
 **UpdateRoleRequest** | [**UpdateRoleRequest**](../Models/UpdateRoleRequest.md)|  |

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="updateRolesForUser"></a>
# **updateRolesForUser**
> UserProfile updateRolesForUser(userId, request\_body)

Update User Roles

    Update Roles of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | 
 **request\_body** | [**List**](../Models/string.md)|  | [optional]

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

