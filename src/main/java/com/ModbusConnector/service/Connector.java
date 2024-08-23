package com.ModbusConnector.service;

import com.ModbusConnector.devices.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class Connector extends Thread {

    public Connector(Stend stend, Epp epp, Ar55 ar55, LiteykaBig liteykaBig, LiteykaMedium liteykaMedium, PechNerg pechNerg, Nk600 nk600,
                     Sclad sclad, Kv12 kv12, Liefeld110 liefeld110, Liefeld135 liefeld135, Progress progress, Sk50 sk50, Press press, Centrator centrator,
                     Trulaser trulaser, Stp4pl stp4pl, Saw saw, Klon klon, RtkLaser rtkLaser) {
        this.tasks = Arrays.asList(
                stend::data,
                epp::data,
                ar55::data,
                liteykaBig::data,
                liteykaMedium::data,
                nk600::data,
                sclad::data,
                pechNerg::data,
                liefeld110::data,
                liefeld135::data,
                kv12::data,
                sk50::data,
                press::data,
                progress::data,
                centrator::data,
                trulaser::data,
                stp4pl::data,
                saw::data,
                klon::data,
                rtkLaser::data
        );
    }

    private static final Logger logger = Logger.getLogger(Connector.class.getName());
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final List<Runnable> tasks;

    @Scheduled(fixedRate = 5000)
    public void connector() {
        logger.info("активные потоки: " + Thread.activeCount());

        // Создаем CountDownLatch с количеством задач
        CountDownLatch latch = new CountDownLatch(tasks.size());

        for (Runnable task : tasks) {
            submitTask(latch, task);
        }

        try {
            // Ожидаем завершения всех задач
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Поток был прерван");
        }
    }

    private void submitTask(CountDownLatch latch, Runnable task) {
        executorService.submit(() -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        });
    }

    @Override
    public void run() {
        try {
            connector();
        } finally {
            executorService.shutdown();
        }
    }
}
