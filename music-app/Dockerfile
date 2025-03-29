FROM ubuntu:20.04

ENV TZ=Europe/Istanbul
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    libgtk-3-0 \
    libglib2.0-0 \
    libcairo2 \
    libpango-1.0-0 \
    libpangocairo-1.0-0 \
    libxtst6 \
    libxi6 \
    libxrender1 \
    libxext6 \
    libglvnd0 \
    libdbus-glib-1-2 \
    xorg \
    xvfb \
    x11vnc \
    fluxbox \
    wget \
    unzip \
    net-tools \
    supervisor \
    tigervnc-common \
    novnc

# JavaFX bileşenlerini indirip kur
RUN mkdir -p /opt/javafx && \
    wget https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_linux-x64_bin-sdk.zip -O /tmp/javafx.zip && \
    unzip /tmp/javafx.zip -d /opt && \
    mv /opt/javafx-sdk-17.0.2/* /opt/javafx/ && \
    rm -rf /opt/javafx-sdk-17.0.2 && \
    rm /tmp/javafx.zip

WORKDIR /app

COPY target/music-app-1.0-SNAPSHOT.jar /app/music-app.jar
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

RUN mkdir -p /app/data
ENV DB_TYPE=sqlite
ENV DB_PATH=/app/data/music_library.db
ENV DISPLAY=:1
ENV MAINCLASS=com.samet.music.ui.gui.MusicLibraryApp

RUN mkdir -p /root/.vnc && \
    echo "password" | vncpasswd -f > /root/.vnc/passwd && \
    chmod 600 /root/.vnc/passwd

# noVNC kurulumunu kontrol et ve gerekirse yeniden kur
RUN if [ ! -f "/usr/share/novnc/utils/launch.sh" ]; then \
    mkdir -p /usr/share/novnc && \
    wget -qO- https://github.com/novnc/noVNC/archive/v1.3.0.tar.gz | tar xz --strip 1 -C /usr/share/novnc && \
    wget -qO- https://github.com/novnc/websockify/archive/v0.10.0.tar.gz | tar xz --strip 1 -C /usr/share/novnc/utils/websockify; \
    fi

# JavaFX dosyalarını listele (debug için)
RUN ls -la /opt/javafx/lib/

EXPOSE 5901 6080

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]