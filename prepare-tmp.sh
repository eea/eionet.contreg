#!/usr/bin/env bash
set -e
echo "Start getting data"

docker_id=$( cat /proc/self/cgroup | grep :memory: | sed  's#.*/\([0-9a-fA-F]*\)$#\1#' )

WORKERDIR=$(docker inspect $docker_id | grep :/var/jenkins_home/worker | awk -F'["|:]' '{print $2}')

echo "Identify path for WORKERDIR"

mkdir /var/jenkins_home/worker/tmp_cr

sed "s/^config.docker.sharedVolume=.*/config.docker.sharedVolume=$WORKERDIR/tmp_cr" tests.properties


exit 0