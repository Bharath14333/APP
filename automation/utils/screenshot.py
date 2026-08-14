import os
from datetime import datetime
from automation.utils.logger import logger

def capture_screenshot(driver, test_id):
    # Ensure Screenshots directory exists
    screenshot_dir = os.path.join(
        os.path.dirname(os.path.dirname(os.path.dirname(__file__))), 
        "Test Results", 
        "Screenshots"
    )
    os.makedirs(screenshot_dir, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{test_id}_{timestamp}.png"
    filepath = os.path.join(screenshot_dir, filename)
    
    try:
        driver.save_screenshot(filepath)
        logger.info(f"Screenshot successfully captured: {filepath}")
        return filepath
    except Exception as e:
        logger.error(f"Failed to capture screenshot for {test_id}: {str(e)}")
        return None
