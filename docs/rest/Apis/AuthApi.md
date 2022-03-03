# AuthApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**login**](AuthApi.md#login) | **POST** /auth/login | Login User
[**logout**](AuthApi.md#logout) | **POST** /auth/logout | Logout User
[**logoutByDelete**](AuthApi.md#logoutByDelete) | **DELETE** /auth/logout | Logout User
[**refreshToken**](AuthApi.md#refreshToken) | **GET** /auth/token/refresh | Refresh User
[**updateApiKey**](AuthApi.md#updateApiKey) | **PATCH** /auth/profile/apikey | Update ApiKey
[**updatePassword**](AuthApi.md#updatePassword) | **PATCH** /auth/profile/password | Update Password
[**userProfile**](AuthApi.md#userProfile) | **GET** /auth/profile | User Profile


<a name="login"></a>
# **login**
> LoginResult login(Login)

Login User

    Login for one user and return Login Information

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **Login** | [**Login**](../Models/Login.md)| Login Information for your Users |

### Return type

[**LoginResult**](../Models/LoginResult.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="logout"></a>
# **logout**
> JsonResult_Boolean logout()

Logout User

    Logout an bearer Token

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="logoutByDelete"></a>
# **logoutByDelete**
> JsonResult_Boolean logoutByDelete()

Logout User

    Logout an bearer Token

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="refreshToken"></a>
# **refreshToken**
> LoginResult refreshToken()

Refresh User

    Refresh Token and return Login Information

### Parameters
This endpoint does not need any parameter.

### Return type

[**LoginResult**](../Models/LoginResult.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="updateApiKey"></a>
# **updateApiKey**
> JsonResult_String updateApiKey(userid)

Update ApiKey

    Change ApiKey of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userid** | **String**| UserId to update or create the ApiKey | [optional] 

### Return type

[**JsonResult_String**](../Models/JsonResult_String.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json, text/plain

<a name="updatePassword"></a>
# **updatePassword**
> JsonResult_Boolean updatePassword(PasswordUpdateRequest)

Update Password

    Change Password of User

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **PasswordUpdateRequest** | [**PasswordUpdateRequest**](../Models/PasswordUpdateRequest.md)|  |

### Return type

[**JsonResult_Boolean**](../Models/JsonResult_Boolean.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="userProfile"></a>
# **userProfile**
> UserProfile userProfile()

User Profile

    Return the User Profile

### Parameters
This endpoint does not need any parameter.

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[apiKeyAuth](../README.md#apiKeyAuth), [httpAuth](../README.md#httpAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

