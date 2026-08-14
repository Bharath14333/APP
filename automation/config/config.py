import os

class Config:
    # Target deployment URL for GitHub Pages
    BASE_URL = os.getenv("BASE_URL", "https://username.github.io/CisisSenseApp/")
    
    # Browser options
    HEADLESS = os.getenv("HEADLESS", "true").lower() in ("true", "1", "yes")
    
    # Wait timeout in seconds
    TIMEOUT = int(os.getenv("TIMEOUT", "10"))
    
    # Credentials
    ADMIN_PASSKEY = os.getenv("ADMIN_PASSKEY", "admin123")
