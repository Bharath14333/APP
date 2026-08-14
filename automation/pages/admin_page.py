from selenium.webdriver.common.by import By
from automation.pages.base_page import BasePage

class AdminPage(BasePage):
    # Passkey Authentication
    PASSKEY_FIELD = (By.ID, "admin-passkey")
    VERIFY_BTN = (By.ID, "btn-admin-verify")
    AUTH_ERROR = (By.ID, "admin-auth-error")
    
    # Workspace Layouts
    ADMIN_WORKSPACE = (By.ID, "admin-workspace-box")
    LOGOUT_BTN = (By.ID, "btn-admin-logout")

    # System Broadcast
    BROADCAST_TITLE = (By.ID, "broadcast-title")
    BROADCAST_MESSAGE = (By.ID, "broadcast-message")
    BROADCAST_SEVERITY = (By.ID, "broadcast-severity")
    BROADCAST_SUBMIT = (By.ID, "btn-broadcast-submit")

    # Queue Table
    INCIDENTS_QUEUE = (By.ID, "admin-incidents-queue")

    def authenticate_admin(self, passkey):
        self.type(self.PASSKEY_FIELD, passkey, "Admin Passkey Field")
        self.click(self.VERIFY_BTN, "Verify Button")

    def is_logged_in(self):
        return self.is_displayed(self.ADMIN_WORKSPACE)

    def get_auth_error(self):
        return self.get_text(self.AUTH_ERROR)

    def perform_logout(self):
        self.click(self.LOGOUT_BTN, "Admin Logout Button")

    def dispatch_broadcast(self, title, message, severity):
        self.type(self.BROADCAST_TITLE, title, "Broadcast Title Input")
        self.type(self.BROADCAST_MESSAGE, message, "Broadcast Message Input")
        self.select_dropdown(self.BROADCAST_SEVERITY, severity, "Broadcast Severity Select")
        self.click(self.BROADCAST_SUBMIT, "Broadcast Submit Button")
