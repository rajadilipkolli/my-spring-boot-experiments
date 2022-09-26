FROM gitpod/workspace-full

USER root

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
             && sdk install java 19-open \
             && sdk install java 22.2.r17-nik \
             && sdk install java 18.0.1-amzn \
             && sdk default java 19-open"