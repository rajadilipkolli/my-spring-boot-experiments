FROM gitpod/workspace-full

USER root

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
             && sdk install java 17.0.5-amzn \
             && sdk install java 22.2.r17-grl \
             && sdk default java 17.0.5-amzn"