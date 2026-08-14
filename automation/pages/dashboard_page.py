from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class DashboardPage(BasePage):
    # Tab Navigations
    NAV_DASHBOARD = (By.ID, "nav-dash")
    NAV_REPORT = (By.ID, "nav-report")
    NAV_ALERTS = (By.ID, "nav-alerts")
    NAV_CONTACTS = (By.ID, "nav-contacts")
    NAV_SERVICES = (By.ID, "nav-services")
    NAV_ADMIN = (By.ID, "nav-admin")

    # Metrics
    COUNT_TOTAL = (By.ID, "count-total")
    COUNT_PENDING = (By.ID, "count-pending")
    COUNT_ACTIVE = (By.ID, "count-active")
    COUNT_RESOLVED = (By.ID, "count-resolved")

    # SOS trigger
    SOS_TRIGGER_BTN = (By.ID, "sos-trigger-btn")
    SOS_COUNTDOWN = (By.ID, "sos-countdown")
    SOS_STATUS_ALERT = (By.ID, "sos-status-alert")

    def click_dashboard(self):
        self.click(self.NAV_DASHBOARD, "Dashboard Tab")

    def click_report(self):
        self.click(self.NAV_REPORT, "Report Emergency Tab")

    def click_alerts(self):
        self.click(self.NAV_ALERTS, "Live Alerts Tab")

    def click_contacts(self):
        self.click(self.NAV_CONTACTS, "Emergency Contacts Tab")

    def click_services(self):
        self.click(self.NAV_SERVICES, "Nearby Services Tab")

    def click_admin(self):
        self.click(self.NAV_ADMIN, "Admin Portal Tab")

    def get_total_incidents(self):
        return int(self.get_text(self.COUNT_TOTAL))

    def get_pending_incidents(self):
        return int(self.get_text(self.COUNT_PENDING))

    def get_active_incidents(self):
        return int(self.get_text(self.COUNT_ACTIVE))

    def get_resolved_incidents(self):
        return int(self.get_text(self.COUNT_RESOLVED))
        
    def trigger_sos(self):
        self.click(self.SOS_TRIGGER_BTN, "SOS Panic Button")
