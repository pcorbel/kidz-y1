FROM ubuntu:20.04

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies needed for ROM building
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    zip \
    git \
    build-essential \
    zlib1g-dev \
    file \
    && rm -rf /var/lib/apt/lists/*

# Build simg2img from source (needed to convert Android sparse images)
RUN git clone https://github.com/anestisb/android-simg2img.git /tmp/android-simg2img && \
    cd /tmp/android-simg2img && \
    make && \
    cp simg2img /usr/local/bin/ && \
    chmod +x /usr/local/bin/simg2img && \
    rm -rf /tmp/android-simg2img

# Download xxd binary for ARM
RUN wget -q https://raw.githubusercontent.com/Zackptg5/Cross-Compiled-Binaries-Android/master/xxd/xxd-arm -O /usr/local/bin/xxd && \
    chmod +x /usr/local/bin/xxd

# Download and extract base ROM from GitHub
RUN mkdir -p /opt/base-rom && \
    wget -q https://github.com/y1-community/y1-stock-rom/releases/download/Latest-3.0.2/rom.zip -O /tmp/base-rom.zip && \
    unzip -q /tmp/base-rom.zip -d /opt/base-rom && \
    rm -f /tmp/base-rom.zip

# Copy entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Set working directory
WORKDIR /workspace

# Set entrypoint
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
