import logging
import os

def setup_logger(name="selenium_runner", log_file="execution.log"):
    # Ensure logs folder exists
    log_dir = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "Test Results", "Logs")
    os.makedirs(log_dir, exist_ok=True)
    
    log_path = os.path.join(log_dir, log_file)
    
    logger = logging.getLogger(name)
    logger.setLevel(logging.INFO)
    
    # Avoid duplicate handlers
    if not logger.handlers:
        file_handler = logging.FileHandler(log_path, mode="w", encoding="utf-8")
        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)
        
        # Console handler for local terminal
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(formatter)
        logger.addHandler(console_handler)
        
    return logger

logger = setup_logger()
