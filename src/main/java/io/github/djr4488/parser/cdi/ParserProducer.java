package io.github.djr4488.parser.cdi;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class ParserProducer {
    private UserAgentAnalyzer fullUserAgentAnalyzer;
    private UserAgentAnalyzer partialUserAgentAnalyzer;
    private static final Object lock = new Object();

    @Produces
    @FullUserAgentAnalyzer
    public UserAgentAnalyzer getFullUserAgentAnalyzer() {
        if (fullUserAgentAnalyzer == null) {
            synchronized (lock) {
                if (fullUserAgentAnalyzer == null) {
                    fullUserAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                            .hideMatcherLoadStats()
                            .withoutCache()
                            .build();
                }
            }
        }
        return fullUserAgentAnalyzer;
    }

    @Produces
    @PartialUserAgentAnalyzer
    public UserAgentAnalyzer getPartialUserAgentAnalyzer(InjectionPoint injectionPoint) {
        if (partialUserAgentAnalyzer == null) {
            synchronized (lock) {
                if (partialUserAgentAnalyzer == null) {
                    PartialUserAgentAnalyzer annotation = injectionPoint.getAnnotated().getAnnotation(PartialUserAgentAnalyzer.class);
                    String[] fields = annotation.value();
                    UserAgentAnalyzer.UserAgentAnalyzerBuilder uaab =
                            UserAgentAnalyzer.newBuilder()
                                             .hideMatcherLoadStats()
                                             .withoutCache();
                    for (String field : fields) {
                        uaab.withField(field);
                    }
                    partialUserAgentAnalyzer = uaab.build();
                }
            }
        }
        return partialUserAgentAnalyzer;
    }
}
