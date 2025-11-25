# 🎡 FairFree – Backend Service  
Backend API for the FairFree community donation and waste-reduction platform.

FairFree helps individuals, households, and organizations track item expiration dates, reduce waste, and donate items to community members in need. This backend provides secure authentication, business logic, notifications, and persistent data management for all system features.

---

## 👥 Team Members

- **Nguyen Khanh, Tran** – Scrum Master, Tech Lead, DevOps  
- **Badri, Paudel** – Project Owner, Backend Developer  
- **Dawit, Fsaha Welegebriel** – SecOps, Backend Developer  
- **Temuujin, Bat Amgalan** – Front-end Developer, CI/CD Owner  

---

## 🔗 Related Repositories

| Component | Repository |
|----------|------------|
| **Frontend (React)** | https://github.com/badripaudel77/FairFree_Front |
| **Backend (Spring Boot)** | *(You are here)* |

---

## 🧩 Features (Backend)

- 🧾 **User & Role Management** (Auth0 integrated)  
- 🍎 **Item Tracking** (create, update, expiration management)  
- 🎁 **Donation & Claim System**  
- 🔔 **Notification Engine** for expiring items  
- 📊 **Dashboard & Analytics APIs**  
- 🔐 **Secure JWT-based authentication**  
- ☁️ **Cloud-ready architecture (AWS)**  

---

## 🧰 Tech Stack

### **Backend**
- Java **24.0.1**  
- Spring Boot  
- Maven  
- Auth0 Authentication  
- Spring Security  
- RESTful API

### **Infrastructure**
- AWS Elastic Beanstalk  
- AWS S3  
- Docker  
- GitHub Actions (CI/CD)

### **Database**
- AWS RDS (PostgreSQL or MySQL)


---

## 🚀 Deployment (AWS Elastic Beanstalk)
- Application is containerized using Docker
- GitHub Actions builds & deploys automatically
- Supports rolling updates and zero-downtime releases
