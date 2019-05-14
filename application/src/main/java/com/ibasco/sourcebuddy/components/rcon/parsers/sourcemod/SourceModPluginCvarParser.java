package com.ibasco.sourcebuddy.components.rcon.parsers.sourcemod;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModCvar;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceModPluginCvarParser extends RconResultLineParser<List<SourceModCvar>> {

    private static final Logger log = LoggerFactory.getLogger(SourceModPluginCvarParser.class);

    private static final Pattern CVAR_LIST_INFO = Pattern.compile("\\A\\s+(?<cvarName>\\w+)\\s+(?<cvarValue>.+)$");

    @Override
    protected List<SourceModCvar> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void parseLine(ManagedServer server, List<SourceModCvar> result, String line, int lineNum) throws ParseException {
        Matcher matcher = CVAR_LIST_INFO.matcher(line);
        if (matcher.matches()) {
            String name = matcher.group("cvarName");
            String value = matcher.group("cvarValue");
            SourceModCvar sourceModCvar = new SourceModCvar();
            sourceModCvar.setName(name);
            sourceModCvar.setValue(value);
            result.add(sourceModCvar);
        }
    }

    public static void main(String[] args) {
        SourceModPluginCvarParser parser = new SourceModPluginCvarParser();

        String test = "[SM] Listing 61 convars for: Confogl's Competitive Mod\n" +
                "  [Name]                           [Value]\n" +
                "  confogl_SM_custommaxdistance     0\n" +
                "  confogl_SM_enable                1\n" +
                "  confogl_SM_healthbonusratio      2.0\n" +
                "  confogl_SM_mapmulti              1\n" +
                "  confogl_SM_survivalbonusratio    0.0\n" +
                "  confogl_SM_tempmulti_incap_0     0.30625\n" +
                "  confogl_SM_tempmulti_incap_1     0.17500\n" +
                "  confogl_SM_tempmulti_incap_2     0.10000\n" +
                "  confogl_adrenaline_limit         -1\n" +
                "  confogl_block_punch_rock         0\n" +
                "  confogl_blockinfectedbots        1\n" +
                "  confogl_boss_tank                1\n" +
                "  confogl_boss_unprohibit          1\n" +
                "  confogl_customcfg                \n" +
                "  confogl_debug                    0\n" +
                "  confogl_disable_ghost_hurt       0\n" +
                "  confogl_disable_tank_hordes      0\n" +
                "  confogl_enable_itemtracking      0\n" +
                "  confogl_ghost_warp               1\n" +
                "  confogl_itemtracking_mapspecifi  0\n" +
                "  confogl_itemtracking_savespawns  0\n" +
                "  confogl_limit_tier2              1\n" +
                "  confogl_limit_tier2_saferoom     1\n" +
                "  confogl_lock_boss_spawns         1\n" +
                "  confogl_match_allowvoting        1\n" +
                "  confogl_match_autoconfig         \n" +
                "  confogl_match_autoload           0\n" +
                "  confogl_match_execcfg_off        confogl_off.cfg\n" +
                "  confogl_match_execcfg_on         confogl.cfg\n" +
                "  confogl_match_execcfg_plugins    confogl_plugins.cfg\n" +
                "  confogl_match_killlobbyres       1\n" +
                "  confogl_match_reloaded           0\n" +
                "  confogl_match_restart            1\n" +
                "  confogl_molotov_limit            -1\n" +
                "  confogl_password                 \n" +
                "  confogl_password_reloaded        \n" +
                "  confogl_pills_limit              -1\n" +
                "  confogl_pipebomb_limit           -1\n" +
                "  confogl_reduce_finalespawnrange  1\n" +
                "  confogl_remove_c5m4_hurts        1\n" +
                "  confogl_remove_chainsaw          1\n" +
                "  confogl_remove_defib             1\n" +
                "  confogl_remove_escape_tank       1\n" +
                "  confogl_remove_grenade           1\n" +
                "  confogl_remove_inf_clips         1\n" +
                "  confogl_remove_lasersight        1\n" +
                "  confogl_remove_m60               1\n" +
                "  confogl_remove_parachutist       1\n" +
                "  confogl_remove_saferoomitems     1\n" +
                "  confogl_remove_statickits        1\n" +
                "  confogl_remove_upg_explosive     1\n" +
                "  confogl_remove_upg_incendiary    1\n" +
                "  confogl_replace_cssweapons       1\n" +
                "  confogl_replace_finalekits       1\n" +
                "  confogl_replace_startkits        1\n" +
                "  confogl_replace_tier2            1\n" +
                "  confogl_replace_tier2_all        1\n" +
                "  confogl_replace_tier2_finale     1\n" +
                "  confogl_slowdown_factor          0.90\n" +
                "  confogl_vomitjar_limit           -1\n" +
                "  confogl_waterslowdown            1\n" +
                "L 05/13/2019 - 05:20:49: rcon from \"192.168.1.1:63231\": command \"sm cvars 17\"";

        List<SourceModCvar> sourceModCvarList = parser.apply(new ManagedServer(), test);
        for (SourceModCvar sourceModCvar : sourceModCvarList) {
            log.info("Cvar = {}", sourceModCvar);
        }
    }
}
