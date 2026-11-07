package co.appointment.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@SuppressWarnings({"HideUtilityClassConstructor"})
public class ObjectUtils {

    public static Date addMillisecondsToCurrentDate(final int expiryTimeInMills) {
        Date now = new Date();
        return new Date((now).getTime() + expiryTimeInMills);
    }

    public static LocalDateTime timstampMillisToLocalDateTime(final long dateInMilliseconds) {
        Instant instant = Instant.ofEpochMilli(dateInMilliseconds);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    //    public static String getUserRegistrationEmailBody(
    //            final String clientUrl,
    //            final User user,
    //            final VerificationToken verificationToken,
    //            final String emailTemplate) {
    //        final String registrationEmailVerificationUrl = String.format(
    //                "%s/account/confirm-email?email=%s&token=%s", clientUrl, user.getEmail(),
    // verificationToken.getToken());
    //        return String.format(
    //                emailTemplate,
    //                getUserFullName(user),
    //                registrationEmailVerificationUrl,
    //                SharedConstants.APPOINTMENT_SYSTEM_EMAIL_FOOTER);
    //    }

    //    public static String getPasswordResetEmailBody(
    //            final String clientUrl,
    //            final VerificationToken verificationToken,
    //            final User user,
    //            final String emailTemplate) {
    //        final String forgotPasswordUrl = String.format(
    //                "%s/account/reset-password?email=%s&token=%s",
    //                clientUrl, user.getEmail(), verificationToken.getToken());
    //        return String.format(
    //                emailTemplate,
    //                getUserFullName(user),
    //                forgotPasswordUrl,
    //                SharedConstants.APPOINTMENT_SYSTEM_EMAIL_FOOTER);
    //    }
    //
    //    public static String getUserFullName(final User user) {
    //        return user.getFirstName() + " " + user.getLastName();
    //    }
}
