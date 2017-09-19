#!/bin/bash
# APP=eeacms/contreg
APP=sofiageo/contreg
BUILDTIME=$(date '+%Y-%m-%dT%H%M')

# docker build runs only if tests are successful
docker build -t $APP:latest . &&  docker tag $APP:latest $APP:$BUILDTIME && docker push $APP:latest && docker push $APP:$BUILDTIME
