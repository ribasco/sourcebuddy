package com.ibasco.sourcebuddy.components.rcon.parsers.metamod;

import com.ibasco.sourcebuddy.components.rcon.MetamodPlugin;
import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MetamodPluginListParser extends RconResultLineParser<List<MetamodPlugin>> {

    private static final Logger log = LoggerFactory.getLogger(MetamodPluginListParser.class);

    @Override
    protected List<MetamodPlugin> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void parseLine(ManagedServer server, List<MetamodPlugin> object, String line, int lineNum) throws ParseException {

    }

    public static void main(String[] args) {

    }
}
