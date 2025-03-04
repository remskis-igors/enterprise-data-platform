FROM golang:1.19-alpine

WORKDIR /app

COPY go-service/go.mod go-service/go.sum ./

RUN go mod download

COPY . .

RUN go build -o go-service ./go-service

CMD ["./go-service"]