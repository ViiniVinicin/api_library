# Usamos a imagem oficial do Eclipse Temurin (Java 21 LTS) versão Alpine (Super leve)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR
COPY target/api_library-*.jar app.jar

# Define o perfil de produção
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]