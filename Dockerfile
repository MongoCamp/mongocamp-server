FROM debian:12.1-slim AS builder
ARG GRAAL_VERSION="graalvm-java17:22.3.3"
ENV COURSIER_FOLDER="/opt/coursier/bin"
ENV PATH="$PATH:$COURSIER_FOLDER"
COPY . /mongocamp-cli/
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y curl wget unzip zip gcc bash zlib1g-dev libc6 build-essential libz-dev zlib1g-dev; apt-get -y upgrade;
RUN chmod +x /mongocamp-cli/prepare-build.sh;
RUN /mongocamp-cli/prepare-build.sh;
WORKDIR /mongocamp-cli/
RUN eval "$(cs java --jvm $GRAAL_VERSION --env)"; $JAVA_HOME/bin/gu install native-image; sbt clean publishLocal mongocamp-cli/graalvm-native-image:packageBin;
# todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
#RUN /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare native
#RUN /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare cache;

FROM debian:12.1-slim
ENV PLUGINS_DIRECTORY="/opt/mongocamp/plugins"
COPY --from=builder /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
## todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
# COPY --from=builder /mongocamp-cli/server-raw /opt/bin/server-raw
ENV MODE="default"
WORKDIR /opt/bin/
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y snappy-dev zlib-dev bash; apt-get -y upgrade;
## todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
# RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli; chmod +x /opt/bin/server-raw; apt-get update;
RUN mkdir -p $PLUGINS_DIRECTORY; chmod -R 777 $PLUGINS_DIRECTORY; chmod +x /opt/bin/mongocamp-cli;
RUN ./mongocamp-cli prepare cache;
ENTRYPOINT ./mongocamp-cli run $MODE
