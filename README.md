# 2D-online-realtime shooter  

## Continuation of [shooter_game](https://github.com/bartoszkruba/shooter_game) project.

## How to run  

In order to run client you have to setup path to your SDK  

Create file `local.properties` in project root  
File should contain path to your SDK  
`sdk.dir=YOUR_PATH`  

Run desktop client thorugh gradle  
`./gradlew desktop:run`  

### Creating desktop executable

Compile project into fat jar  
``./gradlew dekstop:dist``

Fat jar is generated in `desktop/build/libs`

Download the latest [packR](https://github.com/libgdx/packr) build

Edit `packr.config.json` add set following parameters:

`platform`: target platform for your executable (one of "windows32", "windows64", "linux32", "linux64", "mac")  
`jdk`: path to directory, ZIP file or URL for JDK  
`classpath`: path to your compiled fat jar  
`output`: output directory for executable  

Create executable with packR   
``
java -jar packr.jar PATH_TO_CONFIG_JSON
``

