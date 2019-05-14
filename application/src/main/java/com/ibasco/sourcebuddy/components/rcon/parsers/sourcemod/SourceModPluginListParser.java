package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModPlugin;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.util.Check;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceModPluginListParser extends RconResultLineParser<List<SourceModPlugin>> {

    private static final Logger log = LoggerFactory.getLogger(SourceModPluginListParser.class);

    private static final Pattern SM_PLUGIN_INFO = Pattern.compile("\\A[\\t\\s]+(?<pluginIndex>\\d+)\\s+(?<pluginDisabled>Disabled)?:?\\s?\\\"(?<pluginName>.+)\\\"\\s\\((?<pluginVersion>.+)\\)\\sby\\s(?<pluginAuthor>.+)$");

    @Override
    protected List<SourceModPlugin> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void beforeParse(ManagedServer server, List<SourceModPlugin> result) {

    }

    @Override
    protected void afterParse(ManagedServer server, List<SourceModPlugin> result) {

    }

    @Override
    protected void parseLine(ManagedServer server, List<SourceModPlugin> result, String line, int lineNum) throws ParseException {
        if (StringUtils.isBlank(line))
            return;
        Matcher matcher = SM_PLUGIN_INFO.matcher(line);
        if (matcher.matches()) {
            String index = Check.requireNumericString(matcher.group("pluginIndex"), "Index is not numeric: " + line);
            String disabled = matcher.group("pluginDisabled");
            String name = matcher.group("pluginName");
            String version = matcher.group("pluginVersion");
            String author = matcher.group("pluginAuthor");

            SourceModPlugin plugin = new SourceModPlugin();
            plugin.setIndex(Integer.valueOf(index));
            if (StringUtils.isNotBlank(disabled))
                plugin.setDisabled("disabled".equalsIgnoreCase(disabled));
            if (StringUtils.isNotBlank(name))
                plugin.setName(name.trim());
            if (StringUtils.isNotBlank(version))
                plugin.setVersion(version.trim());
            if (StringUtils.isNotBlank(author))
                plugin.setAuthor(author);
            plugin.setServer(server);
            result.add(plugin);
        }
    }

    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        String pluginListStr = "[SM] Listing 19 plugins:\n" +
                "  01 \"Client Preferences\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  02 \"Admin File Reader\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  03 \"Fun Votes\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  04 Disabled: \"Nextmap\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  05 \"Reserved Slots\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  06 \"Basic Info Triggers\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  07 \"Basic Commands\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  08 \"Basic Comm Control\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  09 \"Player Commands\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  10 \"Basic Chat\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  11 \"Match Vote\" (1.1.3) by vintik\n" +
                "  12 \"Admin Menu\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  13 \"Basic Ban Commands\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  14 \"Anti-Flood\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  15 \"Admin Help\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  16 \"Sound Commands\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  17 \"Confogl's Competitive Mod\" (2.2.3) by Confogl Team\n" +
                "  18 \"Fun Commands\" (1.9.0.6275) by AlliedModders LLC\n" +
                "  19 \"Basic Votes\" (1.9.0.6275) by AlliedModders LLC\n";

        SourceModPluginListParser parser = new SourceModPluginListParser();
        List<SourceModPlugin> pluginList = parser.apply(new ManagedServer(), pluginListStr);

        for (SourceModPlugin plugin : pluginList) {
            log.info("Plugin: {}", plugin);
        }
    }
}
