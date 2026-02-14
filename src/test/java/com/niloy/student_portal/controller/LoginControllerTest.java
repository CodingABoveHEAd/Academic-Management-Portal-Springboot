package com.niloy.student_portal.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController Tests")
class LoginControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private LoginController loginController;

    @Nested
    @DisplayName("Login Page Tests")
    class LoginPageTests {

        @Test
        @DisplayName("Should return login view without errors")
        void login_WithoutErrors_ShouldReturnLoginView() {
            // Act
            String viewName = loginController.login(null, null, model);

            // Assert
            assertEquals("login", viewName);
            verify(model, never()).addAttribute(eq("error"), anyString());
            verify(model, never()).addAttribute(eq("message"), anyString());
        }

        @Test
        @DisplayName("Should return login view with error message")
        void login_WithError_ShouldReturnLoginViewWithErrorMessage() {
            // Act
            String viewName = loginController.login("error", null, model);

            // Assert
            assertEquals("login", viewName);
            verify(model).addAttribute("error", "Invalid username or password!");
        }

        @Test
        @DisplayName("Should return login view with logout message")
        void login_WithLogout_ShouldReturnLoginViewWithLogoutMessage() {
            // Act
            String viewName = loginController.login(null, "logout", model);

            // Assert
            assertEquals("login", viewName);
            verify(model).addAttribute("message", "You have been logged out successfully.");
        }

        @Test
        @DisplayName("Should return login view with both error and logout messages")
        void login_WithErrorAndLogout_ShouldReturnLoginViewWithBothMessages() {
            // Act
            String viewName = loginController.login("error", "logout", model);

            // Assert
            assertEquals("login", viewName);
            verify(model).addAttribute("error", "Invalid username or password!");
            verify(model).addAttribute("message", "You have been logged out successfully.");
        }
    }

    @Nested
    @DisplayName("Home Redirect Tests")
    class HomeRedirectTests {

        @Test
        @DisplayName("Should redirect to login page")
        void home_ShouldRedirectToLogin() {
            // Act
            String viewName = loginController.home();

            // Assert
            assertEquals("redirect:/login", viewName);
        }
    }
}

