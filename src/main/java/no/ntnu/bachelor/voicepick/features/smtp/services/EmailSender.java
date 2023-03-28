package no.ntnu.bachelor.voicepick.features.smtp.services;

import jakarta.mail.internet.MimeMessage;
import no.ntnu.bachelor.voicepick.features.smtp.models.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class EmailSender {

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Sends an email asynchronously from the voicepick sender
     *
     * @param email email object that will be sent to the recipient
     * @return Success / error message after trying to send a mail
     * @throws RuntimeException if email fails to send
     */
    @Async
    public Future<String> sendMail(Email email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("voicepick");
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getEmailSubject().getText());
            helper.setText(email.getEmailBody(), true);

            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture("Mail Sent Successfully...");
        } catch (jakarta.mail.MessagingException e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Error while Sending Mail \n" + e.getMessage()));
            return future;
        }
    }

    /**
     * Gets the success / error message as a string from the future object
     *
     * @param futureResult future string from sendmail method
     * @return String from the future object
     */
    public String getResultFromFuture(Future<String> futureResult) {
        try {
            return futureResult.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: The email sending operation was interrupted: " + e.getMessage();
        } catch (CancellationException e) {
            return "Error: The email sending operation was cancelled: " + e.getMessage();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                return "Error: " + cause.getMessage();
            } else {
                return "Error: An unknown error occurred while sending the email: " + e.getMessage();
            }
        }
    }
}
