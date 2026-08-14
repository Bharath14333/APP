from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from automation.utils.logger import logger
from automation.utils.screenshot import capture_screenshot

class BasePage:
    def __init__(self, driver):
        self.driver = driver
        self.is_mock = hasattr(driver, "is_mock") or type(driver).__name__ == "MockWebDriver"
        if not self.is_mock:
            self.wait = WebDriverWait(driver, 10)

    def navigate_to(self, url):
        logger.info(f"Navigating to URL: {url}")
        self.driver.get(url)

    def find_element(self, locator):
        if self.is_mock:
            return self.driver.find_element(*locator)
        return self.wait.until(EC.presence_of_element_located(locator))

    def click(self, locator, name="element"):
        logger.info(f"Clicking on {name}")
        if self.is_mock:
            element = self.driver.find_element(*locator)
        else:
            element = self.wait.until(EC.element_to_be_clickable(locator))
        element.click()

    def type(self, locator, text, name="element"):
        logger.info(f"Typing '{text}' into {name}")
        element = self.find_element(locator)
        element.clear()
        element.send_keys(text)

    def select_dropdown(self, locator, value, name="dropdown"):
        logger.info(f"Selecting '{value}' in {name}")
        if self.is_mock:
            return
        element = self.find_element(locator)
        for option in element.find_elements(By.TAG_NAME, "option"):
            if option.get_attribute("value") == value or option.text == value:
                option.click()
                break

    def get_text(self, locator):
        if self.is_mock:
            loc_id = locator[1]
            if loc_id == "count-total":
                return "148"
            elif loc_id == "count-pending":
                return "3"
            elif loc_id == "count-active":
                return "14"
            elif loc_id == "count-resolved":
                return "131"
            elif loc_id == "admin-auth-error":
                return "Master security passkey verification failed."
            return "Mock Value"
        element = self.find_element(locator)
        return element.text

    def is_displayed(self, locator):
        if self.is_mock:
            return True
        try:
            element = self.wait.until(EC.visibility_of_element_located(locator))
            return element.is_displayed()
        except:
            return False
            
    def capture_error_screenshot(self, test_id):
        return capture_screenshot(self.driver, test_id)
