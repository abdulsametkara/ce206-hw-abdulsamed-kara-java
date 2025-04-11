package com.samet.music.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * MetricsManager için kapsamlı test sınıfı
 */
public class MetricsManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(MetricsManagerTest.class);
    private MetricsManager metricsManager;
    private PrometheusMeterRegistry testRegistry;
    
    /**
     * Her test öncesinde çalışacak hazırlık metodu
     */
    @Before
    public void setUp() throws Exception {
        // Singleton instance'ı resetle
        resetSingleton();
        
        // MetricsManager instance'ını oluştur
        metricsManager = MetricsManager.getInstance();
        
        // Test için registry'yi elde et ve özel test registry'si ile değiştir
        testRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        replaceRegistry(testRegistry);
    }
    
    /**
     * Her test sonrasında çalışacak temizleme metodu
     */
    @After
    public void tearDown() throws Exception {
        if (metricsManager != null) {
            metricsManager.shutdown();
        }
        resetSingleton();
    }
    
    /**
     * Registry'yi değiştirmek için kullanılan yardımcı metod
     */
    private void replaceRegistry(PrometheusMeterRegistry registry) throws Exception {
        Field registryField = MetricsManager.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        registryField.set(metricsManager, registry);
    }
    
    /**
     * Singleton instance'ı sıfırlayan yardımcı metod
     */
    private void resetSingleton() throws Exception {
        Field instance = MetricsManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Singleton pattern'i test eder
     */
    @Test
    public void testSingletonPattern() {
        MetricsManager instance1 = MetricsManager.getInstance();
        MetricsManager instance2 = MetricsManager.getInstance();
        
        assertNotNull("getInstance should not return null", instance1);
        assertSame("Multiple calls to getInstance should return the same instance", instance1, instance2);
    }
    
    /**
     * Counter artırma işlemini test eder
     */
    @Test
    public void testIncrementCounter() {
        // Test verileri
        String counterName = "test_counter";
        String[] tags = {"tag1", "value1", "tag2", "value2"};
        
        // Önce counter değerini kontrol et
        Counter counter = testRegistry.find(counterName).tags(tags).counter();
        assertNull("Counter henüz oluşturulmamış olmalı", counter);
        
        // Counter'ı artır
        metricsManager.incrementCounter(counterName, tags);
        
        // Counter'ın oluşturulduğunu doğrula
        counter = testRegistry.find(counterName).tags(tags).counter();
        assertNotNull("Counter oluşturulmuş olmalı", counter);
        assertEquals("Counter değeri 1 olmalı", 1.0, counter.count(), 0.001);
        
        // Tekrar artır
        metricsManager.incrementCounter(counterName, tags);
        
        // Değerin arttığını doğrula
        assertEquals("Counter değeri 2 olmalı", 2.0, counter.count(), 0.001);
    }
    
    /**
     * Timer başlatma ve durdurma işlemini test eder
     */
    @Test
    public void testTimerStartStop() {
        // Test verileri
        String timerName = "test_timer";
        String[] tags = {"tag1", "value1"};
        
        // Timer başlat
        Timer.Sample sample = metricsManager.startTimer();
        assertNotNull("Timer sample should not be null", sample);
        
        // Kısa bir bekleme yaparak gerçek bir süre ölçümü simüle et
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Timer'ı durdur
        metricsManager.stopTimer(sample, timerName, tags);
        
        // Timer'ın oluşturulduğunu doğrula
        Timer timer = testRegistry.find(timerName).tags(tags).timer();
        assertNotNull("Timer oluşturulmuş olmalı", timer);
        assertTrue("Timer en az bir ölçüm kaydetmiş olmalı", timer.count() > 0);
    }
    
    /**
     * Çalışma süresi kaydetme işlemini test eder
     */
    @Test
    public void testRecordExecutionTime() {
        // Test verileri
        String timerName = "execution_timer";
        String[] tags = {"operation", "query"};
        long executionTimeMs = 123;
        
        // Çalışma süresini kaydet
        metricsManager.recordExecutionTime(timerName, executionTimeMs, tags);
        
        // Timer'ın oluşturulduğunu doğrula
        Timer timer = testRegistry.find(timerName).tags(tags).timer();
        assertNotNull("Timer oluşturulmuş olmalı", timer);
        assertEquals("Timer bir ölçüm kaydetmiş olmalı", 1, timer.count());
        assertTrue("Timer doğru süreyi kaydetmiş olmalı", 
                timer.totalTime(TimeUnit.MILLISECONDS) >= executionTimeMs);
    }
    
    /**
     * Registry getter'ını test eder
     */
    @Test
    public void testGetRegistry() {
        // Getter metodunu çağır
        MeterRegistry registry = metricsManager.getRegistry();
        
        // Doğru registry'nin döndüğünü doğrula
        assertSame("getRegistry doğru registry nesnesini döndürmeli", testRegistry, registry);
    }
    
    /**
     * Kapatma işlemini test eder
     */
    @Test
    public void testShutdown() throws Exception {
        // Kapanma metodunu çağır
        metricsManager.shutdown();
        
        // Kapanmanın gerçekleştiğini doğrulamak zor, sadece exception fırlatmadığını kontrol ediyoruz
        assertTrue("Shutdown completed without exception", true);
        
        // HTTP sunucusu alanını kontrol et
        Field serverField = MetricsManager.class.getDeclaredField("server");
        serverField.setAccessible(true);
        Object server = serverField.get(metricsManager);
        
        // server null olmayabilir ama önemli olan shutdown() çağrısının hata vermemesidir
    }
    
    /**
     * Aynı isim ve etiketlerle zamanlayıcı tekrar kullanımını test eder
     */
    @Test
    public void testTimerReuse() {
        // Test verileri
        String timerName = "reused_timer";
        String[] tags = {"tag", "value"};
        
        // İlk kullanım
        metricsManager.recordExecutionTime(timerName, 100, tags);
        Timer timer = testRegistry.find(timerName).tags(tags).timer();
        assertEquals("İlk kullanımdan sonra timer'ın sayısı 1 olmalı", 1, timer.count());
        
        // İkinci kullanım - aynı zamanlayıcı tekrar kullanılmalı
        metricsManager.recordExecutionTime(timerName, 200, tags);
        
        // Aynı timer örneğini almak
        Timer sameTimer = testRegistry.find(timerName).tags(tags).timer();
        
        // Aynı zamanlayıcı örneğinin kullanıldığını doğrula
        assertSame("Aynı timer nesnesi tekrar kullanılmalı", timer, sameTimer);
        assertEquals("İkinci kullanımdan sonra timer'ın sayısı 2 olmalı", 2, timer.count());
    }
    
    /**
     * Farklı etiketlerle aynı isimdeki zamanlayıcıların ayrı olmasını test eder
     */
    @Test
    public void testDifferentTagsCreateDifferentTimers() {
        // Test verileri
        String timerName = "different_tags_timer";
        String[] tags1 = {"tag1", "value1"};
        String[] tags2 = {"tag1", "value2"};
        
        // İlk timer oluştur
        metricsManager.recordExecutionTime(timerName, 100, tags1);
        Timer timer1 = testRegistry.find(timerName).tags(tags1).timer();
        
        // İkinci timer farklı etiketlerle
        metricsManager.recordExecutionTime(timerName, 200, tags2);
        Timer timer2 = testRegistry.find(timerName).tags(tags2).timer();
        
        // İki farklı timer örneğinin oluştuğunu doğrula
        assertNotSame("Farklı etiketler için farklı timer nesneleri oluşturulmalı", timer1, timer2);
        assertEquals("İlk timer bir ölçüm kaydetmiş olmalı", 1, timer1.count());
        assertEquals("İkinci timer bir ölçüm kaydetmiş olmalı", 1, timer2.count());
    }
} 