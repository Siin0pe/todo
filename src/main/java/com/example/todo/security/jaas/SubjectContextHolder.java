package com.example.todo.security.jaas;

import javax.security.auth.Subject;

public final class SubjectContextHolder {
    private static final ThreadLocal<Subject> CURRENT_SUBJECT = new ThreadLocal<>();

    private SubjectContextHolder() {
    }

    public static void set(Subject subject) {
        CURRENT_SUBJECT.set(subject);
    }

    public static Subject get() {
        return CURRENT_SUBJECT.get();
    }

    public static void clear() {
        CURRENT_SUBJECT.remove();
    }
}
