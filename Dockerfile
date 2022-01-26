FROM openjdk:11-jdk
COPY target/NoSQL-DB-Server-0.0.1-SNAPSHOT.jar NoSQL-DB-Server-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/NoSQL-DB-Server-0.0.1.jar"]