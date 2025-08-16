FROM openjdk:17

ARG PORT=8000
ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx2g -XX:+ExitOnOutOfMemoryError"

EXPOSE ${PORT}

COPY ./build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]