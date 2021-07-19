#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT=../../../../../
cd "$ROOT"
LIB=../lib/apiguardian-api-1.1.0.jar:../lib/junit-jupiter-5.7.0.jar:../lib/junit-jupiter-api-5.7.0.jar:../lib/junit-jupiter-engine-5.7.0.jar:../lib/junit-jupiter-params-5.7.0.jar:../lib/junit-platform-commons-1.7.0.jar:../lib/junit-platform-engine-1.7.0.jar:../lib/opentest4j-1.2.0.jar:../junit-platform-console-standalone-1.7.1-all.jar
java -jar ../lib/junit-platform-console-standalone-1.7.1-all.jar -cp . -scan-classpath

