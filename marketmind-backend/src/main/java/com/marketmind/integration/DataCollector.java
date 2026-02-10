package com.marketmind.integration;

import com.marketmind.domain.PostSource;

public interface DataCollector {
    void collect();
    PostSource getSource();
}
