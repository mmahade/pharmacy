FROM eclipse-temurin:17-jdk-jammy

ARG JAR=target/*.jar

COPY ${JAR} pharmacy.jar

ENTRYPOINT ["java", "-jar", "pharmacy.jar"]

EXPOSE 5051