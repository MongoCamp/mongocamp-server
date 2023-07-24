FROM debian AS build

ENV GRAAL_VERSION="graalvm-java17:22.3.2"

COPY . /mongocamp-cli/
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN apt-get update;
RUN apt-get install -y curl build-essential libz-dev zlib1g-dev;
RUN curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > coursier
RUN chmod +x coursier
RUN eval "$(./coursier setup --env --jvm $GRAAL_VERSION --apps scala,sbt,scalac)";  \
    cd /mongocamp-cli/;  \
    gu install native-image;  \
    sbt clean mongocamp-cli/graalvm-native-image:packageBin; \
    /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli prepare native


FROM debian:bookworm-slim AS mongocamp-build-base
COPY --from=build /mongocamp-cli/mongocamp-cli/target/graalvm-native-image/mongocamp-cli /opt/bin/mongocamp-cli
COPY --from=build /mongocamp-cli/server-raw /opt/bin/server-raw
ENV MODE="default"
WORKDIR /opt/bin/
RUN mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins; chmod +x /opt/bin/mongocamp-cli; chmod +x /opt/bin/server-raw; apt-get update; apt-get -y install build-essential libz-dev zlib1g-dev;
ENTRYPOINT ./mongocamp-cli run $MODE

#FROM mongocamp-build-base as full-build
#RUN ./mongocamp-cli prepare jvm
