# Repro Corretto 302 issue with Mac and JNA

* Build with mvn package
* Run with java -Djna.dump_memory=true -jar target/jna-repro-1.0-SNAPSHOT-jar-with-dependencies.jar

* Crash with export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk/Contents/Home
* Fix with export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
