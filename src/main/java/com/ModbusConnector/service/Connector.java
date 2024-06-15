package com.ModbusConnector.service;

import com.ModbusConnector.devices.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Scheduled(fixedRate = 5000)
    public void connector() {

        System.out.println("активные потоки: " + Thread.activeCount());
        stend.data();
        epp.data();
        ar55.data();
        liteykaBig.data();
        liteykaMedium.data();
        nk600.data();
        sclad.data();
        pechNerg.data();
        liefeld110.data();
        liefeld135.data();
        kv12.data();
        sk50.data();
        press.data();
        progress.data();
        centrator.data();
        trulaser.data();
        stp4pl.data();
        saw.data();
        klon.data();

    }


}
