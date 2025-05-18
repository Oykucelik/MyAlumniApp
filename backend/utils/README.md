# API Response Standardization

## Overview

This documentation explains how to use the `APIResponse` class to ensure consistent API responses across all endpoints in the Alumni API.

## Benefits

- Consistent response format across all API endpoints
- Clear separation of success and error states
- Easy to extend with additional fields as needed
- Simplified error handling

## Response Format

All API responses follow this standardized format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    // Response data goes here
  }
}
```

Error responses include:

```json
{
  "success": false,
  "message": "An error occurred",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

## How to Use

### Basic Usage

```python
from utils import APIResponse

# Success response
@app.route('/api/resource', methods=['GET'])
def get_resource():
    data = {'id': 1, 'name': 'Resource Name'}
    return APIResponse.success_response(
        data=data,
        message="Resource retrieved successfully"
    )

# Error response
@app.route('/api/resource', methods=['POST'])
def create_resource():
    # Validation failed
    errors = [
        {'field': 'name', 'message': 'Name is required'}
    ]
    return APIResponse.error_response(
        message="Validation failed",
        errors=errors,
        status_code=422
    )
```

### Common Responses

```python
# Not found
return APIResponse.not_found('User')

# Unauthorized
return APIResponse.unauthorized('Invalid credentials')

# Forbidden
return APIResponse.forbidden('Access denied')

# Validation errors
return APIResponse.validation_error([
    {'field': 'email', 'message': 'Invalid email format'},
    {'field': 'password', 'message': 'Password too short'}
])
```

## Implementation Guidelines

1. Import the `APIResponse` class at the top of your route files:
   ```python
   from utils import APIResponse
   ```

2. Replace all `jsonify()` and manual status code returns with appropriate APIResponse methods.

3. Use descriptive message strings that help the client understand the response.

4. Always include relevant data in success responses and helpful error information in error responses. 