#!/usr/bin/env bash

sbt_launch_update() {
  if [ ! -d sbt-launch ]; then
    git clone https://github.com/newhoggy/sbt-launch.git
  else
    git -C sbt-launch pull
  fi
}

sbt_launch_update

pwd

./sbt-launch/sbt "$@"
