package com.niloy.student_portal.controller;

import com.niloy.student_portal.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginTests {

        @Test
        @DisplayName("Should return success when authentication is valid")
        void login_WithValidAuthentication_ShouldReturnSuccess() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"));
            when(authentication.getAuthorities()).thenReturn(authorities);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.login(authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Login successful", response.getBody().getMessage());
            assertEquals("testuser", response.getBody().getData().get("username"));
            assertEquals("ROLE_TEACHER", response.getBody().getData().get("role"));
        }

        @Test
        @DisplayName("Should return error when authentication is null")
        void login_WithNullAuthentication_ShouldReturnError() {
            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.login(null);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("Invalid credentials", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should return error when not authenticated")
        void login_WithUnauthenticated_ShouldReturnError() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.login(authentication);

            // Assert
            assertNotNull(response);
            assertFalse(response.getBody().isSuccess());
            assertEquals("Invalid credentials", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should return UNKNOWN role when no authorities present")
        void login_WithNoAuthorities_ShouldReturnUnknownRole() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.login(authentication);

            // Assert
            assertEquals("UNKNOWN", response.getBody().getData().get("role"));
        }
    }

    @Nested
    @DisplayName("Check Status Endpoint Tests")
    class CheckStatusTests {

        @Test
        @DisplayName("Should return authenticated status when valid")
        void checkStatus_WithValidAuthentication_ShouldReturnStatus() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            Collection authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));
            when(authentication.getAuthorities()).thenReturn(authorities);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.checkStatus(authentication);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertTrue((Boolean) response.getBody().getData().get("authenticated"));
            assertEquals("testuser", response.getBody().getData().get("username"));
            assertEquals("ROLE_STUDENT", response.getBody().getData().get("role"));
        }

        @Test
        @DisplayName("Should return not authenticated when authentication is null")
        void checkStatus_WithNullAuthentication_ShouldReturnNotAuthenticated() {
            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.checkStatus(null);

            // Assert
            assertNotNull(response);
            assertFalse(response.getBody().isSuccess());
            assertEquals("Not authenticated", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should return not authenticated when not authenticated")
        void checkStatus_WithUnauthenticated_ShouldReturnNotAuthenticated() {
            // Arrange
            when(authentication.isAuthenticated()).thenReturn(false);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = authController.checkStatus(authentication);

            // Assert
            assertFalse(response.getBody().isSuccess());
            assertEquals("Not authenticated", response.getBody().getMessage());
        }
    }
}