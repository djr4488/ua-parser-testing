package io.github.djr4488.parser.controller;

import io.github.djr4488.parser.cdi.FullUserAgentAnalyzer;
import io.github.djr4488.parser.cdi.PartialUserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.djr.cdi.logs.Slf4jLogger;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ParserController {
    @Inject
    @Slf4jLogger
    private Logger log;

    @Inject
    @FullUserAgentAnalyzer
    private UserAgentAnalyzer fullUserAgentAnalyzer;

    @Inject
    @PartialUserAgentAnalyzer({ "DeviceBrand" })
    private UserAgentAnalyzer partialUserAgentAnalyzer;

    public UserAgent parseFullUserAgent(String userAgent) {
        return fullUserAgentAnalyzer.parse(userAgent);
    }

    public UserAgent parsePartialUserAgent(String userAgent) {
        return partialUserAgentAnalyzer.parse(userAgent);
    }
}
