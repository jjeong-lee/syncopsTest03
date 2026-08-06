package kr.ac.knue.fpe.common.service;

public class ValidationFailure extends RuntimeException {
    private final String field;
    public ValidationFailure(String field, String message) { super(message); this.field = field; }
    public String field() { return field; }
}
