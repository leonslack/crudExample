package com.clever.example.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
public class UserDto {

    private long id;

    private String name;

    private String lastName;

    private String email;

    private String cellphone;

    private Date birthdate;

    private transient int age;

    public int getAge() {
        calculateAge();
        return age;
    }

    private void calculateAge() {
        if (birthdate != null) {
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int birthYear = getYearFromDate(birthdate);
            age = currentYear - birthYear;

            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int birthMonth = getMonthFromDate(birthdate);
            if (birthMonth > currentMonth) {
                age--;
            } else if (birthMonth == currentMonth) {
                int currentDay = cal.get(Calendar.DAY_OF_MONTH);
                int birthDay = getDayFromDate(birthdate);
                if (birthDay > currentDay) {
                    age--;
                }
            }
        }
    }

    private int getYearFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    private int getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    private int getDayFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
