package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModExtension;
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

public class SourceModExtListParser extends RconResultLineParser<List<SourceModExtension>> {

    private static final Logger log = LoggerFactory.getLogger(SourceModExtListParser.class);

    private static final Pattern SM_EXT_INFO = Pattern.compile("\\A\\[(?<extIndex>\\d+)\\]\\s(?<extName>.+)\\s\\((?<extVersion>.+)\\)\\:\\s(?<extDesc>.+)$");

    @Override
    protected List<SourceModExtension> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void parseLine(ManagedServer server, List<SourceModExtension> result, String line, int lineNum) throws ParseException {
        if (StringUtils.isBlank(line))
            return;
        try {
            Matcher matcher = SM_EXT_INFO.matcher(line);
            if (matcher.matches()) {
                String index = Check.requireNumericString(matcher.group("extIndex"), "Extension index is not numeric: " + line);
                String name = matcher.group("extName");
                String version = matcher.group("extVersion");
                String desc = matcher.group("extDesc");

                SourceModExtension extension = new SourceModExtension();
                extension.setIndex(Integer.valueOf(index));
                extension.setName(name.trim());
                extension.setVersion(version.trim());
                extension.setDescription(desc.trim());

                result.add(extension);
            }
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), lineNum);
        }
    }

    public static void main(String[] args) {
        String testStr = "[SM] Displaying 13 extensions:\n" +
                "[01] Automatic Updater (1.9.0.6275): Updates SourceMod gamedata files\n" +
                "[02] Webternet (1.9.0.6275): Extension for interacting with URLs\n" +
                "[03] EQ Ladder Rambos (0.1.1): Enables guns on ladders\n" +
                "[04] Left 4 Downtown 2 (0.6.1): Competitive framework support extension for L4D2\n" +
                "[05] BinTools (1.9.0.6275): Low-level C/C++ Calling API\n" +
                "[06] [L4D2] Melee Spawn Control (1.0.0.4): Give cvar sm_melee_weapon_list for set list of spawned melee weapons\n" +
                "[07] Client Preferences (1.9.0.6275): Saves client preference settings\n" +
                "[08] SQLite (1.9.0.6275): SQLite Driver\n" +
                "[09] SDK Tools (1.9.0.6275): Source SDK Tools\n" +
                "[10] Top Menus (1.9.0.6275): Creates sorted nested menus\n" +
                "[11] Builtin Votes (0.5.8): API to do votes using the L4D, L4D2, and Orange Box Valve game in-game vote interface\n" +
                "[12] SDK Hooks (1.9.0.6275): Source SDK Hooks\n" +
                "[13] Socket (3.0.1alpha): Socket extension for SourceMod\n" +
                "[13] Test (3.0.1alpha): sdfsf sfsfsfsdfsd sfsdf sffd";

        SourceModExtListParser parser = new SourceModExtListParser();
        List<SourceModExtension> res = parser.apply(new ManagedServer(), testStr);

        for (SourceModExtension ext : res) {
            log.info("Extension: {}", ext);
        }
    }
}
