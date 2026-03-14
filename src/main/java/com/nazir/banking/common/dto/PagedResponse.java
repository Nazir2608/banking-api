package com.nazir.banking.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    private PagedResponse(Page<T> sourcePage) {
        this.content       = sourcePage.getContent();
        this.page          = sourcePage.getNumber();
        this.size          = sourcePage.getSize();
        this.totalElements = sourcePage.getTotalElements();
        this.totalPages    = sourcePage.getTotalPages();
        this.last          = sourcePage.isLast();
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(page);
    }
}
