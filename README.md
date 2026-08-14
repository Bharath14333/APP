# CrisisSense - AI-Based Emergency Reporting & Response Management System

CrisisSense is a production-ready enterprise Android application designed for rapid emergency reporting, response scaling, and safety coordinate management. It bridges the gap between citizens, system dispatchers, first responders, and community volunteers in real-time.

---

## 📱 Features & Modules

### 1. Authentication & Onboarding
- **Splash Screen:** Seamless startup loading brand animations.
- **Onboarding/Intro:** Multi-slide interactive carousel explaining the core pillars (Emergency Reporting, AI Severity, Community Volunteers).
- **Welcome Hub:** Dynamic entry routing user to Citizen login/register or the Admin Console gateway.
- **OTP Verification:** Simulates secure login/verification.

### 2. Citizen/User Module
- **Dashboard:** Features alert indicators and quick-launch shortcuts (Report Incident, View Alerts, Dial Contacts, Nearby Services, Volunteer SignUp).
- **Emergency Reporting:** Allows capturing details, category selection, attaching images/evidence, and pinning coordinates using GPS/Reverse Geocoding.
- **Live Alerts:** Dynamic feed showing active emergency situations approved by administration.
- **Emergency Contacts:** Tap-to-dial direct dialer connections (Police, Fire, Ambulance, Disaster Management).
- **Nearby Services:** Filtered tabs (Hospitals, Police, Fire Stations) showing nearest facilities, distances, and contact details.
- **Volunteer Registration:** Elevates user role to "volunteer" allowing first-response alerts based on select skill categories.
- **Profile / Edit Profile:** Dynamic avatar updating and personal details sync.
- **Settings:** Controls notifications, dark mode themes, and GPS tracking preferences.

### 3. Admin Console Module
- **Admin Gateway:** Verification portal with a secure passkey check (`admin123`).
- **Admin Dashboard:** Displays statistics counters for total and pending incidents, alongside shortcut actions.
- **Incident Management:** Shows lists filtered by status where dispatchers update incident priority or status (Pending -> Approved/Resolved).
- **User Directory:** Browse system user bases and audit registration attributes.
- **System Analytics:** Uses charts (`MPAndroidChart`) to breakdown category percentages and severity counts.
- **Audit Trails (System Logs):** Administrative logs documenting system modifications for compliance monitoring.
- **Broadcast Dialog:** Directly broadcast warning alerts system-wide to all citizen notifications feeds.

---

## 🗄️ Database Design (Firestore Collections)

The application utilizes Google Firebase Firestore for real-time document storage.

### 1. `users` Collection
Stores credential profiles of citizens, volunteers, and admins.
```json
{
  "userId": "string (Auth UID)",
  "name": "string",
  "email": "string",
  "phone": "string",
  "role": "string (citizen | volunteer | responder | admin)",
  "profileImage": "string (Storage URL)",
  "address": "string",
  "createdDate": "timestamp"
}
```

### 2. `incidents` Collection
Emergency alerts reported by users.
```json
{
  "incidentId": "string (UUID)",
  "userId": "string (Reporter UID)",
  "reportedByName": "string",
  "title": "string",
  "type": "string (Fire | Flood | Accident | Medical | Crime | Other)",
  "description": "string",
  "locationName": "string",
  "gpsCoordinates": "string",
  "image": "string (Storage URL)",
  "status": "string (pending | approved | rejected | resolved)",
  "severity": "string (low | medium | high | critical)",
  "createdDate": "timestamp"
}
```

### 3. `notifications` Collection
Push and feed alerts.
```json
{
  "notificationId": "string (UUID)",
  "userId": "string (Target UID or 'all' for broadcasts)",
  "title": "string",
  "message": "string",
  "timestamp": "timestamp"
}
```

### 4. `feedback` Collection
Helpdesk and query tickets.
```json
{
  "feedbackId": "string (UUID)",
  "email": "string",
  "message": "string",
  "timestamp": "timestamp"
}
```

### 5. `system_logs` Collection
Audit event trails.
```json
{
  "logId": "string (UUID)",
  "action": "string (USER_LOGIN | USER_REGISTRATION | INCIDENT_REPORTED | INCIDENT_STATUS_CHANGE | VOLUNTEER_REGISTRATION | ADMIN_LOGIN | ADMIN_BROADCAST)",
  "details": "string",
  "performedBy": "string (User email)",
  "timestamp": "timestamp"
}
```

---

## 🛠️ Setup & Installation Instructions

1. **Prerequisites:**
   - Android Studio (Koala 2024.1 or newer recommended)
   - JDK 17 (pre-installed inside Android Studio)
   - An active Firebase Project.

2. **Clone and Import Project:**
   - Open Android Studio, select **File -> Open**, and choose the project directory (`CrisisSenseApp`).
   - Android Studio will configure the project settings and download Gradle 8.5 automatically.

3. **Firebase Setup:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with the package name `com.emergency.crisissense`.
   - Download the `google-services.json` file and place it under the `app/` folder (replacing the development placeholder file).
   - Enable **Firebase Authentication** (Email/Password), **Cloud Firestore**, **Firebase Storage**, and **Firebase Cloud Messaging**.

4. **Build & Run:**
   - Click the green **Run** button to launch on an Emulator or physical Android device.
   - For demo compilation offline testing, prefilled admin login credentials are:
     - **Email:** `admin@crisissense.com`
     - **Password:** `admin123`
     - **Passkey:** `admin123`
