# Geoso Backend

Backend service for Geoso, a location-based social application. The API is built with Spring Boot and provides user authentication, user profiles, location marks, comments, likes, and friendships.

> **Project status:** Work in progress. The authentication, marks, comments, likes, and friendship features are implemented. Photo shrinking exists only as a tested service class and is **not yet connected to any endpoint**. Local Docker Compose setup currently needs manual fixes.

## Features

Implemented and wired into the API:

- User registration (username plus email or phone number) and login
- Stateless authentication with JWT (24-hour expiry)
- Role-based authorization (`FREE`, premium, `ADMIN`) for protected and administrative operations
- Free-tier limit: a `FREE` user can create at most 5 public marks per rolling 24 hours (private marks are not counted); an admin can upgrade a user to premium
- Create, browse, and delete location marks (stored as PostGIS points, SRID 4326)
- Nearby search (progressively widens the radius from 250 m up to 5 km, max 100 results), bounding-box search, per-user marks, and historical marks by date range
- Mark message content is hidden when the requesting user is more than 50 m from the mark
- Mark views, likes, and comments (likes and comments are not allowed on private marks)
- GPS plausibility check when creating a mark: rejects teleportation and speeds above 30 m/s, using the user's last location stored in Redis
- Friendship requests, acceptance, decline, listing, and deletion, with a Redis-backed cooldown after repeated declines
- Distributed rate limiting with Bucket4j and Redis (60 requests per minute per client)
- Docker and Docker Compose files
- Google Cloud Build configuration for building and deploying to Cloud Run

Implemented but not connected to the API yet:

