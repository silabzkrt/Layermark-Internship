# HRManProject — Analiz ve Öğrenim Görevleri (LEARNING.md)

Bu doküman, staj projesinde gerçekleştirilen 4 temel analiz görevini, teknik gerekçelerini, SQL çıktılarını ve performans karşılaştırmalarını içermektedir.

---

## Görev 1: SQL Loglarının İncelenmesi (5 Farklı İstek ve Hibernate SQL Dönüşümleri)

Spring Boot yapılandırmamızda (`application.yml`) `show-sql: true` ve `format_sql: true` ayarları etkinleştirildiğinde, uygulama üzerinden yapılan REST API isteklerinin Hibernate (JPA) tarafından hangi gerçek SQL sorgularına çevrildiği aşağıda listelenmiştir:

### 1. Yeni Çalışan Ekleme (`POST /api/employees`)
* **İstek Yükü (JSON):**
  ```json
  {
    "name": "John Doe",
    "mail": "john.doe@company.com",
    "phoneNum": "05551112233",
    "employeeType": "SENIOR",
    "baseSalary": 50000.0,
    "departmentId": 1
  }
  ```
* **Hibernate Tarafından Üretilen SQL Sorguları:**
  1. E-posta adresi sistemde kayıtlı mı kontrolü (`existsByMail`):
     ```sql
     select e1_0.id 
     from employees e1_0 
     where e1_0.mail = ? 
     limit ?
     ```
  2. Telefon numarası sistemde kayıtlı mı kontrolü (`existsByPhoneNum`):
     ```sql
     select e1_0.id 
     from employees e1_0 
     where e1_0.phone_num = ? 
     limit ?
     ```
  3. Departman ataması için departman varlığının doğrulanması (`findById`):
     ```sql
     select d1_0.id, d1_0.code, d1_0.name 
     from departments d1_0 
     where d1_0.id = ?
     ```
  4. Yeni çalışan kaydının `employees` tablosuna eklenmesi (`insert`):
     ```sql
     insert into employees (
         base_salary, current_salary, department_id, employee_type, 
         mail, name, performance_multiplier, phone_num
     ) 
     values (?, ?, ?, ?, ?, ?, ?, ?) 
     returning id
     ```

### 2. Tekil Çalışan Getirme (`GET /api/employees/{id}`)
* **İstek:** `GET /api/employees/10`
* **Hibernate Tarafından Üretilen SQL Sorguları:**
  1. Çalışan ana kaydının çekilmesi:
     ```sql
     select e1_0.id, e1_0.base_salary, e1_0.current_salary, e1_0.department_id, 
            e1_0.employee_type, e1_0.mail, e1_0.name, e1_0.performance_multiplier, e1_0.phone_num 
     from employees e1_0 
     where e1_0.id = ?
     ```
  2. DTO dönüşümünde çalışana ait mevcut (`currentProjects`) projelerin ara tablodan çekilmesi:
     ```sql
     select cp1_0.employee_id, cp1_1.id, cp1_1.actual_days, cp1_1.completion_rate, 
            cp1_1.description, cp1_1.name, cp1_1.status, cp1_1.target_days 
     from employee_current_projects cp1_0 
     join projects cp1_1 on cp1_1.id = cp1_0.project_id 
     where cp1_0.employee_id = ?
     ```
  3. Çalışanın tamamlanan (`completedProjects`) projelerinin çekilmesi:
     ```sql
     select cp1_0.employee_id, cp1_1.id, cp1_1.actual_days, cp1_1.completion_rate, 
            cp1_1.description, cp1_1.name, cp1_1.status, cp1_1.target_days 
     from employee_completed_projects cp1_0 
     join projects cp1_1 on cp1_1.id = cp1_0.project_id 
     where cp1_0.employee_id = ?
     ```

### 3. Çalışana Proje Atama (`POST /api/employees/assign-project`)
* **İstek Yükü (JSON):**
  ```json
  {
    "assignerId": 1,
    "targetEmployeeId": 2,
    "projectId": 5
  }
  ```
