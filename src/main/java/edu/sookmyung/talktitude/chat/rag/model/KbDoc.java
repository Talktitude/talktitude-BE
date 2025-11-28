package edu.sookmyung.talktitude.chat.rag.model;

import java.util.List;


public record KbDoc(
        String id,
        String category,
        List<String> tags,
        String lastUpdated,
        String blobText,
        String snippet
) {}
