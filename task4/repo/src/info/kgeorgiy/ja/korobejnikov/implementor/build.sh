#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
LIB=../../../../../../../java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar
ROOT=../../../../../
SOURCE=info/kgeorgiy/ja/korobejnikov/implementor/
if javac -cp "$LIB" Implementor.java; then
  cd "$ROOT"
  jar cmf "${SOURCE}MANIFEST.MF" "${SOURCE}Implementor.jar" "${SOURCE}Implementor.class" "${SOURCE}Implementor\$CleanDirectoryVisitor.class"
  rm "${SOURCE}Implementor.class"
  rm "${SOURCE}Implementor\$CleanDirectoryVisitor.class"
else
  echo "compilation failed"
fi