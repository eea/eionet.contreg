#!/bin/bash
APP=eeacms/contreg
#APP=eworxeea/contreg
BUILDTIME=$(date '+%Y-%m-%dT%H%M')

# docker build, tag and push
docker build -t $APP:latest . && docker tag $APP:latest $APP:$BUILDTIME && docker push $APP:latest && docker push $APP:$BUILDTIME
