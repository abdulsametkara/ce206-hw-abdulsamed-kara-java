package com.samet.music.main;

import com.samet.music.dao.DAOFactory;
import com.samet.music.service.MusicCollectionService;
import com.samet.music.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uygulama başlatıcı sınıfı
 * Uygulama bileşenlerinin başlatılmasından sorumludur
 */
public class MusicAppInitializer {
    // Loglama için logger nesnesi
    private static final Logger logger = LoggerFactory.getLogger(MusicAppInitializer.class);

    // Singleton instance
    private static volatile MusicAppInitializer instance;

    /**
     * Singleton tasarım deseni için özel yapıcı metot
     * Doğrudan örnek oluşturulmasını engeller
     */
    private MusicAppInitializer() {
        // Özel yapıcı
    }

    /**
     * Singleton instance'ı döndürür
     * @return MusicAppInitializer instance
     */
    public static MusicAppInitializer getInstance() {
        if (instance == null) {
            synchronized (MusicAppInitializer.class) {
                if (instance == null) {
                    instance = new MusicAppInitializer();
                }
            }
        }
        return instance;
    }

    /**
     * Uygulamayı başlatır
     * Veritabanı yöneticisi, DAO fabrikası ve servis katmanını başlatır
     */
    public void initialize() {
        logger.info("Müzik uygulaması başlatılıyor...");

        try {
            // Veritabanı yöneticisini başlat
            DatabaseManager.getInstance();
            logger.info("Veritabanı yöneticisi başarıyla başlatıldı");

            // DAO fabrikasını başlat
            DAOFactory.getInstance();
            logger.info("DAO fabrikası başarıyla başlatıldı");

            // Servis katmanını başlat
            MusicCollectionService.getInstance();
            logger.info("Servis katmanı başarıyla başlatıldı");

            logger.info("Uygulama başarıyla başlatıldı");
        } catch (Exception e) {
            logger.error("Uygulama başlatılamadı: {}", e.getMessage(), e);
            throw new RuntimeException("Uygulama başlatma başarısız", e);
        }
    }

    /**
     * Veritabanını sıfırlar (test/geliştirme için)
     * @param resetDatabase veritabanını sıfırlama bayrağı
     */
    public void resetDatabase(boolean resetDatabase) {
        if (resetDatabase) {
            try {
                logger.info("Veritabanı sıfırlanıyor...");
                DatabaseManager.getInstance().setShouldResetDatabase(true);
                DatabaseManager.getInstance().initializeDatabase();
                MusicCollectionService.getInstance().reinitializeDatabase();
                logger.info("Veritabanı başarıyla sıfırlandı");
            } catch (Exception e) {
                logger.error("Veritabanı sıfırlanamadı: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Uygulamayı kapatır
     * Veritabanı bağlantılarını kapatır ve kaynakları temizler
     */
    public void shutdown() {
        logger.info("Uygulama kapatılıyor...");

        try {
            // Veritabanı bağlantılarını kapat
            DatabaseManager.getInstance().closeAllConnections();
            logger.info("Veritabanı bağlantıları kapatıldı");

            logger.info("Uygulama kapatma işlemi tamamlandı");
        } catch (Exception e) {
            logger.error("Uygulama kapatma sırasında hata: {}", e.getMessage(), e);
        }
    }
}