package com.nhom7.quiz.quizapp.model.dto;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Attempts-by-hour response for admin dashboard chart.
 */
public class AttemptsByHourDTO {
    private int[] countsByHour; // length 24, index = hour 0-23
    private String timezone;
    private LocalDate date; // the local date in provided timezone
    private long total;

    public AttemptsByHourDTO() {
    }

    public AttemptsByHourDTO(int[] countsByHour, String timezone, LocalDate date) {
        this.countsByHour = countsByHour;
        this.timezone = timezone;
        this.date = date;
        long sum = 0;
        if (countsByHour != null) {
            for (int value : countsByHour) {
                sum += value;
            }
        }
        this.total = sum;
    }

    public int[] getCountsByHour() {
        return countsByHour;
    }

    public void setCountsByHour(int[] countsByHour) {
        this.countsByHour = countsByHour;
        this.total = countsByHour == null ? 0 : Arrays.stream(countsByHour).asLongStream().sum();
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotal() {
        return total;
    }
}


