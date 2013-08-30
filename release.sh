#!/bin/bash

set -o errexit

mvn -Prelease clean verify
mvn clean
mvn -Prelease release:prepare
mvn -Prelease release:perform
