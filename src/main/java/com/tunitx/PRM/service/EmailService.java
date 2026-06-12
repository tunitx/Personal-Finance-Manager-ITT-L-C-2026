package com.tunitx.PRM.service;

import com.tunitx.PRM.model.Project;
import com.tunitx.PRM.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AiService aiService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── Notification 1a — Timesheet Reminder ──────────────────────────────

    public void sendTimesheetReminder(User employee, LocalDate weekStart,
                                      int reminderNumber) {
        String subject = reminderNumber == 1
                ? "[PRM] Reminder: Timesheet pending for week of "
                + weekStart.format(FMT)
                : "[PRM] Final Reminder: Timesheet still pending — action required";

        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #E65100;">⏰ Timesheet %s</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>Your timesheet for the week of <strong>%s</strong>
                has not been submitted yet.</p>
                %s
                <p>Please log in to the PRM system and submit your timesheet
                as soon as possible.</p>
                <br/>
                <p style="color: #999; font-size: 12px;">
                This is an automated message from the PRM System.</p>
                </body></html>
                """.formatted(
                reminderNumber == 1 ? "Reminder" : "— Final Warning",
                employee.getFullName() != null
                        ? employee.getFullName() : employee.getUsername(),
                weekStart.format(FMT),
                reminderNumber == 2
                        ? "<p style=\"color: red;\"><strong>⚠ Warning:</strong> "
                        + "If not submitted today, your timesheet access will "
                        + "be frozen and your manager will be notified.</p>"
                        : ""
        );

        sendHtmlEmail(employee.getEmail(), subject, body);
        log.info("Sent timesheet reminder {} to {}", reminderNumber,
                employee.getUsername());
    }

    // ── Notification 1b — Timesheet Freeze Notice ─────────────────────────

    public void sendTimesheetFreezeNotice(User employee, User manager,
                                          LocalDate weekStart) {

        // Email to employee
        String empSubject = "[PRM] Your timesheet access has been frozen";
        String empBody = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #B71C1C;">🔒 Timesheet Access Frozen</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>Your timesheet submission access has been <strong>frozen</strong>
                because the timesheet for the week of <strong>%s</strong>
                was not submitted after two reminders.</p>
                <p>You can still log in and view your timesheets, but you
                <strong>cannot create or submit</strong> new entries until
                your manager restores your access.</p>
                <p>Please contact your manager: <strong>%s</strong>.</p>
                <br/>
                <p style="color: #999; font-size: 12px;">
                This is an automated message from the PRM System.</p>
                </body></html>
                """.formatted(
                employee.getFullName() != null
                        ? employee.getFullName() : employee.getUsername(),
                weekStart.format(FMT),
                manager != null && manager.getFullName() != null
                        ? manager.getFullName() : "your manager"
        );
        sendHtmlEmail(employee.getEmail(), empSubject, empBody);

        // Email to manager
        if (manager != null && manager.getEmail() != null) {
            String mgrSubject = "[PRM] Employee timesheet frozen — "
                    + (employee.getFullName() != null
                    ? employee.getFullName() : employee.getUsername());
            String mgrBody = """
                    <html><body style="font-family: Arial, sans-serif; color: #333;">
                    <h2 style="color: #B71C1C;">🔒 Employee Timesheet Access Frozen</h2>
                    <p>Hi <strong>%s</strong>,</p>
                    <p>The timesheet access for your team member
                    <strong>%s</strong> has been frozen because their
                    timesheet for week of <strong>%s</strong> was not
                    submitted after two reminders.</p>
                    <p>They can still log in and view timesheets but
                    cannot create or submit entries.</p>
                    <p>Once you have reviewed the situation, you can
                    restore their access from the Employee Management menu.</p>
                    <br/>
                    <p style="color: #999; font-size: 12px;">
                    This is an automated message from the PRM System.</p>
                    </body></html>
                    """.formatted(
                    manager.getFullName() != null
                            ? manager.getFullName() : manager.getUsername(),
                    employee.getFullName() != null
                            ? employee.getFullName() : employee.getUsername(),
                    weekStart.format(FMT)
            );
            sendHtmlEmail(manager.getEmail(), mgrSubject, mgrBody);
            log.info("Sent freeze notice to manager {} for employee {}",
                    manager.getUsername(), employee.getUsername());
        }

        log.info("Sent freeze notice to employee {}", employee.getUsername());
    }

    // ── Notification 2 — Project At-Risk Alert ────────────────────────────

    public void sendProjectAtRiskAlert(Project project, User manager,
                                       String riskSummary, String suggestedHelp) {

        if (manager == null || manager.getEmail() == null) {
            log.warn("Cannot send at-risk alert — manager email missing for project {}",
                    project.getName());
            return;
        }

        String subject = "[PRM] 🔴 Project At-Risk: " + project.getName();

        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #B71C1C;">🔴 Project Health Alert — AT RISK</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>The following project has been flagged as
                <strong style="color: red;">AT RISK</strong>
                by the automated health scheduler.</p>
                
                <table style="border-collapse: collapse; width: 100%%;
                              margin: 16px 0;">
                  <tr style="background: #f5f5f5;">
                    <td style="padding: 8px; border: 1px solid #ddd;
                               font-weight: bold;">Project</td>
                    <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 8px; border: 1px solid #ddd;
                               font-weight: bold;">Health Status</td>
                    <td style="padding: 8px; border: 1px solid #ddd;
                               color: red;">🔴 AT RISK</td>
                  </tr>
                  <tr style="background: #f5f5f5;">
                    <td style="padding: 8px; border: 1px solid #ddd;
                               font-weight: bold;">Timeline</td>
                    <td style="padding: 8px; border: 1px solid #ddd;">
                      %s to %s</td>
                  </tr>
                  <tr>
                    <td style="padding: 8px; border: 1px solid #ddd;
                               font-weight: bold;">Status</td>
                    <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                  </tr>
                </table>
                
                <h3 style="color: #E65100;">AI Risk Summary</h3>
                <p style="background: #fff3e0; padding: 12px;
                          border-left: 4px solid #E65100;">%s</p>
                
                <h3 style="color: #1565C0;">Suggested Help</h3>
                <p style="background: #e3f2fd; padding: 12px;
                          border-left: 4px solid #1565C0;">%s</p>
                
                <br/>
                <p style="color: #999; font-size: 12px;">
                This is an automated message from the PRM System.
                Log in to review and take action.</p>
                </body></html>
                """.formatted(
                manager.getFullName() != null
                        ? manager.getFullName() : manager.getUsername(),
                project.getName(),
                project.getStartDate().format(FMT),
                project.getEndDate().format(FMT),
                project.getStatus(),
                riskSummary != null ? riskSummary : "See project dashboard.",
                suggestedHelp != null ? suggestedHelp
                        : "Check bench resources for available employees."
        );

        sendHtmlEmail(manager.getEmail(), subject, body);
        log.info("Sent AT_RISK alert to manager {} for project {}",
                manager.getUsername(), project.getName());
    }

    // ── Internal helper ───────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("noreply@techserve.com", "PRM System");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Don't throw — email failure should never crash the scheduler
        }
    }
}
