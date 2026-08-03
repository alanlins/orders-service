# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY client.truststore.jks /etc/ssl/certs/java/cacerts/client.truststore.jks
COPY client.keystore.jks /etc/ssl/certs/java/cacerts/client.keystore.jks
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Shell script to dynamically generate keystores from Render Environment Variables
RUN echo '#!/bin/sh\n\
echo "$KAFKA_CA_PEM" > ca.pem\n\
echo "$KAFKA_CERT_PEM" > service.cert\n\
echo "$KAFKA_KEY_PEM" > service.key\n\
\n\
# Convert key to PKCS12\n\
openssl pkcs12 -export -in service.cert -inkey service.key -out client.p12 -name client -passout pass:$SSL_STORE_PASSWORD\n\
\n\
# Create Keystore and Truststore\n\
keytool -importkeystore -deststorepass $SSL_STORE_PASSWORD -destkeystore client.keystore.jks -srckeystore client.p12 -srcstoretype PKCS12 -srcstorepass $SSL_STORE_PASSWORD -noprompt\n\
keytool -import -file ca.pem -alias AivenCA -keystore client.truststore.jks -storepass $SSL_STORE_PASSWORD -noprompt\n\
\n\
exec java -jar app.jar' > entrypoint.sh

RUN chmod +x entrypoint.sh
ENTRYPOINT ["./entrypoint.sh"]
