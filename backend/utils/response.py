from flask import jsonify
from typing import Any, Dict, List, Optional, Union


class APIResponse:
    """
    A generic API response class to ensure consistency across all endpoints.
    
    Attributes:
        success (bool): Indicates if the request was successful
        message (str): A human-readable message about the response
        data (Any): The main response payload
        errors (List[Dict]): List of errors if any occurred
        status_code (int): HTTP status code
    """
    
    def __init__(
        self,
        success: bool = True,
        message: str = "",
        data: Any = None,
        errors: Optional[List[Dict[str, Any]]] = None,
        status_code: int = 200
    ):
        self.success = success
        self.message = message
        self.data = data
        self.errors = errors or []
        self.status_code = status_code
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert the response to a dictionary format"""
        response_dict = {
            "success": self.success,
            "message": self.message,
        }
        
        # Only include data if it's provided
        if self.data is not None:
            response_dict["data"] = self.data
            
        # Only include errors if there are any
        if self.errors:
            response_dict["errors"] = self.errors
            
        return response_dict
    
    def to_response(self):
        """Convert to a Flask response with appropriate status code"""
        return jsonify(self.to_dict()), self.status_code
    
    @classmethod
    def success_response(
        cls,
        data: Any = None,
        message: str = "Operation successful",
        status_code: int = 200
    ):
        """Create a success response"""
        return cls(
            success=True,
            message=message,
            data=data,
            status_code=status_code
        ).to_response()
    
    @classmethod
    def error_response(
        cls,
        message: str = "An error occurred",
        errors: Optional[List[Dict[str, Any]]] = None,
        status_code: int = 400
    ):
        """Create an error response"""
        return cls(
            success=False,
            message=message,
            errors=errors,
            status_code=status_code
        ).to_response()
    
    @classmethod
    def not_found(cls, resource_name: str = "Resource"):
        """Create a not found response"""
        return cls(
            success=False,
            message=f"{resource_name} not found",
            status_code=404
        ).to_response()
    
    @classmethod
    def unauthorized(cls, message: str = "Unauthorized access"):
        """Create an unauthorized response"""
        return cls(
            success=False,
            message=message,
            status_code=401
        ).to_response()
    
    @classmethod
    def forbidden(cls, message: str = "Access forbidden"):
        """Create a forbidden response"""
        return cls(
            success=False,
            message=message,
            status_code=403
        ).to_response()
    
    @classmethod
    def validation_error(cls, errors: List[Dict[str, Any]]):
        """Create a validation error response"""
        return cls(
            success=False,
            message="Validation failed",
            errors=errors,
            status_code=422
        ).to_response() 