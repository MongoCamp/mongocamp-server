---
title: Config
---

# Introduction

MongoCamp can be configured by using the environment variables of docker or by changing the values in your own MongoDb.

But there are also [some variables](environment.md) that can only overridden by using the docker (or you system environment variables if you run MongoCamp Server locally).

::: info
Configuration over environment variable always wins. If you changed the value of a config in you database the system checks that there is an env variable and reset the value in you database.
:::

## Allowed Types and how we store them to Database

|       Type       | Stored as in MongoDb |
|:----------------:|:--------------------:|
|     Boolean      |         bool         |
|      String      |        string        |
|      Double      |        double        |
|       Long       |         long         |
|       Int        |         long         |
|     Duration     |        string        |
| Option[Boolean]  |         bool         |
|  Option[String]  |        string        |
|  Option[Double]  |        double        |
|   Option[Long]   |         long         |
|   Option[Int]    |         long         |
| Option[Duration] |        string        |
|  List[Boolean]   |        [bool]        |
|   List[String]   |       [string]       |
|   List[Double]   |       [double]       |
|    List[Long]    |        [long]        |
|    List[Int]     |        [long]        |
|  List[Duration]  |       [string]       |

