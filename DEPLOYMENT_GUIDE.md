# KRS Spring Boot + PostgreSQL Free Cloud Deployment Guide

This guide walks you through deploying your Spring Boot Backend and PostgreSQL database on **100% Free Hosting Services**.

---

## 🚀 Recommended Stack (100% Free)
- **Database**: [Neon.tech](https://neon.tech) or [Supabase](https://supabase.com) (Free Managed PostgreSQL)
- **Backend Service**: [Render.com](https://render.com) or [Koyeb.com](https://www.koyeb.com) (Free Docker Hosting)
- **Frontend Hosting**: [Vercel](https://vercel.com) or [Netlify](https://netlify.com)

---

## Step 1: Create Free PostgreSQL Database on Neon.tech

1. Go to [Neon.tech](https://neon.tech) and sign up for a free account.
2. Click **Create Project**, name it `krs-db`, and set PostgreSQL version to 16/17.
3. Once created, copy your Connection Details:
   - **Host**: `ep-xyz-123456.us-east-2.aws.neon.tech`
   - **Database**: `neondb`
   - **Username**: `neondb_owner`
   - **Password**: `<YOUR_NEON_PASSWORD>`
4. Construct your Spring Boot Connection URL:
   ```text
   jdbc:postgresql://ep-xyz-123456.us-east-2.aws.neon.tech:5432/neondb?sslmode=require
   ```

---

## Step 2: Deploy Backend to Render.com (Free Tier)

### Method 1: Connecting via GitHub (Recommended)
1. Push your repository to GitHub.
2. Log into [Render.com](https://render.com) and click **New +** -> **Web Service**.
3. Select your GitHub repository.
4. Fill in the following details:
   - **Name**: `krs-backend`
   - **Region**: Select closest region (e.g. Singapore / Frankfurt / Oregon)
   - **Runtime**: **Docker**
   - **Dockerfile Path**: `./Backend/Dockerfile`
   - **Docker Build Context**: `./Backend`
   - **Instance Type**: **Free**

5. Under **Environment Variables**, add the following keys:

| Key | Example Value |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-xyz.neon.tech:5432/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | `<your-neon-password>` |
| `DB_SCHEMA` | `public` (or `krs_schema`) |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `PORT` | `8080` |
| `CORS_ORIGINS` | `*` (or your frontend URL) |

6. Click **Create Web Service**. Render will pull your Dockerfile, build the Java 21 container, run Liquibase migrations automatically on startup, and launch your API!

---

## Step 3: Alternative Option - Deploy on Koyeb.com

1. Sign up on [Koyeb.com](https://www.koyeb.com).
2. Click **Create App** -> **GitHub**.
3. Point to your repository, select `Backend` directory, and set Builder to **Dockerfile**.
4. Set the exact same environment variables as above.
5. Deploy! Koyeb provides free SSL and instant HTTP/2 endpoints.

---

## 📂 Files Created in Project for Deployment
- [`Backend/Dockerfile`](file:///c:/Projects/KRS/Backend/Dockerfile): Multi-stage Maven + Java 21 production image.
- [`Backend/render.yaml`](file:///c:/Projects/KRS/Backend/render.yaml): Render Blueprint for automatic setup.
- [`Backend/src/main/resources/application.properties`](file:///c:/Projects/KRS/Backend/src/main/resources/application.properties): Updated with environment variable fallbacks for cloud deployment.
