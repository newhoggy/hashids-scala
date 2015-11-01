#!/bin/bash

DIR=$HOME/.sbt

mkdir -p $DIR

cat <<EOF > $DIR/.s3credentials_dl.john-ky.io
accessKey=$PUBLISH_ACCESS_KEY
secretKey=$PUBLISH_SECRET_KEY
EOF
