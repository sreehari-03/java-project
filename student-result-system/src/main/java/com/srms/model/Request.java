package com.srms.model;

/** A correction request (topic is empty) or a topic-assistance request. */
public class Request {
    public final int id;
    public final String reg, subject, topic, message, date;
    public String status = "Pending";

    public Request(int id, String reg, String subject, String topic, String message, String date) {
        this.id = id; this.reg = reg; this.subject = subject; this.topic = topic; this.message = message; this.date = date;
    }
}
