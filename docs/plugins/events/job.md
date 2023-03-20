---
title: Job Events
---
# {{ $frontmatter.title }}

## CreateJobEvent
Published when the [`registerJob` Route](../../rest/Apis/JobsApi.md#registerjob) is called or an plugin create a new Job by calling `JobPlugin.addJob`.

## UpdateJobEvent
Published when the [`updateJob` Route](../../rest/Apis/JobsApi.md#updatejob) is called.

## DeleteJobEvent
Published when the [`deleteJob` Route](../../rest/Apis/JobsApi.md#deletejob) is called.
