#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT=../../../../../
cd "$ROOT"
LIB=../lib/*
javac -classpath "$LIB" info/kgeorgiy/ja/korobejnikov/i18n/*.java info/kgeorgiy/ja/korobejnikov/i18n/tests/*.java

