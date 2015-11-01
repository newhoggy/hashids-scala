#!/bin/bash

declare -a var_descriptions=(\
"PUBLISH_ACCESS_KEY S3 access key for publishing" \
"PUBLISH_SECRET_KEY S3 secret key for publishing" \
)

for var_description in "${var_descriptions[@]}"; do
  var_name="${var_description%% *}"
  if [ "${!var_name-x}" = "x" -a "${!var_name-y}" = "y" ]; then
    echo -e "\x1B[31mError: Variable not defined: $var_description\x1B[0m"
    _not_found=true
  else
    echo -e "\x1B[32mVariable defined: $var_description\x1B[0m"
  fi
done

test -z "${_not_found}"
