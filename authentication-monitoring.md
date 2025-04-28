# Authentication ve Monitoring Uygulaması

Bu doküman, Müzik Kütüphanesi uygulamasına eklenen Authentication (Keycloak) ve Monitoring (Prometheus/Actuator) özelliklerinin nasıl uygulandığını, ne işe yaradığını ve nasıl test edileceğini açıklar.

## 1. Monitoring (Prometheus ve Spring Boot Actuator)

### Ne İşe Yarar?

Monitoring sistemi, uygulamanın sağlık durumunu, performansını ve çeşitli metriklerini izlememizi sağlar. Bu sayede:

- Sistemin sağlık durumunu gerçek zamanlı olarak görebiliriz
- Performans sorunlarını tespit edebiliriz
- Kaynakların kullanımını izleyebiliriz (CPU, bellek, vb.)
- Hata durumlarını ve HTTP isteklerini takip edebiliriz

### Nasıl Uygulandı?

Monitoring sistemi iki ana bileşen kullanılarak uygulanmıştır:

1. **Spring Boot Actuator**: Spring uygulamasının çalışma durumunu izleyen ve metrikler sağlayan bir kütüphanedir.
2. **Prometheus**: Metrikleri toplayan ve depolayan bir monitoring sistemidir.

#### Bağımlılıklar

`pom.xml` dosyasına eklenen bağımlılıklar:

```xml
<!-- Monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

#### Yapılandırma

`application.properties` dosyasında yapılan yapılandırmalar:

```properties
# Actuator configuration
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
```

#### Docker Compose Entegrasyonu

`docker-compose.yml` dosyasında Prometheus servisi tanımlanmıştır:

```yaml
prometheus:
  image: prom/prometheus
  container_name: musicapp-prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  networks:
    - musicapp-network
```

### Projede Nerede ve Nasıl Kullanıldı?

Monitoring sistemi, projenin farklı kısımlarında şu şekilde kullanılmaktadır:

1. **API Performans İzleme**: Tüm REST endpointlerinin çağrı sayıları, yanıt süreleri ve hata oranları izlenir.
2. **Sistem Kaynakları İzleme**: CPU, bellek ve disk kullanımı gibi metrikler toplanır.
3. **Özel Metrikler**: Müzik uygulamasına özgü metrikler (örn. çalınan şarkı sayısı, aktif kullanıcı sayısı) tanımlanabilir.

### Nasıl Test Edilir?

Monitoring sistemini test etmek için:

1. Prometheus arayüzüne erişin: http://localhost:9090
2. Actuator endpointleri görüntüleyin: http://localhost:8080/api/actuator
3. Prometheus metriklerini görüntüleyin: http://localhost:8080/api/actuator/prometheus
4. Sağlık durumunu kontrol edin: http://localhost:8080/api/actuator/health

## 2. Authentication ve Authorization (Keycloak)

### Ne İşe Yarar?

Keycloak entegrasyonu, uygulamanın güvenlik altyapısını sağlar. Bu sayede:

- Kullanıcı kimlik doğrulama (authentication) yapılır
- Kullanıcı yetkilendirme (authorization) kontrolleri yapılır
- Roller ve izinler yönetilir
- Single Sign-On (SSO) imkanı sunulur
- Token tabanlı güvenlik (JWT) uygulanır

### Nasıl Uygulandı?

Keycloak entegrasyonu aşağıdaki adımlarla uygulanmıştır:

1. **Keycloak Sunucusu**: Docker ile çalıştırılan bir Keycloak sunucusu
2. **Spring Security Entegrasyonu**: Spring Security ve Keycloak adaptörleri kullanılarak yapılan entegrasyon

#### Bağımlılıklar

`pom.xml` dosyasına eklenen bağımlılıklar:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Keycloak Integration -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-spring-boot-starter</artifactId>
    <version>21.1.1</version>
</dependency>
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-spring-security-adapter</artifactId>
    <version>21.1.1</version>
</dependency>
```

#### Yapılandırma

`application.properties` dosyasında yapılan Keycloak yapılandırması:

```properties
# Keycloak configuration
keycloak.enabled=true
keycloak.realm=musicapp
keycloak.auth-server-url=http://localhost:8180/auth
keycloak.resource=music-app
keycloak.public-client=true
keycloak.bearer-only=true
keycloak.principal-attribute=preferred_username

# Security configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=${keycloak.auth-server-url}/realms/${keycloak.realm}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/certs
```

#### Keycloak Yapılandırması

Keycloak sunucusunda yapılan yapılandırmalar:

1. **"musicapp" Realm** oluşturuldu
2. **"music-app" Client** tanımlandı
3. **Roller oluşturuldu**: "user" ve "admin" 
4. **Örnek kullanıcılar**: "user1" (user rolü) ve "admin1" (admin rolü)

#### Spring Security Yapılandırması

Web güvenlik yapılandırması şu şekilde düzenlenmiştir:

- `/api/public/**`, `/api-docs/**`, `/swagger-ui/**`, `/api/actuator/**` - Herkese açık
- `/api/users/**` - USER veya ADMIN rolüne sahip kullanıcılar erişebilir 
- `/api/admin/**` - Sadece ADMIN rolüne sahip kullanıcılar erişebilir

### Projede Nerede ve Nasıl Kullanıldı?

Keycloak entegrasyonu, projenin farklı kısımlarında şu şekilde kullanılmaktadır:

1. **API Güvenliği**: REST API endpointlerinin korunması ve erişim kontrolü
2. **Rol Tabanlı Erişim Kontrolü**: Farklı kullanıcı rollerine göre erişim kısıtlamaları
3. **Token Doğrulama**: JWT token yapısı kullanılarak kimlik doğrulama ve yetkilendirme

### Nasıl Test Edilir?

Keycloak entegrasyonunu test etmek için:

1. **Token Alma**:
   ```
   curl -X POST "http://localhost:8180/realms/musicapp/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password&client_id=music-app&username=user1&password=password"
   ```

2. **Token ile API Çağrısı**:
   ```
   curl -X GET "http://localhost:8080/api/users" \
     -H "Authorization: Bearer [TOKEN]"
   ```

3. **Swagger UI ile Test**:
   - Swagger UI arayüzüne erişin: http://localhost:8080/api/swagger-ui.html
   - "Authorize" butonu görünmüyorsa, doğrudan bir endpoint seçip "Try it out" ile test edebilirsiniz
   - Farklı rollerle erişim kontrolünü test etmek için user1 (user rolü) ve admin1 (admin rolü) kullanıcılarıyla deneme yapın

## Özet

Bu projede entegre edilen Authentication ve Monitoring sistemleri, modern bir web uygulamasının temel bileşenlerini oluşturmaktadır. Keycloak ile güvenli bir kimlik doğrulama ve yetkilendirme sistemi kurulmuş, Prometheus ve Spring Actuator ile de uygulamanın performansını ve sağlık durumunu izlemek için güçlü bir altyapı sağlanmıştır.

Bu sistemler sayesinde:
- Güvenli bir kullanıcı kimlik doğrulama sistemi
- Rol tabanlı erişim kontrolü
- Gerçek zamanlı performans izleme
- Sistem sağlığı takibi 
- Detaylı metrik toplama

özellikleri projeye kazandırılmıştır. 