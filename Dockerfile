FROM debian AS build
COPY . /mongocamp-cli/
RUN apt-get update; apt-get install -y curl gcc bash zlib1g-dev; curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > coursier \
        && chmod +x coursier \
        && eval "$(./coursier setup --env --jvm graalvm-java17:22.3.2 --apps scala,sbt,scalac)"; \
        && cd /mongocamp-cli/ \
        && gu install native-image \
        && sbt clean mongocamp-cli/graalvm-native-image:packageBin

FROM debian:bookworm-slim
COPY --from=build /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
ENV MODE="default"
WORKDIR /opt/bin/
RUN chmod +x /opt/bin/mongocamp-cli
RUN ./mongocamp-cli prepare
ENTRYPOINT ./mongocamp-cli run $MODE