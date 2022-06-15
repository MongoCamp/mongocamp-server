# AdminApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addRole**](AdminApi.md#addRole) | **PUT** /admin/roles | Add Role
[**addUser**](AdminApi.md#addUser) | **PUT** /admin/users | Add User
[**deleteRole**](AdminApi.md#deleteRole) | **DELETE** /admin/roles/{roleName} | Delete Role
[**deleteUser**](AdminApi.md#deleteUser) | **DELETE** /admin/users/{userId} | Delete User
[**getRole**](AdminApi.md#getRole) | **GET** /admin/roles/{roleName} | Get Role
[**getUser**](AdminApi.md#getUser) | **GET** /admin/users/{userId} | UserProfile for userId
[**gnerateNewApiKeyForUser**](AdminApi.md#gnerateNewApiKeyForUser) | **PATCH** /admin/users/{userId}/apikey | Update ApiKey
[**listRoles**](AdminApi.md#listRoles) | **GET** /admin/roles | List Roles
[**listUsers**](AdminApi.md#listUsers) | **GET** /admin/users | List Users
[**updatePasswordForUser**](AdminApi.md#updatePasswordForUser) | **PATCH** /admin/users/{userId}/password | Update Password
[**updateRole**](AdminApi.md#updateRole) | **PATCH** /admin/roles/{roleName} | Update Role
[**updateRolesForUser**](AdminApi.md#updateRolesForUser) | **PATCH** /admin/users/{userId}/roles | Update User Roles


<a name="addRole"></a>
# **addRole**
> Role addRole(Role)

Add Role

    Add a new Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **Role** | [**Role**](../Models/Role.md)|  |

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="deleteRole"></a>
# **deleteRole**
> JsonResult_Boolean deleteRole(roleName)

Delete Role

    Delete Role

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleName** | **String**| RoleKey | [default to null]

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **userId** | **String**| UserId to Delete | [default to null]

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="getRole"></a>
# **getRole**
> Role getRole(roleName)

Get Role

    Get Role by RoleKey

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **roleName** | **String**| RoleKey to search | [default to null]

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **userId** | **String**| UserId to search | [default to null]

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="gnerateNewApiKeyForUser"></a>
# **gnerateNewApiKeyForUser**
> JsonResult_String gnerateNewApiKeyForUser(userId)

Update ApiKey

    Generate an new APIkey for the user

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | [default to null]

### Return type

[**JsonResult_String**](../Models/JsonResult_String.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **filter** | **String**| filter after userId by contains | [optional] [default to null]
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

[**List**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **filter** | **String**| filter after userId by contains | [optional] [default to null]
 **rowsPerPage** | **Long**| Count elements per page | [optional] [default to null]
 **page** | **Long**| Requested page of the ResultSets | [optional] [default to null]

### Return type

[**List**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="updatePasswordForUser"></a>
# **updatePasswordForUser**
> JsonResult_Boolean updatePasswordForUser(userId, PasswordUpdateRequest)

Update Password

    Change Password of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userId** | **String**| UserId to Update | [default to null]
 **PasswordUpdateRequest** | [**PasswordUpdateRequest**](../Models/PasswordUpdateRequest.md)|  |

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **roleName** | **String**| RoleKey | [default to null]
 **UpdateRoleRequest** | [**UpdateRoleRequest**](../Models/UpdateRoleRequest.md)|  |

### Return type

[**Role**](../Models/Role.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

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
 **userId** | **String**| UserId to Update | [default to null]
 **request\_body** | [**List**](../Models/string.md)|  | [optional]

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth), [httpAuth1](../README.md#httpAuth1)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