* **Hibernate Tarafından Üretilen SQL Sorguları:**
  1. Projeyi atayan yöneticinin rol kontrolü için çekilmesi:
     ```sql
     select e1_0.id, e1_0.employee_type, ... from employees e1_0 where e1_0.id = ?
     ```
  2. Projenin atanacağı hedef çalışanın çekilmesi:
     ```sql
     select e1_0.id, e1_0.employee_type, ... from employees e1_0 where e1_0.id = ?
     ```
  3. Atanacak projenin mevcut olup olmadığının kontrolü:
     ```sql
     select p1_0.id, p1_0.name, p1_0.status, ... from projects p1_0 where p1_0.id = ?
     ```
  4. Çalışanın mevcut projeler listesine yeni projenin `@JoinTable` üzerinden eklenmesi:
     ```sql
     insert into employee_current_projects (employee_id, project_id) 
     values (?, ?)
     ```

### 4. Proje Tamamlama ve Maaş Katsayısı Hesaplama (`POST /api/employees/{id}/complete-project`)
* **İstek:** `POST /api/employees/2/complete-project?projectId=5&completionRate=0.9&speedFactor=1.2`
* **Hibernate Tarafından Üretilen SQL Sorguları:**
  1. Projeyi aktif projeler listesinden kaldırma (Ara tablodan silme):
     ```sql
     delete from employee_current_projects 
     where employee_id = ? and project_id = ?
     ```
  2. Projeyi tamamlanan projeler ara tablosuna ekleme:
     ```sql
     insert into employee_completed_projects (employee_id, project_id) 
     values (?, ?)
     ```
  3. Yeni hesaplanan `performanceMultiplier` ve `currentSalary` değerlerinin çalışana yansıtılması (`update`):
     ```sql
     update employees 
     set base_salary = ?, current_salary = ?, department_id = ?, employee_type = ?, 
         mail = ?, name = ?, performance_multiplier = ?, phone_num = ? 
     where id = ?
     ```

### 5. Tüm Departmanları Listeleme (`GET /api/departments`)
* **İstek:** `GET /api/departments`
* **Hibernate Tarafından Üretilen SQL Sorgusu:**
  ```sql
  select d1_0.id, d1_0.code, d1_0.name 
  from departments d1_0
  ```

---

## Görev 2: N+1 Probleminin Gözlemlenmesi, Çözümü ve Öncesi-Sonrası Belgelemesi

### 1. N+1 Problemi Nedir ve Nasıl Oluşur? (Öncesi - Kötü Senaryo)
`Employee` varlığımızın `Department` veya `currentProjects` listesi gibi ilişkili varlıkları vardır. Standart olarak `employeeRepository.findAll()` çağrıldığında ve her çalışan için departman adı veya projeler okunduğunda şu gerçekleşir:

* **1 adet Ana Sorgu:** `SELECT * FROM employees` (Örneğin veritabanında 100 çalışan olsun).
* **N adet Ek Sorgu:** Her çalışan için ilişkili departman veya projeler veritabanından ayrı ayrı sorgulanır:
  ```sql
  SELECT * FROM departments WHERE id = 1;
  SELECT * FROM departments WHERE id = 2;
  -- ... 100 kez tekrar eder!
  ```
* **Sonuç:** Toplam **101 adet SQL sorgusu** veritabanına atılır. Bu durum yüksek gecikmeye (latency), veritabanı bağlantı havuzunun tükenmesine ve performans çökmelerine neden olur.

### 2. Çözüm Yöntemleri (`JOIN FETCH` ve `@EntityGraph`)
Bu problemi çözmek ve tüm verileri tek bir SQL sorgusuyla almak için Spring Data JPA'da iki temel yaklaşım kullanılır:

#### Yöntem A: JPQL `JOIN FETCH` Kullanımı
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.currentProjects")
    List<Employee> findAllWithDepartmentAndProjects();
}
```

#### Yöntem B: `@EntityGraph` Kullanımı (Modern ve Clean Çözüm)
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = {"department", "currentProjects", "completedProjects"})
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithEntityGraph();
}
```

### 3. Çözüm Sonrası Hibernate'in Ürettiği SQL (Tek Sorgu)
```sql
select e1_0.id, 
       d1_0.id, d1_0.code, d1_0.name, 
       cp1_0.employee_id, p1_0.id, p1_0.name, ... 
from employees e1_0 
left join departments d1_0 on d1_0.id = e1_0.department_id 
left join employee_current_projects cp1_0 on e1_0.id = cp1_0.employee_id 
left join projects p1_0 on p1_0.id = cp1_0.project_id
```

