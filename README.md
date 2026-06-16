# 🎓 AI-Powered Campus Issue Resolution System

A full-stack Java Spring Boot application that allows students to submit campus complaints
and uses **AI (via OpenRouter API)** to automatically classify and route them to the correct department.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Client (Browser / Postman)                │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP REST API
┌──────────────────────────▼──────────────────────────────────┐
│              Controller Layer (REST Endpoints)               │
│  ComplaintController │ AdminController │ StudentController   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                Service Layer (Business Logic)                │
│    ComplaintService │ StudentService │ DepartmentService     │
│              ┌─────────────────────┐                        │
│              │    AIService        │  ← Calls OpenRouter     │
│              │ (OpenRouter API)    │                        │
│              └─────────────────────┘                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│            Repository Layer (Spring Data JPA)                │
│  ComplaintRepository │ StudentRepository │ DeptRepository    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      MySQL Database                          │
│    students │ complaints │ departments                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
campus-resolver/
├── pom.xml
└── src/
    └── main/
        ├── java/com/campusresolver/
        │   ├── CampusResolverApplication.java      ← Main entry point
        │   │
        │   ├── model/                              ← JPA Entities
        │   │   ├── Student.java
        │   │   ├── Department.java
        │   │   ├── Complaint.java
        │   │   ├── ComplaintStatus.java             ← Enum
        │   │   └── Priority.java                   ← Enum
        │   │
        │   ├── dto/                                ← Data Transfer Objects
        │   │   ├── ComplaintRequest.java
        │   │   ├── ComplaintResponse.java
        │   │   ├── StatusUpdateRequest.java
        │   │   ├── DashboardStats.java
        │   │   └── AIClassificationResult.java
        │   │
        │   ├── repository/                         ← Spring Data JPA
        │   │   ├── ComplaintRepository.java
        │   │   ├── StudentRepository.java
        │   │   └── DepartmentRepository.java
        │   │
        │   ├── service/                            ← Business Logic
        │   │   ├── ComplaintService.java
        │   │   ├── StudentService.java
        │   │   └── DepartmentService.java
        │   │
        │   ├── ai/
        │   │   └── AIService.java                  ← OpenRouter API integration
        │   │
        │   ├── controller/                         ← REST Endpoints
        │   │   ├── ComplaintController.java
        │   │   ├── AdminController.java
        │   │   ├── StudentController.java
        │   │   └── DepartmentController.java
        │   │
        │   └── config/
        │       ├── SecurityConfig.java             ← Spring Security
        │       ├── CorsConfig.java
        │       └── DataInitializer.java            ← Seeds departments
        │
        └── resources/
            ├── application.properties
            └── schema.sql                         ← MySQL DDL
```

---

## 🚀 Setup & Run Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- OpenRouter API key (free at https://openrouter.ai)

---

### Step 1: Set Up MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# Run the schema script
source /path/to/campus-resolver/src/main/resources/schema.sql
```

---

### Step 2: Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/campus_resolver_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# OpenRouter AI
openrouter.api.key=YOUR_OPENROUTER_API_KEY
openrouter.api.model=openai/gpt-3.5-turbo
```

---

### Step 3: Build and Run

```bash
cd campus-resolver

# Build
mvn clean install

# Run
mvn spring-boot:run
```

The application starts at: **http://localhost:8080**

---

## 📡 API Endpoints

### Student Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/students` | Register new student |
| GET | `/api/students/{id}` | Get student by ID |
| POST | `/api/complaints` | Submit complaint (triggers AI) |
| GET | `/api/complaints/{id}` | Get complaint details |
| GET | `/api/complaints/student/{studentId}` | Get student's complaints |

### Admin Endpoints (Basic Auth: admin / admin123)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/admin/dashboard` | Dashboard statistics |
| GET | `/api/admin/complaints` | All complaints |
| GET | `/api/admin/complaints/status/PENDING` | Filter by status |
| GET | `/api/admin/complaints/urgent` | Urgent pending complaints |
| GET | `/api/admin/complaints/search?keyword=wifi` | Search |
| PUT | `/api/admin/complaints/{id}/status` | Update status |

### Department Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/departments` | List all departments |
| POST | `/api/departments` | Create department (admin) |

---

## 📋 Example JSON Requests & Responses

### Submit Complaint

**Request:** `POST /api/complaints`
```json
{
  "title": "WiFi not working in Block C hostel",
  "description": "The internet connection in Block C has been completely down for the past 2 days. This is affecting my exam preparation. Multiple students are facing this issue.",
  "studentId": 1
}
```

**Response:**
```json
{
  "id": 1,
  "title": "WiFi not working in Block C hostel",
  "description": "The internet connection in Block C has been completely down...",
  "studentId": 1,
  "studentName": "Rahul Sharma",
  "studentRollNumber": "CS21B001",
  "aiCategory": "wifi",
  "priority": "URGENT",
  "aiReasoning": "Network outage affecting exam preparation for multiple students qualifies as urgent",
  "status": "PENDING",
  "departmentId": 1,
  "departmentName": "IT Department",
  "departmentContact": "it@college.edu",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Update Complaint Status

**Request:** `PUT /api/admin/complaints/1/status`
```json
{
  "status": "IN_PROGRESS",
  "adminNotes": "IT team has been dispatched. Expected resolution by tomorrow 5pm."
}
```

### Dashboard Response

**Request:** `GET /api/admin/dashboard`
```json
{
  "totalComplaints": 42,
  "pendingComplaints": 15,
  "inProgressComplaints": 8,
  "resolvedComplaints": 19,
  "urgentComplaints": 7,
  "complaintsByCategory": {
    "wifi": 12,
    "hostel": 9,
    "transport": 6,
    "maintenance": 10,
    "other": 5
  },
  "complaintsByStatus": {
    "PENDING": 15,
    "IN_PROGRESS": 8,
    "RESOLVED": 19,
    "CLOSED": 0,
    "REJECTED": 0
  }
}
```

---

## 🤖 AI Classification Flow

```
Student submits complaint
        │
        ▼
ComplaintController.submitComplaint()
        │
        ▼
ComplaintService.submitComplaint()
        │
        ├─── Validates student exists
        │
        ├─── Calls AIService.classifyComplaint(title, description)
        │         │
        │         ▼
        │    Sends prompt to OpenRouter API (GPT-3.5)
        │    Prompt: "Classify into: wifi/hostel/transport/maintenance/other
        │             Priority: urgent/normal. Return JSON."
        │         │
        │         ▼
        │    Parses AI response: { category, priority, reasoning, dept }
        │         │
        │         ▼
        │    Returns AIClassificationResult
        │
        ├─── Looks up Department by category
        │
        ├─── Creates Complaint entity with AI data
        │
        └─── Saves to MySQL → Returns ComplaintResponse
```

---

## 🔐 Security

- **Student endpoints** (`/api/complaints`, `/api/students`): Public (no auth required)
- **Admin endpoints** (`/api/admin/**`): Requires Basic Auth
  - Username: `admin`
  - Password: `admin123`

> 💡 For production: Replace Basic Auth with JWT tokens

---

## 🛠️ Technologies Used

| Technology | Purpose |
|-----------|---------|
| Java 17 | Programming language |
| Spring Boot 3.2 | Application framework |
| Spring Data JPA | Database ORM |
| Spring Security | Authentication |
| MySQL 8 | Database |
| OpenRouter API | AI classification |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

---

