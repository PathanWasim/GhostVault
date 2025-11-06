package com.ghostvault.ui.components;

import javafx.scene.control.Label;

/**
 * Status indicator badge component
 */
public class StatusIndicatorBadge extends Label {
    
    public enum BadgeType {
        SUCCESS, ERROR, WARNING, INFO
    }
    
    public StatusIndicatorBadge(String text, BadgeType type) {
        super(text);
        getStyleClass().add("status-badge");
        
        switch (type) {
            case SUCCESS:
                getStyleClass().add("status-badge-success");
                break;
            case ERROR:
                getStyleClass().add("status-badge-error");
                break;
            case WARNING:
                getStyleClass().add("status-badge-warning");
                break;
            case INFO:
                getStyleClass().add("status-badge-info");
                break;
        }
    }
}