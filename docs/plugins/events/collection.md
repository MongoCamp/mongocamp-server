---
title: Collection Events
---
# {{ $frontmatter.title }}

## ClearCollectionEvent
Published when the [`clearCollection`](../../rest/Apis/CollectionApi.md#clearcollection) Route is called.

## CopyCollectionEvent
Published when the `copyCollection` Route is called, with the name of the collection and of the copy.

## RenameCollectionEvent
Published when the `renameCollection` Route is called, with the old and the new name of the collection.

## DropCollectionEvent
Published when the [`deleteCollection`](../../rest/Apis/CollectionApi.md#deletecollection) Route is called.
