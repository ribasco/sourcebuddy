package com.ibasco.sourcebuddy.components.rcon.parsers;

import com.ibasco.sourcebuddy.components.rcon.RconResultLineParser;
import com.ibasco.sourcebuddy.components.rcon.SourceModCvar;
import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CvarListParser extends RconResultLineParser<List<SourceModCvar>> {

    private static final Logger log = LoggerFactory.getLogger(CvarListParser.class);

    @Override
    protected List<SourceModCvar> createResultObject(ManagedServer server) {
        return new ArrayList<>();
    }

    @Override
    protected void parseLine(ManagedServer server, List<SourceModCvar> result, String line, int lineNum) throws ParseException {
        String[] values = StringUtils.splitPreserveAllTokens(line, ":", 4);
        if (values == null)
            return;
        if (values.length == 4) {
            if (values[0].trim().contains(" "))
                return;
            String name = values[0].trim();
            String value = values[1].trim();
            String[] types = splitByComma(values[2]);
            String desc = values[3].trim();

            SourceModCvar sourceModCvar = new SourceModCvar();
            sourceModCvar.setName(name);
            sourceModCvar.setValue(value);
            sourceModCvar.setTypes(types == null ? new ArrayList<>() : Arrays.stream(types).filter(StringUtils::isNotBlank).map(p -> StringUtils.remove(p, "\"")).collect(Collectors.toList()));
            sourceModCvar.setDescription(desc);
            result.add(sourceModCvar);
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(ResourceUtil.loadResourceAsStream("/cvarlist-sample.txt"))) {
            StringBuilder cvarlist = new StringBuilder();
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                cvarlist.append(line);
                cvarlist.append("\n");
            }
            CvarListParser cvarListParser = new CvarListParser();
            List<SourceModCvar> sourceModCvars = cvarListParser.apply(new ManagedServer(), cvarlist.toString());
            for (SourceModCvar sourceModCvar : sourceModCvars) {
                log.info("CVAR: {}", sourceModCvar);
            }
        }
    }
}
