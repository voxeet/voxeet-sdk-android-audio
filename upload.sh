#!/bin/bash

rm -rf build */build

./gradlew :audio:assembleRelease :audio:bintrayUpload
