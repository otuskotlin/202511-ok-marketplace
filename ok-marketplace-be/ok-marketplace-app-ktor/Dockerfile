FROM --platform=linux/amd64 ubuntu:22.04
COPY ok-marketplace-app-ktor.kexe /app/
COPY application.yaml /app/
RUN apt update \
  && apt install -y libpq-dev curl \
  && rm -rf /var/cache/apt/archives /var/lib/apt/lists
EXPOSE 8080
WORKDIR /app
ENTRYPOINT ["/app/ok-marketplace-app-ktor.kexe", "-config=./application.yaml"]
