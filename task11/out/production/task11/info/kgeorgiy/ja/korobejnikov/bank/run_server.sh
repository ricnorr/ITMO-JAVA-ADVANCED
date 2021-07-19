#!/bin/bash
cd "$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT=../../../../../
cd "$ROOT"
java info.kgeorgiy.ja.korobejnikov.bank.demo.Server
