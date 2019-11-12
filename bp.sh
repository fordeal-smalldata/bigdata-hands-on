#!/usr/bin/env bash
sbt ornate
cp -r target/doc/* ./docs
cp googlee3f3d850638dd798.html ./docs
git add .
git commit -m $1
git push
