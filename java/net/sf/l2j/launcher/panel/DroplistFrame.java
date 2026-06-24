package net.sf.l2j.launcher.panel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import net.sf.l2j.launcher.drops.DropCategory;
import net.sf.l2j.launcher.drops.NpcDrop;
import net.sf.l2j.launcher.drops.NpcDropData;
import net.sf.l2j.launcher.drops.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DroplistFrame extends JDialog
{
	private static final long serialVersionUID = 1L;
	private static final Set<String> ALLOWED_TYPES = Set.of("L2Monster", "L2GrandBoss", "L2RaidBoss", "L2DungeonMob", "L2Chest");
	
	private JTextField txtSearch;
	private JLabel lblNpc;
	private JTable table;
	private DefaultTableModel model;
	private JButton btnSave;
	private JButton btnReload;
	
	private boolean dirty = false;
	private String originalHash;
	
	private NpcDropData currentNpc;
	
	private final File npcRoot = new File("../game/data/xml/npcs");
	
	public DroplistFrame(JFrame parent)
	{
		super(parent, "NPC Droplist Editor", true);
		setSize(900, 550);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());
		
		add(createTop(), BorderLayout.NORTH);
		add(createCenter(), BorderLayout.CENTER);
		add(createBottom(), BorderLayout.SOUTH);
		registerShortcuts();
	}
	
	/* ================= UI ================= */
	
	private JPanel createTop()
	{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		txtSearch = new JTextField(20);
		txtSearch.addActionListener(e -> searchNpc());
		
		JButton btnSearch = new JButton("Buscar");
		JButton btnOpenFolder = new JButton("Abrir pasta");
		
		lblNpc = new JLabel("");
		
		btnSearch.addActionListener(e -> searchNpc());
		btnOpenFolder.addActionListener(e -> openNpcFolder());
		
		p.add(new JLabel("NPC ID / Nome:"));
		p.add(txtSearch);
		p.add(btnSearch);
		p.add(btnOpenFolder);
		p.add(lblNpc);
		
		return p;
	}
	
	private JScrollPane createCenter()
	{
		model = new DefaultTableModel(new Object[]
		{
			"Categoria",
			"Item ID",
			"Min",
			"Max",
			"Chance"
		}, 0);
		
		model.addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent e)
			{
				if (currentNpc != null)
				{
					dirty = true;
					btnSave.setEnabled(true);
				}
			}
		});
		
		table = new JTable(model);
		return new JScrollPane(table);
	}
	
	private JPanel createBottom()
	{
		JPanel p = new JPanel();
		
		JButton btnAdd = new JButton("Adicionar");
		JButton btnRemove = new JButton("Remover");
		btnReload = new JButton("Recarregar");
		btnSave = new JButton("Salvar");
		
		btnSave.setEnabled(false);
		btnReload.setEnabled(false);
		
		btnAdd.addActionListener(e -> model.addRow(new Object[]
		{
			0,
			0,
			1,
			1,
			1000
		}));
		
		btnRemove.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row >= 0)
				model.removeRow(row);
		});
		
		btnSave.addActionListener(e -> saveDrops());
		btnReload.addActionListener(e -> reloadNpc());
		
		p.add(btnAdd);
		p.add(btnRemove);
		p.add(btnReload);
		p.add(btnSave);
		
		return p;
	}
	
	private static boolean isDropsEmpty(NpcDropData data)
	{
	    if (data.categories == null || data.categories.isEmpty())
	        return true;

	    for (DropCategory c : data.categories)
	    {
	        if (c.drops != null && !c.drops.isEmpty())
	            return false;
	    }
	    return true;
	}

	
	/* ================= LOGIC ================= */
	
	private void searchNpc()
	{
		String query = txtSearch.getText().trim();
		if (query.isEmpty())
			return;
		
		List<NpcDropData> found = findNpcs(query);
		
		if (found.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "NPC não encontrado ou não possui droplist.\n" + "Somente NPCs com <drops> podem ser editados.");
			return;
		}
		
		if (found.size() > 1)
		{
			currentNpc = chooseNpc(found);
			if (currentNpc == null)
				return;
		}
		else
			currentNpc = found.get(0);
		
		lblNpc.setText(currentNpc.npcId + " - " + currentNpc.npcName);
		loadTable();
		
		originalHash = computeHash();
		dirty = false;
		btnSave.setEnabled(false);
		btnReload.setEnabled(true);
		
	}
	
	private void loadTable()
	{
		model.setRowCount(0);
		
		for (DropCategory cat : currentNpc.categories)
			for (NpcDrop d : cat.drops)
				model.addRow(new Object[]
				{
					cat.id,
					d.itemId,
					d.min,
					d.max,
					d.chance
				});
	}
	
	private void saveDrops()
	{
		if (!dirty || computeHash().equals(originalHash))
		{
			JOptionPane.showMessageDialog(this, "Nenhuma alteração para salvar.");
			return;
		}
		
		currentNpc.categories.clear();
		
		for (int i = 0; i < model.getRowCount(); i++)
		{
			int catId = Integer.parseInt(model.getValueAt(i, 0).toString());
			
			DropCategory cat = currentNpc.categories.stream().filter(c -> c.id == catId).findFirst().orElseGet(() -> {
				DropCategory c = new DropCategory(catId);
				currentNpc.categories.add(c);
				return c;
			});
			
			NpcDrop d = new NpcDrop();
			d.itemId = Integer.parseInt(model.getValueAt(i, 1).toString());
			d.min = Integer.parseInt(model.getValueAt(i, 2).toString());
			d.max = Integer.parseInt(model.getValueAt(i, 3).toString());
			d.chance = Long.parseLong(model.getValueAt(i, 4).toString());
			
			cat.drops.add(d);
		}
		
		saveNpcDrops(currentNpc);
		
		originalHash = computeHash();
		dirty = false;
		btnSave.setEnabled(false);
		
		JOptionPane.showMessageDialog(this, "Droplist salva com sucesso!");
	}
	
	private void openNpcFolder()
	{
		try
		{
			File f = (currentNpc != null) ? currentNpc.sourceFile : npcRoot;
			Desktop.getDesktop().open(f.getParentFile());
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Não foi possível abrir a pasta.");
		}
	}
	
	private String computeHash()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < model.getRowCount(); i++)
			for (int j = 0; j < model.getColumnCount(); j++)
				sb.append(model.getValueAt(i, j)).append('|');
			
		return Integer.toHexString(sb.toString().hashCode());
	}
	
	/* ================= NPC SEARCH ================= */
	
	private List<NpcDropData> findNpcs(String query)
	{
		List<NpcDropData> result = new ArrayList<>();
		String q = query.toLowerCase();
		
		File[] files = npcRoot.listFiles(f -> f.getName().endsWith(".xml"));
		if (files == null)
			return result;
		
		for (File f : files)
		{
			Document doc = XmlUtil.load(f);
			NodeList npcs = doc.getElementsByTagName("npc");
			
			for (int i = 0; i < npcs.getLength(); i++)
			{
				Element npc = (Element) npcs.item(i);
				
				if (!isAllowedNpcType(npc))
					continue;
				
				String id = npc.getAttribute("id");
				String name = npc.getAttribute("name").toLowerCase();
				
				if (id.equals(query) || name.contains(q))
				{
					result.add(parseNpc(npc, doc, f));
				}
				
			}
		}
		return result;
	}
	
	private static String extractInnerIndent(String npcBlock)
	{
		int idx = npcBlock.indexOf("\n");
		if (idx == -1)
			return "\t";
		
		int nextLineStart = idx + 1;
		int nextLineEnd = npcBlock.indexOf("<", nextLineStart);
		if (nextLineEnd == -1)
			return "\t";
		
		return npcBlock.substring(nextLineStart, nextLineEnd);
	}
 
	
	private NpcDropData chooseNpc(List<NpcDropData> list)
	{
		DefaultTableModel model = new DefaultTableModel(new Object[]
		{
			"ID",
			"Nome",
			"Arquivo"
		}, 0)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		
		for (NpcDropData d : list)
		{
			model.addRow(new Object[]
			{
				d.npcId,
				d.npcName,
				d.sourceFile.getName()
			});
		}
		
		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		
		if (model.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
		
		JLabel hint = new JLabel("🔍 Selecione um NPC (duplo clique ou OK)");
		hint.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(650, 300));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(hint, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		
		final JDialog dialog = new JDialog(this, "Selecione o NPC", true);
		dialog.setLayout(new BorderLayout());
		dialog.add(panel, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		JButton ok = new JButton("Selecionar");
		JButton cancel = new JButton("Cancelar");
		
		final NpcDropData[] selected = new NpcDropData[1];
		
		ok.addActionListener(e -> {
			int row = table.getSelectedRow();
			if (row >= 0)
			{
				selected[0] = list.get(row);
				dialog.dispose();
			}
			else
				JOptionPane.showMessageDialog(dialog, "Selecione um NPC da lista.");
		});
		
		cancel.addActionListener(e -> dialog.dispose());
		
		buttons.add(ok);
		buttons.add(cancel);
		
		dialog.add(buttons, BorderLayout.SOUTH);
		
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
				{
					selected[0] = list.get(table.getSelectedRow());
					dialog.dispose();
				}
			}
		});
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		return selected[0];
	}
	
	private static String getNpcType(Element npc)
	{
		NodeList sets = npc.getElementsByTagName("set");
		
		for (int i = 0; i < sets.getLength(); i++)
		{
			Element set = (Element) sets.item(i);
			
			if ("type".equalsIgnoreCase(set.getAttribute("name")))
				return set.getAttribute("val");
		}
		return null;
	}
	
	private static boolean isAllowedNpcType(Element npc)
	{
		String type = getNpcType(npc);
		return type != null && ALLOWED_TYPES.contains(type);
	}
	
 
	
	/* ================= XML ================= */
	
	private static NpcDropData parseNpc(Element npc, Document doc, File file)
	{
		NpcDropData data = new NpcDropData();
		data.npcId = Integer.parseInt(npc.getAttribute("id"));
		data.npcName = npc.getAttribute("name");
		data.sourceFile = file;
		data.categories = new ArrayList<>();
		
		NodeList dropsList = npc.getElementsByTagName("drops");
		if (dropsList.getLength() > 0)
		{
			NodeList cats = ((Element) dropsList.item(0)).getElementsByTagName("category");
			for (int i = 0; i < cats.getLength(); i++)
			{
				Element c = (Element) cats.item(i);
				DropCategory cat = new DropCategory(Integer.parseInt(c.getAttribute("id")));
				
				NodeList drops = c.getElementsByTagName("drop");
				for (int j = 0; j < drops.getLength(); j++)
				{
					Element d = (Element) drops.item(j);
					
					NpcDrop drop = new NpcDrop();
					drop.itemId = Integer.parseInt(d.getAttribute("itemid"));
					drop.min = Integer.parseInt(d.getAttribute("min"));
					drop.max = Integer.parseInt(d.getAttribute("max"));
					drop.chance = Long.parseLong(d.getAttribute("chance"));
					
					cat.drops.add(drop);
				}
				data.categories.add(cat);
			}
		}
		return data;
	}
	
	private static void saveNpcDrops(NpcDropData data)
	{
	    try
	    {
	        String xml = Files.readString(data.sourceFile.toPath(), StandardCharsets.UTF_8);

	        if (isDropsEmpty(data))
	        {

	            xml = removeDropsBlock(xml, data.npcId);
	        }
	        else
	        {
	            String oldDrops = extractDropsBlock(xml, data.npcId);

	            if (oldDrops != null)
	            {
	                String prefix = extractLinePrefix(oldDrops);
	                String newDrops = buildDropsBlock(data.categories, prefix);
	                xml = xml.replace(oldDrops, newDrops);
	            }
	            else
	            {
	                xml = insertDropsBlock(xml, data);
	            }
	        }

	        Files.writeString(data.sourceFile.toPath(), xml, StandardCharsets.UTF_8);
	    }
	    catch (Exception e)
	    {
	        throw new RuntimeException(e);
	    }
	}
	
	private static String insertDropsBlock(String xml, NpcDropData data)
	{
		String npcStart = "<npc id=\"" + data.npcId + "\"";
		int npcPos = xml.indexOf(npcStart);
		if (npcPos == -1)
			throw new RuntimeException("NPC não encontrado no XML");
		
		int npcEnd = xml.indexOf("</npc>", npcPos);
		if (npcEnd == -1)
			throw new RuntimeException("Fechamento </npc> não encontrado");
		
		String npcBlock = xml.substring(npcPos, npcEnd);
		
		int insertPos = npcBlock.lastIndexOf("</skills>");
		if (insertPos != -1)
			insertPos += "</skills>".length();
		else
		{
			insertPos = npcBlock.lastIndexOf("</ai>");
			if (insertPos != -1)
				insertPos += "</ai>".length();
			else
				throw new RuntimeException("Não foi possível localizar ponto de inserção para <drops>");
		}
		
		String prefix = extractInnerIndent(npcBlock);
		
		String drops = "\n" + prefix + "<drops>\n" + prefix + "</drops>";
		
		String newNpcBlock = npcBlock.substring(0, insertPos) + drops + npcBlock.substring(insertPos);
		
		return xml.substring(0, npcPos) + newNpcBlock + xml.substring(npcEnd);
	}

	private static String extractDropsBlock(String xml, int npcId)
	{
		String npcStart = "<npc id=\"" + npcId + "\"";
		int npcPos = xml.indexOf(npcStart);
		if (npcPos == -1)
			return null;
		
		int dropsStart = xml.indexOf("<drops>", npcPos);
		if (dropsStart == -1)
			return null;
		
		int lineStart = xml.lastIndexOf("\n", dropsStart);
		int dropsEnd = xml.indexOf("</drops>", dropsStart);
		if (dropsEnd == -1)
			return null;
		
		return xml.substring(lineStart + 1, dropsEnd + "</drops>".length());
	}
	
	private static String buildDropsBlock(List<DropCategory> categories, String prefix)
	{
		String nl = "\n";
		StringBuilder sb = new StringBuilder();
		
		sb.append(prefix).append("<drops>").append(nl);
		
		for (DropCategory cat : categories)
		{
			sb.append(prefix).append("\t<category id=\"").append(cat.id).append("\">").append(nl);
			for (NpcDrop d : cat.drops)
			{
				sb.append(prefix).append("\t\t<drop itemid=\"").append(d.itemId).append("\" min=\"").append(d.min).append("\" max=\"").append(d.max).append("\" chance=\"").append(d.chance).append("\"/>").append(nl);
			}
			sb.append(prefix).append("\t</category>").append(nl);
		}
		
		sb.append(prefix).append("</drops>");
		return sb.toString();
	}
	
	private void reloadNpc()
	{
		if (currentNpc == null)
			return;
		
		if (dirty)
		{
			int r = JOptionPane.showConfirmDialog(this, "Existem alterações não salvas.\nDeseja descartá-las?", "Confirmar reload", JOptionPane.YES_NO_OPTION);
			
			if (r != JOptionPane.YES_OPTION)
				return;
		}
		
		// Recarrega do XML
		List<NpcDropData> list = findNpcs(String.valueOf(currentNpc.npcId));
		if (!list.isEmpty())
		{
			currentNpc = list.get(0);
			loadTable();
			
			originalHash = computeHash();
			dirty = false;
			btnSave.setEnabled(false);
		}
	}
	
	private static String extractLinePrefix(String dropsBlock)
	{
		int i = dropsBlock.indexOf("<drops>");
		return dropsBlock.substring(0, i);
	}
	
	private void registerShortcuts()
	{
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save");
		
		getRootPane().getActionMap().put("save", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (btnSave.isEnabled())
					saveDrops();
			}
		});
	}
	private static String removeDropsBlock(String xml, int npcId)
	{
	    String npcStart = "<npc id=\"" + npcId + "\"";
	    int npcPos = xml.indexOf(npcStart);
	    if (npcPos == -1)
	        return xml;

	    int dropsStart = xml.indexOf("<drops>", npcPos);
	    if (dropsStart == -1)
	        return xml;

	    int lineStart = xml.lastIndexOf("\n", dropsStart);
	    if (lineStart == -1)
	        lineStart = dropsStart;

	    int dropsEnd = xml.indexOf("</drops>", dropsStart);
	    if (dropsEnd == -1)
	        return xml;

	    dropsEnd += "</drops>".length();

	    return xml.substring(0, lineStart) + xml.substring(dropsEnd);
	}

}
