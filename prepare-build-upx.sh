#!/bin/bash

UPX_ARCH=""
ARCH=$(uname -m | tr '[:upper:]' '[:lower:]');
echo "Architecture found: $ARCH";
if [[ "$ARCH" == *"aarch"* ]]; then
  UPX_ARCH="arm64"
elif [[ "$ARCH" == *"arm"* ]]; then
  UPX_ARCH="arm64"
else
  UPX_ARCH="amd64"
fi

if [[ "$ARCH" == "" ]]; then
  echo "no download of upx"
else
  UPX_FILENAME="upx-${UPX_VERSION}-${UPX_ARCH}_linux"
  wget https://github.com/upx/upx/releases/download/v${UPX_VERSION}/${UPX_FILENAME}.tar.xz -O /tmp/${UPX_FILENAME}.tar.xz;
  tar -xf /tmp/${UPX_FILENAME}.tar.xz -C /tmp
  chmod -R 777 /tmp/${UPX_FILENAME};
  mv /tmp/${UPX_FILENAME}/upx /bin/upx;
  upx -V
fi
