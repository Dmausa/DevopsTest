#!/bin/bash

# Set default values for service versions and additional parameters
version_service1="v1.0"
version_service2="v1.0"
additional_param_1=5

# Parse command line arguments
while [ "$#" -gt 0 ]; do
  case "$1" in
    --version_service1=*) version_service1="${1#*=}"; shift 1;;
    --version_service2=*) version_service2="${1#*=}"; shift 1;;
    --additional_param_1=*) additional_param_1="${1#*=}"; shift 1;;
    *) echo "Unknown parameter passed: $1"; shift 1;;
  esac
done

# Run the microservices using Docker, passing the additional_param_1 as an environment variable
docker run -d -p 8082:8080 -e ADDITIONAL_PARAM_1=$additional_param_1 --name=service1 dmausa/service1:$version_service1
docker run -d -p 8081:8080 -e ADDITIONAL_PARAM_1=$additional_param_1 --name=service2 dmausa/service2:$version_service2