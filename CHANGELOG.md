## [v1.5.7] - 2023-10-23
### :bug: Bug Fixes
- [`4fc82cd`](https://github.com/MongoCamp/mongocamp-server/commit/4fc82cdad7479cebc69b09e8c3ce4a2bfd477276) - change deployment for docs *(commit by [@QuadStingray](https://github.com/QuadStingray))*
- [`661553a`](https://github.com/MongoCamp/mongocamp-server/commit/661553ab9e91f013d0eda596d84ab08000720591) - test-server dependency not published *(commit by [@QuadStingray](https://github.com/QuadStingray))*


## [v1.5.6] - 2023-10-21
### :bug: Bug Fixes
- [`5efb246`](https://github.com/MongoCamp/mongocamp-server/commit/5efb246b1c8a93d1c13019124cc2d188aee1ab73) - startup on native image *(commit by [@QuadStingray](https://github.com/QuadStingray))*
- [`489f2af`](https://github.com/MongoCamp/mongocamp-server/commit/489f2af2f697e5cc54790c9f156a2d973992cd84) - reflection service needs to many time for scan *(commit by [@QuadStingray](https://github.com/QuadStingray))*

### :wrench: Chores
- [`aebe10f`](https://github.com/MongoCamp/mongocamp-server/commit/aebe10fdaf6641eeef681d20b3f6c4e16682755c) - dependency updates *(commit by [@QuadStingray](https://github.com/QuadStingray))*
- [`7f2effe`](https://github.com/MongoCamp/mongocamp-server/commit/7f2effe387d898f1c4bf38adf65af5687a622cf0) - changed to stable version of mongodb-driver *(commit by [@QuadStingray](https://github.com/QuadStingray))*


## [v1.5.5] - 2023-10-16
### :bug: Bug Fixes
- [`61fe23f`](https://github.com/MongoCamp/mongocamp-server/commit/61fe23f08cfae04ba9500344a51ebdb61353e2e1) - **docker**: jars not precached *(commit by [@QuadStingray](https://github.com/QuadStingray))*


## [v1.5.4] - 2023-10-14
### :sparkles: New Features
- [`6528932`](https://github.com/MongoCamp/mongocamp-server/commit/652893262cd913e9b910281513a3ab14e407a999) - error codes for mongocamp-cli
- [`123673d`](https://github.com/MongoCamp/mongocamp-server/commit/123673d13e8a2beeda9021dcf5a55c59190fb1b1) - Build Process for Docker
- [`dbd042a`](https://github.com/MongoCamp/mongocamp-server/commit/dbd042a538e737b192f60b6f9b5abfd6a51e7be8) - replace akka with pekko
- [`31607cb`](https://github.com/MongoCamp/mongocamp-server/commit/31607cbf81e1fe8e56cd263ad66a80fae6678919) - replace akka with pekko

### :bug: Bug Fixes
- [`995e216`](https://github.com/MongoCamp/mongocamp-server/commit/995e216930d9f15761add996fc809328b01b0bed) - searching for build failure in graalvm
- [`2c90184`](https://github.com/MongoCamp/mongocamp-server/commit/2c901845b43ec3b097f943350da8a20ffd6fd18c) - searching for build failure in graalvm
- [`e324098`](https://github.com/MongoCamp/mongocamp-server/commit/e324098d7fe6c2acc63ffcb44fa417bcda0753bf) - searching for build failure in graalvm
- [`3616723`](https://github.com/MongoCamp/mongocamp-server/commit/36167236370bbaec1bd1c3590d8e40034607f8ca) - missing import
- [`c70933f`](https://github.com/MongoCamp/mongocamp-server/commit/c70933f68c0c77a758fefef207744a9227c5d265) - missing import
- [`cd14b09`](https://github.com/MongoCamp/mongocamp-server/commit/cd14b0973fb10125138ec9889a8326acb60439d7) - missing import
- [`3ed52e6`](https://github.com/MongoCamp/mongocamp-server/commit/3ed52e606107c3a8c447c753a460748d912b0bec) - fix mongodb test server startup
- [`4bb1e57`](https://github.com/MongoCamp/mongocamp-server/commit/4bb1e57b72a49e13f4d359206876311c1e660550) - fix mongodb test server startup
- [`ad712a4`](https://github.com/MongoCamp/mongocamp-server/commit/ad712a4b4a2c31bd4437dc6d2844c30a6f8a8b3b) - fix Dockerfile
- [`a445b04`](https://github.com/MongoCamp/mongocamp-server/commit/a445b04ca89bce70532f96882a7fd77520df486a) - ReflectionService class initialization failed
- [`795511f`](https://github.com/MongoCamp/mongocamp-server/commit/795511f1ef34ddda43c3bd5530041c3ba380ba64) - ReflectionService class search fixed
- [`b4fb724`](https://github.com/MongoCamp/mongocamp-server/commit/b4fb7244aff3bdcd22cb11cdba008bcc367c35e4) - containername *(commit by [@QuadStingray](https://github.com/QuadStingray))*

### :recycle: Refactors
- [`21ae938`](https://github.com/MongoCamp/mongocamp-server/commit/21ae93805c9bf4fc9d291c82343c8b94fbe1b159) - changed build settings

### :white_check_mark: Tests
- [`6a022bf`](https://github.com/MongoCamp/mongocamp-server/commit/6a022bf430c93ee5d2c9be4a441dd123239b5b80) - added mongodb 7 to tests
- [`df87a6d`](https://github.com/MongoCamp/mongocamp-server/commit/df87a6d4aba612bca20b1430e63cccd7568c95db) - build container for test
- [`d52f146`](https://github.com/MongoCamp/mongocamp-server/commit/d52f14645ebbdf6ce3ac088a2d97caca527a3fcb) - test

### :wrench: Chores
- [`1161c49`](https://github.com/MongoCamp/mongocamp-server/commit/1161c498eefaf037d3c80798bca5585b55fc48bd) - dependency updates on many dependencies
- [`ae4369c`](https://github.com/MongoCamp/mongocamp-server/commit/ae4369c6f145e1700a63743006148d20a50b1ed6) - update tapir to 1.8.0

## [1.5.2]() (2023-07-22)

## [1.5.1]() (2023-07-22)


### Code Refactoring

* Split to multi project to prepare build native image with own cli ([fbf89a8](https://github.com/MongoCamp/mongocamp-server/commit/fbf89a82634e4e0d7c61c9433c83c51b5befe4fb))

### Features

* building native image ([9030678](https://github.com/MongoCamp/mongocamp-server/commit/9030678abf4049f03ebb9608c5c8686be43b64eb))* cli for generating native images and start server ([3c41140](https://github.com/MongoCamp/mongocamp-server/commit/3c41140d31aa3c01d4329a2dce382dbb0e81deb3))

### Maintenance

* Found 11 dependency updates ([fbec087](https://github.com/MongoCamp/mongocamp-server/commit/fbec087cde522fffe5eb87374509783943a41dba))
## [1.4.4]() (2023-06-07)


### Maintenance

* **dependencies:** 8 dependency updates for mongocamp-server ([6271721](https://github.com/MongoCamp/mongocamp-server/commit/6271721ec0675b75f5798ef13c73e9cc34d50dbf))
## [1.4.3]() (2023-05-15)


### Maintenance

* **dependencies:** Fix empty aggregation response with 3 dependency updates for mongocamp-server ([ea129e0](https://github.com/MongoCamp/mongocamp-server/commit/ea129e070c016377db3de5f94f3ab7c5b7a77fc6))
## [1.4.2]() (2023-05-05)


### Bug Fixes

* **converter:** Fix Issue with Not Map Sublist in Body ([ca59285](https://github.com/MongoCamp/mongocamp-server/commit/ca59285ee43cda8da27c9f0fda3eba2dd55cbe52)), closes [#45](https://github.com/MongoCamp/mongocamp-server/issues/45)* extracted metrics to own plugin [mongocamp-micrometer-plugin](https://github.com/MongoCamp/mongocamp-micrometer-plugin) ([531f4af](https://github.com/MongoCamp/mongocamp-server/commit/531f4af4134442cfc566e0115d59dba372657ecc))* test file name ([b77c178](https://github.com/MongoCamp/mongocamp-server/commit/b77c178faac7181cb2ac4ced12a5a7a3cd0e9c57))* **Tests:** fix configuration size ([6e788a9](https://github.com/MongoCamp/mongocamp-server/commit/6e788a9da2bb19146580efc45fcdfec49dfef9b3))

### Features

* added remove configuration for deleting ([5d244df](https://github.com/MongoCamp/mongocamp-server/commit/5d244dfd50fa6afbd08a6db812ae8c528852c0b5))* Changed RequestId to String ([e2a6137](https://github.com/MongoCamp/mongocamp-server/commit/e2a6137a8528df9d82ac07c4faf8fbfade79a6c6))* extracted metrics to own plugin [mongocamp-requestlogging-plugin](https://github.com/MongoCamp/mongocamp-requestlogging-plugin) ([8bd8318](https://github.com/MongoCamp/mongocamp-server/commit/8bd8318e5c97faaa61461c7c1d166cfd8901227c))* extracted to [test package](https://github.com/MongoCamp/mongocamp-test-server) for usage in plugins ([c715864](https://github.com/MongoCamp/mongocamp-server/commit/c71586401079f9923800983ae376e93e7d0136a6))* RequestId added to Request for using and output as header ([41989fd](https://github.com/MongoCamp/mongocamp-server/commit/41989fd95e4c132b288b224ecee66950ea4359ac))

### Maintenance

* **dependencies:** 4 dependency updates for mongocamp-server ([9c3a861](https://github.com/MongoCamp/mongocamp-server/commit/9c3a861f13316dcba955d452d897e8a7cd5aab01))* **dependencies:** update to mongo-driver 2.6.2 ([29ea28d](https://github.com/MongoCamp/mongocamp-server/commit/29ea28df21c41ddf44c0b2162ff0b9ab8ca18633))* **dependencies:** update to tapir 1.3.0 ([c6d4ac5](https://github.com/MongoCamp/mongocamp-server/commit/c6d4ac537301063f2d41457d5a5363cb9cf6eab6))* **dependencies:** update vitepress and npm deps ([f0eaeb6](https://github.com/MongoCamp/mongocamp-server/commit/f0eaeb6388258f7a9f94f275d5215f6e23dc2437))
## [1.4.1]() (2023-04-13)


### Bug Fixes

* date converter for update requests ([1d3754b](https://github.com/MongoCamp/mongocamp-server/commit/1d3754be61200cf68fff05cf4e54c68825561f68))* **jobs:** activate JobPlugin needs to clear jobs if one job is added from other plugins ([4fcd545](https://github.com/MongoCamp/mongocamp-server/commit/4fcd545af9665039e4da34b4deb1cd3919d6f74c))

### Code Refactoring

* disabled and removed github packages release ([ee6f2c4](https://github.com/MongoCamp/mongocamp-server/commit/ee6f2c444af01577bc07c1882160298b17c43487))* extracted metrics to own plugin [mongocamp-micrometer-plugin](https://github.com/MongoCamp/mongocamp-micrometer-plugin) ([a6a2eea](https://github.com/MongoCamp/mongocamp-server/commit/a6a2eead0616b69bb59c6c2d3a71db21fecd57c1))* extracted metrics to own plugin [mongocamp-micrometer-plugin](https://github.com/MongoCamp/mongocamp-micrometer-plugin) ([c20d227](https://github.com/MongoCamp/mongocamp-server/commit/c20d2276a3efee3c20692c84134dbbd43e62dc9c))
## [1.4.0]() (2023-04-04)


### ⚠ BREAKING CHANGES

* **document:** Filter Query, Sort and Paging Parameter has changed from Json String to Lucene Style

### Features

* **document:** Use Lucene Query for Get List of Documents ([45d6dce](https://github.com/MongoCamp/mongocamp-server/commit/45d6dce0c6bb73d912f26d4d8493f5393b8509bb))

### Maintenance

* **dependencies:** dependency updates ([8bdaea9](https://github.com/MongoCamp/mongocamp-server/commit/8bdaea9f5402955ca61582941942da5e80f4fd99))
## [1.3.4]() (2023-03-21)


### Bug Fixes

* gitpod scala ([c35e2ca](https://github.com/MongoCamp/mongocamp-server/commit/c35e2ca0a76795c894c065cd1d206c7aa6489bda))

### Code Refactoring

* **config:** Constants for Option and List Types ([37eb24b](https://github.com/MongoCamp/mongocamp-server/commit/37eb24b402e04be91cadd2b2bbee7d13eefc582d))* **metrics:** Micrometer MongoDb Package ([8a1bc92](https://github.com/MongoCamp/mongocamp-server/commit/8a1bc9216de9a9a9bd10e6b3aedca6e1f9f967a4))* **metrics:** Micrometer MongoDb Package ([c69a6e7](https://github.com/MongoCamp/mongocamp-server/commit/c69a6e707639913027f1510465de79bab7d1df9a))* **metrics:** renaming config keys for Logging Plugin ([a145938](https://github.com/MongoCamp/mongocamp-server/commit/a145938e0f0b244d5b8ece39da6facc0a1a320be))* Split ApplicationStatusRoutes to ConfigurationRoutes and  MetricsRoutes ([dc472d4](https://github.com/MongoCamp/mongocamp-server/commit/dc472d41053d911bf0f520da6fbafe764c4a9ecf))

### Features

* **metrics:** Extracted metrics to own packages ([ee7d37b](https://github.com/MongoCamp/mongocamp-server/commit/ee7d37b39471f6a8b0eb6113a6fc74ae5f0f7a65))* **metrics:** MicroMeter Plugin for MongoDb Metrics ([e651845](https://github.com/MongoCamp/mongocamp-server/commit/e651845904e0c0fb60ef6c2a6b3477e7aadea8d5))* More details in Metrics Store for mongodb ([85b0971](https://github.com/MongoCamp/mongocamp-server/commit/85b0971608c6cab2cf0957591d732fffb40e32a7))

### Maintenance

* **dependencies:** Found 13 dependency updates for mongocamp-server ([776ff9f](https://github.com/MongoCamp/mongocamp-server/commit/776ff9f8e6540e93f9ececd3a551b15c5e2354d6))* DependencyUpdates ([2806e02](https://github.com/MongoCamp/mongocamp-server/commit/2806e0266dadcc03d1c77238311222e85c53ee3f))* Found 17 dependency updates for mongocamp-server ([cabe85e](https://github.com/MongoCamp/mongocamp-server/commit/cabe85e0906cb68e9905eec582d576ea2e8f3c1e))* Found 17 dependency updates for mongocamp-server ([65d7c11](https://github.com/MongoCamp/mongocamp-server/commit/65d7c11f8e3d12e598399e534ed857f4ae9b1008))
## [1.3.3]() (2023-01-30)


### Bug Fixes

* create parent dirs when plugin directory not exists ([76d1387](https://github.com/MongoCamp/mongocamp-server/commit/76d13872148cbf5ce9551f2b9de0958bb938ee33))* fix splitter for checking auth is needed ([87d6b3e](https://github.com/MongoCamp/mongocamp-server/commit/87d6b3e484f05b1b489cc188e6c4a9e930e85ad4))* fix splitter for checking auth is needed ([02d562d](https://github.com/MongoCamp/mongocamp-server/commit/02d562d0a065f8e422d113c57129bd39f84bc179))* Removed import for not longer existing Classes ([1fdb799](https://github.com/MongoCamp/mongocamp-server/commit/1fdb799b30485d4f01533338c0ec713ae874c619))* use `addRepository` instead of withDependency ([2fd129d](https://github.com/MongoCamp/mongocamp-server/commit/2fd129d00702910df97f178de59f66991bed83b5))

### Features

* Coursier as Handler for Plugins and more ([8c74398](https://github.com/MongoCamp/mongocamp-server/commit/8c743984ecc57d125696133e5e01179b8a27200e))* plugin service change to coursier ([6690e38](https://github.com/MongoCamp/mongocamp-server/commit/6690e38158df68c5be314766ed527bcfe80dd680))* Startup fault tolerant by excluding invalid configured maven modules ([bb4cf63](https://github.com/MongoCamp/mongocamp-server/commit/bb4cf63cb212561041a09b2fc5b6c83b55d89d74))

### Maintenance

* **dependencies:** update sttp.client and and tapir ([1d004bc](https://github.com/MongoCamp/mongocamp-server/commit/1d004bc55c59bf204563765eca3d9640c5e1d7f9))
## [1.3.2]() (2023-01-20)


### Bug Fixes

* MongoMetrics Plugins were loaded to late ([1827f51](https://github.com/MongoCamp/mongocamp-server/commit/1827f5160d536d3959b3f2bdb46f39395500dd56))

### Features

* Configurations for micrometer mongodb plugin ([801f52b](https://github.com/MongoCamp/mongocamp-server/commit/801f52b15fb1ce0635807762908cc2c9d17354bf))* Events on Configuration Settings and Changes ([b54a6b9](https://github.com/MongoCamp/mongocamp-server/commit/b54a6b94d6a040df07c63f3b36f6fc1de658d492))* Micrometer as Plugin ([5bf84fb](https://github.com/MongoCamp/mongocamp-server/commit/5bf84fb26b7563db0abbe9407efd10059f49d628))* MongoDb Micrometer Registry ([d14363c](https://github.com/MongoCamp/mongocamp-server/commit/d14363c6cca131df46fca4fa477e1c29cc9f3d55))

### Maintenance

* **dependencies:** Found 9 dependency updates for mongocamp-server ([fae90d4](https://github.com/MongoCamp/mongocamp-server/commit/fae90d4677f5a720a3113c69535ac6d8f014626e))
## [1.3.1]() (2023-01-08)


### Maintenance

* dependency updates plugins ([c595300](https://github.com/MongoCamp/mongocamp-server/commit/c595300baf79a89afbae2805d3e7932eddd5a360))* Many dependencies updated (tapir, sttp-client, mongodb-driver) ([c128d6f](https://github.com/MongoCamp/mongocamp-server/commit/c128d6fe593add7cfe11ec9d4496b324cab7955e))
## [1.3.0]() (2022-10-18)


### Bug Fixes

* Auth Route for update ApiKey only for own user ([568558a](https://github.com/MongoCamp/mongocamp-server/commit/568558a5d83b6a2c693b665ebb790393cb52776f))* check if plugin dir is writeable ([bcf9f12](https://github.com/MongoCamp/mongocamp-server/commit/bcf9f127ad459d75089d5b9575190d03483755e5))

### Code Refactoring

* **config:** refactoring configuration handling ([2c85ef3](https://github.com/MongoCamp/mongocamp-server/commit/2c85ef3adedea7bfab2fa5c3ee8538b63188493e))* Moved ConfigurationManager to ConfigurationService ([3da3d3a](https://github.com/MongoCamp/mongocamp-server/commit/3da3d3a569534c692d5e041a7ae3d548746e1164))* Renamed JsonResult to JsonValue ([a3a6d63](https://github.com/MongoCamp/mongocamp-server/commit/a3a6d638a7fd1cd1e3d13c63b1b9e595d1d50590))* Use same key for Database or Environment loaded Keys ([c3aa688](https://github.com/MongoCamp/mongocamp-server/commit/c3aa68813f22e225326c5bda47d85a4d22851eef))

### Features

* added routes for MongoCampConfiguration ([3fb6ba6](https://github.com/MongoCamp/mongocamp-server/commit/3fb6ba6a9191fd96e51a2797c3abfb7a8cfbc94f))* Caching for Configuration ([108b314](https://github.com/MongoCamp/mongocamp-server/commit/108b314c2c60d1974baef82d1f7efd7cde4dd60f))* Cleanup for Token DB for not working TTL Indices ([ec1c1be](https://github.com/MongoCamp/mongocamp-server/commit/ec1c1bedc5ac969b2bffc8958f43f0cc8eac47f2))* download plugins by url on server startup ([019f7ca](https://github.com/MongoCamp/mongocamp-server/commit/019f7cac9e54e842ca77d7ac4c3be8a3857f986e))* **events:** Added old values to Document Events ([4851bbc](https://github.com/MongoCamp/mongocamp-server/commit/4851bbc24ff3abe366f17eef2a3bed99e69feed7)), closes [#36](https://github.com/MongoCamp/mongocamp-server/issues/36)

### Maintenance

* **dependencies:** Removed unnecessary dependency override for slf4j ([eff6fba](https://github.com/MongoCamp/mongocamp-server/commit/eff6fba68fb9c3ab8dff3950f82cb610e9284607))* **dependencies:** Update 19 dependencies and SBT ([36bb0b9](https://github.com/MongoCamp/mongocamp-server/commit/36bb0b9a520cf20c08739d05595e1ad0f875c5c8))
## [1.2.1]() (2022-07-24)


### Bug Fixes

* changes for events in jobs ([f4e568f](https://github.com/MongoCamp/mongocamp-server/commit/f4e568f7358174d6ec282db9eb8e75bca9a5b758))

### Features

* Events for Jobs Plugin ([5e26d49](https://github.com/MongoCamp/mongocamp-server/commit/5e26d49ca040f04c2821784aa955efdeb15a73eb))* **gitpod:** First Try to code at gitpod ([7e2f9ea](https://github.com/MongoCamp/mongocamp-server/commit/7e2f9ea0de35d52e732b494aabf53d5704ce8ed5))* **jobs:** Moved JobPlugin to  `plugin` ([3a8afb8](https://github.com/MongoCamp/mongocamp-server/commit/3a8afb868708ced8c89d720756e2b1eda64c94cc))
## [1.2.0]() (2022-07-21)


### Bug Fixes

* missing return type ([27e5685](https://github.com/MongoCamp/mongocamp-server/commit/27e568510d75ccc31466054aebef7602023ffe40))

### Features

* Added Quartz scheduling methods ([423b16f](https://github.com/MongoCamp/mongocamp-server/commit/423b16fb557213073856db15691973966f627c97)), closes [#31](https://github.com/MongoCamp/mongocamp-server/issues/31)* Added some more reflection methods ([8e7240c](https://github.com/MongoCamp/mongocamp-server/commit/8e7240c54f0c83b58894bfbddb9ffcac1fa793cb)), closes [#31](https://github.com/MongoCamp/mongocamp-server/issues/31)* Routes for Jobs ([340c7a8](https://github.com/MongoCamp/mongocamp-server/commit/340c7a828368bdd2ceb32d547dfad50f81d61969)), closes [#31](https://github.com/MongoCamp/mongocamp-server/issues/31)* Routes for Jobs ([daa167b](https://github.com/MongoCamp/mongocamp-server/commit/daa167b0d6f66b10fb8b8ef7d5fa0d0134390ee5)), closes [#31](https://github.com/MongoCamp/mongocamp-server/issues/31)
## [1.1.1]() (2022-07-15)


### Bug Fixes

* Exception by Change in BsonConverter ([d85fd22](https://github.com/MongoCamp/mongocamp-server/commit/d85fd22d74c2d43bf2ccfef9098c3d9249e7b990)), closes [#30](https://github.com/MongoCamp/mongocamp-server/issues/30)
## [1.1.0]() (2022-07-13)


### Bug Fixes

* Activate Server Plugin on startup ([b234b52](https://github.com/MongoCamp/mongocamp-server/commit/b234b5252aa53afe766f4ccc45c80ba8051f1a68))* cleanup ([48ce013](https://github.com/MongoCamp/mongocamp-server/commit/48ce013fa6df734ed744c20dfc9dd4d1495a01ab))* recursive BsonConverter.asMap for List of Documents ([526bd98](https://github.com/MongoCamp/mongocamp-server/commit/526bd982980211544b0a45fe3b0798583733c4dd)), closes [#28](https://github.com/MongoCamp/mongocamp-server/issues/28)

### Code Refactoring

* Moved RoutesPlugin and FilePlugin to package plugin ([a8cbfff](https://github.com/MongoCamp/mongocamp-server/commit/a8cbfff6e0f86937f6c0eebfa6ebcb78de2a6cad))

### Features

* Sever LifeCycle Hooks ([5274679](https://github.com/MongoCamp/mongocamp-server/commit/527467968459ed807bebebae10b7750dd9cf42fa))

### Maintenance

* 7 Dependency Updates ([a73b98d](https://github.com/MongoCamp/mongocamp-server/commit/a73b98dcc93e9b2502e118f301d2f8178c7259e1))* update version of `micrometer-core` ([fdf6727](https://github.com/MongoCamp/mongocamp-server/commit/fdf6727d343b7972189def40ac1bafded05e1443))
## [1.0.1]() (2022-06-16)


### Bug Fixes

* Add id to FileInformation and rename method on buckets for valid openapi specification ([fe39f0e](https://github.com/MongoCamp/mongocamp-server/commit/fe39f0e5188ae0f154c6d9aaa8df7e20eec29258))* ChangeLog Generation Error ([0aefaa6](https://github.com/MongoCamp/mongocamp-server/commit/0aefaa6b75b3b51afa7be6960337e061e548de9a))* collection status from other database collection ([3a61031](https://github.com/MongoCamp/mongocamp-server/commit/3a61031dfe5543054d1a9ba1223dd7500b29ff9f))* convert bson ids in one method ([8529163](https://github.com/MongoCamp/mongocamp-server/commit/8529163cc80b64cedba7adc344a752186740ef46))* Date in Request Logging ([84a1b9a](https://github.com/MongoCamp/mongocamp-server/commit/84a1b9a26d6ed276c7d58ddeb964e6ad9a46cf0d)), closes [Issue#19](https://github.com/Issue/issues/19)* Delete wrong Collection from GridFsFileAdapter ([4c57400](https://github.com/MongoCamp/mongocamp-server/commit/4c5740058b68e20e886290a33f053f88d241a658))* Disabled logger for MongoCampException ([fa6fa79](https://github.com/MongoCamp/mongocamp-server/commit/fa6fa79c8de46018690935ff8e41e40c41e7ec2f))* **file:** Fix Delete for GridFsFileAdapter ([011a272](https://github.com/MongoCamp/mongocamp-server/commit/011a2728b387ee08c42b80fad73385447fdf1831))* filter routes with _id field needs to be converted ([fe2b8f5](https://github.com/MongoCamp/mongocamp-server/commit/fe2b8f525dac208d88201599fc4697a4d151e500))* fixed empty role name or userId to add ([eff57e7](https://github.com/MongoCamp/mongocamp-server/commit/eff57e7b42ae0998e9f7f4bc80e23ab53c1fb3d9))* For events without duration use summary instead of timer ([4ef8bff](https://github.com/MongoCamp/mongocamp-server/commit/4ef8bff6d57bc9a96d3fb04876b8670d1402a1c7))* Type and some other small errors at schema generation ([dce548f](https://github.com/MongoCamp/mongocamp-server/commit/dce548f024fb355a59d837b513b408657139015e))* update many routs needs conversion to OperationMap ([0adb6ab](https://github.com/MongoCamp/mongocamp-server/commit/0adb6aba9c5edcf11c67ffcbc8a923619718f689))* Wrong Return Value for cache control ([bf7ca58](https://github.com/MongoCamp/mongocamp-server/commit/bf7ca58c42baf03a7a6210d8c5c66d028b94c666))

### Code Refactoring

* add document routes for mor RESTlike route naming ([1831ac5](https://github.com/MongoCamp/mongocamp-server/commit/1831ac51e392a2145d0e4aa50b59087bd70dcf02))* add document routes for mor RESTlike route naming ([3c30a41](https://github.com/MongoCamp/mongocamp-server/commit/3c30a41b2cce1f427b65981d627aef65765e9680))* Extract CollectionBaseRoute from BaseRoute ([ef8b870](https://github.com/MongoCamp/mongocamp-server/commit/ef8b8709bed1e64c4dfdd260b1e8e7b5d459442c))* Extract Collections to own API Routes ([3300414](https://github.com/MongoCamp/mongocamp-server/commit/3300414c02916eb943242e2a9579a1404ff57a70))* Extract Routes for Database Routes ([446daa1](https://github.com/MongoCamp/mongocamp-server/commit/446daa127ecac2f9a326c8263ebf5b81ee0472a5))* fix test for BucketSuite ([2452c5c](https://github.com/MongoCamp/mongocamp-server/commit/2452c5c6d307e44383bb1be51c6725aee9da0375))* fix test for BucketSuite ([896c9d4](https://github.com/MongoCamp/mongocamp-server/commit/896c9d46127f9ac889d62799328b891cbcc07e81))* Fixed Typos and `rename` functions ([466ccff](https://github.com/MongoCamp/mongocamp-server/commit/466ccff4056e16a22a771a2bafcb19e5e3f51e32))* min port for mongodb and http server ([7cb0c4a](https://github.com/MongoCamp/mongocamp-server/commit/7cb0c4ae9ccba618b5e583ea91effeccdf883fd4))* Move from QuadStingray/mongocamp to  mongocamp/mongocamp-server ([8c1dfb9](https://github.com/MongoCamp/mongocamp-server/commit/8c1dfb9c0b0172f0867948c65c42c4caae6a4fa2))* Moved Configuration for BucketSuffixes to BucketInformation.scala ([70e4e6e](https://github.com/MongoCamp/mongocamp-server/commit/70e4e6e9c53d2cd6b16b39937f3d1d2d54cc130c))* Removed "READ" Routes and spitted to DocumentRoutes and CollectionRoutes ([fecde9e](https://github.com/MongoCamp/mongocamp-server/commit/fecde9e5bf4203a8fc3de2ea779f36c48dd2d90c))* Removed ExecutionContext.global ([1bb961d](https://github.com/MongoCamp/mongocamp-server/commit/1bb961d2a39665d596f357b83ae4fc4c49540052))* Rename Api Documents to Document ([c438152](https://github.com/MongoCamp/mongocamp-server/commit/c4381521a68b1b12cc1c4df29a9634ce167044a2))* Rename method to convertFields ([bfb7b42](https://github.com/MongoCamp/mongocamp-server/commit/bfb7b42daf6ea791eb4ed9bf2fdddfecf27349b0))* rename userRole to role ([63678c3](https://github.com/MongoCamp/mongocamp-server/commit/63678c3ae571ba86b89872ecfbf07b8719ed4165))* rename vals to endpoints ([cacfbec](https://github.com/MongoCamp/mongocamp-server/commit/cacfbeca590c348242cbf48a03388a186188846b))* rename vals to endpoints ([bae1baf](https://github.com/MongoCamp/mongocamp-server/commit/bae1baf2e0d62080f7ce45c426c06ad421d905c1))* Replaced `com.sfxcode.nosql.mongo` with `dev.mongocamp.driver.mongodb` ([86e97ca](https://github.com/MongoCamp/mongocamp-server/commit/86e97caf84c47add477a92d18235d84c1c2770a3))* Singular name for package ([d7f2fbe](https://github.com/MongoCamp/mongocamp-server/commit/d7f2fbe8958e7f4fc112b49d42d0b795b809bfaa))* **test:** simplify test request execution ([910a0be](https://github.com/MongoCamp/mongocamp-server/commit/910a0bed7a2d8a0b69bf8f030584b0484aa25733))* typo in plugin list name ([c2bca46](https://github.com/MongoCamp/mongocamp-server/commit/c2bca46b7d46969579d65b1b674567a42f3262a9))

### Features

* `AuthInputAllMethods` and `AuthInputBearerWithBasic` added ([dadf7bb](https://github.com/MongoCamp/mongocamp-server/commit/dadf7bb7816ba6befcb5bab34cf2ca1902aa1705))* Added Filter to get Route all documents ([6e40be2](https://github.com/MongoCamp/mongocamp-server/commit/6e40be23d90c2aa08e88cc2ebeea8bcbaf9ab8b9))* Analyze Collection ([cca0fa5](https://github.com/MongoCamp/mongocamp-server/commit/cca0fa53bdf567dc914275d826ef18ac0dd20e6a))* **auth:** Added Route to check isAuthenticated ([0c51f3b](https://github.com/MongoCamp/mongocamp-server/commit/0c51f3bd5b9fdea0f6ace4fc428bc957eca15126))* **auth:** Route to check Authenticated ([f99427c](https://github.com/MongoCamp/mongocamp-server/commit/f99427c2d126fa9d5fbac03dc8eee5229ecc4efa)), closes [Issue#20](https://github.com/Issue/issues/20)* Bucket Api to List, Get and Delete Buckets ([f1b055c](https://github.com/MongoCamp/mongocamp-server/commit/f1b055ccdad36949c4eeda742f1e1d3fb08dd5cf))* cache tokens to database ([3a87e08](https://github.com/MongoCamp/mongocamp-server/commit/3a87e084b1b1cbfb1f70d0cb46153bd3f257b9b9))* changed some defaults for container ([12ca66d](https://github.com/MongoCamp/mongocamp-server/commit/12ca66da6752e6de350e42d85cd52198599c8c84))* Collections by Database ([c25fe73](https://github.com/MongoCamp/mongocamp-server/commit/c25fe731454a9740d4793f9d9b5d97cd8343cde9))* **Cors:** Access Control Expose Headers ([835975d](https://github.com/MongoCamp/mongocamp-server/commit/835975d036ed68dea1ae8a991ff9bccf6c73d43c))* Delete File by Adapter Holder ([b851a74](https://github.com/MongoCamp/mongocamp-server/commit/b851a745f29201730db80c943a6553912be2bf9c))* Dynamic Plugin Loading at StartUp ([2f65e6e](https://github.com/MongoCamp/mongocamp-server/commit/2f65e6e097c5e839d266fdd2e8851f4892452888)), closes [Issue#21](https://github.com/Issue/issues/21) [Issue#9](https://github.com/Issue/issues/9)* **Endpoint:** Collection Fields ([f953cac](https://github.com/MongoCamp/mongocamp-server/commit/f953cace4a89cd40792220849e8f0c67b3372d7e))* **Endpoint:** Collection Fields ([012b24b](https://github.com/MongoCamp/mongocamp-server/commit/012b24b17f7ef104ec63aba3e2929fbd90071fbe))* **file:** FileInformation instead of Map[String, Any] as response ([d0c758a](https://github.com/MongoCamp/mongocamp-server/commit/d0c758a3a6364dd2345493d55d847c27416bdacb))* **file:** Implement File Download ([75cdd01](https://github.com/MongoCamp/mongocamp-server/commit/75cdd014488835794c9be107f94224f4aa6ba063))* **file:** Implement File Upload ([a7ef597](https://github.com/MongoCamp/mongocamp-server/commit/a7ef597b380752c647df970f30d0b9cd5e3f0e5c))* **file:** Implement Routes for Files ([a31b2b1](https://github.com/MongoCamp/mongocamp-server/commit/a31b2b1f80a046c669b123f65d33e39c71eae99e))* **file:** Use other FilePlugin ([c08ac98](https://github.com/MongoCamp/mongocamp-server/commit/c08ac98e591638ff0fe5f47116cc5a31d2da00a0)), closes [Issue#13](https://github.com/Issue/issues/13)* Generate JsonSchema for Collection ([60c9a1d](https://github.com/MongoCamp/mongocamp-server/commit/60c9a1dd285659f75e543f714d66f66cb33be0d4))* Grants for Buckets and Collections ([9fb9eca](https://github.com/MongoCamp/mongocamp-server/commit/9fb9eca217c941f2c4b06762609a78eb0d7d3a75))* Ignore plugins ([7f35304](https://github.com/MongoCamp/mongocamp-server/commit/7f35304a6a6043ecff66a465b6f6566d08afa9b1))* IndexOptions now optional in requests ([f77ecfa](https://github.com/MongoCamp/mongocamp-server/commit/f77ecfaed6f9ca409d20579ed806c025e2e95924))* Load Configurations at global place ([9a91164](https://github.com/MongoCamp/mongocamp-server/commit/9a9116464f344d1f0ce82055a08cabf8dffd30b9))* Load Routes and File Adapter by Reflection ([573f4d7](https://github.com/MongoCamp/mongocamp-server/commit/573f4d7fbbf5497d6c643a395d60e30286e0d0f3))* Metrics Monitoring ([af3f2ba](https://github.com/MongoCamp/mongocamp-server/commit/af3f2ba89adf88836af99040283934c709655853))* Migration to Tapir 0.20.1 ([83405bb](https://github.com/MongoCamp/mongocamp-server/commit/83405bbff6fcfff9ea3e5f11072caf2e1a15e429))* migration to Tapir 1.0.0-RC3 ([79537e8](https://github.com/MongoCamp/mongocamp-server/commit/79537e888f85b6c865efbee997a0065efd2950d7))* more auth methods ([4675ef1](https://github.com/MongoCamp/mongocamp-server/commit/4675ef180c6818a451c7f3f1960b4041180b112a))* Publish Events for many Events to EventStream ([48c9961](https://github.com/MongoCamp/mongocamp-server/commit/48c99610b98e6ded5ed67cee466db6c10aac7737))* Reload UserInfos for Refresh or Profile Route ([be05407](https://github.com/MongoCamp/mongocamp-server/commit/be0540746bdce9c012245bae12ba887b78acab24))* Update FileInformation Request ([385626c](https://github.com/MongoCamp/mongocamp-server/commit/385626c322033a3b11d01762e79b30f3b62b4c19))* update to Tapir 1.0.0 ([bc94f6a](https://github.com/MongoCamp/mongocamp-server/commit/bc94f6a9c09d6875687294dbbd6eddc928270f19))* Use EventListener and EventStream for RequestLogging ([bc7743e](https://github.com/MongoCamp/mongocamp-server/commit/bc7743e5429852a0b390ff8521f15e6e95a814c7))

### Maintenance

* 5 dependency updates ([1ba3d79](https://github.com/MongoCamp/mongocamp-server/commit/1ba3d79ccc8ac30b89e1cb7c4c2962e2063a83f3))* DependencyUpdate "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.4.3" % Test ([664353e](https://github.com/MongoCamp/mongocamp-server/commit/664353e288c9109482bfe41103e30b8743166c42))* DependencyUpdates joda-time, jwt-scala ([22859ea](https://github.com/MongoCamp/mongocamp-server/commit/22859ea541807b612ec567157384fb33237be321))* DependencyUpdates sfxcode, sttClient, embed.mongo ([d03719c](https://github.com/MongoCamp/mongocamp-server/commit/d03719cb9546677f8f1edd9276de0be3923210ad))* DependencyUpdates sfxcode, sttClient, embed.mongo ([00a7c8d](https://github.com/MongoCamp/mongocamp-server/commit/00a7c8db67a6104a994f993433b4cc2846531f5b))* DependencyUpdates sttClient ([af7eed9](https://github.com/MongoCamp/mongocamp-server/commit/af7eed9db1f18b5a943897cab131bc6373cc3ae2))* DependencyUpdates Tapir, Circe and scaffeine ([503b493](https://github.com/MongoCamp/mongocamp-server/commit/503b49380e0d9ab2db2b29ebf0e87749d9d21add))

### Reverts

* CollectionSuite reverted ([68f0e3e](https://github.com/MongoCamp/mongocamp-server/commit/68f0e3e38295e5a81fd7317fad41335db236373e))* DatabaseSuite reverted ([21fece2](https://github.com/MongoCamp/mongocamp-server/commit/21fece2bf33aca61bd1895fdd97b0a0a5ecc4d3a))
## [0.3.4]() (2022-02-26)


### Bug Fixes

* Build Pipelines ([7d9062e](https://github.com/MongoCamp/mongocamp-server/commit/7d9062ee3e3290a950084ceb8be9a0054f3b9ddb))* Build Pipelines ([dee52a4](https://github.com/MongoCamp/mongocamp-server/commit/dee52a4154856089d9dc7b99bea72ae4c6fa73cb))* Build Pipelines ([4944b8e](https://github.com/MongoCamp/mongocamp-server/commit/4944b8ee8b9a809f8d74663bbf53fee8eed1c1d6))* Build Pipelines ([684b446](https://github.com/MongoCamp/mongocamp-server/commit/684b4462858621a1546101cf5d963bbd6fc61e26))* Build Pipelines ([0266cfe](https://github.com/MongoCamp/mongocamp-server/commit/0266cfe8dc6dab0d03de0253d24a755d02ef881a))* **header:** Added Header Interceptor for more Headers ([605acc6](https://github.com/MongoCamp/mongocamp-server/commit/605acc6fd41b79aaf82d26024aa26f6d3d6877f4))* Http-Methode PATCH for Updates ([60e4be6](https://github.com/MongoCamp/mongocamp-server/commit/60e4be65186ef64d09848fb1fb9f3612936e8009))* updateUser Fields in mongodb ([0c5d9dc](https://github.com/MongoCamp/mongocamp-server/commit/0c5d9dc1f5d42dcb7d7a6370d118bd175a03706c))

### Code Refactoring

* **auth:** UserName to UserId renamed ([c62d0cc](https://github.com/MongoCamp/mongocamp-server/commit/c62d0cc166cb80fd3b9d9432a93a579acaed18dc))* **Mongo:** FindAll Route ([16f8184](https://github.com/MongoCamp/mongocamp-server/commit/16f8184b5970d8fd4e98ff514f816afd80ab8aa6))* Rename AuthToken to ApiKey ([ccb26b4](https://github.com/MongoCamp/mongocamp-server/commit/ccb26b41a1726b98ae435301389c640dc94fccd0))* Rename MongoRest to MongoCamp ([5601e19](https://github.com/MongoCamp/mongocamp-server/commit/5601e196a43576919be17aa573bec9ec9aa7196f))* Switch to default Port 8080 ([e716073](https://github.com/MongoCamp/mongocamp-server/commit/e716073da955fa657480b873e9c85902cfb5fcb4))

### Features

* **auth:** Crud-Functions for User ([898a5db](https://github.com/MongoCamp/mongocamp-server/commit/898a5db9991ee20ff9dee304bbda6bf5bcc77b8e))* **auth:** Implementing Login Methods ([dc04f41](https://github.com/MongoCamp/mongocamp-server/commit/dc04f415b2fae99440029c95c45198a6b33ebb18))* **auth:** Implementing Login Methods ([2b4172d](https://github.com/MongoCamp/mongocamp-server/commit/2b4172de1eb3617a7e5c403fd369edaab735ac2e))* **auth:** Implementing StaticAuthHolder ([45525c0](https://github.com/MongoCamp/mongocamp-server/commit/45525c00bb3ba4adf4dc87585b24ab75482118dc))* Build Docker Container ([fd681af](https://github.com/MongoCamp/mongocamp-server/commit/fd681af293f08b686d08d1c07445b3801f42993f))* **chore:** Git ([019d055](https://github.com/MongoCamp/mongocamp-server/commit/019d055c92be95f12c64a6407c69208cb535892b))* Crud-Functions for UserRole ([628709a](https://github.com/MongoCamp/mongocamp-server/commit/628709a9fdc563643c3557314740413a14b9c112))* **Documentation:** Start ([ea268b3](https://github.com/MongoCamp/mongocamp-server/commit/ea268b30ff2fe10997703b93ff1d53908d098fd5))* **Documentation:** Start ([8bb0085](https://github.com/MongoCamp/mongocamp-server/commit/8bb00857f1fbdaccde7d95f56d1f47de0e7579c0))* **Documentation:** Start ([89e5bd3](https://github.com/MongoCamp/mongocamp-server/commit/89e5bd334c446467b9a111aaf896ee14e32fb0a0))* Initial Project Setup ([2da91bf](https://github.com/MongoCamp/mongocamp-server/commit/2da91bf348c241ac31459055b9460898100318a7))* MongoAuthHolder with Sample Data on StartUp ([dd86c87](https://github.com/MongoCamp/mongocamp-server/commit/dd86c87aeb9e12fe61b47b08fcc2b731c7cb0a3c))* **Mongo:** FindAll Route ([3dc3a64](https://github.com/MongoCamp/mongocamp-server/commit/3dc3a64b38384278e2c6f51778ea9ec4e2dd1184))* **Mongo:** FindAll Route ([bdc5ef0](https://github.com/MongoCamp/mongocamp-server/commit/bdc5ef01acdf97fb47676fb53bfc161269ed2b84))* Pagination for Find and Aggregation Routes ([1df577a](https://github.com/MongoCamp/mongocamp-server/commit/1df577a0c77055d11d4d9ef9dd9f3f39c2980508))* Pagination for Find and Aggregation Routes ([d8b82f4](https://github.com/MongoCamp/mongocamp-server/commit/d8b82f49ae9a6c71ae8fe4b6542a563cd3725315))* Pagination for List of Users and UserRoles ([815daa9](https://github.com/MongoCamp/mongocamp-server/commit/815daa9e872c93905bb4d4bf1383ca46e876b9f0))* RequestLogging ([cec1895](https://github.com/MongoCamp/mongocamp-server/commit/cec189546bb97ddd303ea0a5c85ddcc088fb99a3))* Securing Routes ([4c761dc](https://github.com/MongoCamp/mongocamp-server/commit/4c761dcade34d7db964f2d7d97caab0daad01438))* Workflow fix ([81d467e](https://github.com/MongoCamp/mongocamp-server/commit/81d467ef5a4bde61fd2690c5f1d8f20bc5deea29))* Workflow fix ([6897ec7](https://github.com/MongoCamp/mongocamp-server/commit/6897ec7b12a3f607bbf6fb2de877580ad4c421e2))

### Maintenance

* **Config:** Change Docs Serve Port to 5555 ([4f8501b](https://github.com/MongoCamp/mongocamp-server/commit/4f8501b6d9f1472d7fb1847a5da6ff7f071b1245))* **Git:** Fix defected git files ([6193fb1](https://github.com/MongoCamp/mongocamp-server/commit/6193fb1809139b56db02ac1ee978a4d5735d59e5))

[v1.5.4]: https://github.com/MongoCamp/mongocamp-server/compare/v1.5.2...v1.5.4
[v1.5.5]: https://github.com/MongoCamp/mongocamp-server/compare/v1.5.4...v1.5.5
[v1.5.6]: https://github.com/MongoCamp/mongocamp-server/compare/v1.5.5...v1.5.6
[v1.5.7]: https://github.com/MongoCamp/mongocamp-server/compare/v1.5.6...v1.5.7