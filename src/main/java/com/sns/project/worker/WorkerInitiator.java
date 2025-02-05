package com.sns.project.worker;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkerInitiator {

    private final MailSenderWorker mailSenderWorker;
    private final int MAIL_SENDER_WORKER_THREAD_NUM = 2;

    @EventListener(ApplicationReadyEvent.class)
    public void runMailSenderWorker() {
        
        ExecutorService executorService =
                Executors.newFixedThreadPool(MAIL_SENDER_WORKER_THREAD_NUM);

        for (int i = 0; i < MAIL_SENDER_WORKER_THREAD_NUM; i++) {
            executorService.submit(mailSenderWorker::processTasks);
        }
    }

}
