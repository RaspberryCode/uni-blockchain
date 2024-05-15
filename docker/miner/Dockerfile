# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file into the container
COPY blockchain-miner/build/libs/blockchain-miner-node-*.jar /app/miner.jar

# Make port 5000 available to the world outside this container
EXPOSE 5000

# Define environment variable for port
ENV MINER_PORT=5000

# Run miner.jar when the container launches
CMD ["java", "-jar", "miner.jar", "-Dport=${MINER_PORT}"]
