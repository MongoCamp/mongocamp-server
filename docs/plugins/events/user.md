---
title: User Events
---
# {{ $frontmatter.title }}

## LoginEvent
Published when the [AdminApi `gnerateNewApiKeyForUser` Route](../../rest/Apis/AuthApi.md#login) is called.

## LogoutEvent
Published when the [AuthApi `logout` Route](../../rest/Apis/AuthApi.md#logout) or [AuthApi `logoutbydelete` Route](../../rest/Apis/AuthApi.md#logoutbydelete) is called.

## CreateUserEvent
Published when the [AdminApi `addUser` Route](../../rest/Apis/AdminApi.md#adduser) is called.

## DeleteUserEvent
Published when the [AdminApi `deleteUser` Route](../../rest/Apis/AdminApi.md#deleteuser) is called.

## UpdateApiKeyEvent
Published when the [AdminApi `gnerateNewApiKeyForUser` Route](../../rest/Apis/AdminApi.md#gneratenewapikeyforuser) or [AuthApi `generateNewApiKey` Route](../../rest/Apis/AuthApi.md#generatenewapikey) is called.

## UpdatePasswordEvent
Published when the [AdminApi `updatePasswordForUser` Route](../../rest/Apis/AdminApi.md#updatepasswordforuser) or [AuthApi `updatePassword` Route](../../rest/Apis/AuthApi.md#updatepassword) is called.

## UpdateUserRoleEvent
Published when the [AdminApi `updateRolesForUser` Route](../../rest/Apis/AdminApi.md#updaterolesforuser) is called.


