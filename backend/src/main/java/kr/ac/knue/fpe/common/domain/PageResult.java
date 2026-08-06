package kr.ac.knue.fpe.common.domain;

import java.util.List;

public record PageResult<T>(List<T> items, PageInfo page) {
    public record PageInfo(int page, int size, long totalElements) {}
}
