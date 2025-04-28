# Bonus Content: Database Integration & Authentication

## 1. Database Integration with Docker (Bonus)

### Ne İşe Yarar?

- Uygulamanın verilerini kalıcı ve güvenli bir şekilde saklamak için dosya tabanlı depolama yerine modern, ilişkisel bir veritabanı (PostgreSQL veya MySQL) kullanılır.
- Docker ile veritabanı ortamı herkes için aynı ve kolayca yönetilebilir olur.
- Veritabanı şeması, ilişkiler ve kısıtlamalar ile veri tutarlılığı sağlanır.
- Bağlantı havuzu (connection pooling) ile performans ve ölçeklenebilirlik artar.
- Docker Compose ile uygulama ve veritabanı birlikte, kolayca başlatılıp yönetilebilir.

---

### Nasıl Uygulandı?

#### 1. Database Implementation

- **Kapsam:** Uygulama verileri (kullanıcılar, şarkılar, playlistler) artık dosya yerine ilişkisel veritabanında saklanıyor.
- **Nasıl:**  
  - PostgreSQL Docker imajı kullanıldı.
  - Veritabanı şeması (tablolar, ilişkiler, kısıtlamalar) JPA Entity sınıfları ve migration dosyaları ile tanımlandı.
  - Spring Data JPA ile veri erişim katmanı oluşturuldu.
  - HikariCP ile bağlantı havuzu sağlandı.

#### 2. Docker Configuration

- **Kapsam:** Veritabanı ve uygulama konteynerleri birlikte yönetiliyor.
- **Nasıl:**  
  - `docker-compose.yml` ile PostgreSQL ve uygulama servisi tanımlandı.
  - Volume ile veritabanı verileri kalıcı hale getirildi.
  - Ortam değişkenleriyle (env) veritabanı ayarları yönetildi.
  - Uygulama için ayrı bir Dockerfile oluşturuldu (isteğe bağlı).

**docker-compose.yml örneği:**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    container_name: musicapp-postgres
    environment:
      POSTGRES_DB: musicdb
      POSTGRES_USER: musicuser
      POSTGRES_PASSWORD: musicpass
    ports:
      - "5432:5432"
    volumes:
      - db_data:/var/lib/postgresql/data
    networks:
      - musicapp-network

  musicapp:
    build: .
    container_name: musicapp-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/musicdb
      SPRING_DATASOURCE_USERNAME: musicuser
      SPRING_DATASOURCE_PASSWORD: musicpass
    depends_on:
      - postgres
    ports:
      - "8080:8080"
    networks:
      - musicapp-network

volumes:
  db_data:

networks:
  musicapp-network:
