# Stage 1: Build using an Ubuntu base image
FROM ubuntu:22.04 AS scala-builder

# Set frontend to noninteractive to avoid prompts during install
ENV DEBIAN_FRONTEND=noninteractive

# Install all dependencies: curl, Java, and Node.js
# (Node.js is needed to run the unit tests on the Scala.js)
RUN apt-get update && \
    apt-get install -y curl openjdk-11-jdk && \
    curl -fsSL https://deb.nodesource.com/setup_lts.x | bash - && \
    apt-get install -y nodejs

# Install Mill
RUN curl -L https://github.com/com-lihaoyi/mill/releases/download/0.11.7/0.11.7 > /usr/local/bin/mill && \
    chmod +x /usr/local/bin/mill

# Replace fastLinkJS.dest with fullLinkJS.dest in main.html for production
WORKDIR /app/public/main_html_css_files
COPY public/main_html_css_files .
RUN sed -i 's/fastLinkJS.dest/fullLinkJS.dest/g' main.html

WORKDIR /app/public/scalajs

# Copy the Scala project files
COPY public/scalajs .

# Forcefully remove the 'out' directory and then compile the project
RUN rm -rf out && ./mill graphcontroller.fullLinkJS

# Run unit tests
RUN ./mill graphcontroller.test

# Stage 2: Build the final, lightweight production image
FROM node:18-slim

WORKDIR /app

# Copy package files and install production Node dependencies
COPY package*.json ./
RUN npm install --production

# Copy the rest of your application code
COPY . .

# Copy the compiled JavaScript from the build stage
COPY --from=scala-builder /app/public/scalajs/out /app/public/scalajs/out

COPY --from=scala-builder /app/public/main_html_css_files/main.html /app/public/main_html_css_files/main.html

EXPOSE 3000
CMD [ "bash", "start.sh" ]
