FROM gitpod/workspace-full

RUN curl -fLo /tmp/coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x /tmp/coursier && /tmp/coursier setup

RUN sudo env "PATH=$PATH" coursier bootstrap org.scalameta:scalafmt-cli_2.12:2.4.2 -r sonatype:snapshots -o /usr/local/bin/scalafmt --standalone --main org.scalafmt.cli.Cli

RUN scalaenv install scala-2.13.6 && scalaenv global scala-2.13.6

