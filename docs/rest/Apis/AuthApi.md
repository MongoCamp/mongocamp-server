# AuthApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**generateNewApiKey**](AuthApi.md#generateNewApiKey) | **PATCH** /auth/profile/apikey | Update ApiKey |
| [**isAuthenticated**](AuthApi.md#isAuthenticated) | **GET** /auth/authenticated | Check Authentication |
| [**login**](AuthApi.md#login) | **POST** /auth/login | Login User |
| [**logout**](AuthApi.md#logout) | **POST** /auth/logout | Logout User |
| [**logoutByDelete**](AuthApi.md#logoutByDelete) | **DELETE** /auth/logout | Logout User |
| [**refreshToken**](AuthApi.md#refreshToken) | **GET** /auth/token/refresh | Refresh User |
| [**updatePassword**](AuthApi.md#updatePassword) | **PATCH** /auth/profile/password | Update Password |
| [**userProfile**](AuthApi.md#userProfile) | **GET** /auth/profile | User Profile |


<a name="generateNewApiKey"></a>
# **generateNewApiKey**
> JsonValue_String generateNewApiKey()

Update ApiKey

    Generate new ApiKey of logged in User

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonValue_String**](../Models/JsonValue_String.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="isAuthenticated"></a>
# **isAuthenticated**
> JsonValue_Boolean isAuthenticated()

Check Authentication

    Check a given Login for is authenticated

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="login"></a>
# **login**
> LoginResult login(Login)

Login User

    Login for one user and return Login Information

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **Login** | [**Login**](../Models/Login.md)| Login Information for your Users | |

### Return type

[**LoginResult**](../Models/LoginResult.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="logout"></a>
# **logout**
> JsonValue_Boolean logout()

Logout User

    Logout by bearer Token

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="logoutByDelete"></a>
# **logoutByDelete**
> JsonValue_Boolean logoutByDelete()

Logout User

    Logout by bearer Token

### Parameters
This endpoint does not need any parameter.

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

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

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

<a name="updatePassword"></a>
# **updatePassword**
> JsonValue_Boolean updatePassword(PasswordUpdateRequest)

Update Password

    Change Password of logged in User

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **PasswordUpdateRequest** | [**PasswordUpdateRequest**](../Models/PasswordUpdateRequest.md)|  | |

### Return type

[**JsonValue_Boolean**](../Models/JsonValue_Boolean.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json, text/plain

<a name="userProfile"></a>
# **userProfile**
> UserProfile userProfile()

User Profile

    Return the User Profile of loggedin user

### Parameters
This endpoint does not need any parameter.

### Return type

[**UserProfile**](../Models/UserProfile.md)

### Authorization

[httpAuth1](../README.md#httpAuth1), [httpAuth](../README.md#httpAuth), [apiKeyAuth](../README.md#apiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

