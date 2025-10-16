package com.ghostvault.ui.components;

import com.ghostvault.ui.controllers.ModeController;

/**
 * Authentication provider interface for backend integration
 */
@FunctionalInterface
public interface AuthenticationProvider {
    /**
     * Authenticate user with password and return detected mode
     */
    ModeController.VaultMode authenticate(String password) throws Exception;
}