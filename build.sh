#!/bin/bash

rm /bin/sh
ln -s /bin/bash /bin/sh
mkdir -p /opt/coursier;
ARCH=$(uname -m | tr '[:upper:]' '[:lower:]');
if [[ "$ARCH" == *"amd"* ]]; then
  echo "Download AMD Version"
  curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > /opt/coursier/cs
else
  echo "Download AARCH Version"
  curl -fL "https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz" | gzip -d > /opt/coursier/cs
fi

chmod +x /opt/coursier/cs;

mkdir "/opt/coursier/bin";

/opt/coursier/cs setup --yes --jvm $GRAAL_VERSION --install-dir /opt/coursier/bin --apps scala,sbt,scalac;