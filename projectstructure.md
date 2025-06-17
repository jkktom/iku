# Project Structure & Deployment Overview

This document outlines the architecture, technology stack, and deployment strategy for the project.

---

## 🏗️ Tech Stack

- **Backend:**
  - Java Spring Boot
  - JPA (Java Persistence API) for ORM
  - PostgreSQL database
- **Frontend:**
  - Remix (planned migration from Next.js)
  - Lightweight, modern, and optimized for free-tier deployment

---

## 🚀 Deployment Strategy

- **Containerization:**
  - Docker for local, development, and production environments
- **Backend Hosting:**
  - AWS EC2 (t2.small) for backend server
  - Elastic IP for stable access
- **Database:**
  - AWS RDS (PostgreSQL, t2.micro for free tier eligibility)
- **Frontend Hosting:**
  - Deploy Remix app to Cloudflare Pages or Vercel (supports free tier, fast global delivery)

---

## 🔧 CI/CD & Operations

- GitHub Actions for automated testing and deployment
- Environment variables and AWS Secrets Manager for secure configuration

---

## 🌱 Future Considerations

- Consider AWS ECS or Fargate for scalable container orchestration
- Monitor RDS performance; upgrade instance type as needed
- Add load balancer and auto-scaling for backend if user base grows
- Expand documentation with architecture diagrams and onboarding guides

---

> **Note:**
> The current frontend is Next.js, but migration to Remix is planned for improved developer experience and deployment flexibility.
