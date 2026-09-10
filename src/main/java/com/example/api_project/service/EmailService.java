package com.example.api_project.service;

import com.example.api_project.entity.SalesOrder;
import com.example.api_project.entity.SalesOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;




@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-name}")
    private String fromName;

    public void sendReceiptEmail(SalesOrder order) {

        if (order.getCustomer() == null) {
            log.warn("Receipt email skipped: Order #{} has no customer", order.getId());
            return;
        }

        if (order.getCustomer().getEmail() == null
                || order.getCustomer().getEmail().isBlank()) {
            log.warn("Receipt email skipped: Order #{} customer has no email", order.getId());
            return;
        }

        String email = order.getCustomer().getEmail();

        try {
            log.info("Preparing receipt email for order #{} to {}",
                    order.getId(), email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(fromName + " — Receipt for Order #" + order.getId());
            message.setText(buildReceiptBody(order));

            log.info("About to send email for order #{}", order.getId());
            mailSender.send(message);
            log.info("Email successfully sent for order #{} to {}",
                    order.getId(), email);

        } catch (Exception e) {
            log.error("Could not send receipt email for order #{}",
                    order.getId(), e);
        }
    }



    private String buildReceiptBody(SalesOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(order.getCustomer().getName()).append(",\n\n");
        sb.append("Thank you for shopping with us! Here's your receipt:\n\n");
        sb.append("Order #").append(order.getId()).append("\n");
        sb.append("Branch: ").append(order.getBranch().getName()).append("\n");
        sb.append("-----------------------------------------\n");

        for (SalesOrderItem item : order.getItems()) {
            sb.append(String.format("%-20s x%-3d  Rs %.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getSubtotal()));
        }


        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL: Rs %.2f%n%n", order.getTotalAmount()));
        sb.append("Thank you for shopping with us!\n");
        sb.append("— Greenline Grocery");
        return sb.toString();
    }


}











