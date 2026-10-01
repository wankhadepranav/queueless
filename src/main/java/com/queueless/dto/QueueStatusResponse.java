package com.queueless.dto;

public class QueueStatusResponse {

    private String tokenNumber;
    private String status;
    private int position;
    private int peopleAhead;
    private int estimatedWaitMinutes;

    public QueueStatusResponse(
            String tokenNumber,
            String status,
            int position,
            int peopleAhead,
            int estimatedWaitMinutes) {

        this.tokenNumber = tokenNumber;
        this.status = status;
        this.position = position;
        this.peopleAhead = peopleAhead;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public String getStatus() {
        return status;
    }

    public int getPosition() {
        return position;
    }

    public int getPeopleAhead() {
        return peopleAhead;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }
}