#!/usr/bin/env bash
sbt ornate
cp -r target/doc/* ./docs
git add .
git commit -m $1
git push
