# Deployment Guide

This project has been verified with:

- Frontend: Cloudflare Pages
- Backend: Railway
- Database: Aiven MySQL
- Storage: Cloudflare R2

No real secrets are stored in this repository.

## 1. Aiven MySQL

The current Aiven database name is:

```text
defaultdb
```

For a fresh database, run:

```text
database/schema.sql
```

For the already deployed MVP database, run:

```text
database/migration-folder-sync.sql
```

Use a JDBC URL like this:

```text
jdbc:mysql://campus-taste-campustaste.h.aivencloud.com:22468/defaultdb?sslMode=REQUIRED&connectTimeout=30000&socketTimeout=30000&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
```

Important:

- Use `sslMode=REQUIRED`
- Do not use `ssl-mode=REQUIRED`
- Do not commit the real database password

## 2. Cloudflare R2

1. Create an R2 bucket, for example `imagemanager`.
2. Create an R2 API token with object read/write permissions.
3. Configure a public bucket URL or custom domain.
4. Set:

```text
R2_PUBLIC_BASE_URL=https://your-r2-public-domain
```

The backend uploads objects to R2 and stores metadata in MySQL. Image binary data is not stored in MySQL.

## 3. Railway Backend

Railway backend public URL:

```text
https://imagemanager-final-production.up.railway.app
```

Railway should build from:

```text
backend
```

Required environment variables:

```text
PORT=8080
DB_URL=jdbc:mysql://campus-taste-campustaste.h.aivencloud.com:22468/defaultdb?sslMode=REQUIRED&connectTimeout=30000&socketTimeout=30000&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
DB_USERNAME=avnadmin
DB_PASSWORD=your-password
JWT_SECRET=please-change-this-secret-to-a-long-random-value
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://your-pages-domain.pages.dev
STORAGE_TYPE=r2
R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com
R2_ACCESS_KEY_ID=your-access-key
R2_SECRET_ACCESS_KEY=your-secret-key
R2_BUCKET=imagemanager
R2_PUBLIC_BASE_URL=https://your-r2-public-domain
```

`STORAGE_TYPE=local` is only for local fallback testing. Use `STORAGE_TYPE=r2` for cloud deployment.

After deployment, test:

```text
https://imagemanager-final-production.up.railway.app/api/health
```

Expected response:

```json
{
  "status": "ok"
}
```

## 4. Cloudflare Pages Frontend

Cloudflare Pages project root:

```text
frontend
```

Build command:

```text
npm run build
```

Output directory:

```text
dist
```

Required environment variable:

```text
VITE_API_BASE_URL=https://imagemanager-final-production.up.railway.app
```

Important:

- Do not use only `imagemanager-final-production.up.railway.app`
- Do not add `/api`

## 5. Folder Sync Deployment Notes

Folder Sync cannot silently read a fixed local path from the cloud backend. The browser must ask the user for folder access.

- Desktop Chromium browsers can use `showDirectoryPicker`
- Unsupported browsers use `webkitdirectory` file input fallback
- Directory handles are stored in IndexedDB when available
- Mobile browsers should mainly use normal image upload
- `Delete cloud images missing from this folder` is optional and off by default

## 6. Local Development

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Local frontend `.env`:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## 7. Troubleshooting

### CORS error

Check `CORS_ALLOWED_ORIGINS` on Railway. It must include the exact Cloudflare Pages domain.

### R2 upload failed

Check:

- `STORAGE_TYPE=r2`
- `R2_ENDPOINT`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`
- `R2_BUCKET`
- `R2_PUBLIC_BASE_URL`
- R2 token permissions

### Aiven MySQL SSL error

Use:

```text
sslMode=REQUIRED
```

If Aiven provides a CA certificate requirement, add the certificate configuration recommended by Aiven.

### Frontend cannot reach backend

Check:

- `VITE_API_BASE_URL=https://imagemanager-final-production.up.railway.app`
- `/api/health` works in a browser
- CORS allowed origins include the Pages domain

### Copy image fallback behavior

Copying image binary data requires HTTPS, user interaction, and browser Clipboard API support. The frontend first requests `GET /api/images/{id}/content` with JWT and writes the image blob to the clipboard. If binary copy fails, it copies the public image link. If link copy also fails, it starts a download.
