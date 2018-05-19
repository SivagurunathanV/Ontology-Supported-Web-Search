package com.sw.osws.view;

import java.util.ArrayList;
import java.util.List;

public class TestAutoCompletable implements AutoCompletable<RenderItem, String> {
    public List<RenderItem> autoComplete(String prefix) {
        final RenderItem renderItem1 = new RenderItem(prefix + "a", false);
        final RenderItem renderItem2 = new RenderItem(prefix + "ab", true);
        final RenderItem renderItem3 = new RenderItem(prefix + "abc", false);
        List<RenderItem> renderItems = new ArrayList<RenderItem>();
        renderItems.add(new RenderItem(prefix, false));
        renderItems.add(renderItem1);
        renderItems.add(renderItem2);
        renderItems.add(renderItem3);
        return renderItems;
    }
}
