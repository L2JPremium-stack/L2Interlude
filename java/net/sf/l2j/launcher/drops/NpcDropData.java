package net.sf.l2j.launcher.drops;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NpcDropData
{
    public int npcId;
    public String npcName;
    public File sourceFile;
    public Document document;
    public Element npcElement;
    public Element dropsElement;
    public List<DropCategory> categories;
}