```

**Dockerfile örneği (Java uygulaması için):**

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/music-app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Projede Nerede ve Nasıl Kullanıldı?

- **Veritabanı:** Tüm uygulama verileri (kullanıcı, şarkı, playlist) PostgreSQL veritabanında saklanır.
- **JPA/Hibernate:** Repository katmanı ile veritabanı işlemleri yapılır.
- **Bağlantı Havuzu:** HikariCP ile veritabanı bağlantıları yönetilir.
- **Docker:** Hem veritabanı hem uygulama konteyner olarak çalışır, kolayca başlatılır/durdurulur.
- **Volume:** Veritabanı verileri konteyner silinse bile kaybolmaz.
- **Ortam Değişkenleri:** Hem uygulama hem veritabanı için şifre, kullanıcı adı gibi bilgiler güvenli ve esnek şekilde yönetilir.

---

## Test Etmek/Göstermek İçin Ne Yapılır?

1. `docker-compose up` komutu ile hem veritabanı hem uygulama başlatılır.
2. Uygulama API'leri üzerinden veri eklenir, güncellenir, silinir.
3. Veritabanı verilerinin kalıcı olduğu, volume sayesinde konteyner silinse bile kaybolmadığı gösterilir.
4. Dilerseniz `pgAdmin` veya başka bir SQL aracı ile veritabanına bağlanıp tablolar, ilişkiler ve veriler incelenir.
5. Uygulama loglarında bağlantı havuzunun (HikariCP) aktif olduğu ve bağlantıların yönetildiği gözlemlenir.
6. Ortam değişkenleri değiştirilerek farklı veritabanı ayarları kolayca test edilir.

---

## Kısa Özet

- **Amaç:** Modern, güvenli, taşınabilir ve ölçeklenebilir bir veritabanı altyapısı sağlamak.
- **Nasıl:** Docker, PostgreSQL, JPA/Hibernate, HikariCP, volume ve environment variable kullanımı ile.
- **Projede Kullanımı:** Tüm veri işlemleri, kalıcılık ve veri bütünlüğü bu altyapı ile sağlanır.
- **Test/Gösterim:** Docker Compose ile başlat, API ile veri işle, volume ve bağlantı havuzunu gözlemle, veritabanını dışarıdan incele.

---

## 2. Authentication and Authorization (Bonus)

### Ne İşe Yarar?
- Kullanıcıların kimliğini doğrulamak (authentication) ve farklı rollerle (ör. user, admin) API erişimlerini kısıtlamak için merkezi bir kimlik yönetim sistemi (Keycloak) entegre edilmiştir.
- Sadece yetkili kullanıcılar belirli işlemleri yapabilir.
- Güvenli, token tabanlı erişim sağlanır.
- Roller ve izinler merkezi olarak yönetilir.
- Kullanıcı oturumları ve kimlik doğrulama işlemleri standart, güvenli ve ölçeklenebilir şekilde yönetilir.

---

### Nasıl Uygulandı?

#### 1. Keycloak Integration
- **Kapsam:** Kullanıcı kimlik doğrulama ve yetkilendirme işlemleri Keycloak ile yönetilir.
- **Nasıl:**
  - Docker ile Keycloak konteyneri başlatıldı.
  - Spring Boot uygulamasına Keycloak ve Spring Security bağımlılıkları eklendi (`pom.xml`).
  - `application.properties` dosyasında Keycloak bağlantı ayarları yapıldı.

#### 2. Set up Keycloak in your Docker Compose environment
- **Kapsam:** Keycloak, Docker Compose ile diğer servislerle birlikte kolayca başlatılır.
- **Nasıl:**
  - `docker-compose.yml` dosyasına Keycloak servisi eklendi.
  - Gerekli ortam değişkenleri (admin kullanıcı adı, şifre, veritabanı bağlantısı) tanımlandı.
  - Keycloak için ayrı bir veritabanı (PostgreSQL) servisi de eklenebilir.

**docker-compose.yml örneği:**
```yaml
keycloak:
  image: quay.io/keycloak/keycloak:21.1.1
  container_name: musicapp-keycloak
  environment:
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin
    KC_DB: postgres
    KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
    KC_DB_USERNAME: musicuser
    KC_DB_PASSWORD: musicpass
  ports:
    - "8180:8080"
  command: start-dev
  depends_on:
    - postgres
  networks:
    - musicapp-network