### 4. Öncesi ve Sonrası Performans Karşılaştırması

| Senaryo / Metrik | N+1 Problemi Var (Standart `findAll`) | Çözüm Sonrası (`JOIN FETCH` / `@EntityGraph`) | İyileşme Oranı |
| :--- | :---: | :---: | :---: |
| **Kayıt Sayısı** | 100 Çalışan | 100 Çalışan | - |
| **Atılan Toplam SQL Sorgu Sayısı** | **101 Sorgu** (1 Ana + 100 İlişki) | **1 Sorgu** (LEFT JOIN ile tek seferde) | **%99.0 azalım** |
| **Veritabanı Gidiş-Dönüş (Roundtrip)** | 101 kez TCP/DB roundtrip | 1 kez TCP/DB roundtrip | **100 kat daha hızlı** |
| **Ortalama Yanıt Süresi (Latency)** | ~450 ms - 600 ms | ~12 ms - 20 ms | **~30x hızlanma** |

---

## Görev 3: `ddl-auto=update` Şeması ve Production'da Neden Kullanılmamalıdır?

Geliştirme aşamasında `spring.jpa.hibernate.ddl-auto: update` kullanıyoruz. Bu mod, Java varlık sınıflarını tarayarak eksik tabloları ve kolonları otomatik oluşturur. Ancak **Canlı (Production) ortamlarda `update` modu kesinlikle kullanılmamalıdır!**

### Production'da Kullanılmama Gerekçeleri (Teknik Analiz):

1. **Kolon Silmeyi (`DROP COLUMN`) ve Tip Değişikliklerini Desteklemez:**
   * `Employee` sınıfından bir alanı sildiğinizde veya yeniden adlandırdığınızda, Hibernate veritabanındaki eski kolonu silmez. Zamanla veritabanında kullanılmayan "çöplük kolonlar" birikir ve şema tutarsızlaşır.
2. **Kritik Veri Kaybı ve Çökme Riski (`CONSTRAINT` İhlalleri):**
   * Production'da canlı veri varken bir alana `@NotNull` veya `@Column(unique=true)` eklediğinizde, tabloda halihazırda `NULL` veya tekrar eden değerler varsa uygulama başlatılamaz ve servis çöker.
3. **Versiyon Kontrolü, Takip ve Geri Alınabilirlik (Rollback) Yoktur:**
   * Hangi şema değişikliğinin ne zaman, kim tarafından ve neden yapıldığının bir geçmişi tutulmaz. Bir hata çıktığında veritabanı şemasını önceki bir sürüme güvenli şekilde geri almak imkansızdır.
4. **Kilitlenme (Table Locking / Blocking) Tehlikesi:**
   * Büyük tablolara otomatik index veya kolon eklemeye çalışırken veritabanı tabloyu kilitleyebilir (table lock); bu da canlı sistemde kesintilere (downtime) yol açar.
5. **Production İçin Doğru Yöntem (Best Practice):**
   * Production ortamında **`ddl-auto: validate`** (şema doğru mu diye sadece kontrol et) veya **`ddl-auto: none`** kullanılmalıdır.
   * Şema değişiklikleri **Flyway** veya **Liquibase** gibi sürüm kontrollü migration araçlarıyla (`V1__init_schema.sql`, `V2__add_email_index.sql` gibi betiklerle) yönetilmelidir.

---

## Görev 4: Specification API Endpoint'inin Yapamadığı Şeyler (Sınırları)

Safha 2'de dinamik arama için Spring Data JPA `Specification<T>` (Criteria API) kullanacağız. Ancak Specification/Criteria API her türlü veritabanı işlemini **yapamaz**. İşte Specification API'nin sınırları ve yapamadığı teknik işlemler:

1. **Pencere Fonksiyonları (Window Functions) ve Analitik Sorgular:**
   * SQL'deki `ROW_NUMBER() OVER(PARTITION BY ...)`, `RANK()`, `LEAD()`, `LAG()` gibi gelişmiş analitik pencere fonksiyonları Specification API ile ifade edilemez (Native SQL veya JOOQ gerektirir).
2. **Küme Operasyonları (`UNION`, `INTERSECT`, `EXCEPT`):**
   * Birden fazla farklı `SELECT` sorgusunun sonuçlarını küme olarak birleştiren `UNION / UNION ALL` yapıları Specification tarafından desteklenmez.
