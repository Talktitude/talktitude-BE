package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MediaDto {
    private MediaType mediaType;
    private String url;
    private Long size;
}