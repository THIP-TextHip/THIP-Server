FROM amazoncorretto:17

# 작업 디렉토리 생성
WORKDIR /app

# 컨테이너 환경에 맞춰 힙 메모리를 자동 조절하는 옵션 적용
# MaxRAMPercentage=75.0 : 컨테이너 메모리의 75%를 힙으로 사용 (나머지는 메타스페이스, 스레드, OS 등을 위해 남겨둠)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# 빌드된 JAR 파일 복사
COPY ./build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
