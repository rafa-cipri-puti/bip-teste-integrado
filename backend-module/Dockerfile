FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY domain-module/pom.xml ./domain-module/pom.xml
COPY backend-module/pom.xml ./backend-module/pom.xml
COPY ejb-module/pom.xml ./ejb-module/pom.xml
COPY app-ear/pom.xml ./app-ear/pom.xml

RUN mvn -pl backend-module -am -DskipTests dependency:go-offline

COPY domain-module/src ./domain-module/src
COPY backend-module/src ./backend-module/src
COPY ejb-module/src ./ejb-module/src

RUN mvn -pl backend-module -am -DskipTests package

FROM eclipse-temurin:17-jre-noble
WORKDIR /app

COPY --from=build /workspace/backend-module/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
