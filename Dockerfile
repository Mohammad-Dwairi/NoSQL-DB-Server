FROM openjdk:11-jdk
COPY target/NoSQL-DB-Server-0.0.1-SNAPSHOT.jar NoSQL-DB-Server-0.0.1.jar
ENV NODE_MODE=master
ENV PORT=8080
ENV MASTER_ADDRESS="http://localhost:8080"
ENTRYPOINT ["java","-jar","/NoSQL-DB-Server-0.0.1.jar","--spring.profiles.active=${NODE_MODE}","--server.port=${PORT}","--master.address=${MASTER_ADDRESS}"]