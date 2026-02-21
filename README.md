# GovtJobs.in — Indian Government Job Notification Aggregator

A production-ready full-stack aggregator for Indian government, banking, SSC, RRB, PSU, and state government job notifications.

## 🏗️ Project Structure

```
govt-job-aggregator/
├── backend/          ← Spring Boot 3.2 (Java 17, Maven)
└── frontend/         ← React 18 (Vite)
```

## 🚀 Quick Start

### Backend

**Prerequisites:** Java 17+, Maven 3.8+

```bash
cd backend
mvn spring-boot:run
```

- API runs at `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/govtjobs`
  - User: `sa` | Password: *(empty)*
- **Data is auto-fetched at startup** (background thread, ~3s delay)
- Scheduled re-fetch every 6 hours at midnight, 6am, noon, 6pm IST

### Frontend

**Prerequisites:** Node.js 18+

```bash
cd frontend
npm install
npm run dev
```

- App runs at `http://localhost:5173`
- Proxies `/api` calls to the backend automatically

## 📡 REST API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notices` | Paginated notices with filters |
| GET | `/api/notices?category=SSC` | Filter by category |
| GET | `/api/notices?state=Tamil+Nadu` | Filter by state |
| GET | `/api/notices?period=today` | Today's notices |
| GET | `/api/notices?period=this_week` | Last 7 days |
| GET | `/api/notices/{id}` | Single notice |
| GET | `/api/categories` | Available categories |
| GET | `/api/states` | Available states |
| POST | `/api/admin/refresh` | Trigger manual scrape |

### Query Parameters for `/api/notices`

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `category` | String | - | BANK, SSC, RAILWAYS, UPSC, PSU, STATE, DEFENCE, OTHERS |
| `state` | String | - | "Central", "Tamil Nadu", etc. |
| `period` | String | `all` | `all`, `today`, `this_week` |
| `page` | int | `0` | Page number (0-indexed) |
| `size` | int | `20` | Page size (max 50) |

## 📰 Sources

| Source | Category | Method |
|--------|----------|--------|
| Employment News (GOI) | Mixed | RSS Feed |
| SSC (ssc.gov.in) | SSC | HTML Scraping |
| IBPS (ibps.in) | Bank | HTML Scraping |
| SBI (bank.sbi) | Bank | HTML Scraping |
| UPSC (upsc.gov.in) | UPSC | HTML Scraping |
| TNPSC (tnpsc.gov.in) | State (TN) | HTML Scraping |
| RRB (rrbcdg.gov.in) | Railways | HTML Scraping |
| NHPC (nhpc.nic.in) | PSU | HTML Scraping |

## ➕ Adding New Sources

1. Create a class in `backend/src/main/java/in/govtjobs/scraper/impl/`
2. Implement `JobNoticeSource`
3. Annotate with `@Component`
4. Spring auto-discovers and registers it — no other changes needed

```java
@Component
public class NewSourceScraper implements JobNoticeSource {
    @Override public String getSourceName() { return "New Source"; }
    @Override public String getSourceUrl()  { return "https://newsource.gov.in"; }
    @Override public String getCategory()   { return "STATE"; }
    @Override public String getState()      { return "Tamil Nadu"; }
    @Override public List<RawNotice> fetchRaw() { /* ... */ }
}
```

## 🚢 Production Deployment

### Backend (JAR)
```bash
cd backend
mvn clean package -DskipTests
java -jar target/govt-job-aggregator-1.0.0.jar
```

### Frontend (Static Build)
```bash
cd frontend
npm run build
# Serve dist/ with nginx / Apache / Netlify / Vercel
```

### Environment Variables (Production)
```properties
# In application.properties or as -D flags
spring.h2.console.enabled=false  # Disable H2 console in prod
spring.datasource.url=jdbc:mysql://localhost/govtjobs  # Switch to MySQL
spring.datasource.username=root
spring.datasource.password=secret
```

## ⚖️ Compliance

- All notices sourced from official government websites
- Not storing full content — only metadata (title, dates, links)
- Links go directly to official pages only
- Disclaimer displayed on every page
- Respects `robots.txt` via polite crawl delays (1.5s between sources)
- User-Agent clearly identifies the aggregator

## 📊 AdSense Optimization

- Semantic HTML with rich meta descriptions
- Content updates every 6 hours (fresh content = better crawl)
- Mobile-first design (Core Web Vitals friendly)
- No pop-ups or misleading CTAs
- Category-based targeting for high-CPC keywords
