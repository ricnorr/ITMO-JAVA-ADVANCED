#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT=../../../../../
cd "$ROOT"
LIB=../lib/*
java -jar ../lib/junit-platform-console-standalone-1.7.1-all.jar -cp . -scan-classpath

