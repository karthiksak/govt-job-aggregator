# Deployment Guide

This project is a full-stack application consisting of a **React + Vite Frontend** and a **Java Spring Boot Backend**.
Since Vercel is specifically designed for frontends and serverless functions, we cannot host the Java backend directly on Vercel.

The recommended architecture is:
- **Frontend**: Hosted on [Vercel](https://vercel.com) (Free Tier)
- **Backend**: Hosted on [Render](https://render.com) (Free Tier) or [Railway](https://railway.app) (Free Tier)

---

## 🚀 1. Deploy the Backend (Render)

We deploy the backend first, so we know what its URL is. This URL will be used by the frontend.

1. **Push your code to GitHub.**
2. Go to [Render Dashboard](https://dashboard.render.com/) and sign in.
3. Click **New +** and select **Web Service**.
4. Connect your GitHub repository.
5. Configure the Web Service:
   - **Name**: `govt-job-aggregator-backend` (or similar)
   - **Root Directory**: `backend` (Important!)
   - **Environment**: `Docker`
   - **Region**: Choose the closest to your users.
   - **Branch**: `main`
6. Click **Create Web Service**.
7. Wait for the build and deployment to finish. Once it's live, copy the URL (e.g., `https://govt-job-aggregator-backend.onrender.com`).

*Note regarding data:* This app uses an H2 file-based database. On Render's Free tier, the disk is ephemeral (it resets on every deploy/restart). If you want persistent job data, you can add a **Disk** to your Render Web Service mounted at `/app/data/`.

---

## 🎨 2. Deploy the Frontend (Vercel)

Now that you have your backend URL, let's connect the frontend.

### Step 2.1: Update the Vercel Config
Before deploying, open the `vercel.json` file inside the `frontend` directory (`frontend/vercel.json`). Make sure the Destination URL points to your actual Render backend URL that you created in Step 1.

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://<YOUR_RENDER_URL>/api/:path*" 
    }
  ]
}
```
*Commit and push this change to GitHub.*

### Step 2.2: Deploy on Vercel
1. Go to [Vercel Dashboard](https://vercel.com/dashboard).
2. Click **Add New** -> **Project**.
3. Import your GitHub repository.
4. In the **Project Settings**, find the **Root Directory** section and click **Edit**.
5. Select the `frontend` directory and save.
6. Click **Deploy**.

Once the deployment finishes, open your Vercel URL. All `/api` requests made by the frontend will now automatically proxy to your securely hosted Render backend!
