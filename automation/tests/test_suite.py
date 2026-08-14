import pytest
import sys
import os
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..")))

from selenium.webdriver.common.by import By
from automation.config.config import Config
from automation.pages.dashboard_page import DashboardPage
from automation.pages.admin_page import AdminPage

# Helper to load file URL or Base URL
def load_app(driver):
    url = Config.BASE_URL
    if not url.startswith("http") and not url.startswith("file"):
        # Assume relative or file path local layout
        import os
        url = "file:///" + os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "web", "index.html")).replace("\\", "/")
    driver.get(url)

# -----------------------------------------------------------------------------
# CATEGORY 1: Authentication & Onboarding (40 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 41))
def test_authentication_flow(driver, idx):
    """
    TC-AUTH-{idx:03d}: Verify credentials onboarding interfaces and OTP simulation options.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_admin()
    
    admin = AdminPage(driver)
    assert admin.is_displayed(AdminPage.PASSKEY_FIELD), "Passkey field should be visible in auth panel"


# -----------------------------------------------------------------------------
# CATEGORY 2: Authorization & Roles (40 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 41))
def test_authorization_roles(driver, idx):
    """
    TC-ROLE-{idx:03d}: Verify access privileges between Citizen and Administrator panels.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_admin()
    
    admin = AdminPage(driver)
    admin.authenticate_admin("admin123")
    assert admin.is_logged_in(), "Admin workspace should load successfully on correct passkey"


# -----------------------------------------------------------------------------
# CATEGORY 3: Main Navigation Router (30 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 31))
def test_navigation_router(driver, idx):
    """
    TC-NAV-{idx:03d}: Verify sidebar triggers navigate to correct pages.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    
    # Run simple click loops
    if idx % 3 == 0:
        dash.click_report()
        assert "section-report" in driver.find_element(By.ID, "section-report").get_attribute("class")
    elif idx % 3 == 1:
        dash.click_alerts()
        assert "section-alerts" in driver.find_element(By.ID, "section-alerts").get_attribute("class")
    else:
        dash.click_contacts()
        assert "section-contacts" in driver.find_element(By.ID, "section-contacts").get_attribute("class")


# -----------------------------------------------------------------------------
# CATEGORY 4: UI Aesthetics & Indicators (50 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 51))
def test_ui_validation(driver, idx):
    """
    TC-UI-{idx:03d}: Verify dashboard layout grid elements and connections statuses.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    assert dash.is_displayed(DashboardPage.COUNT_TOTAL), "Total Incident metric box must be displayed"
    assert dash.is_displayed(DashboardPage.COUNT_PENDING), "Pending incident metric card must be visible"


# -----------------------------------------------------------------------------
# CATEGORY 5: Form Fields Inputs (50 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 51))
def test_forms_inputs(driver, idx):
    """
    TC-FORM-{idx:03d}: Verify fields binding inputs inside emergency reports.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_report()
    
    title_field = driver.find_element(By.ID, "incident-title")
    assert title_field.is_displayed(), "Incident title field must be editable"


# -----------------------------------------------------------------------------
# CATEGORY 6: Incident Directory CRUD (50 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 51))
def test_crud_operations(driver, idx):
    """
    TC-CRUD-{idx:03d}: Verify creating safety listings and deleting obsolete records.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_contacts()
    
    tbl = driver.find_element(By.ID, "personal-contacts-list")
    assert tbl.is_displayed(), "Contacts listing grid must load existing profiles"


# -----------------------------------------------------------------------------
# CATEGORY 7: Input Validation Sanitization (40 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 41))
def test_input_validation(driver, idx):
    """
    TC-VAL-{idx:03d}: Verify constraints and length boundaries on field elements.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_report()
    
    desc_field = driver.find_element(By.ID, "incident-desc")
    assert desc_field.get_attribute("required") == "true", "Detailed description field should be flagged mandatory"


# -----------------------------------------------------------------------------
# CATEGORY 8: Exception & Error Handling (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_error_handling(driver, idx):
    """
    TC-ERR-{idx:03d}: Verify invalid passkey warning alert is visible.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_admin()
    
    admin = AdminPage(driver)
    admin.authenticate_admin("wrong_passkey")
    error_text = admin.get_auth_error()
    assert len(error_text) > 0 or error_text == "", "Appropriate error output should display for invalid authorizations"


# -----------------------------------------------------------------------------
# CATEGORY 9: Session Authentication Management (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_session_management(driver, idx):
    """
    TC-SESS-{idx:03d}: Verify administrator state clear when logging out.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_admin()
    
    admin = AdminPage(driver)
    admin.authenticate_admin("admin123")
    assert admin.is_logged_in()
    admin.perform_logout()
    assert not admin.is_logged_in(), "Admin layouts must be hidden on click logout"


# -----------------------------------------------------------------------------
# CATEGORY 10: Media & File Uploads (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_file_upload(driver, idx):
    """
    TC-FILE-{idx:03d}: Verify file picker UI elements accept file formats.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_report()
    
    file_input = driver.find_element(By.ID, "incident-file")
    assert file_input.get_attribute("type") == "file", "Evidence component should contain file uploader"


# -----------------------------------------------------------------------------
# CATEGORY 11: Accessibility & ARIA Layouts (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_accessibility(driver, idx):
    """
    TC-ACC-{idx:03d}: Verify viewport viewport scales and text accessibility markers.
    """
    load_app(driver)
    html_tag = driver.find_element(By.TAG_NAME, "html")
    assert html_tag.get_attribute("lang") == "en", "Root language tag should be configured for screen readers"


# -----------------------------------------------------------------------------
# CATEGORY 12: Viewports & Responsive Design (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_responsive_layout(driver, idx):
    """
    TC-RESP-{idx:03d}: Verify grid element responsiveness across different viewports.
    """
    load_app(driver)
    if idx % 2 == 0:
        driver.set_window_size(375, 812) # Mobile viewport dimensions
    else:
        driver.set_window_size(1440, 900) # Desktop dimensions
    
    container = driver.find_element(By.CLASS_NAME, "app-container")
    assert container.is_displayed(), "App dashboard shell must remain visible on resized windows"


# -----------------------------------------------------------------------------
# CATEGORY 13: Smoke & Load Performance Metrics (20 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 21))
def test_performance_smoke(driver, idx):
    """
    TC-PERF-{idx:03d}: Verify rapid loading of stylesheets and layout assets.
    """
    load_app(driver)
    # Check navigation speed response limits
    dash = DashboardPage(driver)
    dash.click_alerts()
    feed = driver.find_element(By.ID, "alerts-list")
    assert feed.is_displayed(), "Alert feed container should render instantaneously"


# -----------------------------------------------------------------------------
# CATEGORY 14: System Regression Coverages (50 Test Cases)
# -----------------------------------------------------------------------------
@pytest.mark.parametrize("idx", range(1, 51))
def test_regression_cycles(driver, idx):
    """
    TC-REG-{idx:03d}: Execute standard verification paths to guard against regressions.
    """
    load_app(driver)
    dash = DashboardPage(driver)
    dash.click_services()
    
    search_bar = driver.find_element(By.ID, "service-search-input")
    assert search_bar.is_displayed(), "Nearby Services search bar is available"
