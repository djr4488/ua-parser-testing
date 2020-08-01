package io.github.djr4488.parser.controller;

import io.github.djr4488.parser.cdi.ParserProducer;
import nl.basjes.parse.useragent.UserAgent;
import org.djr.cdi.logs.LoggerProducer;
import org.djr.cdi.logs.Slf4jLogger;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

@EnableAutoWeld
@AddBeanClasses({ LoggerProducer.class, ParserProducer.class, ParserController.class })
public class ParserControllerTest {
    @Inject
    @Slf4jLogger
    private Logger log;

    @Inject
    private ParserController parserController;

    public static final String userAgentTest = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";
    public static final String mobileSafariApple = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/603.1.23 (KHTML, like Gecko) Version/10.0 Mobile/14E5239e Safari/602.1";
    public static final String chromeApple = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1";
    public static final String chromeAndroid = "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>";
    public static final String facebookIos = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 [FBAN/FBIOS;FBDV/iPhone11,8;FBMD/iPhone;FBSN/iOS;FBSV/13.3.1;FBSS/2;FBID/phone;FBLC/en_US;FBOP/5;FBCR/]";
    public static final String ieWindows = "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko";
    public static final String edgeWindows = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18362";

    @Test
    public void testUserAgentParsingFull() {
        UserAgent ua =
                parserController.parseFullUserAgent(userAgentTest);
        assertEquals("Phone", ua.getValue("DeviceClass"));
    }

    @Test
    public void testUserAgentParsingPartial() {
        UserAgent ua = parserController.parsePartialUserAgent(userAgentTest);
        assertEquals("Phone", ua.getValue("DeviceClass"));
        assertEquals("Android 7.0", ua.getValue("OperatingSystemNameVersion"));
    }

    @Test
    public void testMultiThreadedFullAnalyzer() throws Exception {
        List<Future<UserAgent.ImmutableUserAgent>> futures = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) {
            futures.add(es.submit(new UaaRunnable(userAgentTest, parserController, true)));
            futures.add(es.submit(new UaaRunnable(mobileSafariApple, parserController, true)));
            futures.add(es.submit(new UaaRunnable(chromeApple, parserController, true)));
            futures.add(es.submit(new UaaRunnable(chromeAndroid, parserController, true)));
            futures.add(es.submit(new UaaRunnable(facebookIos, parserController, true)));
            futures.add(es.submit(new UaaRunnable(ieWindows, parserController, true)));
            futures.add(es.submit(new UaaRunnable(edgeWindows, parserController, true)));
            futures.add(es.submit(new UaaRunnable(mobileSafariApple, parserController, true)));
            futures.add(es.submit(new UaaRunnable(chromeAndroid, parserController, true)));
            futures.add(es.submit(new UaaRunnable(facebookIos, parserController, true)));
        }
        for (Future<UserAgent.ImmutableUserAgent> futureResult : futures) {
            try {
                while (!futureResult.isDone());
                UserAgent.ImmutableUserAgent ua = futureResult.get();
                log.info("testMultiThreaded() userAgent:{}", ua);
                switch (ua.getUserAgentString()) {
                    case userAgentTest:
                        assertEquals("Phone", ua.get("DeviceClass").getValue());
                        assertEquals("Google", ua.get("DeviceBrand").getValue());
                        assertEquals("Android 7.0", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case mobileSafariApple:
                        assertEquals("Phone", ua.get("DeviceClass").getValue());
                        assertEquals("Apple", ua.get("DeviceBrand").getValue());
                        assertEquals("iOS 10.3", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case chromeApple:
                        assertEquals("Phone", ua.get("DeviceClass").getValue());
                        assertEquals("Apple", ua.get("DeviceBrand").getValue());
                        assertEquals("iOS 10.3", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case chromeAndroid:
                        assertEquals("Desktop", ua.get("DeviceClass").getValue());
                        assertEquals("Unknown", ua.get("DeviceBrand").getValue());
                        assertEquals("Linux ??", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case facebookIos:
                        assertEquals("Phone", ua.get("DeviceClass").getValue());
                        assertEquals("Apple", ua.get("DeviceBrand").getValue());
                        assertEquals("iOS 13.3.1", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case ieWindows:
                        assertEquals("Desktop", ua.get("DeviceClass").getValue());
                        assertEquals("Unknown", ua.get("DeviceBrand").getValue());
                        assertEquals("Windows 7", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case edgeWindows:
                        assertEquals("Desktop", ua.get("DeviceClass").getValue());
                        assertEquals("Unknown", ua.get("DeviceBrand").getValue());
                        assertEquals("Windows 10.0", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                }
            } catch (Exception ex) {
                log.error("testMultiThreaded() exception occurred:", ex);
                fail("With exception");
            }
        }
    }

    @Test
    public void testMultiThreadedPartialAnalyzer() throws Exception {
        List<Future<UserAgent.ImmutableUserAgent>> futures = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) {
            futures.add(es.submit(new UaaRunnable(userAgentTest, parserController, false)));
            futures.add(es.submit(new UaaRunnable(mobileSafariApple, parserController, false)));
            futures.add(es.submit(new UaaRunnable(chromeApple, parserController, false)));
            futures.add(es.submit(new UaaRunnable(chromeAndroid, parserController, false)));
            futures.add(es.submit(new UaaRunnable(facebookIos, parserController, false)));
            futures.add(es.submit(new UaaRunnable(ieWindows, parserController, false)));
            futures.add(es.submit(new UaaRunnable(edgeWindows, parserController, false)));
            futures.add(es.submit(new UaaRunnable(mobileSafariApple, parserController, false)));
            futures.add(es.submit(new UaaRunnable(chromeAndroid, parserController, false)));
            futures.add(es.submit(new UaaRunnable(facebookIos, parserController, false)));
        }
        for (Future<UserAgent.ImmutableUserAgent> futureResult : futures) {
            try {
                while (!futureResult.isDone());
                UserAgent.ImmutableUserAgent ua = futureResult.get();
                log.info("testMultiThreaded() userAgent:{}", ua);
                assertEquals("Unknown", ua.get("DeviceBrand").getValue());
                switch (ua.getUserAgentString()) {
                    case userAgentTest:
                        assertEquals("Android 7.0", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case mobileSafariApple:
                        assertEquals("iOS 10.3", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case chromeApple:
                        assertEquals("iOS 10.3", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case chromeAndroid:
                        assertEquals("Linux ??", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case facebookIos:
                        assertEquals("iOS 13.3.1", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case ieWindows:
                        assertEquals("Windows 7", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                    case edgeWindows:
                        assertEquals("Windows 10.0", ua.get("OperatingSystemNameVersion").getValue());
                        break;
                }
            } catch (Exception ex) {
                log.error("testMultiThreaded() exception occurred:", ex);
                fail("With exception");
            }
        }
    }
}
