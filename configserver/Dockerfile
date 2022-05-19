# Dockerfile.layers
FROM openjdk:8 as builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY  ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:8
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

CMD ["java", "org.springframework.boot.loader.JarLauncher"]
