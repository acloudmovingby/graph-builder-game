# Stage 1: Build the Scala.js code using a Mill-specific image
FROM nightscape/scala-mill:latest AS scala-builder

WORKDIR /app/public/scala

# Copy the Scala project files
COPY public/scala .

# Forcefully remove the 'out' directory and then compile the project
RUN rm -rf out && mill graphcontroller.fastLinkJS

# Stage 2: Build the final, lightweight production image
FROM node:18-slim

WORKDIR /app

# Copy package files and install production Node dependencies
COPY package*.json ./
RUN npm install --production

# Copy the rest of your application code
COPY . .

# Copy the compiled JavaScript from the build stage
COPY --from=scala-builder /app/public/scala/out /app/public/scala/out

EXPOSE 3000
CMD [ "bash", "start.sh" ]
