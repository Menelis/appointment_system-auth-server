package co.appointment.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ObjectUtils {
    public static Date addMillisecondsToCurrentDate(final int expiryTimeInMills) {
        Date now = new Date();
        return new Date((now).getTime() + expiryTimeInMills);
    }
    public static LocalDateTime timstampMillisToLocalDateTime(final long dateInMilliseconds) {
        Instant instant = Instant.ofEpochMilli(dateInMilliseconds);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
    public static String getUserRegistrationEmailBody(final String clientUrl) {
        return "Hi,<br/><br/>" +
                String.format("Welcome to Appointment System.To complete registration process click <a href='%s'>here</a> to verify your account.<br/></br/><br/><br/>", clientUrl) +
                "Kind Regards,<br/>" +
                "Appointment System Team.";
    }
    public static String getParameterizedClientUrl(final String clientUrl,
                                                   final String email,
                                                   final String token) {
        return String.format("%s?email=%s&token=%s", clientUrl, email, token);
    }
}
