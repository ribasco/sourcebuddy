package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModVersion;
import com.ibasco.sourcebuddy.domain.ManagedServer;

import java.text.ParseException;

public class SourceModVersionParser extends RconResultLineParser<SourceModVersion> {

    @Override
    protected SourceModVersion createResultObject(ManagedServer server) {
        return new SourceModVersion();
    }

    @Override
    protected void parseLine(ManagedServer server, SourceModVersion result, String line, int lineNum) throws ParseException {

    }
}
