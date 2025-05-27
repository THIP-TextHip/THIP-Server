FROM amazoncorretto:17

ARG PORT=8000

EXPOSE ${PORT}

COPY ./build/libs/*.jar ./app.jar

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]