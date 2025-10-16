package com.ghostvault.ui.components;

import com.ghostvault.ui.controllers.ModeController;

/**
 * Mode change handler interface for backend integration
 */
@FunctionalInterface
public interface ModeChangeHandler {
    /**
     * Handle mode change request
     * @param newMode The requested new mode
     * @return true if mode change was successful, false otherwise
     */
    boolean handleModeChange(ModeController.VaultMode newMode);
}