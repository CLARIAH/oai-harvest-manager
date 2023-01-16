#!/usr/bin/env bash
### usage ###
# to be used within container, not to run on host
# ./after-harvest.sh <protocol>


# get time stamp, protocol and folder name
datastamp=$(date +"%Y%m%d_%H_%M_%S")

# get protocol in lower case
target_protocol=${1:-oai}
target_protocol=`echo "${target_protocol}" | tr '[:upper:]' '[:lower:]'`

# get target folder name
target_dir=${datastamp}_${target_protocol}

# prepare folder structure for OAI
if [ -d "/app/proddir/${target_protocol}" ]; then
  echo "moving old proddir for ${target_protocol} to bak";
  mv /app/proddir/${target_protocol} /app/proddir/${target_protocol}.bak;
  tar -czf /app/proddir/${target_dir}.tar.gz /app/proddir/${target_protocol}.bak;
fi

if [ -d "/app/workdir/${target_protocol}" ]; then
  echo "Moving data from harvest folder to production folder /app/proddir/${target_protocol}"
  mv /app/workdir/${target_protocol} /app/proddir/${target_protocol}
else
  echo "missing folder structure, harvest failed"
  exit 1
fi

echo "done harvest cleaning up"
