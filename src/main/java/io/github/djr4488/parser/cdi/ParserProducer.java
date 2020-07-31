package io.github.djr4488.parser.cdi;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class ParserProducer {
    @Produces
    @FullUserAgentAnalyzer
    public UserAgentAnalyzer getFullUserAgentAnalyzer() {
        return UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withoutCache()
                .build();
    }

    @Produces
    @PartialUserAgentAnalyzer
    public UserAgentAnalyzer getPartialUserAgentAnalyzer(InjectionPoint injectionPoint) {
        PartialUserAgentAnalyzer annotation = injectionPoint.getAnnotated().getAnnotation(PartialUserAgentAnalyzer.class);
        String[] fields = annotation.value();
        UserAgentAnalyzer.UserAgentAnalyzerBuilder uaab =
                UserAgentAnalyzer.newBuilder()
                                 .hideMatcherLoadStats()
                                 .withCache(20000);
        for (String field : fields) {
            uaab.withField(field);
        }
        return uaab.build();
    }
}
