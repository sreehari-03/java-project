package com.srms.model;

/** role is "teacher" or "student". reg = school registration / staff number. */
public record Account(String username, String password, String role, String name, String reg) {}
