#!/bin/bash

rm /bin/sh
ln -s /bin/bash /bin/sh
mkdir -p $COURSIER_FOLDER;

download_coursier_aarch() {
  echo "Download AARCH Version"
  curl -fL "https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz" | gzip -d > $COURSIER_FOLDER/cs
}

download_coursier_amd() {
  echo "Download AMD Version"
  curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > $COURSIER_FOLDER/cs
}


ARCH=$(uname -m | tr '[:upper:]' '[:lower:]');
echo "Architecture found: $ARCH";
if [[ "$ARCH" == *"aarch"* ]]; then
  download_coursier_aarch
elif [[ "$ARCH" == *"arm"* ]]; then
  download_coursier_aarch
else
  download_coursier_amd
fi

chmod -R 777 $COURSIER_FOLDER/;
find $COURSIER_FOLDER -type d -exec chmod u+x {} \;

which cs;

cs setup --yes --jvm $GRAAL_VERSION --install-dir $COURSIER_FOLDER --apps scala,sbt,scalac;