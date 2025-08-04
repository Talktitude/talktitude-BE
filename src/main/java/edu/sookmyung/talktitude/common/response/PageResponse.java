package edu.sookmyung.talktitude.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content; //실제 데이터
    private long totalElements; //전체 데이터 개수
    private int totalPages; //전체 페이지 수
    private int currentPage; //현재 페이지(0부터 시작)
    private int size; //페이지 크기(없으면 default 값)
    private boolean first; //첫 페이지 여부
    private boolean last; //마지막 페이지 여부

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}