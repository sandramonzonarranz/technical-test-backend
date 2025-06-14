package com.playtomic.tests.wallet.domain.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FakeEmailSenderService {

    public void send(String recipient, String subject, String body) {
        log.info("FAKE EMAIL SENDER:: Sending email to: {} | Subject: {} | Body: {}", recipient, subject, body);
    }

}
