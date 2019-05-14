package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModPlugin;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

public class SourceModPluginInfoParser extends RconResultLineParser<SourceModPlugin> {

    private static final Logger log = LoggerFactory.getLogger(SourceModPluginInfoParser.class);

    private SourceModPlugin existingPlugin;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).withResolverStyle(ResolverStyle.SMART);

    public SourceModPluginInfoParser() {
        this(null);
    }

    /**
     * Creates a new Parser.
     *
     * @param existingPlugin
     *         If an existing plugin instance is specified, it will be used instead and properties will be updated
     */
    public SourceModPluginInfoParser(SourceModPlugin existingPlugin) {
        this.existingPlugin = existingPlugin;
    }

    @Override
    protected SourceModPlugin createResultObject(ManagedServer server) {
        return existingPlugin != null ? existingPlugin : new SourceModPlugin();
    }

    @Override
    protected void parseLine(ManagedServer server, SourceModPlugin result, String line, int lineNum) throws ParseException {
        if (StringUtils.isBlank(line))
            return;

        String[] keyValue = StringUtils.splitPreserveAllTokens(line, ":", 2);
        if (keyValue != null) {
            String key = StringUtils.defaultIfBlank(keyValue[0], "").trim().toLowerCase();
            String value = StringUtils.defaultIfBlank(keyValue[1], "").trim();

            switch (key.toLowerCase()) {
                case "filename": {
                    if (StringUtils.isBlank(result.getFilename())) {
                        result.setFilename(value);
                    }
                    break;
                }
                case "title":
                    if (StringUtils.isBlank(result.getFullName())) {
                        result.setFullName(value);
                    }
                    break;
                case "author": {
                    if (StringUtils.isBlank(result.getAuthor())) {
                        result.setAuthor(value);
                    }
                    break;
                }
                case "version": {
                    if (StringUtils.isBlank(result.getVersion())) {
                        result.setVersion(value);
                    }
                    break;
                }
                case "url": {
                    if (result.getUrl() == null) {
                        result.setUrl(value);
                    }
                    break;
                }
                case "status": {
                    if (StringUtils.isBlank(result.getStatus())) {
                        result.setStatus(value);
                    }
                    break;
                }
                case "timestamp": {
                    if (result.getTimestamp() == null && StringUtils.isNotBlank(value)) {
                        try {
                            LocalDateTime timestamp = LocalDateTime.parse(value.trim(), dateTimeFormatter);
                            result.setTimestamp(timestamp);
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                            throw new ParseException("Could not parse timestamp: " + value, lineNum);
                        }
                    }
                    break;
                }
                case "hash": {
                    if (StringUtils.isBlank(result.getHash())) {
                        result.setHash(value);
                    }
                    break;
                }
                default:
                    throw new ParseException("Unrecognized key: " + key, lineNum);
            }
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

        SourceModPluginListParser smListParser = new SourceModPluginListParser();
        List<SourceModPlugin> pluginList = smListParser.apply(new ManagedServer(), pluginListStr);

        SourceModPlugin existing = pluginList.stream().filter(p -> p.getIndex() == 17).findFirst().orElse(null);

        String test = "  Filename: confoglcompmod.smx\n" +
                "  Title: Confogl's Competitive Mod (A competitive mod for L4D2)\n" +
                "  Author: Confogl Team\n" +
                "  Version: 2.2.3\n" +
                "  URL: http://confogl.googlecode.com/\n" +
                "  Status: running\n" +
                "  Timestamp: 03/13/2016 11:09:09\n" +
                "  Hash: c06ca086b7ddbeae5373953c5d00495c\n";

        SourceModPluginInfoParser parser = new SourceModPluginInfoParser(existing);
        SourceModPlugin plugin = parser.apply(new ManagedServer(), test);

        log.info("Plugin: {}", plugin);

    }
}
