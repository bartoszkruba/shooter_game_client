# 2D-online-realtime shooter  

Written in LibKTX(Kotlin API for libGDX) and Node.js  

## How to run  

### Client  
In order to run client you have to setup path to your SDK  

Create file `local.properties` in project root  
File should contain path to your SDK  
`sdk.dir=YOUR_PATH`  
Run desktop client thorugh gradle
`./gradlew desktop:run`  


### Server
Navigate into server directory  
`cd server/server`  
Install npm dependencies
`npm install`  
Run server  
`node index.js`
