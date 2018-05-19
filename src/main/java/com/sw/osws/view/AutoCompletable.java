package com.sw.osws.view;

import java.util.List;

public interface AutoCompletable<W, P> {
    public List<W> autoComplete(P prefix);
}
