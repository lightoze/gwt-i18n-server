#!/bin/bash

set -o errexit

mvn clean
mvn -Prelease release:prepare
mvn -Prelease release:perform
