# Build React UI
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Build Spring Boot jar with static assets baked in
FROM eclipse-temurin:17-jdk-alpine AS backend-build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN ./mvnw clean package -DskipTests -B

# Runtime image: serves React + REST API from one container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/uapReleaseFilesChatbot-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx384m -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
