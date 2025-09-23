package edu.sookmyung.talktitude.chat.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private boolean enabled = false;
    private int topk = 4;
    private int n = 3; // 추천 개수 (참고용)
    private int bizDays = 3; // 영업일 표기 (참고용)


    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getTopk() { return topk; }
    public void setTopk(int topk) { this.topk = topk; }
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
    public int getBizDays() { return bizDays; }
    public void setBizDays(int bizDays) { this.bizDays = bizDays; }
}
