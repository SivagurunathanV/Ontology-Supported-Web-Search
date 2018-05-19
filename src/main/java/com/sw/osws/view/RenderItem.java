package com.sw.osws.view;

public class RenderItem {
    private final String renderText;
    private final boolean displayDash;

    public RenderItem(final String renderText, final boolean displayDash) {
        this.renderText = renderText;
        this.displayDash = displayDash;
    }

    public String getRenderText() {
        return renderText;
    }

    public boolean isDisplayDash() {
        return displayDash;
    }
}
