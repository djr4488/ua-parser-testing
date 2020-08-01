package io.github.djr4488.parser.controller;

import nl.basjes.parse.useragent.UserAgent;

import java.util.concurrent.Callable;

public class UaaRunnable implements Callable<UserAgent.ImmutableUserAgent> {
    private ParserController controller;
    private String userAgentStr;
    private boolean fullAnalysis = false;

    public UaaRunnable() {

    }

    public UaaRunnable(String userAgentStr, ParserController controller, boolean fullAnalysis) {
        this.controller = controller;
        this.userAgentStr = userAgentStr;
        this.fullAnalysis = fullAnalysis;
    }

    @Override
    public UserAgent.ImmutableUserAgent call() throws Exception {
        UserAgent.ImmutableUserAgent ua;
         if (fullAnalysis) {
             ua = (UserAgent.ImmutableUserAgent)controller.parseFullUserAgent(userAgentStr);
         } else {
             ua = (UserAgent.ImmutableUserAgent)controller.parsePartialUserAgent(userAgentStr);
         }
         return ua;
    }
}
