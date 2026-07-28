package kr.ac.knue.fpe.common.api;

import java.util.List;

public record PageResult<T>(List<T> items, PageMeta page) {
    public record PageMeta(int page, int size, long totalElements, int totalPages) {}
    public static <T> PageResult<T> of(List<T> items, int page, int size) {
        return new PageResult<>(items, new PageMeta(page, size, items.size(), items.isEmpty() ? 0 : 1));
    }
}
