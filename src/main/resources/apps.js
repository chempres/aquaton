var vertx = require('vertx');
var container = require('vertx/container')
var appConfig = container.config;
container.deployModule('io.vertx~mod-web-server~2.0.0-final', appConfig.webServer);
container.deployModule('io.vertx~mod-mongo-persistor~2.1.0', appConfig.mongoPersistor);
container.deployModule('io.vertx~mod-auth-mgr~2.0.0-final', appConfig.authMgr);
container.deployVerticle('net.aquaton.AuthVerticle');
container.deployVerticle('net.aquaton.DataReceiverVerticle');
container.deployWorkerVerticle('net.aquaton.DataGeneratorVerticle');
container.deployVerticle('net.aquaton.ParcelVerticle');