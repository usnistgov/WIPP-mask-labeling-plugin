#!/bin/bash
version=$(<VERSION)
docker build . -t wipp/wipp-mask-labeling-plugin:${version}
