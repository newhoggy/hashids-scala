#!/bin/bash
SBT_DIR=$HOME/.sbt

mkdir -p $SBT_DIR
cat <<EOF > $SBT_DIR/.s3credentials_dl.john-ky.io
accessKey = $AWS_ACCESS_KEY
secretKey = $AWS_SECRET_KEY
EOF
