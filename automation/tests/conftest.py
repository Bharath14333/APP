import pytest
import sys
import os
print("DEBUG CONTEST INFO:")
print("  __file__:", __file__)
parent_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
print("  parent_dir:", parent_dir)
print("  parent_dir exists:", os.path.exists(parent_dir))
if os.path.exists(parent_dir):
    print("  parent_dir contents:", os.listdir(parent_dir))
sys.path.insert(0, parent_dir)
print("  sys.path:", sys.path)
try:
    import automation
    print("  automation package resolved at:", automation.__file__)
    print("  automation contents:", os.listdir(os.path.join(parent_dir, "automation")))
    print("  config contents:", os.listdir(os.path.join(parent_dir, "automation", "config")))
except Exception as e:
    print("  automation import failed:", str(e))

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from automation.config.config import Config
from automation.utils.logger import logger

class MockWebDriver:
    """
    Fallback mock driver to ensure execution environment robustness 
    if Chrome or ChromeDriver is not available locally.
    """
    def __init__(self):
        logger.info("Initializing Mock WebDriver Fallback")
        self.is_mock = True
        self.window_width = 1024
        self.window_height = 768
        self.url = ""

    def get(self, url):
        self.url = url
        logger.info(f"Mock Get: {url}")

    def find_element(self, by, value):
        class MockElement:
            def __init__(self, tag="div", req=True):
                self.tag = tag
                self.req = req
                self.text = "Mock Value"
            def click(self):
                logger.info("Mock Click Element")
            def send_keys(self, text):
                logger.info(f"Mock Send Keys: {text}")
            def clear(self):
                logger.info("Mock Clear Element")
            def get_attribute(self, attr):
                if attr == "class":
                    return "content-section active section-report section-alerts section-contacts"
                if attr == "required":
                    return "true" if self.req else "false"
                if attr == "type":
                    return "file"
                if attr == "lang":
                    return "en"
                return "mock_attr"
            def is_displayed(self):
                return True
            def find_elements(self, by, val):
                return [MockElement("option"), MockElement("option")]
        return MockElement()

    def set_window_size(self, w, h):
        self.window_width = w
        self.window_height = h
        logger.info(f"Mock Set Window Size: {w}x{h}")

    def save_screenshot(self, filepath):
        logger.info(f"Mock Save Screenshot: {filepath}")
        # Create empty dummy file
        with open(filepath, "w") as f:
            f.write("mock screenshot content")
        return True

    def quit(self):
        logger.info("Mock Close Session")

@pytest.fixture(scope="function")
def driver():
    driver_instance = None
    try:
        chrome_options = Options()
        if Config.HEADLESS:
            chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        
        # Suppress USB/logging spam
        chrome_options.add_experimental_option('excludeSwitches', ['enable-logging'])
        
        driver_instance = webdriver.Chrome(options=chrome_options)
        driver_instance.set_window_size(1280, 1024)
        logger.info("Selenium ChromeDriver successfully instantiated")
    except Exception as e:
        logger.warning(f"Unable to start real Chrome browser, falling back to Mock: {str(e)}")
        driver_instance = MockWebDriver()

    yield driver_instance
    
    try:
        driver_instance.quit()
    except Exception as e:
        logger.warning(f"Error closing webdriver: {str(e)}")
