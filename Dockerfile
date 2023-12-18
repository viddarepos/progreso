FROM openjdk:17
ADD target/prime-java-api-0.0.1-SNAPSHOT.jar prime-java-api-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "prime-java-api-0.0.1-SNAPSHOT.jar"]
