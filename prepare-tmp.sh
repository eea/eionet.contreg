#!/usr/bin/env bash
set -e
if [ "${1:-}" = "stop" ]; then
  exit 0
fi

echo "Start getting data"

docker_id=$(cat /proc/self/cgroup | grep :memory: | sed  's#.*/\([0-9a-fA-F]*\)$#\1#' )

if [ -n "$docker_id" ] && [ $(echo "$docker_id" | grep -c memory ) -eq 0 ]; then
  echo "Docker inspect"
  WORKERDIR=$(docker inspect "$docker_id" | grep :/var/jenkins_home/worker | awk -F'["|:]' '{print $2}')
  WORKERDIR=$(pwd | sed -e "s#/var/jenkins_home/worker#$WORKERDIR#")
else
  echo "Working directory"
  WORKERDIR=$(pwd)
fi

echo "Resolved WORKERDIR: $WORKERDIR"

#mkdir /var/jenkins_home/worker/tmp_cr

HOST_TMP="$WORKERDIR/tmp_cr"

mkdir -p "$HOST_TMP/tmp"

sed -i "s+^config.docker.sharedVolume=.*+config.docker.sharedVolume=$HOST_TMP+g" tests.properties
grep '^config.docker.sharedVolume=' tests.properties
sed -i "s#config.app.home=.*#config.app.home=$HOST_TMP#"  tests.properties
grep '^config.app.home=' tests.properties

if command -v docker >/dev/null 2>&1; then
  sed -i "s#config.docker.copy.enabled=.*#config.docker.copy.enabled=true#" tests.properties
else
  sed -i "s#config.docker.copy.enabled=.*#config.docker.copy.enabled=false#" tests.properties
fi

sed -i "s#config.docker.container.path=.*#config.docker.container.path=/tmp/tmp_cr#" tests.properties
sed -i "s#config.docker.container.match=.*#config.docker.container.match=virtuoso#" tests.properties


availableport=${availableport:-8891}
availableport2=${availableport2:-1112}
availableport3=${availableport3:-8181}


sed -i "s/8891:8890/$availableport:8890/" pom.xml
sed -i "s/virtuoso-cr-jenkins/virtuoso-cr-jenkins-$availableport/" pom.xml
sed -i "s/httpd-cr-jenkins/httpd-cr-jenkins-$availableport/" pom.xml
sed -i "s#<valid.images>virtuoso, httpd</valid.images>#<valid.images>virtuoso-$availableport, httpd-$availableport</valid.images>#" pom.xml
sed -i "s#<name>virtuoso</name>#<name>virtuoso-$availableport</name>#" pom.xml
sed -i "s#<name>httpd</name>#<name>httpd-$availableport</name>#" pom.xml
sed -i "s#<link>virtuoso</link>#<link>virtuoso-$availableport:virtuoso</link>#" pom.xml
sed -i "s/tests.virtuoso.port=.*/tests.virtuoso.port=$availableport2/" tests.properties
sed -i "s/tests.httpd.port=.*/tests.httpd.port=$availableport3/" tests.properties
sed -i "s/virtuoso:1112/virtuoso-$availableport:$availableport2/" default.properties
sed -i "s/1112/$availableport2/" src/test/resources/liquibase.properties


if [ "$(docker ps --format '{{.Image}}' | grep -w virtuoso)" == "" ]; then
    exit 0
else
    echo "Waiting for available ports..."
    sleep 10
fi
