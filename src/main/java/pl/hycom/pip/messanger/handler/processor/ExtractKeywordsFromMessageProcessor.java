package pl.hycom.pip.messanger.handler.processor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import pl.hycom.pip.messanger.handler.PipelineMessageHandler;
import pl.hycom.pip.messanger.pipeline.PipelineContext;
import pl.hycom.pip.messanger.pipeline.PipelineException;
import pl.hycom.pip.messanger.pipeline.PipelineProcessor;

/**
 * Created by szale_000 on 2017-04-06.
 */
@Component
@Log4j2
public class ExtractKeywordsFromMessageProcessor implements PipelineProcessor {

    public static final String KEYWORDS = "keywords";
    private static final String CHARS_TO_REMOVE_REGEX = "[{}\\[\\]()!@#$%^&*~'?\".,/+]";

    @Override
    public int runProcess(PipelineContext ctx) throws PipelineException {
        log.info("Started keyword generating");

        String message = ctx.get(PipelineMessageHandler.MESSAGE, String.class);
        Set<String> keywords = extractKeywords(message);
        log.info("Keywords extracted from message [{}]: {}", message, keywords);

        ctx.put(KEYWORDS, keywords);
        return 1;
    }

    Set<String> extractKeywords(String message) {
        return processKeywords(StringUtils.split(processMessage(message), " "));
    }

    protected String processMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return StringUtils.EMPTY;
        }

        message = StringUtils.replaceAll(message, CHARS_TO_REMOVE_REGEX, "");
        message = StringUtils.lowerCase(message);

        return message;
    }

    protected Set<String> processKeywords(@NonNull String[] keywords) {
        return Arrays.stream(keywords)
                .filter(s -> StringUtils.length(s) > 2)
                .collect(Collectors.toSet());
    }

}