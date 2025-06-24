# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IKU is a full-stack web application built with a Java Spring Boot backend and Remix frontend. The project includes AI-powered features using Google Gemini, authentication via Clerk, and integration with Riot Games API for gaming-related functionality.

## Architecture

### Backend (Java Spring Boot)
- **Location**: `/backend/`
- **Framework**: Spring Boot 3.4.5 with Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Authentication**: Clerk integration with JWT tokens
- **External APIs**: Google Gemini, Riot Games API, Naver API
- **Security**: Spring Security with custom JWT handling

### Frontend (Remix)
- **Location**: `/frontend/`
- **Framework**: Remix with React 18
- **Styling**: Tailwind CSS with Radix UI components
- **Build Tool**: Vite
- **Authentication**: Clerk integration

### Key Modules
- **Auth**: JWT authentication with Clerk integration (`backend/src/main/java/org/mtvs/backend/auth/`)
- **User Management**: User entities and services (`backend/src/main/java/org/mtvs/backend/user/`)
- **Announcements**: CRUD operations for announcements (`backend/src/main/java/org/mtvs/backend/announcement/`)
- **Riot API**: Integration with Riot Games API for match data (`backend/src/main/java/org/mtvs/backend/riot/`)
- **Gemini**: AI integration for chat functionality (`backend/src/main/java/org/mtvs/backend/gemini/`)

## Development Commands

### Backend
```bash
# Navigate to backend directory
cd backend

# Run the Spring Boot application
./gradlew bootRun

# Build the project
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Frontend
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run production server
npm start

# Lint code
npm run lint

# Type check
npm run typecheck
```

### Database
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d

# Stop database
docker-compose down
```

## Configuration

### Backend Configuration
- **Main config**: `backend/src/main/resources/application.yml`
- **Environment variables**: Uses Spring Dotenv for `.env.local`
- **Database**: PostgreSQL connection configured via environment variables
- **JWT secrets**: Configured via environment variables
- **API keys**: Gemini, Naver, and Riot API keys via environment variables

### Frontend Configuration
- **Package config**: `frontend/package.json`
- **Vite config**: `frontend/vite.config.ts`
- **API proxy**: Frontend proxies `/api` calls to backend at `localhost:8080`

## Database Schema

The application uses PostgreSQL with the following main entities:
- **User**: User management with roles and signup categories
- **Announcement**: Announcement system with CRUD operations
- **Match/Participant**: Riot Games API data storage for match information
- **MatchTimeline/MatchEvent**: Detailed match timeline and event data

## API Integration

### External APIs
- **Google Gemini**: AI chat functionality at `/api/gemini`
- **Riot Games API**: Player and match data integration
- **Naver API**: Additional API services
- **Clerk**: Authentication and user management

### Internal API Structure
- **Auth endpoints**: `/api/auth/*` - Authentication and JWT management
- **User endpoints**: `/api/user/*` - User management
- **Announcement endpoints**: `/api/announcement/*` - Announcement CRUD
- **Riot endpoints**: `/api/riot/*` - Gaming data
- **Gemini endpoints**: `/api/gemini/*` - AI chat

## Authentication Flow

1. **Frontend**: Uses Clerk for user authentication
2. **Backend**: Validates Clerk JWT tokens via `ClerkJwtVerifier`
3. **Security**: Spring Security configuration handles JWT validation
4. **Authorization**: Role-based access control implemented

## Development Workflow

1. **Start Database**: `docker-compose up -d`
2. **Start Backend**: `cd backend && ./gradlew bootRun`
3. **Start Frontend**: `cd frontend && npm run dev`
4. **Access Application**: Frontend at `http://localhost:5173`, Backend at `http://localhost:8080`

## Testing

### Backend Testing
- Uses JUnit 5 and Spring Boot Test
- Test location: `backend/src/test/java/`
- Run with: `./gradlew test`

### Frontend Testing
- ESLint for code quality
- TypeScript for type checking
- Run linting: `npm run lint`
- Run type checking: `npm run typecheck`

## Important Notes

- **Database**: Application uses `create` DDL mode - database schema is recreated on startup
- **CORS**: Backend configured to handle cross-origin requests from frontend
- **Proxy**: Frontend development server proxies API calls to backend
- **Environment**: Uses `.env.local` for sensitive configuration in backend
- **Migration**: Project originally used Next.js but has been migrated to Remix