#! /bin/bash

## This script is stupidly simple right now but sometimes it's useful to add debugging stuff here
## for when the container starts up. It then serves as a single entry point for the Dockerfile CMD instruction (which
## can only take one shell command).

## Start the Node.js application
node app.js
