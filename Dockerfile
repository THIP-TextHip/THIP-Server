FROM openjdk:17

ARG PORT=8000

EXPOSE ${PORT}

COPY ./build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]