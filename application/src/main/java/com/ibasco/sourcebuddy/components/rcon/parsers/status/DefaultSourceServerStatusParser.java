package com.ibasco.sourcebuddy.components.rcon.parsers.status;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourcePlayerStatus;
import com.ibasco.sourcebuddy.components.rcon.SourceServerStatus;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.util.Check;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultSourceServerStatusParser extends RconResultLineParser<SourceServerStatus> {

    private static final Logger log = LoggerFactory.getLogger(DefaultSourceServerStatusParser.class);

    private static final Pattern LOG_ECHO_REGEX = Pattern.compile("^L\\s[0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}\\s-\\s[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}:\\srcon\\ from\\ \".*\":\\scommand\\ \".*\"");

    private static final Pattern LOCAL_PUB_IP = Pattern.compile("((?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5})\\s*[\\[\\(]\\s?(?:(?:public\\sip\\:\\s)|(?:public\\s))(.*)\\s?[\\]\\)]");//Pattern.compile("((?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5})\\s*[\\[\\(]\\s?(?:(?:public\\sip\\:\\s)|(?:public\\s))((?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3})|(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}))\\s?[\\]\\)]");

    private static final Pattern PLAYER_INFO_FULL = Pattern.compile("\\A\\#\\s+(?<userid>[\\d\\s]*)\\s+\\\"(?<name>.+)\\\"\\s+(?<uniqueid>(?:STEAM_[0-5]:[01]:\\d+)|(?:\\[U.+\\]))\\s+(?<connected>[0-9]+:[0-9]+)\\s+(?<ping>[0-9]+)\\s+(?<loss>[0-9]+)\\s+(?<state>\\w+)(?:\\s+(?<rate>\\d+))\\s+(?<adr>(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:\\d{1,5})$");

    private static final Pattern PLAYER_INFO_FULL_NORATE = Pattern.compile("\\A\\#\\s+(?<userid>[\\d\\s]*)\\s+\\\"(?<name>.+)\\\"\\s+(?<uniqueid>(?:STEAM_[0-5]:[01]:\\d+)|(?:\\[U.+\\]))\\s+(?<connected>[0-9]+:[0-9]+)\\s+(?<ping>[0-9]+)\\s+(?<loss>[0-9]+)\\s+(?<state>\\w+)\\s+(?<adr>(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:\\d{1,5})$");

    private static final Pattern PLAYER_INFO_FULL_NORATEADDR = Pattern.compile("\\A\\#\\s+(?<userid>[\\d\\s]*)\\s+\\\"(?<name>.+)\\\"\\s+(?<uniqueid>(?:STEAM_[0-5]:[01]:\\d+)|(?:\\[U\\:\\d:\\d+\\]))\\s+(?<connected>[0-9]+:[0-9]+)\\s+(?<ping>[0-9]+)\\s+(?<loss>[0-9]+)\\s+(?<state>\\w+)$");

    private static final Pattern PLAYER_INFO_BOT = Pattern.compile("\\A\\#\\s*(?<userid>\\d+)\\s+\\\"(?<name>.+)\\\"\\s+(?<uniqueid>\\w+)\\s+(?<state>\\w+)\\s*(?<rate>\\d*)$");

    private static final Pattern SERVER_INFO_VERSION = Pattern.compile("\\A(?<version>.+)\\s+(?<type>\\w+)(?:\\s+[\\[\\(](?<meta>.+)[\\]\\)])*\\s*$");

    private static final Pattern SERVER_INFO_OS = Pattern.compile("\\A(?<os>\\w+)\\s*(?<type>\\w+)*$");

    private static final Pattern SERVER_INFO_MAP = Pattern.compile("\\A(?<map>\\w+)");

    private static final Pattern SERVER_INFO_PLAYERS = Pattern.compile("\\A(?<humanCount>\\d+)\\s+humans[\\,]*\\s+(?<botCount>\\d+)\\s+bots\\s+\\((?<maxPlayers>[\\w\\/]+)\\smax\\)(?:\\s*\\((?<hibernate>[\\w\\s]+)\\)\\s*)?(?:\\((?<reservationStatus>reserved|unreserved))*\\s*(?:(?<reservationCookie>.+)*\\))*");

    private static final Pattern SERVER_INFO_EDICTS = Pattern.compile("\\A(?<edicts>\\d+)\\sused\\sof\\s(?<maxEdicts>\\d+)\\smax$");

    private static final Pattern SERVER_INFO_STEAMID = Pattern.compile("\\A\\[(?<steamId>.+)\\]\\s+\\((?<steamId64>\\d+)\\)\\s?$");

    private List<SourcePlayerStatus> playerStatusList;

    private List<String> serverTags;

    private Map<String, String> statusEntries;

    private final int linesToSkip = 1;

    private int lineCtr = 0;

    private int playerIdx = 0;

    @Override
    protected SourceServerStatus createResultObject(ManagedServer server) {
        return new SourceServerStatus(server);
    }

    @Override
    protected void beforeParse(ManagedServer server, SourceServerStatus result) {
        playerStatusList = new ArrayList<>();
        statusEntries = new HashMap<>();
        serverTags = new ArrayList<>();
        lineCtr = 0;
        playerIdx = 0;
    }

    @Override
    protected void afterParse(ManagedServer server, SourceServerStatus result) {
        result.setPlayers(FXCollections.observableArrayList(playerStatusList));
        result.setEntries(FXCollections.observableMap(statusEntries));
        result.setTags(FXCollections.observableArrayList(serverTags));
    }

    @Override
    protected void parseLine(ManagedServer server, SourceServerStatus serverStatus, String line, int lineNum) throws ParseException {
        if (!line.startsWith("#")) {
            String[] entry = StringUtils.splitPreserveAllTokens(line, ":", 2);
            if (entry != null) {
                String key = entry[0].trim();
                String value = entry[1].trim();
                log.debug("Key = {}, Value = {}", key, value);
                statusEntries.put(entry[0].trim(), entry[1].trim());
                parseServerInfo(serverStatus, key, value);
            }
        } else {
            if (lineCtr++ < linesToSkip) {
                //extract headers
                if (line.contains("# userid")) {
                    String[] headers = splitBySpace(line);
                    if (headers != null) {
                        ArrayList<String> statusHeaderList = new ArrayList<>();
                        for (String header : headers) {
                            if (!StringUtils.isBlank(header) && !"#".equals(header)) {
                                statusHeaderList.add(header);
                            }
                        }
                        serverStatus.setHeaders(FXCollections.observableArrayList(statusHeaderList));
                        log.info("Processed {} headers", statusHeaderList.size());
                        //statusHeaderList.forEach(h -> log.info("Header: {}", h));
                    }
                    log.debug("Processed header: {}", line);
                } else {
                    log.debug("Skipped line: {}", line);
                }
                return;
            }
            SourcePlayerStatus playerStatus = new SourcePlayerStatus();
            log.debug("Procesing: '{}'", line);

            parsePlayerInfo(playerStatus, ++playerIdx, serverStatus.getHeaders(), line);
        }
    }

    protected void parseServerInfo(SourceServerStatus serverStatus, String key, String value) throws ParseException {
        switch (key) {
            case "hostname": {
                serverStatus.setName(value);
                break;
            }
            case "version": {
                SourceServerStatus.SourceServerVersion version = new SourceServerStatus.SourceServerVersion();
                Matcher matcher = SERVER_INFO_VERSION.matcher(value);
                if (matcher.matches()) {
                    String verStr = matcher.group("version");
                    String typeStr = matcher.group("type");
                    String metaStr = matcher.group("meta");
                    version.setVersion(verStr);
                    version.setType(typeStr);
                    version.setMeta(metaStr);
                }
                serverStatus.setVersion(version);
                break;
            }
            case "udp/ip": {
                Matcher matcher = LOCAL_PUB_IP.matcher(value);
                if (matcher.matches()) {
                    String localIp = matcher.group(1);
                    String publicIp = matcher.group(2);
                    extractAndUpdateIpPort(localIp, serverStatus::setLocalIp, serverStatus::setLocalPort);
                    extractAndUpdateIpPort(publicIp, serverStatus::setPublicIp, serverStatus::setPublicPort);
                    if (serverStatus.getPublicIp() != null && serverStatus.getPublicPort() == null && serverStatus.getLocalPort() != null)
                        serverStatus.setPublicPort(serverStatus.getLocalPort());
                    log.debug("Local IP: {}:{}, Public IP: {}:{}", serverStatus.getLocalIp(), serverStatus.getLocalPort(), serverStatus.getPublicIp(), serverStatus.getPublicPort());
                } else {
                    throw new ParseException(String.format("Could not parse ip/port of line '%s'", value), -1);
                }
                break;
            }
            case "os": {
                Matcher matcher = SERVER_INFO_OS.matcher(value);
                if (matcher.matches()) {
                    String osStr = matcher.group("os");
                    String typeStr = matcher.group("type");
                    serverStatus.setOperatingSystem(osStr.trim());
                    if (serverStatus.getType() != null && !StringUtils.isBlank(typeStr))
                        serverStatus.setType(typeStr.toLowerCase());
                }
                break;
            }
            case "type": {
                serverStatus.setType(value);
                break;
            }
            case "map": {
                Matcher matcher = SERVER_INFO_MAP.matcher(value);
                if (matcher.matches()) {
                    String mapStr = matcher.group("map");
                    if (!StringUtils.isBlank(mapStr)) {
                        serverStatus.setMap(mapStr);
                    }
                }
                break;
            }
            case "players": {
                Matcher matcher = SERVER_INFO_PLAYERS.matcher(value);
                if (matcher.matches()) {
                    String humanCountStr = Check.requireNumericString(matcher.group("humanCount"), String.format("Human count is not numeric: %s", value));
                    String botCountStr = Check.requireNumericString(matcher.group("botCount"), String.format("Bot count is not numeric: %s", value));
                    String maxPlayerCountStr = matcher.group("maxPlayers");
                    String hibernateStr = matcher.group("hibernate");
                    String reservationStatusStr = matcher.group("reservationStatus");
                    String reservationCookieStr = matcher.group("reservationCookie");

                    serverStatus.setHumanCount(Integer.valueOf(humanCountStr));
                    serverStatus.setBotCount(Integer.valueOf(botCountStr));

                    if (StringUtils.isNotBlank(maxPlayerCountStr)) {
                        if (maxPlayerCountStr.contains("/")) {
                            String[] tokens = StringUtils.splitPreserveAllTokens(maxPlayerCountStr, "/", 2);
                            String maxPlayerCount = Check.requireNumericString(tokens[0], "Max player count is not numeric: " + value);
                            serverStatus.setMaxPlayerCount(Integer.valueOf(maxPlayerCount));
                        }
                    }

                    if (StringUtils.isNotBlank(hibernateStr)) {
                        serverStatus.setHibernating("hibernating".equalsIgnoreCase(hibernateStr));
                    }

                    if (StringUtils.isNotBlank(reservationStatusStr)) {
                        serverStatus.setReserved("reserved".equalsIgnoreCase(hibernateStr));
                    }

                    if (StringUtils.isNotBlank(reservationCookieStr)) {
                        serverStatus.setReservationCookie(reservationCookieStr);
                    }
                }
                break;
            }
            case "tags": {
                String[] tags = StringUtils.split(value, ", ;:|");
                if (tags != null)
                    serverTags.addAll(Arrays.asList(tags));
                break;
            }
            case "steamid": {
                Matcher matcher = SERVER_INFO_STEAMID.matcher(value);
                if (matcher.matches()) {
                    String steamId = matcher.group("steamId");
                    String steamId64 = Check.requireNumericString(matcher.group("steamId64"), "Steam ID 64 is not numeric: " + value);
                    serverStatus.setSteamId(steamId);
                    serverStatus.setSteamId64(Long.valueOf(steamId64));
                }
                break;
            }
            case "account": {
                //ignored as this feature is now obsolete (see: https://support.steampowered.com/kb_article.php?ref=2825-AFGJ-3513#how)
                break;
            }
            case "edicts": {
                Matcher matcher = SERVER_INFO_EDICTS.matcher(value);
                if (matcher.matches()) {
                    String edicts = Check.requireNumericString(matcher.group("edicts"), "Edicts is not numeric: " + value);
                    String maxEdicts = Check.requireNumericString(matcher.group("maxEdicts"), "Max Edicts is not numeric: " + value);
                    serverStatus.setEdicts(Integer.valueOf(edicts));
                    serverStatus.setMaxEdicts(Integer.valueOf(maxEdicts));
                }
                break;
            }
            default: {
                throw new IllegalStateException("Server info key is not currently supported/recognized: " + key + " (Value: " + value + ")");
            }
        }
    }

    protected void parsePlayerInfo(SourcePlayerStatus playerStatus, int idx, List<String> headers, String line) throws ParseException {
        log.info("\t{}) Token: '{}'", idx, line);
        Pattern[] patterns = {PLAYER_INFO_FULL, PLAYER_INFO_FULL_NORATE, PLAYER_INFO_FULL_NORATEADDR, PLAYER_INFO_BOT}; //order is important

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                for (String header : headers) {
                    try {
                        String token = matcher.group(header);
                        if (!StringUtils.isBlank(token)) {
                            log.info("\t{} = {}", header, token.trim());
                            updatePlayerStatusByHeader(playerStatus, header, token);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                break;
            }
        }
    }

    protected void updatePlayerStatusByHeader(SourcePlayerStatus playerStatus, String header, String value) {
        switch (header) {
            case "userid": {
                playerStatus.setUserId(value.trim());
                break;
            }
            case "name": {
                playerStatus.setName(value);
                break;
            }
            case "uniqueid": {
                playerStatus.setUniqueId(value);
                break;
            }
            case "connected": {
                playerStatus.setDuration(Duration.ofSeconds(2));
                break;
            }
            case "ping": {
                if (!StringUtils.isNumeric(value))
                    throw new IllegalStateException(String.format("Ping value is not of numeric form (Actual: %s)", value));
                playerStatus.setPing(Integer.valueOf(value));
                break;
            }
            case "loss": {
                if (!StringUtils.isNumeric(value))
                    throw new IllegalStateException(String.format("Loss is not of numeric form (Actual: %s)", value));
                playerStatus.setLoss(Integer.valueOf(value));
                break;
            }
            case "state": {
                playerStatus.setState(value);
                break;
            }
            case "rate": {
                if (!StringUtils.isNumeric(value))
                    throw new IllegalStateException(String.format("Rate is not of numeric form (Actual: %s)", value));
                playerStatus.setRate(Integer.valueOf(value));
                break;
            }
            case "adr": {
                extractAndUpdateIpPort(value, playerStatus::setIpAddress, playerStatus::setPort);
                break;
            }
            default:
                throw new IllegalStateException(String.format("Unhandled header for player status: %s", header));
        }
    }

    protected void extractAndUpdateIpPort(String ipPort, Consumer<String> ipSet, Consumer<Integer> portSet) {
        if (StringUtils.isBlank(ipPort))
            return;
        if (ipPort.contains(":")) {
            String[] tokens = StringUtils.splitPreserveAllTokens(ipPort, ":", 2);
            if (!StringUtils.isBlank(tokens[0])) {
                if (isValidIp(tokens[0])) {
                    ipSet.accept(tokens[0].trim());
                } else {
                    log.warn("Invalid IP: {}", tokens[0]);
                }
            }
            if (!StringUtils.isBlank(tokens[1]) && StringUtils.isNumeric(tokens[1])) {
                portSet.accept(Integer.valueOf(tokens[1].trim()));
            }
        } else {
            if (isValidIp(ipPort.trim())) {
                ipSet.accept(ipPort.trim());
            }
            portSet.accept(null);
        }
    }

    public static void main(String[] args) {
        String csgoStatus = "hostname: mac & cheese\n" +
                "version : 1.36.9.4/13694 915/7478 secure  [G:1:2825328] \n" +
                "udp/ip  : 192.168.1.14:27016  (public ip: 112.211.60.37)\n" +
                "os      :  Linux\n" +
                "type    :  community dedicated\n" +
                "map     : de_dust2\n" +
                "players : 8 humans, 2 bots (16/0 max) (not hibernating)\n" +
                "\n" +
                "# userid name uniqueid connected ping loss state rate adr\n" +
                "# 41 1 \"Muzashi\" STEAM_1:1:185600841 14:58 36 0 active 196608 58.69.173.91:31105\n" +
                "# 42 2 \"Adriek\" STEAM_1:1:85281858 14:35 37 0 active 196608 58.69.173.91:53818\n" +
                "# 43 3 \"Bill Gates\" STEAM_1:0:183212540 14:35 36 0 active 196608 58.69.173.91:62428\n" +
                "# 44 4 \"Damaidec\" STEAM_1:0:104640724 14:22 37 0 active 196608 58.69.173.91:6183\n" +
                "# 45 5 \"KhalifaMan\" STEAM_1:0:198593338 14:02 37 0 active 196608 58.69.173.91:49625\n" +
                "#60 \"Calvin\" BOT active 64\n" +
                "# 58 7 \"Eagle Man\" STEAM_1:0:72151585 07:59 39 0 active 196608 112.203.116.11:27005\n" +
                "# 59 8 \"lil isis\" STEAM_1:1:105424244 07:03 38 0 active 196608 58.69.173.91:21312\n" +
                "#52 \"Jon\" BOT active 64\n" +
                "# 55 15 \"Crownless Kings \"13\"\" STEAM_1:1:96446071 13:30 38 0 active 196608 58.69.173.91:59855\n" +
                "#end";

        String l4d2Status = "hostname: spoonman\n" +
                "version : 2.1.5.5 7311 secure  (unknown)\n" +
                "udp/ip  : 192.168.1.14:27015 [ public 112.211.60.37:27015 ]\n" +
                "os      : Linux Dedicated\n" +
                "map     : c10m4_mainstreet\n" +
                "players : 4 humans, 0 bots (30 max) (not hibernating) (unreserved)\n" +
                "\n" +
                "# userid name uniqueid connected ping loss state rate adr\n" +
                "# 603 1 \"VE\" STEAM_1:0:197533349 25:17 94 0 active 5000 180.191.230.113:11202\n" +
                "#677 \"Smoker\" BOT active\n" +
                "#678 \"Spitter\" BOT active\n" +
                "# 616 5 \"Evilator\" STEAM_1:1:175231728 21:05 67 7 active 20000 175.158.201.14:2049\n" +
                "# 631 8 \"Wick ™\" STEAM_1:1:79761194 13:38 62 0 active 20000 180.190.196.2:57160\n" +
                "# 633 9 \"Gerard\" STEAM_1:1:220022552 13:33 33 0 active 30000 112.211.251.219:27005\n" +
                "#end";

        String l4d2StatusListen = "hostname: A.D.I.D.A.S\n" +
                "version : 2.1.5.5 7227 secure  \n" +
                "udp/ip  : 192.168.246.1:27015 [ public n/a ]\n" +
                "os      : Windows Listen\n" +
                "map     : c10m1_caves at ( -11836, -14471, -114 )\n" +
                "players : 2 humans, 0 bots (4 max) (not hibernating) (reserved 186000000d26f34)\n" +
                "\n" +
                "# userid name uniqueid connected ping loss state rate adr\n" +
                "#  2 1 \"A.D.I.D.A.S\" STEAM_1:1:25303182 01:24 33 0 active 30000 127.0.0.1:27005\n" +
                "# 4 \"Zoey\" BOT active\n" +
                "# 5 \"Louis\" BOT active\n" +
                "#  6 5 \"Alucard\" STEAM_1:0:21162479 00:20 125 0 active 30000 0.0.0.1:1\n" +
                "#end";

        String tf2Status = "hostname: LinuxGSM\n" +
                "version : 5063830/24 5063830 secure\n" +
                "udp/ip  : 192.168.1.14:27017  (public ip: 112.211.60.37)\n" +
                "steamid : [A:1:1104221186:12510] (90125723692504066)\n" +
                "account : not logged in  (No account specified)\n" +
                "map     : cp_badlands at: 0 x, 0 y, 0 z\n" +
                "tags    : cp\n" +
                "players : 1 humans, 0 bots (16 max)\n" +
                "edicts  : 551 used of 2048 max\n" +
                "# userid name                uniqueid            connected ping loss state  adr\n" +
                "#      2 \"A.D.I.D.A.S\"       [U:1:50606365]      01:04      119   27 active 192.168.1.24:27005";

        String tf2Status2 = "hostname: Valve Matchmaking Server (Singapore sgp-3/srcds148 #46)\n" +
                "account : not logged in  (No account specified)\n" +
                "version : 5063830/24 5063830 secure\n" +
                "map     : pl_upward at: 0 x, 0 y, 0 z\n" +
                "udp/ip  : 103.28.55.232:27060  (public ip: 103.28.55.232)\n" +
                "tags    : hidden,increased_maxplayers,payload,valve\n" +
                "steamid : [A:1:2232811525:12496] (90125664691552261)\n" +
                "players : 23 humans, 0 bots (32 max)\n" +
                "edicts  : 1251 used of 2048 max\n" +
                "# userid name                uniqueid            connected ping loss state\n" +
                "#    534 \"no u cockson!\"     [U:1:196028584]     03:08       56    0 active\n" +
                "#    525 \"wwss520\"           [U:1:338115231]     05:09       85    0 active\n" +
                "#    535 \"BPL.SRBbtry\"       [U:1:839450368]     02:43      120    0 active\n" +
                "#    526 \"[TW]B Lun\"         [U:1:331046629]     05:07      108    0 active\n" +
                "#    536 \"PsychoTheRapist\"   [U:1:276291154]     02:36      157    0 active\n" +
                "#    484 \"Pootis Spencer\"    [U:1:162857877]     30:22       54    0 active\n" +
                "#    524 \"ฮั่นแน่\" [U:1:206634329] 06:07       85    0 active\n" +
                "#    523 \"Ebi\"               [U:1:129527629]     09:53       53    0 active\n" +
                "#    538 \"WinterChap\"        [U:1:389528003]     02:21       59    0 active\n" +
                "#    528 \"Nocturne TW\"       [U:1:132320235]     04:59      116    0 active\n" +
                "#    539 \"sneke\"             [U:1:191174014]     02:06      149    0 active\n" +
                "#    529 \"SAD\"               [U:1:81002784]      04:08       89    0 active\n" +
                "#    540 \"A.D.I.D.A.S\"       [U:1:50606365]      01:46       67    0 active\n" +
                "#    520 \"รักตู่นะ\" [U:1:126522040] 16:05    72    0 active\n" +
                "#    481 \"huat-dō\"          [U:1:86033022]      32:46      104    0 active\n" +
                "#    537 \"command me lmao\"   [U:1:925529674]     02:22      166    0 active\n" +
                "#    541 \"C瓜榴槤Starburst Stream\" [U:1:213683850] 01:42   94    0 active\n" +
                "#    531 \"Water Bottles\"     [U:1:267491280]     03:23       74    0 active\n" +
                "#    522 \"Yammy_Awesome101\"  [U:1:840252405]     12:58      106    0 active\n" +
                "#    515 \"chinathipwiriyalay\" [U:1:992322519]    20:25       80    0 active\n" +
                "#    521 \"Cypher2197\"        [U:1:313841720]     13:26      104    0 active\n" +
                "#    532 \"kj-23\"             [U:1:158582297]     03:22       90    0 active\n" +
                "#    533 \"2Spoopy\"           [U:1:262533636]     03:08       54    0 active";

        DefaultSourceServerStatusParser parser = new DefaultSourceServerStatusParser();
        SourceServerStatus csgoServerStatus = parser.apply(new ManagedServer(), csgoStatus);
        SourceServerStatus l4d2ServerStatus = parser.apply(new ManagedServer(), l4d2Status);
        SourceServerStatus l4d2ServerStatusListen = parser.apply(new ManagedServer(), l4d2StatusListen);
        SourceServerStatus tf2ServerStatus = parser.apply(new ManagedServer(), tf2Status2);
    }
}
