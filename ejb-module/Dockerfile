FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY domain-module/pom.xml ./domain-module/pom.xml
COPY backend-module/pom.xml ./backend-module/pom.xml
COPY ejb-module/pom.xml ./ejb-module/pom.xml
COPY app-ear/pom.xml ./app-ear/pom.xml

COPY domain-module/src ./domain-module/src
COPY backend-module/src ./backend-module/src
COPY ejb-module/src ./ejb-module/src

RUN mvn -pl app-ear -am -DskipTests package

FROM quay.io/wildfly/wildfly:38.0.1.Final-jdk17

ENV EJB_USER=ejbuser
ENV EJB_PASS=ejbpass

USER root

RUN mkdir -p /opt/jboss/wildfly/custom/postgres \
  && curl -L -o /opt/jboss/wildfly/custom/postgres/postgresql.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar \
  && chown -R jboss:jboss /opt/jboss/wildfly/custom

USER jboss

RUN /opt/jboss/wildfly/bin/add-user.sh -a -u ${EJB_USER} -p ${EJB_PASS} -s

COPY --chown=jboss:jboss ejb-module/wildfly-config.cli /opt/jboss/wildfly/wildfly-config.cli

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/wildfly-config.cli

COPY --from=build /workspace/app-ear/target/*.ear /opt/jboss/wildfly/standalone/deployments/app.ear
RUN touch /opt/jboss/wildfly/standalone/deployments/app.ear.dodeploy

EXPOSE 8080 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh","-b","0.0.0.0","-bmanagement","0.0.0.0"]
