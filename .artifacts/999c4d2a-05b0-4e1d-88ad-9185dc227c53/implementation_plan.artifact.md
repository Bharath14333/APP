# Update Dashboard with New Features

The user is unable to see the newly added features (SOS, Map, Manual Reporting) because they are either not directly accessible from the main dashboard or the existing buttons have changed behavior without clear UI indicators.

## Proposed Changes

### [Component] UI Layouts

#### [MODIFY] [fragment_dashboard.xml](file:///C:/Users/bhara/Documents/CisisSenseApp/app/src/main/res/layout/fragment_dashboard.xml)
- Reorganize the dashboard to include a prominent **SOS Panic** button.
- Add a new card for **Interactive Live Map**.
- Add a new card for **Manual Incident Report** (form-based).

### [Component] Fragment Logic

#### [MODIFY] [DashboardFragment.java](file:///C:/Users/bhara/Documents/CisisSenseApp/app/src/main/java/com/emergency/crisissense/activity/DashboardFragment.java)
- Link the new UI components to:
    - `SosActivity.java` (Panic Button)
    - `ReportEmergencyActivity.java` (Manual Form)
    - `MapActivity.java` (Live Map)

## Verification Plan

### Automated Tests
- Run `app` and verify the new buttons are present.

### Manual Verification
- Click on **SOS Panic** and verify it opens the hold-to-trigger screen.
- Click on **Interactive Map** and verify it opens the map view.
- Click on **Manual Report** and verify it opens the incident reporting form.
