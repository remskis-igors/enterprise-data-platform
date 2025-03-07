# Use multi-stage build to minimize image size
FROM python:3.9-slim as builder

WORKDIR /app

# Copy and install dependencies
COPY requirements.txt .
RUN pip install --user --no-cache-dir -r requirements.txt

# Copy the source code
COPY aggregator.py .

# Final build stage
FROM python:3.9-slim

WORKDIR /app

# Copy installed dependencies from the builder stage
COPY --from=builder /root/.local /root/.local
COPY --from=builder /app/aggregator.py .

# Ensure scripts in .local are executable
ENV PATH=/root/.local/bin:$PATH

# Expose port for Flask
EXPOSE 5000

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:5000/health || exit 1

# Run the Flask application
CMD ["python", "aggregator.py"]