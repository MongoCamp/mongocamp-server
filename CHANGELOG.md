### [0.12.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.12.0...v0.12.1) (2022-05-02)


### Bug Fixes

* fixed empty role name or userId to add ([89b6ee1](https://github.com/MongoCamp/mongocamp-server/commit/89b6ee1efdb67468da6bab9a43ed8c7f9b0b5d91))
* Type and some other small errors at schema generation ([9607387](https://github.com/MongoCamp/mongocamp-server/commit/96073870f27ad777d92d0d74cf5aa908dc5b0b0a))

## [0.12.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.11.0...v0.12.0) (2022-04-29)


### Features

* Analyze Collection ([3348797](https://github.com/MongoCamp/mongocamp-server/commit/334879732b126b6cd14a6a466fd84b2401e128a8))
* Generate JsonSchema for Collection ([1491bc8](https://github.com/MongoCamp/mongocamp-server/commit/1491bc8df6b20aa21bf7da69f40109ac3b32e599))


### Maintenance

* DependencyUpdates joda-time, jwt-scala ([e79cea1](https://github.com/MongoCamp/mongocamp-server/commit/e79cea12abf7ce5402cd96359c79bfa1cb23846b))
* DependencyUpdates sfxcode, sttClient, embed.mongo ([3be25aa](https://github.com/MongoCamp/mongocamp-server/commit/3be25aa087b5fb736d294706a29cf267b8d66a11))
* DependencyUpdates sfxcode, sttClient, embed.mongo ([35aa145](https://github.com/MongoCamp/mongocamp-server/commit/35aa145564ad5cf3984a988e6cddd9579c9333b8))


### Reverts

* CollectionSuite reverted ([80cb7fb](https://github.com/MongoCamp/mongocamp-server/commit/80cb7fb78f79c3ca6baf806b771ed8ba934ec206))
* DatabaseSuite reverted ([8db9e89](https://github.com/MongoCamp/mongocamp-server/commit/8db9e891990d6c602450693437510fd497235781))

## [0.11.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.10.1...v0.11.0) (2022-03-23)


### Bug Fixes

* ChangeLog Generation Error ([e584c1f](https://github.com/MongoCamp/mongocamp-server/commit/e584c1ff6b6e1d07f7deb7b5c9ead694769dcf4b))
* collection status from other database collection ([fc661ce](https://github.com/MongoCamp/mongocamp-server/commit/fc661ceb6dc16ba1c299fd3af2d5f1060353cace))


### Code Refactoring

* fix test for BucketSuite ([79f7ed0](https://github.com/MongoCamp/mongocamp-server/commit/79f7ed008144c93d83840c576415b7490f7e9883))
* fix test for BucketSuite ([b3dddff](https://github.com/MongoCamp/mongocamp-server/commit/b3dddff0d8940c777fb55d4c722a66c2f05baddf))
* Move from QuadStingray/mongocamp to  mongocamp/mongocamp-server ([8c5e81d](https://github.com/MongoCamp/mongocamp-server/commit/8c5e81df0244c26d07ea51e0b3753a186c9649d5))

### [0.10.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.10.0...v0.10.1) (2022-03-21)


### Features

* **Cors:** Access Control Expose Headers ([673191c](https://github.com/MongoCamp/mongocamp-server/commit/673191c720189e8a8ac8213b6313a08903630023))

## [0.10.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.9.1...v0.10.0) (2022-03-21)


### Bug Fixes

* Date in Request Logging ([f85f274](https://github.com/MongoCamp/mongocamp-server/commit/f85f2744fcbfa10d790f77daab3214c3a798fd07)), closes [Issue#19](https://github.com/MongoCamp/Issue/issues/19)


### Features

* **auth:** Added Route to check isAuthenticated ([3d44283](https://github.com/MongoCamp/mongocamp-server/commit/3d442835eacd27c19cd85f3aa4b24f2cfb9fbd6e))
* **auth:** Route to check Authenticated ([0cba32e](https://github.com/MongoCamp/mongocamp-server/commit/0cba32e637d8bfee82675c7a4fbe67cae57381ba)), closes [Issue#20](https://github.com/MongoCamp/Issue/issues/20)
* Dynamic Plugin Loading at StartUp ([f09a63d](https://github.com/MongoCamp/mongocamp-server/commit/f09a63d3b0c7dde86a819f70a76400c57a1a7138)), closes [Issue#21](https://github.com/MongoCamp/Issue/issues/21) [Issue#9](https://github.com/MongoCamp/Issue/issues/9)

### [0.9.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.9.0...v0.9.1) (2022-03-19)


### Bug Fixes

* Add id to FileInformation and rename method on buckets for valid openapi specification ([9a986b0](https://github.com/MongoCamp/mongocamp-server/commit/9a986b0d3516c6e00e5c2c5f9d886e96684d146e))
* **file:** Fix Delete for GridFsFileAdapter ([67cedd9](https://github.com/MongoCamp/mongocamp-server/commit/67cedd98616bd63992df53ae40d6e2e429b1cff5))


### Features

* Collections by Database ([ce5aa8d](https://github.com/MongoCamp/mongocamp-server/commit/ce5aa8dcf4d4cfb6b449aa53d10ecf00ab512092))
* **file:** FileInformation instead of Map[String, Any] as response ([d57935b](https://github.com/MongoCamp/mongocamp-server/commit/d57935bc3c562ef85519be0780d6e31069dafb91))
* **file:** Use other FilePlugin ([b32705d](https://github.com/MongoCamp/mongocamp-server/commit/b32705d976fccf7a529cad8e01678d0a0d60ae4b)), closes [Issue#13](https://github.com/MongoCamp/Issue/issues/13)
* Load Routes and File Adapter by Reflection ([086061e](https://github.com/MongoCamp/mongocamp-server/commit/086061e65fd2f1a28112d990692170a49bb95a03))
* Update FileInformation Request ([1f49296](https://github.com/MongoCamp/mongocamp-server/commit/1f492960989e65faf49298777baea1f13a52ba88))

## [0.9.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.8.0...v0.9.0) (2022-03-17)


### Bug Fixes

* Delete wrong Collection from GridFsFileAdapter ([1e66d88](https://github.com/MongoCamp/mongocamp-server/commit/1e66d88605b761d553cd51156528b0109ce56d36))


### Code Refactoring

* Extract CollectionBaseRoute from BaseRoute ([fbaadf5](https://github.com/MongoCamp/mongocamp-server/commit/fbaadf5c90f33ec56799bbb96c468b76fa44c615))
* Moved Configuration for BucketSuffixes to BucketInformation.scala ([3a6696e](https://github.com/MongoCamp/mongocamp-server/commit/3a6696e74c332accf098183021f4f7984e1aae73))
* Rename Api Documents to Document ([2acbc5f](https://github.com/MongoCamp/mongocamp-server/commit/2acbc5fe298a4927fcb7f5314ad3db885ad557d8))
* Rename method to convertFields ([24313f9](https://github.com/MongoCamp/mongocamp-server/commit/24313f91d716b224f8b3fa48cc60b779a62b4f2d))


### Features

* Bucket Api to List, Get and Delete Buckets ([c934894](https://github.com/MongoCamp/mongocamp-server/commit/c9348943b0f1c822016503d551a21dd97b371c8b))
* Delete File by Adapter Holder ([9a0b4b4](https://github.com/MongoCamp/mongocamp-server/commit/9a0b4b40c8513ae577665fdd6f203f551d34794f))
* **file:** Implement File Download ([1a5940a](https://github.com/MongoCamp/mongocamp-server/commit/1a5940a4dd48517fa40f94b35604a062ac1d9868))
* **file:** Implement File Upload ([829882e](https://github.com/MongoCamp/mongocamp-server/commit/829882ea551238fbe494f4043490775958cfc092))
* **file:** Implement Routes for Files ([f4a76c9](https://github.com/MongoCamp/mongocamp-server/commit/f4a76c978438d2768568c97884bd5e5d0dda703f))

## [0.8.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.7.1...v0.8.0) (2022-03-14)


### Bug Fixes

* convert bson ids in one method ([04f2366](https://github.com/MongoCamp/mongocamp-server/commit/04f2366c1a41554d1693133d2c4f8090348a0963))
* filter routes with _id field needs to be converted ([a11e8bd](https://github.com/MongoCamp/mongocamp-server/commit/a11e8bd96445aba4e329d2393614eed94cc1c6cd))
* update many routs needs conversion to OperationMap ([0777e0a](https://github.com/MongoCamp/mongocamp-server/commit/0777e0ae58528ec348fbd897e470103dbcb3a12b))


### Code Refactoring

* min port for mongodb and http server ([670eee3](https://github.com/MongoCamp/mongocamp-server/commit/670eee33e76070d0c4a19c0010a69d11bc89855a))


### Features

* more auth methods ([386d5c7](https://github.com/MongoCamp/mongocamp-server/commit/386d5c7c05ae3709e09cbad61df51b286724fce2))

### [0.7.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.7.0...v0.7.1) (2022-03-10)


### Code Refactoring

* Fixed Typos and `rename` functions ([8d531ee](https://github.com/MongoCamp/mongocamp-server/commit/8d531eea5751829363e28a75cb066476535ecfb7))


### Features

* IndexOptions now optional in requests ([39a594e](https://github.com/MongoCamp/mongocamp-server/commit/39a594ef20eeb350e1e93724214c5dc9a3acb431))

## [0.7.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.3...v0.7.0) (2022-03-09)


### Features

* **Endpoint:** Collection Fields ([213c92e](https://github.com/MongoCamp/mongocamp-server/commit/213c92ef66430d84b56eaac76d152a44862cb119))
* **Endpoint:** Collection Fields ([c269ae2](https://github.com/MongoCamp/mongocamp-server/commit/c269ae233a3bc2397a7163d9194e4c79d666326c))
* Reload UserInfos for Refresh or Profile Route ([074b81f](https://github.com/MongoCamp/mongocamp-server/commit/074b81fdafc78040943cdabae2eb3624b3dec6de))

### [0.6.3](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.2...v0.6.3) (2022-03-08)


### Bug Fixes

* Disabled logger for MongoCampException ([3aac45d](https://github.com/MongoCamp/mongocamp-server/commit/3aac45d1cc906e36a4f2e5f9a5ac5e4bd7e09cdf))

### [0.6.2](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.1...v0.6.2) (2022-03-07)

### [0.6.1](https://github.com/MongoCamp/mongocamp-server/compare/v0.6.0...v0.6.1) (2022-03-07)

## [0.6.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.5.0...v0.6.0) (2022-03-07)


### Features

* Migration to Tapir 0.20.1 ([072012b](https://github.com/MongoCamp/mongocamp-server/commit/072012bfa73a166d70c0d7c944935761a68ad430))

## [0.5.0](https://github.com/MongoCamp/mongocamp-server/compare/v0.3.4...v0.5.0) (2022-03-03)


### Code Refactoring

* add document routes for mor RESTlike route naming ([1831ac5](https://github.com/MongoCamp/mongocamp-server/commit/1831ac51e392a2145d0e4aa50b59087bd70dcf02))
* add document routes for mor RESTlike route naming ([3c30a41](https://github.com/MongoCamp/mongocamp-server/commit/3c30a41b2cce1f427b65981d627aef65765e9680))
* Extract Collections to own API Routes ([3300414](https://github.com/MongoCamp/mongocamp-server/commit/3300414c02916eb943242e2a9579a1404ff57a70))
* Extract Routes for Database Routes ([446daa1](https://github.com/MongoCamp/mongocamp-server/commit/446daa127ecac2f9a326c8263ebf5b81ee0472a5))
* Removed "READ" Routes and spitted to DocumentRoutes and CollectionRoutes ([fecde9e](https://github.com/MongoCamp/mongocamp-server/commit/fecde9e5bf4203a8fc3de2ea779f36c48dd2d90c))
* rename userRole to role ([63678c3](https://github.com/MongoCamp/mongocamp-server/commit/63678c3ae571ba86b89872ecfbf07b8719ed4165))
* rename vals to endpoints ([cacfbec](https://github.com/MongoCamp/mongocamp-server/commit/cacfbeca590c348242cbf48a03388a186188846b))
* rename vals to endpoints ([bae1baf](https://github.com/MongoCamp/mongocamp-server/commit/bae1baf2e0d62080f7ce45c426c06ad421d905c1))
* **test:** simplify test request execution ([910a0be](https://github.com/MongoCamp/mongocamp-server/commit/910a0bed7a2d8a0b69bf8f030584b0484aa25733))


### Features

* Added Filter to get Route all documents ([6e40be2](https://github.com/MongoCamp/mongocamp-server/commit/6e40be23d90c2aa08e88cc2ebeea8bcbaf9ab8b9))
* cache tokens to database ([3a87e08](https://github.com/MongoCamp/mongocamp-server/commit/3a87e084b1b1cbfb1f70d0cb46153bd3f257b9b9))
* Grants for Buckets and Collections ([9fb9eca](https://github.com/MongoCamp/mongocamp-server/commit/9fb9eca217c941f2c4b06762609a78eb0d7d3a75))


### Maintenance

* DependencyUpdate "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.4.3" % Test ([664353e](https://github.com/MongoCamp/mongocamp-server/commit/664353e288c9109482bfe41103e30b8743166c42))

### [0.3.4](https://github.com/MongoCamp/mongocamp-server/compare/2da91bf348c241ac31459055b9460898100318a7...v0.3.4) (2022-02-26)


### Bug Fixes

* Build Pipelines ([7d9062e](https://github.com/MongoCamp/mongocamp-server/commit/7d9062ee3e3290a950084ceb8be9a0054f3b9ddb))
* Build Pipelines ([dee52a4](https://github.com/MongoCamp/mongocamp-server/commit/dee52a4154856089d9dc7b99bea72ae4c6fa73cb))
* Build Pipelines ([4944b8e](https://github.com/MongoCamp/mongocamp-server/commit/4944b8ee8b9a809f8d74663bbf53fee8eed1c1d6))
* Build Pipelines ([684b446](https://github.com/MongoCamp/mongocamp-server/commit/684b4462858621a1546101cf5d963bbd6fc61e26))
* Build Pipelines ([0266cfe](https://github.com/MongoCamp/mongocamp-server/commit/0266cfe8dc6dab0d03de0253d24a755d02ef881a))
* **header:** Added Header Interceptor for more Headers ([605acc6](https://github.com/MongoCamp/mongocamp-server/commit/605acc6fd41b79aaf82d26024aa26f6d3d6877f4))
* Http-Methode PATCH for Updates ([60e4be6](https://github.com/MongoCamp/mongocamp-server/commit/60e4be65186ef64d09848fb1fb9f3612936e8009))
* updateUser Fields in mongodb ([0c5d9dc](https://github.com/MongoCamp/mongocamp-server/commit/0c5d9dc1f5d42dcb7d7a6370d118bd175a03706c))


### Code Refactoring

* **auth:** UserName to UserId renamed ([c62d0cc](https://github.com/MongoCamp/mongocamp-server/commit/c62d0cc166cb80fd3b9d9432a93a579acaed18dc))
* **Mongo:** FindAll Route ([16f8184](https://github.com/MongoCamp/mongocamp-server/commit/16f8184b5970d8fd4e98ff514f816afd80ab8aa6))
* Rename AuthToken to ApiKey ([ccb26b4](https://github.com/MongoCamp/mongocamp-server/commit/ccb26b41a1726b98ae435301389c640dc94fccd0))
* Rename MongoRest to MongoCamp ([5601e19](https://github.com/MongoCamp/mongocamp-server/commit/5601e196a43576919be17aa573bec9ec9aa7196f))
* Switch to default Port 8080 ([e716073](https://github.com/MongoCamp/mongocamp-server/commit/e716073da955fa657480b873e9c85902cfb5fcb4))


### Features

* **auth:** Crud-Functions for User ([898a5db](https://github.com/MongoCamp/mongocamp-server/commit/898a5db9991ee20ff9dee304bbda6bf5bcc77b8e))
* **auth:** Implementing Login Methods ([dc04f41](https://github.com/MongoCamp/mongocamp-server/commit/dc04f415b2fae99440029c95c45198a6b33ebb18))
* **auth:** Implementing Login Methods ([2b4172d](https://github.com/MongoCamp/mongocamp-server/commit/2b4172de1eb3617a7e5c403fd369edaab735ac2e))
* **auth:** Implementing StaticAuthHolder ([45525c0](https://github.com/MongoCamp/mongocamp-server/commit/45525c00bb3ba4adf4dc87585b24ab75482118dc))
* Build Docker Container ([fd681af](https://github.com/MongoCamp/mongocamp-server/commit/fd681af293f08b686d08d1c07445b3801f42993f))
* **chore:** Git ([019d055](https://github.com/MongoCamp/mongocamp-server/commit/019d055c92be95f12c64a6407c69208cb535892b))
* Crud-Functions for UserRole ([628709a](https://github.com/MongoCamp/mongocamp-server/commit/628709a9fdc563643c3557314740413a14b9c112))
* **Documentation:** Start ([ea268b3](https://github.com/MongoCamp/mongocamp-server/commit/ea268b30ff2fe10997703b93ff1d53908d098fd5))
* **Documentation:** Start ([8bb0085](https://github.com/MongoCamp/mongocamp-server/commit/8bb00857f1fbdaccde7d95f56d1f47de0e7579c0))
* **Documentation:** Start ([89e5bd3](https://github.com/MongoCamp/mongocamp-server/commit/89e5bd334c446467b9a111aaf896ee14e32fb0a0))
* Initial Project Setup ([2da91bf](https://github.com/MongoCamp/mongocamp-server/commit/2da91bf348c241ac31459055b9460898100318a7))
* MongoAuthHolder with Sample Data on StartUp ([dd86c87](https://github.com/MongoCamp/mongocamp-server/commit/dd86c87aeb9e12fe61b47b08fcc2b731c7cb0a3c))
* **Mongo:** FindAll Route ([3dc3a64](https://github.com/MongoCamp/mongocamp-server/commit/3dc3a64b38384278e2c6f51778ea9ec4e2dd1184))
* **Mongo:** FindAll Route ([bdc5ef0](https://github.com/MongoCamp/mongocamp-server/commit/bdc5ef01acdf97fb47676fb53bfc161269ed2b84))
* Pagination for Find and Aggregation Routes ([1df577a](https://github.com/MongoCamp/mongocamp-server/commit/1df577a0c77055d11d4d9ef9dd9f3f39c2980508))
* Pagination for Find and Aggregation Routes ([d8b82f4](https://github.com/MongoCamp/mongocamp-server/commit/d8b82f49ae9a6c71ae8fe4b6542a563cd3725315))
* Pagination for List of Users and UserRoles ([815daa9](https://github.com/MongoCamp/mongocamp-server/commit/815daa9e872c93905bb4d4bf1383ca46e876b9f0))
* RequestLogging ([cec1895](https://github.com/MongoCamp/mongocamp-server/commit/cec189546bb97ddd303ea0a5c85ddcc088fb99a3))
* Securing Routes ([4c761dc](https://github.com/MongoCamp/mongocamp-server/commit/4c761dcade34d7db964f2d7d97caab0daad01438))
* Workflow fix ([81d467e](https://github.com/MongoCamp/mongocamp-server/commit/81d467ef5a4bde61fd2690c5f1d8f20bc5deea29))
* Workflow fix ([6897ec7](https://github.com/MongoCamp/mongocamp-server/commit/6897ec7b12a3f607bbf6fb2de877580ad4c421e2))


### Maintenance

* **Config:** Change Docs Serve Port to 5555 ([4f8501b](https://github.com/MongoCamp/mongocamp-server/commit/4f8501b6d9f1472d7fb1847a5da6ff7f071b1245))
* **Git:** Fix defected git files ([6193fb1](https://github.com/MongoCamp/mongocamp-server/commit/6193fb1809139b56db02ac1ee978a4d5735d59e5))

