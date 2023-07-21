FROM debian AS build
COPY . /mongocamp-cli/
RUN apt-get update;
RUN apt-get install -y curl gcc bash zlib1g-dev;
RUN curl -fL "https://github.com/coursier/launchers/raw/maslter/cs-x86_64-pc-linux.gz" | gzip -d > coursier
RUN chmod +x coursier
RUN eval "$(./coursier setup --env --jvm graalvm-java17:22.3.2 --apps scala,sbt,scalac)";
RUN cd /mongocamp-cli/
RUN gu install native-image
RUN sbt clean mongocamp-cli/graalvm-native-image:packageBin

FROM debian:bookworm-slim
COPY --from=build /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
ENV MODE="default"
WORKDIR /opt/bin/
RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli;
RUN ./mongocamp-cli prepare
ENTRYPOINT ./mongocamp-cli run $MODE