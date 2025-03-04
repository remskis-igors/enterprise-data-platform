FROM golang:1.19-alpine
WORKDIR /app

# Copy module files from the current folder (which is go-service)
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the source code
COPY . .

# Build the Go binary
RUN go build -o go-service .

CMD ["./go-service"]
