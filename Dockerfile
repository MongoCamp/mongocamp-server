FROM ubuntu:jammy AS builder
ARG GRAAL_VERSION="graalvm-java17:22.3.2"
ENV PATH="$PATH:/opt/coursier/bin"
COPY . /mongocamp-cli/
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y curl gcc bash zlib1g-dev libc6 build-essential libz-dev zlib1g-dev; apt-get -y upgrade;
RUN chmod +x /mongocamp-cli/prepare-build.sh;
RUN /mongocamp-cli/prepare-build.sh;
WORKDIR /mongocamp-cli/
RUN eval "$(/opt/coursier/cs java --jvm $GRAAL_VERSION --env)"; $JAVA_HOME/bin/gu install native-image; env JAVA_OPTS="-Xmx10G" sbt clean publishLocal mongocamp-cli/graalvm-native-image:packageBin;
# todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
#RUN /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare native
RUN /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare cache;

FROM ubuntu:jammy
COPY --from=builder /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
## todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
## COPY --from=builder /mongocamp-cli/server-raw /opt/bin/server-raw
ENV MODE="default"
WORKDIR /opt/bin/
## todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
# RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli; chmod +x /opt/bin/server-raw; apt-get update;
RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli;
ENTRYPOINT ./mongocamp-cli run $MODE