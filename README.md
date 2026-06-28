# ImageManager Final

This project is based on a completed local Java Web ImageManager prototype and has been refactored into a separated frontend-backend architecture for final assessment and cloud deployment.

ImageManager is a cloud-ready image management system. Users can register, log in, upload images, batch import browser-selected files or folders, search and filter metadata, toggle tags, copy image content or links, view details, and delete their own images.

## Architecture

- `backend`: Spring Boot RESTful API
- `frontend`: Vue 3 SPA built with Vite
- `database`: MySQL schema
- Deployment target:
  - Frontend: Cloudflare Pages
  - Backend: Railway
  - Database: Aiven MySQL
  - Image storage: Cloudflare R2

The backend uses Controller / Service / Repository layers. The frontend communicates with the backend through JSON REST APIs and stores JWT tokens in `localStorage`.

## Technology Stack

- Backend: Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security, Validation, MySQL Driver, JWT, AWS SDK S3 Client
- Frontend: Vue 3, Vite, Vue Router, Axios, native CSS
- Database: MySQL 8 compatible relational schema
- Storage: Cloudflare R2 through the S3-compatible API

## Feature Points

1. User registration
2. User login
3. Logout
4. Single image upload
5. Mobile image upload through the responsive upload page
6. Desktop batch image selection
7. Select all / select partial before upload
8. Upload selected images to R2
9. Image list
10. Image detail
11. Search images
12. Filter by source / tag / time
13. Sort images
14. Toggle tags
15. Delete image
16. Copy image / fallback to link or download
17. Responsive layout

These feature points satisfy the course requirement that the final project must include at least 10 functions. Login and registration are counted as two functions.

## REST APIs

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Images:

- `GET /api/images`
- `POST /api/images/upload`
- `GET /api/images/{id}`
- `DELETE /api/images/{id}`
- `POST /api/images/{id}/tags/toggle`
- `GET /api/images/{id}/download`
- `GET /api/images/sources`

Health:

- `GET /api/health`

## Database

Database name: `imagemanager_final`

SQL file:

```text
database/schema.sql
```

The project also uses `spring.jpa.hibernate.ddl-auto=update` so the backend can update missing columns during development. The SQL file is kept for submission, review, and reproducible deployment.

## Local Backend Run

```bash
cd backend
mvn spring-boot:run
```

If Maven is not installed globally, use the Maven Wrapper:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```bat
cd backend
mvnw.cmd spring-boot:run
```

For local testing, create a MySQL database using `database/schema.sql`, then set environment variables from `backend/.env.example`.

If R2 variables are missing, the backend can fall back to local file storage. For cloud deployment, set:

```text
STORAGE_TYPE=r2
```

## Local Frontend Run

```bash
cd frontend
npm install
npm run dev
```

Create `frontend/.env` from `frontend/.env.example`:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Git Commit Suggestion

Recommended commit message:

```text
Add cloud-ready ImageManager frontend and backend
```

Alternative:

```text
Import completed ImageManager prototype and add cloud-ready SPA refactor
```

## Project Highlights

- Separated frontend-backend architecture
- Standard RESTful API design
- JWT authentication with BCrypt password hashing
- Per-user image ownership checks
- JPA Repository-based relational database persistence
- Cloudflare R2 object storage integration
- Browser folder selection and batch upload in groups of five
- Responsive Vue SPA for desktop and mobile use
- Copy image support with link/download fallback

## Future Improvements

- Desktop sync client for automatic local folder scanning
- OCR
- AI classification
- Private R2 signed URLs
- Shared galleries
- Image editing
