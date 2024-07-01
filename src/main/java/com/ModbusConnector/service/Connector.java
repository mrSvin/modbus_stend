package com.ModbusConnector.service;

import com.ModbusConnector.devices.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Connector extends Thread {

    private Stend stend;
    private Epp epp;
    private Ar55 ar55;
    private LiteykaBig liteykaBig;
    private LiteykaMedium liteykaMedium;
    private PechNerg pechNerg;
    private Nk600 nk600;
    private Sclad sclad;
    private Kv12 kv12;
    private Liefeld110 liefeld110;
    private Liefeld135 liefeld135;
    private Progress progress;
    private Sk50 sk50;
    private Press press;
    private Centrator centrator;
    private Trulaser trulaser;
    private Stp4pl stp4pl;
    private Saw saw;
    private Klon klon;

    public Connector(Stend stend, Epp epp, Ar55 ar55, LiteykaBig liteykaBig, LiteykaMedium liteykaMedium, PechNerg pechNerg, Nk600 nk600,
                     Sclad sclad, Kv12 kv12, Liefeld110 liefeld110, Liefeld135 liefeld135, Progress progress, Sk50 sk50, Press press, Centrator centrator, Trulaser trulaser, Stp4pl stp4pl, Saw saw, Klon klon) {
        this.stend = stend;
        this.epp = epp;
        this.ar55 = ar55;
        this.liteykaBig = liteykaBig;
        this.liteykaMedium = liteykaMedium;
        this.pechNerg = pechNerg;
        this.nk600 = nk600;
        this.sclad = sclad;
        this.kv12 = kv12;
        this.liefeld110 = liefeld110;
        this.liefeld135 = liefeld135;
        this.progress = progress;
        this.sk50 = sk50;
        this.press = press;
        this.centrator = centrator;
        this.trulaser = trulaser;
        this.stp4pl = stp4pl;
        this.saw = saw;
        this.klon = klon;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Scheduled(fixedRate = 5000)
    public void connector() {
        System.out.println("активные потоки: " + Thread.activeCount());

        // Создаем CountDownLatch с количеством задач
        CountDownLatch latch = new CountDownLatch(19);

        submitTask(latch, () -> stend.data());
        submitTask(latch, () -> epp.data());
        submitTask(latch, () -> ar55.data());
        submitTask(latch, () -> liteykaBig.data());
        submitTask(latch, () -> liteykaMedium.data());
        submitTask(latch, () -> nk600.data());
        submitTask(latch, () -> sclad.data());
        submitTask(latch, () -> pechNerg.data());
        submitTask(latch, () -> liefeld110.data());
        submitTask(latch, () -> liefeld135.data());
        submitTask(latch, () -> kv12.data());
        submitTask(latch, () -> sk50.data());
        submitTask(latch, () -> press.data());
        submitTask(latch, () -> progress.data());
        submitTask(latch, () -> centrator.data());
        submitTask(latch, () -> trulaser.data());
        submitTask(latch, () -> stp4pl.data());
        submitTask(latch, () -> saw.data());
        submitTask(latch, () -> klon.data());

        try {
            // Ожидаем завершения всех задач
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Поток был прерван");
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
}
