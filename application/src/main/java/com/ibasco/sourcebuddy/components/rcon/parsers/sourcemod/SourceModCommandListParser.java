package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModCommand;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceModCommandListParser extends RconResultLineParser<List<SourceModCommand>> {

    private static final Logger log = LoggerFactory.getLogger(SourceModCommandListParser.class);

    private static final Pattern SM_CMD_LIST = Pattern.compile("\\A\\s+(?<cmdName>[\\w\\_]+)\\s+(?<cmdType>\\w+)(?:\\s+(?<cmdDesc>.+))?$");

    @Override
    protected List<SourceModCommand> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void parseLine(ManagedServer server, List<SourceModCommand> result, String line, int lineNum) throws ParseException {
        try {
            Matcher matcher = SM_CMD_LIST.matcher(line);
            if (matcher.matches()) {
                String name = matcher.group("cmdName");
                String type = matcher.group("cmdType");
                String desc = matcher.group("cmdDesc");
                SourceModCommand cmd = new SourceModCommand();
                cmd.setName(name);
                cmd.setType(type);
                if (StringUtils.isNotBlank(desc))
                    cmd.setDescription(desc);
                result.add(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SourceModCommandListParser parser = new SourceModCommandListParser();
        String test = "[SM] Listing commands for: Confogl's Competitive Mod\n" +
                "  [Name]            [Type]   [Help]\n" +
                "  confogl_addcvar   server       Add a ConVar to be set by Confogl\n" +
                "  confogl_clientse  console      List Client settings enforced by confogl\n" +
                "  confogl_cvardiff  console      List any ConVars that have been changed from their initialized values\n" +
                "  confogl_cvarsett  console      List all ConVars being enforced by Confogl\n" +
                "  confogl_erdata_r  admin\n" +
                "  confogl_midata_s  admin\n" +
                "  confogl_resetcli  server       Remove all tracked client cvars. Cannot be called during matchmode\n" +
                "  confogl_resetcva  server       Resets enforced ConVars.  Cannot be used during a match!\n" +
                "  confogl_save_loc  admin\n" +
                "  confogl_setcvars  server       Starts enforcing ConVars that have been added.\n" +
                "  confogl_startcli  server       Start checking and enforcing client cvars tracked by this plugin\n" +
                "  confogl_trackcli  server       Add a Client CVar to be tracked and enforced by confogl\n" +
                "  sm_fm             admin        Forces the game to use match mode\n" +
                "  sm_forcematch     admin        Forces the game to use match mode\n" +
                "  sm_health         console\n" +
                "  sm_killlobbyres   admin        Forces the plugin to kill lobby reservation\n" +
                "  sm_resetmatch     admin        Forces match mode to turn off REGRADLESS for always on or forced match\n" +
                "  sm_warptosurvivo  console\n" +
                "L 05/13/2019 - 05:20:49: rcon from \"192.168.1.1:63231\": command \"sm cvars 17\"";
        List<SourceModCommand> commandList = parser.apply(new ManagedServer(), test);

        for (SourceModCommand cmd : commandList) {
            log.info("Command: {}", cmd);
        }
    }
}
