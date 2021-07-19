#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
SOURCES=../../java-advanced-2021/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/
ROOT=../../../../../
cd "$ROOT"
javadoc -private -link "https://docs.oracle.com/en/java/javase/11/docs/api/" \
   -d ./info/kgeorgiy/ja/korobejnikov/implementor/doc info.kgeorgiy.ja.korobejnikov.implementor \
   ${SOURCES}/ImplerException.java ${SOURCES}/Impler.java ${SOURCES}/JarImpler.java