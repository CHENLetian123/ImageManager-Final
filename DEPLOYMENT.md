# Deployment Guide

This guide describes the first-stage cloud deployment plan:

- Frontend: Cloudflare Pages
- Backend: Railway
- Database: Aiven MySQL
- Storage: Cloudflare R2

No real secrets are stored in this repository.

## 1. Create Aiven MySQL

1. Create a MySQL service in Aiven.
2. Create or select database `imagemanager_final`.
3. Open a SQL console or connect with MySQL Workbench.
4. Run:

```text
database/schema.sql
```

Aiven may require SSL. Use a JDBC URL similar to:

```text
jdbc:mysql://your-aiven-host:port/imagemanager_final?ssl-mode=REQUIRED
```

If your driver requires certificate settings, add the SSL parameters provided by Aiven to `DB_URL`.

## 2. Create Cloudflare R2 Bucket

1. Open Cloudflare Dashboard.
2. Create an R2 bucket, for example `imagemanager`.
3. Create an R2 API token with object read/write permissions.
4. Record:
   - Account endpoint
   - Access key ID
   - Secret access key
   - Bucket name

## 3. Configure R2 Public URL

For the first stage, the bucket can expose images through a public URL.

Options:

- Use an R2 public bucket domain.
- Use a custom domain connected to the bucket.

Set:

```text
R2_PUBLIC_BASE_URL=https://your-r2-public-domain
```

The backend stores this URL in `images.public_url`.

## 4. Deploy Backend to Railway

1. Push the repository to GitHub.
2. Create a Railway project from the GitHub repository.
3. Set the Railway root or build context to:

```text
backend
```

4. Railway will use `backend/Dockerfile`.
5. Configure environment variables:

```text
PORT=8080
DB_URL=jdbc:mysql://your-aiven-host:port/imagemanager_final?ssl-mode=REQUIRED
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

6. After deployment, test:

```text
https://your-railway-domain/api/health
```

Expected response:

```json
{
  "status": "ok"
}
```

## 5. Deploy Frontend to Cloudflare Pages

1. Create a Cloudflare Pages project from the same GitHub repository.
2. Set project root:

```text
frontend
```

3. Build command:

```text
npm run build
```

4. Output directory:

```text
dist
```

5. Configure environment variable:

```text
VITE_API_BASE_URL=https://your-railway-backend-domain
```

6. Deploy and open the Pages URL.

## 6. Local Development

Backend:

```bash
cd backend
mvn spring-boot:run
```

If Maven is not installed globally, use:

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

## 7. Troubleshooting

### CORS error

Check `CORS_ALLOWED_ORIGINS` on Railway. It must include the exact Cloudflare Pages domain:

```text
https://your-pages-domain.pages.dev
```

Multiple origins are comma-separated.

### R2 upload failed

Check:

- `STORAGE_TYPE=r2`
- `R2_ENDPOINT`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`
- `R2_BUCKET`
- R2 token permissions
- Bucket name spelling

### Aiven MySQL SSL error

Use the SSL JDBC parameters from Aiven. Start with:

```text
ssl-mode=REQUIRED
```

If Aiven provides a CA certificate requirement, add the certificate configuration recommended by Aiven.

### Frontend cannot reach backend

Check:

- `VITE_API_BASE_URL` on Cloudflare Pages
- Railway backend public domain
- `/api/health` works in a browser
- CORS allowed origins include the Pages domain

### Copy image fallback behavior

Copying image binary data requires HTTPS, user interaction, browser Clipboard API support, and public image CORS access. If binary copy fails, the frontend tries to copy the image link. If link copy also fails, it starts a download.
