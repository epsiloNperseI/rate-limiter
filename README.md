# Rate Limiter — Spring Boot Starter

Переиспользуемый распределённый rate limiter в виде Spring Boot Starter. Подключаешь зависимость, 
добавляешь одну аннотацию над методом — и endpoint автоматически защищён от перегрузки.

## Зачем это нужно

Rate limiting защищает сервис от злоупотреблений, 
DDoS-атак и случайных петель на стороне клиента. Большинство реализаций либо слишком простые
(in-memory, не работают в кластере), либо требуют тяжёлых зависимостей.
Этот проект показывает как сделать это правильно — через Redis с атомарным Lua скриптом.

## Как это работает

Каждый входящий запрос перехватывается через **Spring AOP**.
Аспект читает параметры аннотации `@RateLimit` и обращается к Redis, 
где выполняется **Lua скрипт** — атомарная реализация алгоритма **Sliding Window**.

Sliding Window хранит в Redis `ZSET` (sorted set) временные метки всех запросов. 
При каждом обращении скрипт:
1. Удаляет из множества все записи старше `now - windowSeconds`
2. Считает оставшиеся
3. Если `count < limit` — добавляет текущий запрос и пропускает
4. Если `count >= limit` — возвращает отказ

Весь этот цикл выполняется **атомарно** — Lua скрипты в Redis однопоточны, 
что гарантирует отсутствие race conditions даже при высокой конкурентности и 
нескольких инстансах приложения.

Если запрос заблокирован — `GlobalExceptionHandler` перехватывает `RateLimitExceededException`,
сохраняет событие в **PostgreSQL** через JPA и возвращает клиенту `HTTP 429`.
Схема миграций управляется **Flyway** — база всегда в консистентном состоянии.
Библиотека оформлена как настоящий Spring Boot Starter: через `AutoConfiguration.imports` 
она автоматически регистрирует все бины при подключении к любому 
Spring Boot проекту — без лишних аннотаций.

## Tech Stack

| | |
|---|---|
| Java 21 + Spring Boot 3.5 | Основа |
| Spring AOP + AspectJ | Перехват методов без изменения бизнес-логики |
| Redis 7 (Lettuce) | Хранение sliding window через ZSET |
| Lua script | Атомарное выполнение проверки и записи |
| PostgreSQL 16 + Hibernate | Аудит лог заблокированных запросов |
| Flyway | Версионирование схемы БД |
| Gradle Kotlin DSL | Multi-module сборка |
| Docker Compose | Локальный запуск всего стека |

## Getting Started

**Вариант 1 — всё в Docker:**
```bash
git clone https://github.com/epsiloNperseI/rate-limiter.git
cd rate-limiter
docker-compose up --build
```

**Вариант 2 — локальная разработка:**
```bash
docker-compose up -d postgres redis
./gradlew :rate-limiter-demo:bootRun
```

Приложение поднимается на `http://localhost:8080`.

## Тестирование

Отправь 7 запросов подряд — первые 5 вернут `200`, остальные `429`:

```bash
for i in {1..7}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/hello
done
```

Посмотреть лог заблокированных запросов в PostgreSQL:

```bash
curl http://localhost:8080/api/logs
```

Пример ответа при превышении лимита:
HTTP 429 Too Many Requests
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for key: 127.0.0.1:hello — 5 requests per 10s",
  "retryAfter": 10
}
```

## Usage

```java
@GetMapping("/api/search")
@RateLimit(limit = 10, windowSeconds = 60)
public ResponseEntity<?> search() {
    // максимум 10 запросов за 60 секунд с одного IP
}
```

| Параметр | По умолчанию | Описание |
|---|---|---|
| `limit` | 100 | Максимум запросов в окне |
| `windowSeconds` | 60 | Размер скользящего окна в секундах |
| `key` | `""` | Кастомный ключ (по умолчанию — IP + имя метода) |

## License

MIT