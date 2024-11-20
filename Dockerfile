FROM debian:12.7-slim AS builder
ARG GRAAL_VERSION="graalvm-java23:23.0.1"
ARG UPX_VERSION="4.2.4"
ENV COURSIER_FOLDER="/opt/coursier/bin"
ENV PATH="$PATH:$COURSIER_FOLDER"
COPY . /mongocamp-cli/
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y curl wget unzip zip gcc bash zlib1g-dev libc6 build-essential libz-dev zlib1g-dev; apt-get -y upgrade;
RUN chmod +x /mongocamp-cli/prepare-build.sh;
RUN /mongocamp-cli/prepare-build.sh;
RUN chmod +x /mongocamp-cli/prepare-build-upx.sh;
RUN /mongocamp-cli/prepare-build-upx.sh
WORKDIR /mongocamp-cli/
RUN eval "$(cs java --jvm $GRAAL_VERSION --env)"; $JAVA_HOME/bin/gu install native-image; sbt clean publishLocal mongocamp-cli/graalvm-native-image:packageBin;
RUN /bin/upx --best /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli
RUN /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare native
RUN /bin/upx --best /mongocamp-cli/server-raw

FROM debian:12.7-slim
ENV PLUGINS_DIRECTORY="/opt/mongocamp/plugins"
COPY --from=builder /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
COPY --from=builder /mongocamp-cli/server-raw /opt/bin/server-raw
ENV MODE="default"
WORKDIR /opt/bin/
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y snappy-dev zlib-dev bash; apt-get -y upgrade;
RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli; chmod +x /opt/bin/server-raw; apt-get update;
RUN mkdir -p $PLUGINS_DIRECTORY; chmod -R 777 $PLUGINS_DIRECTORY; chmod +x /opt/bin/mongocamp-cli;
ENTRYPOINT ./mongocamp-cli run $MODE
