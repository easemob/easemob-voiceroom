package com.voiceroom.common.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class MdStringUtils {


    public static String randomDelete(String str,int num){
        if(str.length() < num){
            return str;
        }
        char[] chars = str.toCharArray();
        String randomStr = "ABCDEFGHIZKLMNOPQRSTUVWXYZ";
        for(int i = 0;i < num;i++){
            Random random = new Random();
            char c = randomStr.charAt((int)(Math.random() * 26));
            chars[random.nextInt(str.length() - 1)] = c;
        }
        return String.valueOf(chars);
    }

    public static String verificationCode(){
        Random random = new Random();
        String result  = String.format("%04d",random.nextInt(9999));
        return result;
    }

    public static Integer getRemainSecondsOneDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        long seconds = ChronoUnit.SECONDS.between(now, midnight);
        return (int) seconds;
    }
}
