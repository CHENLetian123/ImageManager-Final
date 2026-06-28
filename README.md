# ImageManager Final

This project is based on a completed local Java Web ImageManager prototype and has been refactored into a separated frontend-backend architecture for final assessment and cloud deployment.

ImageManager is a cloud-ready image management system. Users can register, log in, upload images, batch import browser-selected files or folders, search and filter metadata, toggle tags, copy image content, view details, sync authorized folders, and delete their own images.

## Architecture

- `backend`: Spring Boot RESTful API
- `frontend`: Vue 3 SPA built with Vite
- `database`: MySQL schema and migration files
- Frontend deployment: Cloudflare Pages
- Backend deployment: Railway
- Database: Aiven MySQL, currently using database `defaultdb`
- Image storage: Cloudflare R2

The backend uses Controller / Service / Repository layers. The frontend communicates with REST APIs through Axios and stores JWT tokens in `localStorage`.

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
12. Instant filter by source / tag / time
13. Sort images
14. Toggle tags with a continuous tag menu
15. Delete image and remove the R2 object
16. Copy image binary to clipboard with link/download fallback
17. Folder presets
18. Browser directory authorization
19. Folder sync with selected upload
20. Optional delete missing cloud images
21. Responsive layout

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
- `GET /api/images/{id}/content`
- `DELETE /api/images/{id}`
- `POST /api/images/{id}/tags/toggle`
- `GET /api/images/{id}/download`
- `GET /api/images/sources`

Folder presets:

- `GET /api/folder-presets`
- `POST /api/folder-presets`
- `PUT /api/folder-presets/{id}`
- `DELETE /api/folder-presets/{id}`
- `GET /api/folder-presets/{id}/images/index`
- `POST /api/folder-presets/{id}/images/upload`
- `POST /api/folder-presets/{id}/cleanup-missing`

Health:

- `GET /api/health`

## Database

Current cloud database name: `defaultdb`

SQL files:

```text
database/schema.sql
database/migration-folder-sync.sql
```

For a fresh database, run `database/schema.sql`. For the already deployed MVP database, run `database/migration-folder-sync.sql` to add Folder Sync support without dropping existing data.

The project also uses `spring.jpa.hibernate.ddl-auto=update`, but SQL files are kept for submission, review, and reproducible deployment.

## Local Backend Run

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```bat
cd backend
mvnw.cmd spring-boot:run
```

For local fallback storage:

```text
STORAGE_TYPE=local
```

For cloud deployment:

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

Cloudflare Pages must use the backend origin without `/api`:

```text
VITE_API_BASE_URL=https://imagemanager-final-production.up.railway.app
```

## Folder Sync Notes

Browsers cannot silently read fixed local paths such as `D:/Screenshots`. Folder Sync uses user authorization in the browser:

- Preferred: `showDirectoryPicker`
- Fallback: `input type="file" webkitdirectory multiple`
- Directory handles are stored in IndexedDB when supported
- The backend never reads the user's local disk directly
- Mobile browsers can use regular Upload when directory authorization is unavailable

## Git Commit Suggestion

```text
Add cloud-ready ImageManager frontend and backend
```

or:

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
- Protected image content streaming for clipboard copy
- Folder presets and browser-authorized folder sync
- Batch uploads in groups of five
- Responsive Vue SPA for desktop and mobile use

## Future Improvements

- Desktop sync client for automatic local folder scanning
- OCR
- AI classification
- Private R2 signed URLs
- Shared galleries
- Image editing