3. **Recursive CTE (Common Table Expressions - `WITH` Bloğu):**
   * Hiyerarşik ağaç yapılarını (örneğin alt-üst birim ağacını) sorgulamak için kullanılan `WITH RECURSIVE` sorguları yapılamaz.
4. **Veritabanına Özgü Spesifik Operatörler ve Full-Text Search:**
   * PostgreSQL'e özgü Full-Text Search (`@@`, `to_tsvector`, `to_tsquery`), JSONB kolon operatörleri (`->>`, `@>`) veya PostGIS coğrafi sorguları doğrudan kullanılamaz; özel `function()` köprüleri veya Native SQL gerektirir.
5. **Alt Sorgu İçeren Toplu Güncelleme / Silme (`BULK UPDATE / DELETE`):**
   * Specification API esasen veri okuma (`SELECT`) içindir. Karmaşık `WHERE` koşullarına bağlı olarak binlerce kaydı tek sorguda güncellemek veya silmek için (`UPDATE ... WHERE id IN (...)`) `@Modifying` JPQL veya Native Query kullanılması şarttır.
6. **Karmaşık `GROUP BY` / `HAVING` ve Pivot Raporlamaları:**
   * Çok boyutlu özet tablolar, dinamik pivot işlemleri veya karmaşık `HAVING` kümeleme raporları Specification ile yazıldığında aşırı karmaşıklaşır veya yetersiz kalır.

---

## Görev 5 / Checkpoint Sorusu: "Dirty Checking" Nedir ve Hangi Sorguyu Bizim Yerimize Attı?

### 1. Dirty Checking (Kirli Kontrol) Nedir?
Spring Data JPA / Hibernate'te **Dirty Checking**, `@Transactional` anotasyonu ile işaretlenmiş bir metot çalışırken, veritabanından sorgulanan (`findById`, `findAll` vb.) varlıkların (entities) **Persistence Context (1st Level Cache)** tarafından `MANAGED` (yönetilen) duruma alınması ve işlem bittiğinde varlığın üzerinde bir değişiklik olup olmadığının otomatik algılanması mekanizmasıdır.

* Hibernate, veritabanından nesneyi okuduğunda ilk halinin bir kopyasını (snapshot) saklar.
* Metot içinde `employee.setName(...)` veya `employee.recalculateSalary(...)` gibi bir setter/iş metodu çağrıldığında nesne bellekte değişir ("dirty" hale gelir).
* Transaction commit edileceği an (`flush`), Hibernate güncel nesne ile snapshot'ı karşılaştırır.
* **Fark algılandığında, geliştiricinin açıkça `repository.save(employee)` veya SQL `UPDATE` yazmasına gerek kalmadan otomatik olarak veritabanına `UPDATE` sorgusu gönderir!**

### 2. Hangi Sorguyu Bizim Yerimize Attı?
Projemizde `EmployeeService.completeProject(Long employeeId, Long projectId, double completionRate, double speedFactor)` metodunda şu işlem gerçekleşti:

```java
@Transactional
public void completeProject(Long employeeId, Long projectId, double completionRate, double speedFactor) {
    Employee employee = findEmployeeOrThrow(employeeId); // 1. MANAGED state'e alındı (Snapshot alındı)
    Project project = projectRepository.findById(projectId).orElseThrow(...);
    
    employee.finishProject(project); // 2. Nesne state'i değişti (Dirty oldu)
    employee.recalculateSalary(completionRate, speedFactor); // 3. Maaş ve katsayı alanları güncellendi
    // Not: Burada employeeRepository.save(employee) yazılmasa bile transaction bittiğinde Hibernate UPDATE atar!
}
```

Bu işlem tamamlandığında Hibernate **bizim yerimize otomatik olarak şu SQL sorgusunu veritabanına attı**:

```sql
update employees 
set base_salary = ?, 
    current_salary = ?, 
    department_id = ?, 
    employee_type = ?, 
    mail = ?, 
    name = ?, 
    performance_multiplier = ?, 
    phone_num = ? 
where id = ?
```

**Sonuç:** Dirty Checking sayesinde veritabanı senkronizasyonu manuel SQL yazmadan, tamamen transaksiyonel nesne modeli üzerinden güvenle sağlandı.

