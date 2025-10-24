package edu.sookmyung.talktitude.chat.recommend.kb;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// cs_kb.jsonl를 메모리로 로딩
@Component
@RequiredArgsConstructor
public class KnowledgeBase {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper om = new ObjectMapper();

    @Getter
    public static class Doc {
        public String id;
        public String category;
        public List<String> tags;
        public String last_updated;
        public String applies_when;
        public String policy;
        public List<String> steps;
        public List<String> templates;
        public String script; // style류
        @Override public String toString() {
            return String.format("[%s] %s | %s", id, category, policy);
        }
    }

    private final List<Doc> docs = new ArrayList<>();

    @PostConstruct
    public void load() throws Exception {
        Resource res = resourceLoader.getResource("classpath:kb/cs_kb.jsonl");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                Doc d = om.readValue(line, Doc.class);
                docs.add(d);
            }
        }
    }

    public List<Doc> all() { return Collections.unmodifiableList(docs); }
}
