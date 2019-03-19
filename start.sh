#!/usr/bin/env bash

PORT=${HTTP_PORT:-9000}
sbt "run -Dhttp.port=$PORT"
