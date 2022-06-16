## [](https://github.com/MongoCamp/mongocamp-server/compare/v1.0.1...v) (2022-06-16)


### Bug Fixes

* cleanup ([48ce013](https://github.com/MongoCamp/mongocamp-server/commit/48ce013fa6df734ed744c20dfc9dd4d1495a01ab))

### [1.0.1](https://github.com/MongoCamp/mongocamp-server/compare/v1.0.0...v1.0.1) (2022-06-16)


### Features

* changed some defaults for container ([12ca66d](https://github.com/MongoCamp/mongocamp-server/commit/12ca66da6752e6de350e42d85cd52198599c8c84))

## [1.0.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.15.0...v1.0.0) (2022-06-15)


### Features

* `AuthInputAllMethods` and `AuthInputBearerWithBasic` added ([dadf7bb](https://github.com/MongoCamp/mongocamp-server/commit/dadf7bb7816ba6befcb5bab34cf2ca1902aa1705))
* update to Tapir 1.0.0 ([bc94f6a](https://github.com/MongoCamp/mongocamp-server/commit/bc94f6a9c09d6875687294dbbd6eddc928270f19))

## [0.15.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.14.1...v0.15.0) (2022-06-10)


### Features

* migration to Tapir 1.0.0-RC3 ([79537e8](https://github.com/MongoCamp/mongocamp-server/commit/79537e888f85b6c865efbee997a0065efd2950d7))

### [0.14.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.14.0...v0.14.1) (2022-06-10)


### Bug Fixes

* For events without duration use summary instead of timer ([4ef8bff](https://github.com/MongoCamp/mongocamp-server/commit/4ef8bff6d57bc9a96d3fb04876b8670d1402a1c7))

## [0.14.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.13.0...v0.14.0) (2022-06-09)


### Features

* Metrics Monitoring ([af3f2ba](https://github.com/MongoCamp/mongocamp-server/commit/af3f2ba89adf88836af99040283934c709655853))


### Maintenance

* 5 dependency updates ([1ba3d79](https://github.com/MongoCamp/mongocamp-server/commit/1ba3d79ccc8ac30b89e1cb7c4c2962e2063a83f3))

## [0.13.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.12.1...v0.13.0) (2022-05-20)


### Bug Fixes

* Wrong Return Value for cache control ([bf7ca58](https://github.com/MongoCamp/mongocamp-server/commit/bf7ca58c42baf03a7a6210d8c5c66d028b94c666))


### Code Refactoring

* Removed ExecutionContext.global ([1bb961d](https://github.com/MongoCamp/mongocamp-server/commit/1bb961d2a39665d596f357b83ae4fc4c49540052))
* Replaced `com.sfxcode.nosql.mongo` with `dev.mongocamp.driver.mongodb` ([86e97ca](https://github.com/MongoCamp/mongocamp-server/commit/86e97caf84c47add477a92d18235d84c1c2770a3))
* Singular name for package ([d7f2fbe](https://github.com/MongoCamp/mongocamp-server/commit/d7f2fbe8958e7f4fc112b49d42d0b795b809bfaa))
* typo in plugin list name ([c2bca46](https://github.com/MongoCamp/mongocamp-server/commit/c2bca46b7d46969579d65b1b674567a42f3262a9))


### Features

* Ignore plugins ([7f35304](https://github.com/MongoCamp/mongocamp-server/commit/7f35304a6a6043ecff66a465b6f6566d08afa9b1))
* Load Configurations at global place ([9a91164](https://github.com/MongoCamp/mongocamp-server/commit/9a9116464f344d1f0ce82055a08cabf8dffd30b9))
* Publish Events for many Events to EventStream ([48c9961](https://github.com/MongoCamp/mongocamp-server/commit/48c99610b98e6ded5ed67cee466db6c10aac7737))
* Use EventListener and EventStream for RequestLogging ([bc7743e](https://github.com/MongoCamp/mongocamp-server/commit/bc7743e5429852a0b390ff8521f15e6e95a814c7))


### Maintenance

* DependencyUpdates sttClient ([af7eed9](https://github.com/MongoCamp/mongocamp-server/commit/af7eed9db1f18b5a943897cab131bc6373cc3ae2))
* DependencyUpdates Tapir, Circe and scaffeine ([503b493](https://github.com/MongoCamp/mongocamp-server/commit/503b49380e0d9ab2db2b29ebf0e87749d9d21add))

### [0.12.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.12.0...v0.12.1) (2022-05-02)


### Bug Fixes

* fixed empty role name or userId to add ([eff57e7](https://github.com/MongoCamp/mongocamp-server/commit/eff57e7b42ae0998e9f7f4bc80e23ab53c1fb3d9))
* Type and some other small errors at schema generation ([dce548f](https://github.com/MongoCamp/mongocamp-server/commit/dce548f024fb355a59d837b513b408657139015e))

## [0.12.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.11.0...v0.12.0) (2022-04-29)


### Features

* Analyze Collection ([cca0fa5](https://github.com/MongoCamp/mongocamp-server/commit/cca0fa53bdf567dc914275d826ef18ac0dd20e6a))
* Generate JsonSchema for Collection ([60c9a1d](https://github.com/MongoCamp/mongocamp-server/commit/60c9a1dd285659f75e543f714d66f66cb33be0d4))


### Maintenance

* DependencyUpdates joda-time, jwt-scala ([22859ea](https://github.com/MongoCamp/mongocamp-server/commit/22859ea541807b612ec567157384fb33237be321))
* DependencyUpdates sfxcode, sttClient, embed.mongo ([d03719c](https://github.com/MongoCamp/mongocamp-server/commit/d03719cb9546677f8f1edd9276de0be3923210ad))
* DependencyUpdates sfxcode, sttClient, embed.mongo ([00a7c8d](https://github.com/MongoCamp/mongocamp-server/commit/00a7c8db67a6104a994f993433b4cc2846531f5b))


### Reverts

* CollectionSuite reverted ([68f0e3e](https://github.com/MongoCamp/mongocamp-server/commit/68f0e3e38295e5a81fd7317fad41335db236373e))
* DatabaseSuite reverted ([21fece2](https://github.com/MongoCamp/mongocamp-server/commit/21fece2bf33aca61bd1895fdd97b0a0a5ecc4d3a))

## [0.11.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.10.1...v0.11.0) (2022-03-23)


### Bug Fixes

* ChangeLog Generation Error ([0aefaa6](https://github.com/MongoCamp/mongocamp-server/commit/0aefaa6b75b3b51afa7be6960337e061e548de9a))
* collection status from other database collection ([3a61031](https://github.com/MongoCamp/mongocamp-server/commit/3a61031dfe5543054d1a9ba1223dd7500b29ff9f))


### Code Refactoring

* fix test for BucketSuite ([2452c5c](https://github.com/MongoCamp/mongocamp-server/commit/2452c5c6d307e44383bb1be51c6725aee9da0375))
* fix test for BucketSuite ([896c9d4](https://github.com/MongoCamp/mongocamp-server/commit/896c9d46127f9ac889d62799328b891cbcc07e81))
* Move from QuadStingray/mongocamp to  mongocamp/mongocamp-server ([8c1dfb9](https://github.com/MongoCamp/mongocamp-server/commit/8c1dfb9c0b0172f0867948c65c42c4caae6a4fa2))

### [0.10.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.10.0...v0.10.1) (2022-03-21)


### Features

* **Cors:** Access Control Expose Headers ([835975d](https://github.com/MongoCamp/mongocamp-server/commit/835975d036ed68dea1ae8a991ff9bccf6c73d43c))

## [0.10.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.9.1...v0.10.0) (2022-03-21)


### Bug Fixes

* Date in Request Logging ([84a1b9a](https://github.com/MongoCamp/mongocamp-server/commit/84a1b9a26d6ed276c7d58ddeb964e6ad9a46cf0d)), closes [Issue#19](https://github.com/MongoCamp/Issue/issues/19)


### Features

* **auth:** Added Route to check isAuthenticated ([0c51f3b](https://github.com/MongoCamp/mongocamp-server/commit/0c51f3bd5b9fdea0f6ace4fc428bc957eca15126))
* **auth:** Route to check Authenticated ([f99427c](https://github.com/MongoCamp/mongocamp-server/commit/f99427c2d126fa9d5fbac03dc8eee5229ecc4efa)), closes [Issue#20](https://github.com/MongoCamp/Issue/issues/20)
* Dynamic Plugin Loading at StartUp ([2f65e6e](https://github.com/MongoCamp/mongocamp-server/commit/2f65e6e097c5e839d266fdd2e8851f4892452888)), closes [Issue#21](https://github.com/MongoCamp/Issue/issues/21) [Issue#9](https://github.com/MongoCamp/Issue/issues/9)

### [0.9.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.9.0...v0.9.1) (2022-03-19)


### Bug Fixes

* Add id to FileInformation and rename method on buckets for valid openapi specification ([fe39f0e](https://github.com/MongoCamp/mongocamp-server/commit/fe39f0e5188ae0f154c6d9aaa8df7e20eec29258))
* **file:** Fix Delete for GridFsFileAdapter ([011a272](https://github.com/MongoCamp/mongocamp-server/commit/011a2728b387ee08c42b80fad73385447fdf1831))


### Features

* Collections by Database ([c25fe73](https://github.com/MongoCamp/mongocamp-server/commit/c25fe731454a9740d4793f9d9b5d97cd8343cde9))
* **file:** FileInformation instead of Map[String, Any] as response ([d0c758a](https://github.com/MongoCamp/mongocamp-server/commit/d0c758a3a6364dd2345493d55d847c27416bdacb))
* **file:** Use other FilePlugin ([c08ac98](https://github.com/MongoCamp/mongocamp-server/commit/c08ac98e591638ff0fe5f47116cc5a31d2da00a0)), closes [Issue#13](https://github.com/MongoCamp/Issue/issues/13)
* Load Routes and File Adapter by Reflection ([573f4d7](https://github.com/MongoCamp/mongocamp-server/commit/573f4d7fbbf5497d6c643a395d60e30286e0d0f3))
* Update FileInformation Request ([385626c](https://github.com/MongoCamp/mongocamp-server/commit/385626c322033a3b11d01762e79b30f3b62b4c19))

## [0.9.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.8.0...v0.9.0) (2022-03-17)


### Bug Fixes

* Delete wrong Collection from GridFsFileAdapter ([4c57400](https://github.com/MongoCamp/mongocamp-server/commit/4c5740058b68e20e886290a33f053f88d241a658))


### Code Refactoring

* Extract CollectionBaseRoute from BaseRoute ([ef8b870](https://github.com/MongoCamp/mongocamp-server/commit/ef8b8709bed1e64c4dfdd260b1e8e7b5d459442c))
* Moved Configuration for BucketSuffixes to BucketInformation.scala ([70e4e6e](https://github.com/MongoCamp/mongocamp-server/commit/70e4e6e9c53d2cd6b16b39937f3d1d2d54cc130c))
* Rename Api Documents to Document ([c438152](https://github.com/MongoCamp/mongocamp-server/commit/c4381521a68b1b12cc1c4df29a9634ce167044a2))
* Rename method to convertFields ([bfb7b42](https://github.com/MongoCamp/mongocamp-server/commit/bfb7b42daf6ea791eb4ed9bf2fdddfecf27349b0))


### Features

* Bucket Api to List, Get and Delete Buckets ([f1b055c](https://github.com/MongoCamp/mongocamp-server/commit/f1b055ccdad36949c4eeda742f1e1d3fb08dd5cf))
* Delete File by Adapter Holder ([b851a74](https://github.com/MongoCamp/mongocamp-server/commit/b851a745f29201730db80c943a6553912be2bf9c))
* **file:** Implement File Download ([75cdd01](https://github.com/MongoCamp/mongocamp-server/commit/75cdd014488835794c9be107f94224f4aa6ba063))
* **file:** Implement File Upload ([a7ef597](https://github.com/MongoCamp/mongocamp-server/commit/a7ef597b380752c647df970f30d0b9cd5e3f0e5c))
* **file:** Implement Routes for Files ([a31b2b1](https://github.com/MongoCamp/mongocamp-server/commit/a31b2b1f80a046c669b123f65d33e39c71eae99e))

## [0.8.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.7.1...v0.8.0) (2022-03-14)


### Bug Fixes

* convert bson ids in one method ([8529163](https://github.com/MongoCamp/mongocamp-server/commit/8529163cc80b64cedba7adc344a752186740ef46))
* filter routes with _id field needs to be converted ([fe2b8f5](https://github.com/MongoCamp/mongocamp-server/commit/fe2b8f525dac208d88201599fc4697a4d151e500))
* update many routs needs conversion to OperationMap ([0adb6ab](https://github.com/MongoCamp/mongocamp-server/commit/0adb6aba9c5edcf11c67ffcbc8a923619718f689))


### Code Refactoring

* min port for mongodb and http server ([7cb0c4a](https://github.com/MongoCamp/mongocamp-server/commit/7cb0c4ae9ccba618b5e583ea91effeccdf883fd4))


### Features

* more auth methods ([4675ef1](https://github.com/MongoCamp/mongocamp-server/commit/4675ef180c6818a451c7f3f1960b4041180b112a))

### [0.7.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.7.0...v0.7.1) (2022-03-10)


### Code Refactoring

* Fixed Typos and `rename` functions ([466ccff](https://github.com/MongoCamp/mongocamp-server/commit/466ccff4056e16a22a771a2bafcb19e5e3f51e32))


### Features

* IndexOptions now optional in requests ([f77ecfa](https://github.com/MongoCamp/mongocamp-server/commit/f77ecfaed6f9ca409d20579ed806c025e2e95924))

## [0.7.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.3...v0.7.0) (2022-03-09)


### Features

* **Endpoint:** Collection Fields ([f953cac](https://github.com/MongoCamp/mongocamp-server/commit/f953cace4a89cd40792220849e8f0c67b3372d7e))
* **Endpoint:** Collection Fields ([012b24b](https://github.com/MongoCamp/mongocamp-server/commit/012b24b17f7ef104ec63aba3e2929fbd90071fbe))
* Reload UserInfos for Refresh or Profile Route ([be05407](https://github.com/MongoCamp/mongocamp-server/commit/be0540746bdce9c012245bae12ba887b78acab24))

### [0.6.3](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.2...v0.6.3) (2022-03-08)


### Bug Fixes

* Disabled logger for MongoCampException ([fa6fa79](https://github.com/MongoCamp/mongocamp-server/commit/fa6fa79c8de46018690935ff8e41e40c41e7ec2f))

### [0.6.2](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.1...v0.6.2) (2022-03-07)

### [0.6.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.0...v0.6.1) (2022-03-07)

## [0.6.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.5.0...v0.6.0) (2022-03-07)


### Features

* Migration to Tapir 0.20.1 ([83405bb](https://github.com/MongoCamp/mongocamp-server/commit/83405bbff6fcfff9ea3e5f11072caf2e1a15e429))

## [0.5.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.4.0...v0.5.0) (2022-03-02)


### Code Refactoring

* Removed "READ" Routes and spitted to DocumentRoutes and CollectionRoutes ([fecde9e](https://github.com/MongoCamp/mongocamp-server/commit/fecde9e5bf4203a8fc3de2ea779f36c48dd2d90c))
* **test:** simplify test request execution ([910a0be](https://github.com/MongoCamp/mongocamp-server/commit/910a0bed7a2d8a0b69bf8f030584b0484aa25733))


### Features

* Added Filter to get Route all documents ([6e40be2](https://github.com/MongoCamp/mongocamp-server/commit/6e40be23d90c2aa08e88cc2ebeea8bcbaf9ab8b9))
* cache tokens to database ([3a87e08](https://github.com/MongoCamp/mongocamp-server/commit/3a87e084b1b1cbfb1f70d0cb46153bd3f257b9b9))
* Grants for Buckets and Collections ([9fb9eca](https://github.com/MongoCamp/mongocamp-server/commit/9fb9eca217c941f2c4b06762609a78eb0d7d3a75))

## [0.4.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.3.4...v0.4.0) (2022-03-01)


### Code Refactoring

* add document routes for mor RESTlike route naming ([1831ac5](https://github.com/MongoCamp/mongocamp-server/commit/1831ac51e392a2145d0e4aa50b59087bd70dcf02))
* add document routes for mor RESTlike route naming ([3c30a41](https://github.com/MongoCamp/mongocamp-server/commit/3c30a41b2cce1f427b65981d627aef65765e9680))
* Extract Collections to own API Routes ([3300414](https://github.com/MongoCamp/mongocamp-server/commit/3300414c02916eb943242e2a9579a1404ff57a70))
* Extract Routes for Database Routes ([446daa1](https://github.com/MongoCamp/mongocamp-server/commit/446daa127ecac2f9a326c8263ebf5b81ee0472a5))
* rename userRole to role ([63678c3](https://github.com/MongoCamp/mongocamp-server/commit/63678c3ae571ba86b89872ecfbf07b8719ed4165))
* rename vals to endpoints ([cacfbec](https://github.com/MongoCamp/mongocamp-server/commit/cacfbeca590c348242cbf48a03388a186188846b))
* rename vals to endpoints ([bae1baf](https://github.com/MongoCamp/mongocamp-server/commit/bae1baf2e0d62080f7ce45c426c06ad421d905c1))


### Maintenance

* DependencyUpdate "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.4.3" % Test ([664353e](https://github.com/MongoCamp/mongocamp-server/commit/664353e288c9109482bfe41103e30b8743166c42))

### [0.3.4](https://github.com/MongoCamp/mongocamp-server/compare/v0.3.3...v0.3.4) (2022-02-26)


### Code Refactoring

* **Mongo:** FindAll Route ([16f8184](https://github.com/MongoCamp/mongocamp-server/commit/16f8184b5970d8fd4e98ff514f816afd80ab8aa6))

### [0.3.3](https://github.com/MongoCamp/mongocamp-server/compare/v0.3.2...v0.3.3) (2022-02-26)


### Code Refactoring

* Switch to default Port 8080 ([e716073](https://github.com/MongoCamp/mongocamp-server/commit/e716073da955fa657480b873e9c85902cfb5fcb4))


### Features

* **Mongo:** FindAll Route ([3dc3a64](https://github.com/MongoCamp/mongocamp-server/commit/3dc3a64b38384278e2c6f51778ea9ec4e2dd1184))
* **Mongo:** FindAll Route ([bdc5ef0](https://github.com/MongoCamp/mongocamp-server/commit/bdc5ef01acdf97fb47676fb53bfc161269ed2b84))

### [0.3.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.3.0...v0.3.1) (2022-02-23)


### Features

* RequestLogging ([cec1895](https://github.com/MongoCamp/mongocamp-server/commit/cec189546bb97ddd303ea0a5c85ddcc088fb99a3))

## [0.3.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.5...v0.3.0) (2022-02-22)


### Features

* Pagination for Find and Aggregation Routes ([1df577a](https://github.com/MongoCamp/mongocamp-server/commit/1df577a0c77055d11d4d9ef9dd9f3f39c2980508))
* Pagination for List of Users and UserRoles ([815daa9](https://github.com/MongoCamp/mongocamp-server/commit/815daa9e872c93905bb4d4bf1383ca46e876b9f0))

### [0.2.5](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.4...v0.2.5) (2022-02-21)


### Features

* Pagination for Find and Aggregation Routes ([d8b82f4](https://github.com/MongoCamp/mongocamp-server/commit/d8b82f49ae9a6c71ae8fe4b6542a563cd3725315))

### [0.2.4](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.3...v0.2.4) (2022-02-20)

### [0.2.3](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.2...v0.2.3) (2022-02-20)

### [0.2.2](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.1...v0.2.2) (2022-02-20)

### [0.2.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.2.0...v0.2.1) (2022-02-20)


### Maintenance

* **Git:** Fix defected git files ([6193fb1](https://github.com/MongoCamp/mongocamp-server/commit/6193fb1809139b56db02ac1ee978a4d5735d59e5))

## [0.2.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.1.0...v0.2.0) (2022-02-20)


### Bug Fixes

* Http-Methode PATCH for Updates ([60e4be6](https://github.com/MongoCamp/mongocamp-server/commit/60e4be65186ef64d09848fb1fb9f3612936e8009))
* updateUser Fields in mongodb ([0c5d9dc](https://github.com/MongoCamp/mongocamp-server/commit/0c5d9dc1f5d42dcb7d7a6370d118bd175a03706c))


### Code Refactoring

* **auth:** UserName to UserId renamed ([c62d0cc](https://github.com/MongoCamp/mongocamp-server/commit/c62d0cc166cb80fd3b9d9432a93a579acaed18dc))
* Rename MongoRest to MongoCamp ([5601e19](https://github.com/MongoCamp/mongocamp-server/commit/5601e196a43576919be17aa573bec9ec9aa7196f))


### Features

* **auth:** Crud-Functions for User ([898a5db](https://github.com/MongoCamp/mongocamp-server/commit/898a5db9991ee20ff9dee304bbda6bf5bcc77b8e))
* **chore:** Git ([019d055](https://github.com/MongoCamp/mongocamp-server/commit/019d055c92be95f12c64a6407c69208cb535892b))
* Crud-Functions for UserRole ([628709a](https://github.com/MongoCamp/mongocamp-server/commit/628709a9fdc563643c3557314740413a14b9c112))
* **Documentation:** Start ([ea268b3](https://github.com/MongoCamp/mongocamp-server/commit/ea268b30ff2fe10997703b93ff1d53908d098fd5))
* **Documentation:** Start ([8bb0085](https://github.com/MongoCamp/mongocamp-server/commit/8bb00857f1fbdaccde7d95f56d1f47de0e7579c0))
* MongoAuthHolder with Sample Data on StartUp ([dd86c87](https://github.com/MongoCamp/mongocamp-server/commit/dd86c87aeb9e12fe61b47b08fcc2b731c7cb0a3c))
* Workflow fix ([81d467e](https://github.com/MongoCamp/mongocamp-server/commit/81d467ef5a4bde61fd2690c5f1d8f20bc5deea29))


### Maintenance

* **Config:** Change Docs Serve Port to 5555 ([4f8501b](https://github.com/MongoCamp/mongocamp-server/commit/4f8501b6d9f1472d7fb1847a5da6ff7f071b1245))

## [0.1.0](https://github.com/MongoCamp/mongocamp-server/compare/2da91bf348c241ac31459055b9460898100318a7...v0.1.0) (2022-02-14)


### Bug Fixes

* Build Pipelines ([7d9062e](https://github.com/MongoCamp/mongocamp-server/commit/7d9062ee3e3290a950084ceb8be9a0054f3b9ddb))
* Build Pipelines ([dee52a4](https://github.com/MongoCamp/mongocamp-server/commit/dee52a4154856089d9dc7b99bea72ae4c6fa73cb))
* Build Pipelines ([4944b8e](https://github.com/MongoCamp/mongocamp-server/commit/4944b8ee8b9a809f8d74663bbf53fee8eed1c1d6))
* Build Pipelines ([684b446](https://github.com/MongoCamp/mongocamp-server/commit/684b4462858621a1546101cf5d963bbd6fc61e26))
* Build Pipelines ([0266cfe](https://github.com/MongoCamp/mongocamp-server/commit/0266cfe8dc6dab0d03de0253d24a755d02ef881a))
* **header:** Added Header Interceptor for more Headers ([605acc6](https://github.com/MongoCamp/mongocamp-server/commit/605acc6fd41b79aaf82d26024aa26f6d3d6877f4))


### Code Refactoring

* Rename AuthToken to ApiKey ([ccb26b4](https://github.com/MongoCamp/mongocamp-server/commit/ccb26b41a1726b98ae435301389c640dc94fccd0))


### Features

* **auth:** Implementing Login Methods ([dc04f41](https://github.com/MongoCamp/mongocamp-server/commit/dc04f415b2fae99440029c95c45198a6b33ebb18))
* **auth:** Implementing Login Methods ([2b4172d](https://github.com/MongoCamp/mongocamp-server/commit/2b4172de1eb3617a7e5c403fd369edaab735ac2e))
* **auth:** Implementing StaticAuthHolder ([45525c0](https://github.com/MongoCamp/mongocamp-server/commit/45525c00bb3ba4adf4dc87585b24ab75482118dc))
* Build Docker Container ([fd681af](https://github.com/MongoCamp/mongocamp-server/commit/fd681af293f08b686d08d1c07445b3801f42993f))
* **Documentation:** Start ([89e5bd3](https://github.com/MongoCamp/mongocamp-server/commit/89e5bd334c446467b9a111aaf896ee14e32fb0a0))
* Initial Project Setup ([2da91bf](https://github.com/MongoCamp/mongocamp-server/commit/2da91bf348c241ac31459055b9460898100318a7))
* Securing Routes ([4c761dc](https://github.com/MongoCamp/mongocamp-server/commit/4c761dcade34d7db964f2d7d97caab0daad01438))
* Workflow fix ([6897ec7](https://github.com/MongoCamp/mongocamp-server/commit/6897ec7b12a3f607bbf6fb2de877580ad4c421e2))

