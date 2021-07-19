#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
SOURCE=../../java-advanced-2021/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java
java -cp \
 "$SOURCE" \
  -jar Implementor.jar info.kgeorgiy.java.advanced.implementor.Impler -jar impler.jar
