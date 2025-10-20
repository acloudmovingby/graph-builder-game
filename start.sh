#! /bin/bash

echo "ls -al"
ls -al
echo "ls -al public"
ls -al public/
echo "ls -al public/scala/out/graphcontroller/fastLinkJS.dest/main.js"
ls -al public/scala/out/graphcontroller/fastLinkJS.dest/main.js

node app.js
