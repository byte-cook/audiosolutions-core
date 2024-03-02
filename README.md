# AudioSolutions Core

This project contains the service layer of the application AudioSolutions.

## Building

The mp3plugin.jar must be installed in the local Maven repository as follows:

> mvn org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file -Dfile=<path-to>/mp3plugin.jar -DgroupId=com.sun -DartifactId=mp3plugin -Dversion=1.0.0 -Dpackaging=jar

See: https://maven.apache.org/plugins/maven-install-plugin/examples/generic-pom-generation.html
