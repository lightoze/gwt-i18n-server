#!/bin/bash

set -o errexit

./mvnw clean
./mvnw -Prelease release:prepare
./mvnw -Prelease release:perform