- `ImageProcessingService`: photo validation, EXIF orientation handling, resizing, and JPEG compression. See [Photo shrinking](#photo-shrinking).

## Technology stack

- Java 21
- Spring Boot 4.1.0 (Spring MVC, Spring Security, Spring Data JPA)
- JWT
- Hibernate Spatial / JTS
- PostgreSQL with PostGIS
- Redis (cache, GPS validation state, friendship cooldowns) and Bucket4j (rate limiting)
- Maven Wrapper
- Docker
- Google Cloud Run and Cloud Build

## Project structure

```text
src/main/java/com/demo/my_backend_java/
├── configuration/    Security, Redis, and rate-limit configuration
├── controller/       REST API controllers (User, Mark, Friendship)
├── dto/              API data-transfer objects
├── entity/           JPA entities
├── filter/           JWT and rate-limit filters
├── repository/       Database repositories
└── service/          Business logic (marks, users, friendships, GPS, JWT, rate limiting, image processing)

src/main/resources/
└── application.properties

src/test/java/com/demo/my_backend_java/
├── ImageProcessingServiceTest.java
└── MyBackendJavaApplicationTests.java
```

## Requirements

- JDK 21
- Docker and Docker Compose
- Git

The Maven Wrapper downloads the configured Maven version automatically, so Maven does not need to be installed globally.

## Configuration

Configuration is read from environment variables:

| Variable | Default in `application.properties` | Notes |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://10.40.16.3:5432/postgres` | Default is a private IP from the cloud setup. Always override locally. |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | |
| `SPRING_DATASOURCE_PASSWORD` | none | **Required** |
| `SPRING_DATA_REDIS_HOST` | `localhost` | |
| `SPRING_DATA_REDIS_PORT` | `6379` | |
| `SPRING_DATA_REDIS_PASSWORD` | empty | |
| `JWT_SECRET` | none | **Required**, Base64-encoded secret |
| `PORT` | `8080` | |

Example:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_database_password
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=
JWT_SECRET=your_base64_encoded_jwt_secret
PORT=8080
```

Other settings:

- `spring.jpa.hibernate.ddl-auto=update` creates and updates tables automatically. `spring.jpa.show-sql=true` logs every SQL statement.
- **Redis TLS is always on.** `spring.data.redis.ssl.enabled=true` is set in `application.properties`, and `RateLimitConfig` also calls `withSsl(true)`. A plain local Redis (such as the one in `docker-compose.yml`) will not accept the connection. See [Known issues](#known-issues).
- The PostgreSQL database must have the PostGIS extension available.

Do not commit real passwords, JWT secrets, cloud credentials, or other sensitive values.

## Run locally

### With Docker Compose

`docker-compose.yml` defines PostgreSQL/PostGIS (`postgis/postgis:15-3.4`), Redis, and the backend. **It does not work as written.** Before `docker compose up --build` will start the backend, you need to:

1. Fix the database port mapping, which currently reads `"5432:  "`. It should be `"5432:5432"`.
2. Change the backend port mapping from `8088:8088` to `8080:8080` (the application and `Dockerfile` use port 8080).
3. Add `SPRING_DATASOURCE_PASSWORD` (the compose database password is `123`) and `JWT_SECRET` to the backend's `environment` section. The application has no default for either.
4. Resolve the Redis TLS problem: either make TLS configurable in `application.properties` and `RateLimitConfig`, or run Redis with TLS.

```bash
docker compose up --build
docker compose down        # stop
docker compose down -v     # stop and delete the local database volume
```

### Without Docker

Start PostgreSQL/PostGIS and a TLS-enabled Redis separately, set the environment variables above, then run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

The default URL is `http://localhost:8080`.

## Build and test

```bash
./mvnw test                         # run all tests
./mvnw clean package -DskipTests    # build the jar without tests
```

The image-processing tests write generated files to `target/test-output/exif/`.

## Photo shrinking

`ImageProcessingService.processAndCompress(MultipartFile)` does the following:

1. Rejects empty uploads and files above the input limit.
2. Validates JPEG and PNG magic bytes instead of trusting the client content type.
3. Reads EXIF orientation and applies it before resizing.
4. Resizes proportionally to a maximum long edge of 1024 px.
5. Compresses to JPEG.
6. Shrinks further if the result is over 512 KB, down to a minimum long edge of 320 px (minimum JPEG quality 0.40).
7. Rejects images over the pixel or output-size limits.

| Setting | Property | Value |
|---|---|---:|
| Maximum input size | `photo.max-input-bytes` | 10 MB |
| Maximum pixel count | `photo.max-pixels` | 25,000,000 |
| Maximum long edge | `photo.max-long-edge` | 1024 px |
| Minimum long edge | `photo.min-long-edge` | 320 px |
| Target output size | `photo.max-target-bytes` | 512,000 bytes |
| Minimum JPEG quality | `photo.min-quality` | 0.40 |

### Current status

- **Not connected to the API.** In the current source, `processAndCompress(...)` is called only from `ImageProcessingServiceTest`. No controller accepts a photo upload, and `POST /api/marks` takes a JSON body only.
- **No storage yet.** The `Mark` entity has photo metadata columns (`photo_object_key`, `photo_content_type`, `photo_size_bytes`, `photo_width`, `photo_height`), but nothing uploads a processed image anywhere or sets them.
- **Known visual defect.** `ImageProcessingServiceTest` runs eight EXIF-orientation fixtures (`landscape_1.jpg` to `landscape_8.jpg`). In the latest manual check, 7 of 8 outputs had the expected orientation; 1 output was shifted left with a black or empty area on the right. The cause is probably in the output bounds, transformation matrix, or canvas handling.
- **The tests do not catch this defect.** They only assert that the output is non-null, is `image/jpeg`, has positive dimensions, and that the reported dimensions match the encoded image. They do not check pixel alignment or borders.

## Deployment

`cloudbuild.yaml` defines a Cloud Build pipeline that:

1. Builds a Docker image tagged `gcr.io/$PROJECT_ID/geoso-backend:$COMMIT_SHA`.
2. Pushes it to Google Container Registry.
3. Deploys it to the Cloud Run service `geoso-backend` in `asia-southeast1`.

A deployment configuration is not proof of a successful deployment. After a deploy, check the Cloud Build history, the active Cloud Run revision, and that revision's environment variables (`SPRING_DATASOURCE_*`, `SPRING_DATA_REDIS_*`, `JWT_SECRET`).

```bash
gcloud builds submit --config cloudbuild.yaml .
```

The service needs working PostgreSQL, Redis (TLS), and secret configuration. Note that `cloudbuild.yaml` contains a hardcoded service-account path; Google Container Registry has also been superseded by Artifact Registry.

## API overview

All endpoints require a `Bearer` token unless marked public. Responses and error formats are defined in the controller classes.

### Authentication and users (`/api/user`)

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/api/user/register` | public | Body: `username`, `contact` (email or phone), `password` |
| POST | `/api/user/login` | public | Body: `loginId`, `password`. Returns `{ "token": "..." }` |
| GET | `/api/user/me` | authenticated | Current user |
| GET | `/api/user/{id}` | **admin only** | Numeric id |
| DELETE | `/api/user/delete/{username}` | owner or admin | |
| POST | `/api/user/admin/upgrade/{username}` | admin only | Upgrades a user to premium |

### Marks (`/api/marks`)

| Method | Path | Notes |
|---|---|---|
| POST | `/api/marks` | JSON body (`Mark`); includes GPS check and the free-tier limit |
| GET | `/api/marks/{id}` | |
| GET | `/api/marks/nearby` | Query: `lat`, `lng` |
| GET | `/api/marks/bounded` | Query: `minLat`, `maxLat`, `minLng`, `maxLng`, `userLat`, `userLng` |
| GET | `/api/marks/user/{username}` | |
| GET | `/api/marks/historical` | Query: `lat`, `lng`, `startDate`, `endDate` (ISO date-time) |
| POST | `/api/marks/{id}/view` | |
| POST | `/api/marks/{id}/comments` | |
| GET | `/api/marks/{id}/comments` | |
| POST | `/api/marks/{id}/like` | Query: `userId` |
| DELETE | `/api/marks/delete/like/{id}` | |
| DELETE | `/api/marks/delete/comment/{id}` | |
| DELETE | `/api/marks/{id}` | |

### Friendships (`/api/friendships`)

| Method | Path |
|---|---|
| POST | `/api/friendships/request` |
| GET | `/api/friendships/pending` |
| PUT | `/api/friendships/accept/{id}` |
| PUT | `/api/friendships/decline/{id}` |
| GET | `/api/friendships/list/{id}` |
| DELETE | `/api/friendships/delete-friend/{id}` |

### Example

```bash
# Register
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","contact":"alice@example.com","password":"S3cure!pass"}'

# Log in and use the token
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"alice","password":"S3cure!pass"}'

curl "http://localhost:8080/api/marks/nearby?lat=10.77&lng=106.70" \
  -H "Authorization: Bearer <token>"
```
