# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file into the container
COPY user-api-node/build/libs/user-api-node-*.jar /app/user-api-node.jar

# Make port 5000 available to the world outside this container
EXPOSE 5000

# Define environment variable for port
ENV MINER_PORT=5000

# Run user-api-node.jar when the container launches
CMD ["java", "-jar", "user-api-node.jar", "-Dport=${MINER_PORT}"]
