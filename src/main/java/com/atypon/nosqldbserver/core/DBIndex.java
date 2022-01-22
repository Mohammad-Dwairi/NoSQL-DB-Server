package com.atypon.nosqldbserver.core;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DBIndex {

    private final String path;
    private final Map<String, List<DBDocumentLocation>> indexMap;

}
