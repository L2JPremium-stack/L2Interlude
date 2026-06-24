package net.sf.l2j.timezone;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TimeFarmZoneData
{
    private static final Map<String, ZoneInfo> _zones = new HashMap<>();

    public static class MobSpawn
    {
        public int mobId;
        public Location loc;

        public MobSpawn(int mobId, Location loc)
        {
            this.mobId = mobId;
            this.loc = loc;
        }
    }

    public static class ZoneInfo
    {
        public String name;
        public int intervalMinutes;
        public List<MobSpawn> spawns = new ArrayList<>();
        public boolean enabled = true;  // novo campo para controlar se a zona está habilitada
    }

    public static void load(File file)
    {
        try
        {
            Document doc = XMLDocumentFactory.getInstance().loadDocument(file);

            Node listNode = doc.getFirstChild();

            for (Node zoneNode = listNode.getFirstChild(); zoneNode != null; zoneNode = zoneNode.getNextSibling())
            {
                if (!"zone".equalsIgnoreCase(zoneNode.getNodeName()))
                    continue;

                NamedNodeMap attrs = zoneNode.getAttributes();

                String name = attrs.getNamedItem("name").getNodeValue();

                int intervalMinutes = 10; // padrão
                if (attrs.getNamedItem("intervalMinutes") != null)
                    intervalMinutes = Integer.parseInt(attrs.getNamedItem("intervalMinutes").getNodeValue());

                boolean enabled = true; // padrão habilitado
                if (attrs.getNamedItem("enabled") != null)
                    enabled = Boolean.parseBoolean(attrs.getNamedItem("enabled").getNodeValue());

                if (!enabled)
                {
                    System.out.println("TimeFarmZoneData: Zona '" + name + "' esta desabilitada e nao sera carregada.");
                    continue; // pula esta zona
                }

                ZoneInfo zoneInfo = new ZoneInfo();
                zoneInfo.name = name;
                zoneInfo.intervalMinutes = intervalMinutes;
                zoneInfo.enabled = enabled;

                for (Node spawnNode = zoneNode.getFirstChild(); spawnNode != null; spawnNode = spawnNode.getNextSibling())
                {
                    if (!"spawn".equalsIgnoreCase(spawnNode.getNodeName()))
                        continue;

                    NamedNodeMap sAttrs = spawnNode.getAttributes();

                    int mobId = Integer.parseInt(sAttrs.getNamedItem("mobId").getNodeValue());
                    int x = Integer.parseInt(sAttrs.getNamedItem("x").getNodeValue());
                    int y = Integer.parseInt(sAttrs.getNamedItem("y").getNodeValue());
                    int z = Integer.parseInt(sAttrs.getNamedItem("z").getNodeValue());

                    zoneInfo.spawns.add(new MobSpawn(mobId, new Location(x, y, z)));
                }

                _zones.put(name, zoneInfo);
                System.out.println("TimeFarmZoneData: Zona carregada: " + name + " com " + zoneInfo.spawns.size() + " spawns.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("TimeFarmZoneData: Erro ao carregar arquivo " + file.getAbsolutePath());
        }
    }

    public static void loadZonesFromFile()
    {
        File file = new File("./data/xml/custom/TimeFarmZones.xml");
        if (file.exists())
        {
       //     System.out.println("TimeFarmZoneManager: Carregando zonas de " + file.getAbsolutePath());
            TimeFarmZoneData.load(file);
        }
        else
        {
            System.out.println("TimeFarmZoneManager: Arquivo nao encontrado: " + file.getAbsolutePath());
        }
    }

    public static void save()
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sb.append("<list>\n");

            for (ZoneInfo zone : _zones.values())
            {
                sb.append(String.format(
                    "\t<zone name=\"%s\" intervalMinutes=\"%d\" enabled=\"%b\">\n",
                    zone.name,
                    zone.intervalMinutes,
                    zone.enabled));

                for (MobSpawn spawn : zone.spawns)
                {
                    sb.append(String.format(
                        "\t\t<spawn mobId=\"%s\" x=\"%d\" y=\"%d\" z=\"%d\" />\n",
                        (spawn.mobId == 0 ? "" : String.valueOf(spawn.mobId)),
                        spawn.loc.getX(), spawn.loc.getY(), spawn.loc.getZ()));
                }

                sb.append("\t</zone>\n");
            }

            sb.append("</list>\n");

            File file = new File("./data/xml/custom/TimeFarmZones.xml");
            try (java.io.FileWriter writer = new java.io.FileWriter(file))
            {
                writer.write(sb.toString());
            }

            System.out.println("TimeFarmZoneData: Spawns salvos em TimeFarmZones.xml.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("TimeFarmZoneData: Erro ao salvar TimeFarmZones.xml.");
        }
    }

    public static ZoneInfo getZoneInfo(String name)
    {
        return _zones.get(name);
    }
}
