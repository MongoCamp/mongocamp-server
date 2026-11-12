FROM debian:12.7-slim AS builder
ARG GRAAL_VERSION="graalvm-java25:25.0.2"
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
# GraalVM for JDK 25 includes native-image (there is no gu anymore). sbt 2 writes the build output to target/out/jvm/<scala version>/<module>/,
# the binary is copied to a fixed path so that a new scala version does not break the following steps
RUN eval "$(cs java --jvm $GRAAL_VERSION --env)"; sbt ";clean;publishLocal;mongocamp-cli/GraalVMNativeImage/packageBin";
RUN cp "$(find /mongocamp-cli/target/out -type f -path '*/mongocamp-cli/graalvm-native-image/mongocamp-cli' | head -n 1)" /mongocamp-cli/mongocamp-cli-native
RUN /bin/upx --best /mongocamp-cli/mongocamp-cli-native
# the CLI takes JAVA_HOME if set, otherwise it downloads the highest graalvm-java25 of the coursier index (a newer build than GRAAL_VERSION
# since 2026-11, its native-image output is not understood by the CLI): pin the same GraalVM as the sbt step
RUN eval "$(cs java --jvm $GRAAL_VERSION --env)"; /mongocamp-cli/mongocamp-cli-native prepare native
RUN /bin/upx --best /mongocamp-cli/server-raw

FROM debian:12.7-slim
ENV PLUGINS_DIRECTORY="/opt/mongocamp/plugins"
COPY --from=builder /mongocamp-cli/mongocamp-cli-native /opt/bin/mongocamp-cli
COPY --from=builder /mongocamp-cli/server-raw /opt/bin/server-raw
ENV MODE="default"
WORKDIR /opt/bin/
RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections; apt-get update; apt-get install -y snappy-dev zlib-dev bash; apt-get -y upgrade;
RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli; chmod +x /opt/bin/server-raw; apt-get update;
RUN mkdir -p $PLUGINS_DIRECTORY; chmod -R 777 $PLUGINS_DIRECTORY; chmod +x /opt/bin/mongocamp-cli;
ENTRYPOINT ./mongocamp-cli run $MODE
