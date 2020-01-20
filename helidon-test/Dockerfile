FROM gradle:jdk11 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml .
RUN mvn package -DskipTests

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD src src
RUN mvn package -DskipTests

RUN echo "done!"

# 2nd stage, build the runtime image
FROM mcr.microsoft.com/java/jre:11u5-zulu-alpine
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/build/helidon-quickstart-se.jar ./
COPY --from=build /helidon/build/libs ./libs

CMD ["java", "-jar", "helidon-quickstart-se.jar"]

EXPOSE 8080
