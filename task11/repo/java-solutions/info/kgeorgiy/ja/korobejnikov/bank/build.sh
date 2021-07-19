#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT=../../../../../
cd "$ROOT"
LIB=../lib/*
javac -classpath "$LIB" info/kgeorgiy/ja/korobejnikov/bank/rmi/*.java info/kgeorgiy/ja/korobejnikov/bank/demo/*.java info/kgeorgiy/ja/korobejnikov/bank/test/*.java

