FROM openjdk:17-jdk
WORKDIR /WEBA11Y-Server
VOLUME /tmp
ARG JAR_FILE=./build/libs/*.jar
ADD ${JAR_FILE} weba11y-server.jar
ENTRYPOINT ["java", "-jar", "weba11y-server.jar"]