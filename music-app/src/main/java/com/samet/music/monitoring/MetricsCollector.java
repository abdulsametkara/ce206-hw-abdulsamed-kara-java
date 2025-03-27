package com.samet.music.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    private static MetricsCollector instance;

    // Metrikler
    private final Counter songPlayCounter;
    private final Counter songAddCounter;
    private final Counter artistAddCounter;
    private final Counter albumAddCounter;
    private final Counter playlistAddCounter;
    private final Gauge activeUsersGauge;
    private final Histogram requestLatency;

    private HTTPServer server;

    private MetricsCollector() {
        // JVM ve sistem metriklerini otomatik olarak topla
        DefaultExports.initialize();

        // Şarkı çalma sayacı
        songPlayCounter = Counter.build()
                .name("music_app_song_plays_total")
                .help("Toplam şarkı çalma sayısı")
                .labelNames("artist", "genre")
                .register();

        // Ekleme sayaçları
        songAddCounter = Counter.build()
                .name("music_app_songs_added_total")
                .help("Eklenen toplam şarkı sayısı")
                .register();

        artistAddCounter = Counter.build()
                .name("music_app_artists_added_total")
                .help("Eklenen toplam sanatçı sayısı")
                .register();

        albumAddCounter = Counter.build()
                .name("music_app_albums_added_total")
                .help("Eklenen toplam albüm sayısı")
                .register();

        playlistAddCounter = Counter.build()
                .name("music_app_playlists_added_total")
                .help("Oluşturulan toplam çalma listesi sayısı")
                .register();

        // Aktif kullanıcı göstergesi
        activeUsersGauge = Gauge.build()
                .name("music_app_active_users")
                .help("Şu anda aktif olan kullanıcı sayısı")
                .register();

        // İstek gecikmesi histogram'ı
        requestLatency = Histogram.build()
                .name("music_app_request_latency_seconds")
                .help("İstek gecikme süresi")
                .labelNames("operation")
                .buckets(0.1, 0.5, 1.0, 2.0, 5.0)
                .register();

        try {
            // HTTP sunucusunu 8081 portunda başlat (8080 zaten uygulamanız tarafından kullanılıyor)
            server = new HTTPServer(8081);
            logger.info("Prometheus metrics server started on port 8081");
        } catch (IOException e) {
            logger.error("Failed to start Prometheus metrics server", e);
        }
    }

    public static synchronized MetricsCollector getInstance() {
        if (instance == null) {
            instance = new MetricsCollector();
        }
        return instance;
    }

    public void incrementSongPlay(String artist, String genre) {
        songPlayCounter.labels(artist, genre).inc();
    }

    public void incrementSongAdded() {
        songAddCounter.inc();
    }

    public void incrementArtistAdded() {
        artistAddCounter.inc();
    }

    public void incrementAlbumAdded() {
        albumAddCounter.inc();
    }

    public void incrementPlaylistAdded() {
        playlistAddCounter.inc();
    }

    public void setActiveUsers(int count) {
        activeUsersGauge.set(count);
    }

    public Histogram.Timer startRequestTimer(String operation) {
        return requestLatency.labels(operation).startTimer();
    }

    public void shutdown() {
        if (server != null) {
            server.stop();
        }
    }
}