```

#### 3. Configure realms, clients, and roles
- **Kapsam:** Kimlik doğrulama ve yetkilendirme için gerekli yapıların Keycloak arayüzünde tanımlanması.
- **Nasıl:**
  - Keycloak arayüzünden `musicapp` realm oluşturuldu.
  - `music-app` client tanımlandı (public client, bearer-only, OpenID Connect).
  - `user` ve `admin` rolleri oluşturuldu.
  - Örnek kullanıcılar (user1, admin1) oluşturulup ilgili rollere atandı.

#### 4. Implement login/register functionality in your application
- **Kapsam:** Kullanıcılar sisteme giriş yapabilir ve yeni kullanıcı kaydı oluşturabilir.
- **Nasıl:**
  - Kayıt (register) işlemi için API'de yeni kullanıcı oluşturma endpointi sağlandı.
  - Giriş (login) işlemi için Keycloak token endpointi kullanıldı.
  - Kullanıcılar Keycloak üzerinden kimlik doğrulaması yapar, uygulama JWT token ile erişimi kontrol eder.

#### 5. Manage user sessions and tokens
- **Kapsam:** Kullanıcı oturumları ve erişim tokenları güvenli şekilde yönetilir.
- **Nasıl:**
  - Kullanıcı girişinde Keycloak tarafından JWT access token ve refresh token üretilir.
  - Uygulama, gelen isteklerde bu tokenları doğrular.
  - Oturum süresi, token yenileme ve geçersiz kılma işlemleri Keycloak tarafından yönetilir.

#### 6. Implement role-based access control
- **Kapsam:** Farklı rollerin farklı endpointlere erişimi sınırlandırılır.
- **Nasıl:**
  - Spring Security yapılandırmasında endpoint bazlı erişim kuralları tanımlandı.
  - `/api/users/**` endpointleri: Sadece user veya admin rolündeki kullanıcılar erişebilir.
  - `/api/admin/**` endpointleri: Sadece admin rolündeki kullanıcılar erişebilir.

**Spring Security örnek yapılandırma:**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .antMatchers("/api/public/**", "/api-docs/**", "/swagger-ui/**", "/api/actuator/**").permitAll()
            .antMatchers("/api/users/**").hasAnyRole("user", "admin")
            .antMatchers("/api/admin/**").hasRole("admin")
            .anyRequest().authenticated()
        .and()
            .oauth2ResourceServer().jwt();
}
```

---

### Projede Nerede ve Nasıl Kullanıldı?
- Tüm kullanıcı kimlik doğrulama ve yetkilendirme işlemleri Keycloak ile yönetilir.
- Kullanıcılar Keycloak üzerinden giriş yapar, uygulama JWT token ile erişimi kontrol eder.
- API endpointlerine erişim, kullanıcının rolüne göre sınırlandırılır.
- Kullanıcı oturumları ve token yönetimi merkezi olarak Keycloak tarafından sağlanır.
- Kayıt ve giriş işlemleri için API'de ilgili endpointler bulunur.

---

### Test Etmek/Göstermek İçin Ne Yapılır?
1. `docker-compose up` ile Keycloak ve uygulama başlatılır.
2. Keycloak arayüzünden realm, client, roller ve kullanıcılar oluşturulur.
3. Kullanıcı için token almak:
   ```
   curl -X POST "http://localhost:8180/realms/musicapp/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password&client_id=music-app&username=user1&password=password"
   ```
4. Aldığın token ile korumalı bir endpointi çağır:
   ```
   curl -X GET "http://localhost:8080/api/users" \
     -H "Authorization: Bearer [TOKEN]"
   ```
5. Yanlış rol ile admin endpointine erişmeye çalışırsan 403 Forbidden hatası alırsın.
6. Swagger UI'da endpointleri test etmeye çalıştığında, token olmadan erişim kısıtlanır.
7. Oturum yönetimi ve token yenileme işlemleri Keycloak arayüzünden veya API üzerinden gözlemlenebilir.

---

### Kısa Özet
- **Amaç:** Güvenli, merkezi ve ölçeklenebilir bir kimlik doğrulama ve yetkilendirme altyapısı sağlamak.
- **Nasıl:** Keycloak, Docker, Spring Security, JWT, roller ve merkezi oturum yönetimi ile.
- **Projede Kullanımı:** Tüm kullanıcı işlemleri, erişim kontrolleri ve oturum yönetimi bu altyapı ile sağlanır.
- **Test/Gösterim:** Docker Compose ile başlat, Keycloak arayüzünde yapılandır, token al ve API erişimini test et, rollerle erişim kısıtlamasını gözlemle.

---
