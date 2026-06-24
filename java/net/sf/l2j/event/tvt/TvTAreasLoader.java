package net.sf.l2j.event.tvt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2j.event.partyfarm.RewardHolder;
import net.sf.l2j.gameserver.model.Location;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TvTAreasLoader
{
    public static class Area
    {
        public final String name; // Novo campo para nome da área
        public final String team1Name;
        public final List<Location> team1Spawns;
        public final String team2Name;
        public final List<Location> team2Spawns;

        public List<RewardHolder> topKillsRewards = new ArrayList<>();
        public final List<RewardHolder> rewardsWin;
        public final List<RewardHolder> rewardsLos;
        public final List<RewardHolder> rewardsTie;

        public final List<Integer> doorsToClose = new ArrayList<>();
        public final List<Integer> doorsToOpen = new ArrayList<>();

        public Area(String name, String team1Name, List<Location> t1Spawns, String team2Name, List<Location> t2Spawns,
                    List<RewardHolder> rewardsWin, List<RewardHolder> rewardsLos, List<RewardHolder> rewardsTie)
        {
            this.name = name;
            this.team1Name = team1Name;
            this.team1Spawns = t1Spawns;
            this.team2Name = team2Name;
            this.team2Spawns = t2Spawns;
            this.rewardsWin = rewardsWin != null ? rewardsWin : new ArrayList<>();
            this.rewardsLos = rewardsLos != null ? rewardsLos : new ArrayList<>();
            this.rewardsTie = rewardsTie != null ? rewardsTie : new ArrayList<>();
        }
    }

    private static final List<Area> areas = new ArrayList<>();

    public static void load()
    {
        areas.clear();
        try
        {
            File fXmlFile = new File("config/events/TeamVsTeamData.xml");
            if (!fXmlFile.exists())
            {
                System.err.println("TvTAreasLoader: XML file not found: " + fXmlFile.getAbsolutePath());
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList areaList = doc.getElementsByTagName("area");

            for (int i = 0; i < areaList.getLength(); i++)
            {
                Node areaNode = areaList.item(i);
                if (areaNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element areaElement = (Element) areaNode;

                    // Pega o atributo name da área
                    String areaName = areaElement.getAttribute("name");

                    Element team1Elem = (Element) areaElement.getElementsByTagName("team1").item(0);
                    Element team2Elem = (Element) areaElement.getElementsByTagName("team2").item(0);

                    String team1Name = team1Elem.getAttribute("name");
                    String team2Name = team2Elem.getAttribute("name");

                    List<Location> team1Spawns = new ArrayList<>();
                    NodeList team1SpawnList = team1Elem.getElementsByTagName("spawn");
                    for (int j = 0; j < team1SpawnList.getLength(); j++)
                    {
                        Element spawn = (Element) team1SpawnList.item(j);
                        team1Spawns.add(new Location(
                            Integer.parseInt(spawn.getAttribute("x")),
                            Integer.parseInt(spawn.getAttribute("y")),
                            Integer.parseInt(spawn.getAttribute("z"))
                        ));
                    }

                    List<Location> team2Spawns = new ArrayList<>();
                    NodeList team2SpawnList = team2Elem.getElementsByTagName("spawn");
                    for (int j = 0; j < team2SpawnList.getLength(); j++)
                    {
                        Element spawn = (Element) team2SpawnList.item(j);
                        team2Spawns.add(new Location(
                            Integer.parseInt(spawn.getAttribute("x")),
                            Integer.parseInt(spawn.getAttribute("y")),
                            Integer.parseInt(spawn.getAttribute("z"))
                        ));
                    }

                    List<RewardHolder> rewardsWin = parseRewards(areaElement, "rewardsWin");
                    List<RewardHolder> rewardsLos = parseRewards(areaElement, "rewardsLos");
                    List<RewardHolder> rewardsTie = parseRewards(areaElement, "rewardsTie");
                    List<RewardHolder> rewardsTopKills = parseRewards(areaElement, "rewardTopKills");

                    Area area = new Area(areaName, team1Name, team1Spawns, team2Name, team2Spawns, rewardsWin, rewardsLos, rewardsTie);
                    area.topKillsRewards = rewardsTopKills;

                    // Parse doorsToClose
                    NodeList closeNodes = areaElement.getElementsByTagName("doorsToClose");
                    if (closeNodes.getLength() > 0)
                    {
                        NodeList doors = ((Element) closeNodes.item(0)).getElementsByTagName("door");
                        for (int d = 0; d < doors.getLength(); d++)
                        {
                            Element door = (Element) doors.item(d);
                            area.doorsToClose.add(Integer.parseInt(door.getTextContent()));
                        }
                    }

                    // Parse doorsToOpen
                    NodeList openNodes = areaElement.getElementsByTagName("doorsToOpen");
                    if (openNodes.getLength() > 0)
                    {
                        NodeList doors = ((Element) openNodes.item(0)).getElementsByTagName("door");
                        for (int d = 0; d < doors.getLength(); d++)
                        {
                            Element door = (Element) doors.item(d);
                            area.doorsToOpen.add(Integer.parseInt(door.getTextContent()));
                        }
                    }

                    areas.add(area);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static List<RewardHolder> parseRewards(Element parent, String tagName)
    {
        List<RewardHolder> rewards = new ArrayList<>();
        NodeList rewardsNodes = parent.getElementsByTagName(tagName);
        if (rewardsNodes.getLength() > 0)
        {
            Element rewardsElement = (Element) rewardsNodes.item(0);
            NodeList rewardList = rewardsElement.getElementsByTagName("reward");
            for (int j = 0; j < rewardList.getLength(); j++)
            {
                Element rewardElem = (Element) rewardList.item(j);
                try
                {
                    int id = Integer.parseInt(rewardElem.getAttribute("id"));
                    int min = Integer.parseInt(rewardElem.getAttribute("min"));
                    int max = Integer.parseInt(rewardElem.getAttribute("max"));
                    int chance = Integer.parseInt(rewardElem.getAttribute("chance"));
                    rewards.add(new RewardHolder(id, min, max, chance));
                }
                catch (NumberFormatException nfe)
                {
                    System.err.println("TvTAreasLoader: Invalid reward attribute in " + tagName + ": " + nfe.getMessage());
                }
            }
        }
        return rewards;
    }

    public static List<Area> getAreas()
    {
        return areas;
    }
}
