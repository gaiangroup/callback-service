FROM 064267045885.dkr.ecr.ap-south-1.amazonaws.com/dev:alpine-openjre-8-apm

MAINTAINER durga.prasad@gaiansolutions.com

ADD docker/startup.sh /

# Install Tini
ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini-static /tini
RUN chmod +x /tini


EXPOSE 8080
#CMD [""]
ENTRYPOINT ["/tini", "--", "sh", "/startup.sh"]

# add the jar file
ADD src/main/resources/logback-spring.xml /etc/conf/gaian/api-service/logback-spring.xml

ADD target/api-service.jar api-service.jar
