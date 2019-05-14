package com.ibasco.sourcebuddy.components.rcon;

import com.ibasco.sourcebuddy.components.rcon.parsers.RconResultParser;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

abstract public class RconResultLineParser<T> implements RconResultParser<T> {

    private static final Logger log = LoggerFactory.getLogger(RconResultLineParser.class);

    private static final String SPLIT_BY_SPACE_REGEX = "\\s(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    private static final String SPLIT_BY_COMMA_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    private static final Pattern VALID_IP_REGEX = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\b");

    @Override
    public T apply(ManagedServer server, String rawRconData) {
        if (StringUtils.isBlank(rawRconData))
            return null;
        T result = createResultObject(server);
        beforeParse(server, result);
        try (Scanner scanner = new Scanner(new StringReader(rawRconData))) {
            int lineNum = 0;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (StringUtils.isBlank(line))
                    continue;
                try {
                    parseLine(server, result, line, ++lineNum);
                } catch (ParseException e) {
                    //log.debug("Parse Error on line: {}", lineNum);
                    //log.debug("Parse Error on line {} = '{}' (Reason: {})", lineNum, line, e.getMessage());
                }
            }
        } finally {
            afterParse(server, result);
        }
        return result;
    }

    abstract protected T createResultObject(ManagedServer server);

    protected void beforeParse(ManagedServer server, T result) {
        //no-op
    }

    protected void afterParse(ManagedServer server, T result) {
        //no-op
    }

    abstract protected void parseLine(ManagedServer server, T object, String line, int lineNum) throws ParseException;

    protected final boolean isValidIp(String ip) {
        return VALID_IP_REGEX.matcher(ip).matches();
    }

    protected String[] splitBySpace(String data) {
        if (StringUtils.isBlank(data))
            return null;
        return data.split(SPLIT_BY_SPACE_REGEX);
    }

    protected String[] splitByComma(String data) {
        if (StringUtils.isBlank(data))
            return null;
        return data.split(SPLIT_BY_COMMA_REGEX);
    }
}
