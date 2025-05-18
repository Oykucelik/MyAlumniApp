import os
from dotenv import load_dotenv
from app import create_app

# Load environment variables from .env file
load_dotenv()

# Determine environment from FLASK_ENV (default to development)
env = os.getenv('FLASK_ENV', 'development')

# Create app instance
app = create_app(env)

if __name__ == '__main__':
    # Get host and port from environment or use defaults
    host = os.getenv('HOST', '0.0.0.0')
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('FLASK_DEBUG', '1') == '1'
    
    # Run the app
    app.run(host=host, port=port, debug=debug) 