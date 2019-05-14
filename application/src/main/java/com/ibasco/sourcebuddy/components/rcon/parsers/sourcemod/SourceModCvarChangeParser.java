package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModCvarChangeResult;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceModCvarChangeParser extends RconResultLineParser<SourceModCvarChangeResult> {

    private static final Logger log = LoggerFactory.getLogger(SourceModCvarChangeParser.class);

    private static final Pattern CVAR_CHANGE = Pattern.compile("\\A\\[SM\\]\\sChanged\\scvar\\s\\\"(?<cvarName>.+)\\\"\\sto\\s\\\"(?<cvarValue>.+)\\\".?$");

    @Override
    protected SourceModCvarChangeResult createResultObject(ManagedServer server) {
        return new SourceModCvarChangeResult();
    }

    @Override
    protected void parseLine(ManagedServer server, SourceModCvarChangeResult object, String line, int lineNum) throws ParseException {
        Matcher matcher = CVAR_CHANGE.matcher(line);
        if (matcher.matches()) {
            String cvarName = matcher.group("cvarName");
            String cvarValue = matcher.group("cvarValue");
            object.setName(cvarName);
            object.setValue(cvarValue);
        }
    }

    public static void main(String[] args) {
        SourceModCvarChangeParser parser = new SourceModCvarChangeParser();
        String test = "[SM] Changed cvar \"confogl_molotov_limit\" to \"10\".";
        SourceModCvarChangeResult res = parser.apply(new ManagedServer(), test);
        log.info("Result: {}", res);
    }
}
