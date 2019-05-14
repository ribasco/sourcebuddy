package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModExtension;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;

public class SourceModExtInfoParser extends RconResultLineParser<SourceModExtension> {

    private static final Logger log = LoggerFactory.getLogger(SourceModExtInfoParser.class);

    private SourceModExtension extension;

    public SourceModExtInfoParser() {
        this(null);
    }

    public SourceModExtInfoParser(SourceModExtension extension) {
        this.extension = extension;
    }

    @Override
    protected SourceModExtension createResultObject(ManagedServer server) {
        return extension != null ? extension : new SourceModExtension();
    }

    @Override
    protected void parseLine(ManagedServer server, SourceModExtension result, String line, int lineNum) throws ParseException {
        if (StringUtils.isBlank(line))
            return;
        String[] keyValue = StringUtils.splitPreserveAllTokens(line, ":", 2);
        if (keyValue != null) {
            String key = StringUtils.defaultIfBlank(keyValue[0], "").trim().toLowerCase();
            String value = StringUtils.defaultIfBlank(keyValue[1], "").trim();
            switch (key) {
                case "file": {
                    if (StringUtils.isBlank(result.getFilename())) {
                        result.setFilename(value);
                    }
                    break;
                }
                case "loaded": {
                    if (result.isLoaded() == null && StringUtils.isNotBlank(value)) {
                        result.setLoaded(value.toLowerCase().contains("yes"));
                    }
                    break;
                }
                case "name": {
                    if (StringUtils.isBlank(result.getFullName())) {
                        result.setFullName(value);
                    }
                    break;
                }
                case "author": {
                    if (StringUtils.isBlank(result.getAuthor())) {
                        result.setAuthor(value);
                    }
                    break;
                }
                case "binary info": {
                    if (StringUtils.isBlank(result.getBinaryInfo())) {
                        result.setBinaryInfo(value);
                    }
                    break;
                }
                case "method": {
                    if (StringUtils.isBlank(result.getMethod())) {
                        result.setMethod(value);
                    }
                    break;
                }
                default:
                    throw new ParseException("Unrecognized key: " + key, lineNum);
            }
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

        SourceModExtListParser extListParser = new SourceModExtListParser();
        List<SourceModExtension> res = extListParser.apply(new ManagedServer(), testStr);

        SourceModExtension extension = res.stream().filter(p -> p.getIndex() == 6).findFirst().orElse(null);

        String test = " File: l4d2_meleespawncontrol.ext.so\n" +
                " Loaded: Yes (version 1.0.0.4)\n" +
                " Name: [L4D2] Melee Spawn Control (Give cvar sm_melee_weapon_list for set list of spawned melee weapons)\n" +
                " Author: V10 (http://sourcemod.v10.name/)\n" +
                " Binary info: API version 5 (compiled Aug  4 2013)\n" +
                " Method: Loaded by SourceMod, attached to Metamod:Source\n";

        SourceModExtInfoParser infoParser = new SourceModExtInfoParser(extension);
        SourceModExtension ext = infoParser.apply(new ManagedServer(), test);

        log.info("Extension: {}", ext);
    }
}
