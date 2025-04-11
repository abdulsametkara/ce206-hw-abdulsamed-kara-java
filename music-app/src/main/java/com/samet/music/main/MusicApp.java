package com.samet.music.main;

import com.samet.music.service.MusicCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Ana uygulama sınıfı - Müzik kütüphanesi uygulamasının başlangıç noktası
 * Bu sınıf uygulamanın başlatılması, başlatma ve kapatma işlemlerini yönetir
 */
public class MusicApp {
  // Loglama için logger nesnesi
  private static final Logger logger = LoggerFactory.getLogger(MusicApp.class);

  /**
   * Uygulama giriş noktası
   * @param args Komut satırı argümanları
   */
  public static void main(String[] args) {
    logger.info("Müzik uygulaması başlatılıyor...");

    try {
      // Uygulamayı başlat
      MusicAppInitializer initializer = MusicAppInitializer.getInstance();
      initializer.initialize();

      // Kullanıcı girişi için scanner ve çıktı için PrintStream oluştur
      Scanner scanner = new Scanner(System.in);
      Music music = new Music(scanner, System.out);

      // Ana menüyü başlat - Kullanıcı kimlik bilgileri dosyası yolu ile
      music.mainMenu("data/user_credentials.txt");

      // Uygulamayı kapat
      initializer.shutdown();

      // Kaynakları temizle
      scanner.close();

      logger.info("Uygulama başarıyla sonlandırıldı");
    } catch (Exception e) {
      logger.error("Uygulama hata ile sonlandırıldı: {}", e.getMessage(), e);
      System.exit(1);
    }
  }
}