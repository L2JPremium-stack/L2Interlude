package net.sf.l2j.launcher.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ItemsSearchFrame
{
	private static final File ITEMS_ROOT = new File("../game/data/xml/items");
	private static final Object CACHE_LOCK = new Object();
	private static final Map<Integer, ItemInfo> ITEM_CACHE = new HashMap<>();
	private static boolean cacheLoaded = false;
	
	public static final class ItemInfo
	{
		public final int id;
		public final String name;
		public final File sourceFile;
		
		public ItemInfo(int id, String name, File sourceFile)
		{
			this.id = id;
			this.name = name;
			this.sourceFile = sourceFile;
		}
		
		@Override
		public String toString()
		{
			return id + " - " + name;
		}
	}
	
	/* ================= SEARCH API ================= */
	
	public static List<ItemInfo> findItems(String query)
	{
		String q = query == null ? "" : query.trim().toLowerCase();
		if (q.isEmpty())
			return Collections.emptyList();
		
		boolean searchById = q.matches("\\d+");
		if (!searchById && q.length() < 2)
			return Collections.emptyList();
		
		loadItemCache();
		
		List<ItemInfo> result = new ArrayList<>();
		for (ItemInfo item : ITEM_CACHE.values())
		{
			boolean match = searchById ? String.valueOf(item.id).equals(q) : item.name.toLowerCase().contains(q);
			if (match)
				result.add(item);
		}
		
		Collections.sort(result, Comparator.comparingInt(i -> i.id));
		return result;
	}
	
	public static ItemInfo getItem(int itemId)
	{
		loadItemCache();
		return ITEM_CACHE.get(itemId);
	}
	
	public static String getItemName(int itemId)
	{
		ItemInfo item = getItem(itemId);
		return item == null ? "" : item.name;
	}
	
	public static ItemInfo chooseItem(Component parent, String initialQuery)
	{
		Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = owner == null ? new JDialog((JFrame) null, "Selecionar Item", true) : new JDialog(owner, "Selecionar Item", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new BorderLayout());
		dialog.setPreferredSize(new Dimension(560, 380));
		dialog.setMinimumSize(new Dimension(520, 360));
		
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		JLabel lblItemName = new JLabel("Item Nome / ID:");
		JTextField txtItemName = new JTextField(24);
		JButton btnSearch = new JButton("Buscar");
		
		searchPanel.add(lblItemName);
		searchPanel.add(txtItemName);
		searchPanel.add(btnSearch);
		dialog.add(searchPanel, BorderLayout.NORTH);
		
		DefaultTableModel model = createItemModel();
		JTable table = createItemTable(model);
		dialog.add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnOk = new JButton("Selecionar");
		JButton btnCancel = new JButton("Cancelar");
		bottom.add(btnOk);
		bottom.add(btnCancel);
		dialog.add(bottom, BorderLayout.SOUTH);
		
		final ItemInfo[] selected = new ItemInfo[1];
		
		Runnable doSearch = () -> fillItemModel(txtItemName.getText(), model, table);
		Runnable doSelect = () -> {
			int row = table.getSelectedRow();
			if (row < 0)
				return;
			
			int modelRow = table.convertRowIndexToModel(row);
			int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
			selected[0] = getItem(id);
			dialog.dispose();
		};
		
		btnSearch.addActionListener(e -> doSearch.run());
		btnOk.addActionListener(e -> doSelect.run());
		btnCancel.addActionListener(e -> dialog.dispose());
		txtItemName.addActionListener(e -> doSearch.run());
		table.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				if (e.getClickCount() == 2)
					doSelect.run();
			}
		});
		
		txtItemName.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
		});
		
		if (initialQuery != null && !initialQuery.trim().isEmpty())
		{
			txtItemName.setText(initialQuery.trim());
			doSearch.run();
		}
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return selected[0];
	}
	
	private static void loadItemCache()
	{
		synchronized (CACHE_LOCK)
		{
			if (cacheLoaded)
				return;
			
			ITEM_CACHE.clear();
			for (File file : listXmlRecursively(ITEMS_ROOT))
				readItemsFile(file);
			
			cacheLoaded = true;
		}
	}
	
	private static void readItemsFile(File file)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);
			NodeList items = doc.getElementsByTagName("item");
			
			for (int i = 0; i < items.getLength(); i++)
			{
				Element item = (Element) items.item(i);
				String id = item.getAttribute("id");
				String name = item.getAttribute("name");
				
				if (id == null || id.isEmpty() || name == null || name.isEmpty())
					continue;
				
				int itemId = Integer.parseInt(id);
				ITEM_CACHE.put(itemId, new ItemInfo(itemId, name, file));
			}
		}
		catch (Exception e)
		{
			// Item XML invalido nao deve derrubar o painel de administracao.
		}
	}
	
	/* ================= UI ================= */
	
	public static void openSearchItem()
	{
		final List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2jdev_16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2jdev_32x32.png").getImage());
		
		JFrame frame = new JFrame("Search Item");
		frame.setSize(480, 320);
		frame.setMinimumSize(new Dimension(420, 260));
		frame.setLayout(new BorderLayout());
		frame.setIconImages(icons);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JLabel lblItemName = new JLabel("Item Name / ID:");
		JTextField txtItemName = new JTextField(22);
		JButton btnSearch = new JButton("Search");
		
		searchPanel.add(lblItemName);
		searchPanel.add(txtItemName);
		searchPanel.add(btnSearch);
		frame.add(searchPanel, BorderLayout.NORTH);
		
		DefaultTableModel model = createItemModel();
		JTable table = createItemTable(model);
		frame.add(new JScrollPane(table), BorderLayout.CENTER);
		
		Runnable doSearch = () -> fillItemModel(txtItemName.getText(), model, table);
		
		btnSearch.addActionListener(e -> doSearch.run());
		txtItemName.addActionListener(e -> doSearch.run());
		txtItemName.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				doSearch.run();
			}
		});
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private static DefaultTableModel createItemModel()
	{
		return new DefaultTableModel(new Object[]
		{
			"Item ID",
			"Item Name"
		}, 0)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
	}
	
	private static JTable createItemTable(DefaultTableModel model)
	{
		JTable table = new JTable(model);
		table.setRowHeight(22);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(0).setMaxWidth(95);
		table.getColumnModel().getColumn(1).setPreferredWidth(340);
		return table;
	}
	
	private static void fillItemModel(String query, DefaultTableModel model, JTable table)
	{
		model.setRowCount(0);
		
		for (ItemInfo item : findItems(query))
		{
			model.addRow(new Object[]
			{
				item.id,
				item.name
			});
		}
		
		if (model.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
	}
	
	private static List<File> listXmlRecursively(File root)
	{
		if (root == null || !root.exists())
			return Collections.emptyList();
		
		try (Stream<Path> walk = Files.walk(root.toPath()))
		{
			return walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml")).map(Path::toFile).collect(Collectors.toList());
		}
		catch (Exception e)
		{
			return Collections.emptyList();
		}
	}
}
