FROM rust:1.56
WORKDIR /app
COPY Cargo.toml .
COPY src ./src
RUN cargo build --release
CMD ["./target/release/rust-service"]
