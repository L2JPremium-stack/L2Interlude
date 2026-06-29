package net.sf.l2j.launcher.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import net.sf.l2j.launcher.drops.DropCategory;
import net.sf.l2j.launcher.drops.NpcDrop;
import net.sf.l2j.launcher.drops.NpcDropData;
import net.sf.l2j.launcher.drops.XmlUtil;
import net.sf.l2j.launcher.panel.ItemsSearchFrame.ItemInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DroplistFrame extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private static final int COL_KIND = 0;
	private static final int COL_CATEGORY = 1;
	private static final int COL_ITEM_ID = 2;
	private static final int COL_ITEM_NAME = 3;
	private static final int COL_MIN = 4;
	private static final int COL_MAX = 5;
	private static final int COL_CHANCE = 6;
	private static final int COL_CHANCE_PERCENT = 7;
	
	private static final long FULL_CHANCE = 1000000L;
	private static final Set<String> DROP_TYPES = new HashSet<>(Arrays.asList("L2Monster", "L2GrandBoss", "L2RaidBoss", "L2DungeonMob", "L2Chest", "L2FestivalMonster", "L2PenaltyMonster", "L2SepulcherMonster", "L2RiftInvader", "L2FeedableBeast", "L2FriendlyMob", "L2SoloFarm"));
	
	private static final Pattern DROPS_OPEN_PATTERN = Pattern.compile("<drops\\s*>", Pattern.CASE_INSENSITIVE);
	private static final String NPC_CLOSE = "</npc>";
	private static final String DROPS_CLOSE = "</drops>";
	
	private final File npcRoot = new File("../game/data/xml/npcs");
	
	private JTextField txtSearch;
	private JLabel lblNpc;
	private JLabel lblSummary;
	private JTable table;
	private DropTableModel model;
	
	private JButton btnAddDrop;
	private JButton btnAddSpoil;
	private JButton btnPickItem;
	private JButton btnDuplicate;
	private JButton btnRemove;
	private JButton btnSort;
	private JButton btnUp;
	private JButton btnDown;
	private JButton btnSave;
	private JButton btnReload;
	
	private boolean dirty = false;
	private boolean updatingModel = false;
	private String originalHash = "";
	private NpcDropData currentNpc;
	
	public DroplistFrame(JFrame parent)
	{
		super(parent, "NPC Droplist Editor", true);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(1050, 620));
		setMinimumSize(new Dimension(900, 520));
		
		add(createTop(), BorderLayout.NORTH);
		add(createCenter(), BorderLayout.CENTER);
		add(createBottom(), BorderLayout.SOUTH);
		registerShortcuts();
		updateButtonState();
		
		pack();
		setLocationRelativeTo(parent);
	}
	
	/* ================= UI ================= */
	
	private JPanel createTop()
	{
		JPanel wrapper = new JPanel(new BorderLayout());
		
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
		txtSearch = new JTextField(22);
		txtSearch.addActionListener(e -> searchNpc());
		
		JButton btnSearch = new JButton("Buscar");
		JButton btnClear = new JButton("Limpar");
		JButton btnOpenFolder = new JButton("Abrir pasta");
		lblNpc = new JLabel("Nenhum NPC carregado");
		
		btnSearch.addActionListener(e -> searchNpc());
		btnClear.addActionListener(e -> clearCurrentNpc());
		btnOpenFolder.addActionListener(e -> openNpcFolder());
		
		searchPanel.add(new JLabel("NPC ID / Nome:"));
		searchPanel.add(txtSearch);
		searchPanel.add(btnSearch);
		searchPanel.add(btnClear);
		searchPanel.add(btnOpenFolder);
		searchPanel.add(lblNpc);
		
		lblSummary = new JLabel("Categoria < 0 = SPOIL/SWEEP | Categoria >= 0 = DROP | Chance: 1000000 = 100%");
		lblSummary.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
		
		wrapper.add(searchPanel, BorderLayout.NORTH);
		wrapper.add(lblSummary, BorderLayout.SOUTH);
		return wrapper;
	}
	
	private JPanel createCenter()
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		model = new DropTableModel();
		model.addTableModelListener(new DropTableListener());
		
		table = new JTable(model);
		table.setRowHeight(24);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.setDefaultRenderer(Object.class, new DropCellRenderer());
		table.getSelectionModel().addListSelectionListener(e -> updateButtonState());
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int column = table.columnAtPoint(e.getPoint());
					if (column == COL_ITEM_ID || column == COL_ITEM_NAME)
						openItemPicker();
				}
			}
		});
		
		setupColumnWidths();
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createBottom()
	{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
		
		btnAddDrop = new JButton("Adicionar Drop");
		btnAddSpoil = new JButton("Adicionar Spoil");
		btnPickItem = new JButton("Buscar Item");
		btnDuplicate = new JButton("Duplicar");
		btnRemove = new JButton("Remover");
		btnSort = new JButton("Organizar");
		btnUp = new JButton("Subir");
		btnDown = new JButton("Descer");
		btnReload = new JButton("Recarregar");
		btnSave = new JButton("Salvar");
		
		btnAddDrop.addActionListener(e -> addBlankDrop(0));
		btnAddSpoil.addActionListener(e -> addBlankDrop(-1));
		btnPickItem.addActionListener(e -> openItemPicker());
		btnDuplicate.addActionListener(e -> duplicateSelectedRow());
		btnRemove.addActionListener(e -> removeSelectedRows());
		btnSort.addActionListener(e -> sortRows());
		btnUp.addActionListener(e -> moveSelectedRow(-1));
		btnDown.addActionListener(e -> moveSelectedRow(1));
		btnReload.addActionListener(e -> reloadNpc());
		btnSave.addActionListener(e -> saveDrops());
		
		p.add(btnAddDrop);
		p.add(btnAddSpoil);
		p.add(btnPickItem);
		p.add(btnDuplicate);
		p.add(btnRemove);
		p.add(btnSort);
		p.add(btnUp);
		p.add(btnDown);
		p.add(btnReload);
		p.add(btnSave);
		
		return p;
	}
	
	private void setupColumnWidths()
	{
		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(COL_KIND).setPreferredWidth(105);
		columns.getColumn(COL_KIND).setMaxWidth(130);
		columns.getColumn(COL_CATEGORY).setPreferredWidth(80);
		columns.getColumn(COL_CATEGORY).setMaxWidth(95);
		columns.getColumn(COL_ITEM_ID).setPreferredWidth(90);
		columns.getColumn(COL_ITEM_ID).setMaxWidth(105);
		columns.getColumn(COL_ITEM_NAME).setPreferredWidth(330);
		columns.getColumn(COL_MIN).setPreferredWidth(70);
		columns.getColumn(COL_MIN).setMaxWidth(90);
		columns.getColumn(COL_MAX).setPreferredWidth(70);
		columns.getColumn(COL_MAX).setMaxWidth(90);
		columns.getColumn(COL_CHANCE).setPreferredWidth(110);
		columns.getColumn(COL_CHANCE).setMaxWidth(130);
		columns.getColumn(COL_CHANCE_PERCENT).setPreferredWidth(95);
		columns.getColumn(COL_CHANCE_PERCENT).setMaxWidth(110);
	}
	
	/* ================= LOGIC ================= */
	
	private void searchNpc()
	{
		String query = txtSearch.getText().trim();
		if (query.isEmpty())
			return;
		
		if (!confirmDiscardDirty())
			return;
		
		List<NpcDropData> found = findNpcs(query);
		if (found.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "NPC nao encontrado nos XMLs de npcs.", "Buscar NPC", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		currentNpc = found.size() == 1 ? found.get(0) : chooseNpc(found);
		if (currentNpc == null)
			return;
		
		lblNpc.setText(currentNpc.npcId + " - " + currentNpc.npcName + " (" + safeText(currentNpc.npcType) + ")");
		loadTable();
		originalHash = computeHash();
		dirty = false;
		updateButtonState();
		updateSummary();
	}
	
	private void clearCurrentNpc()
	{
		if (!confirmDiscardDirty())
			return;
		
		currentNpc = null;
		updatingModel = true;
		model.setRowCount(0);
		updatingModel = false;
		lblNpc.setText("Nenhum NPC carregado");
		lblSummary.setText("Categoria < 0 = SPOIL/SWEEP | Categoria >= 0 = DROP | Chance: 1000000 = 100%");
		originalHash = "";
		dirty = false;
		updateButtonState();
	}
	
	private void loadTable()
	{
		updatingModel = true;
		model.setRowCount(0);
		
		for (DropCategory cat : currentNpc.categories)
		{
			for (NpcDrop d : cat.drops)
				addDropRow(cat.id, d);
		}
		
		refreshAllRows();
		updatingModel = false;
	}
	
	private void addBlankDrop(int categoryId)
	{
		if (currentNpc == null)
			return;
		
		addDropRow(categoryId, new NpcDrop(0, 1, 1, 1000));
		int row = model.getRowCount() - 1;
		refreshRow(row);
		selectModelRow(row);
		markDirty();
	}
	
	private void addDropRow(int categoryId, NpcDrop drop)
	{
		model.addRow(new Object[]
		{
			getKindLabel(categoryId),
			categoryId,
			drop.itemId,
			ItemsSearchFrame.getItemName(drop.itemId),
			drop.min,
			drop.max,
			drop.chance,
			formatChancePercent(drop.chance)
		});
	}
	
	private void openItemPicker()
	{
		if (currentNpc == null)
			return;
		
		int row = getSelectedModelRow();
		String initialQuery = row >= 0 ? String.valueOf(model.getValueAt(row, COL_ITEM_ID)) : "";
		ItemInfo item = ItemsSearchFrame.chooseItem(this, initialQuery);
		if (item == null)
			return;
		
		if (row >= 0)
		{
			int categoryId = parseIntOrDefault(model.getValueAt(row, COL_CATEGORY), 0);
			int duplicatedRow = findDropRow(categoryId, item.id, row);
			if (duplicatedRow >= 0)
			{
				selectModelRow(duplicatedRow);
				JOptionPane.showMessageDialog(this, "Esse item ja existe nessa categoria. Edite a linha existente.", "Item existente", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			model.setValueAt(item.id, row, COL_ITEM_ID);
			model.setValueAt(item.name, row, COL_ITEM_NAME);
			selectModelRow(row);
			markDirty();
			return;
		}
		
		addItemDrop(0, item);
	}
	
	private void addItemDrop(int categoryId, ItemInfo item)
	{
		int row = findDropRow(categoryId, item.id, -1);
		if (row >= 0)
		{
			selectModelRow(row);
			JOptionPane.showMessageDialog(this, "Esse item ja existe nessa categoria. Edite a linha existente.", "Item existente", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		addDropRow(categoryId, new NpcDrop(item.id, 1, 1, 1000));
		selectModelRow(model.getRowCount() - 1);
		markDirty();
	}
	
	private void duplicateSelectedRow()
	{
		int row = getSelectedModelRow();
		if (row < 0)
			return;
		
		model.addRow(new Object[]
		{
			model.getValueAt(row, COL_KIND),
			model.getValueAt(row, COL_CATEGORY),
			model.getValueAt(row, COL_ITEM_ID),
			model.getValueAt(row, COL_ITEM_NAME),
			model.getValueAt(row, COL_MIN),
			model.getValueAt(row, COL_MAX),
			model.getValueAt(row, COL_CHANCE),
			model.getValueAt(row, COL_CHANCE_PERCENT)
		});
		
		int newRow = model.getRowCount() - 1;
		selectModelRow(newRow);
		markDirty();
	}
	
	private void removeSelectedRows()
	{
		if (currentNpc == null)
			return;
		
		int[] selected = table.getSelectedRows();
		if (selected.length == 0)
			return;
		
		List<Integer> rows = new ArrayList<>();
		for (int viewRow : selected)
			rows.add(table.convertRowIndexToModel(viewRow));
		
		Collections.sort(rows, Collections.reverseOrder());
		for (int row : rows)
			model.removeRow(row);
		
		markDirty();
		updateSummary();
	}
	
	private void moveSelectedRow(int direction)
	{
		int row = getSelectedModelRow();
		if (row < 0)
			return;
		
		int target = row + direction;
		if (target < 0 || target >= model.getRowCount())
			return;
		
		model.moveRow(row, row, target);
		selectModelRow(target);
		markDirty();
	}
	
	private void sortRows()
	{
		if (currentNpc == null || model.getRowCount() <= 1)
			return;
		
		List<Object[]> rows = new ArrayList<>();
		for (int i = 0; i < model.getRowCount(); i++)
		{
			Object[] row = new Object[model.getColumnCount()];
			for (int j = 0; j < model.getColumnCount(); j++)
				row[j] = model.getValueAt(i, j);
			rows.add(row);
		}
		
		Collections.sort(rows, new Comparator<Object[]>()
		{
			@Override
			public int compare(Object[] a, Object[] b)
			{
				int catA = parseIntOrDefault(a[COL_CATEGORY], 0);
				int catB = parseIntOrDefault(b[COL_CATEGORY], 0);
				int groupA = catA < 0 ? 1 : 0;
				int groupB = catB < 0 ? 1 : 0;
				if (groupA != groupB)
					return Integer.compare(groupA, groupB);
				
				int catCompare = catA < 0 && catB < 0 ? Integer.compare(catB, catA) : Integer.compare(catA, catB);
				if (catCompare != 0)
					return catCompare;
				
				int itemA = parseIntOrDefault(a[COL_ITEM_ID], 0);
				int itemB = parseIntOrDefault(b[COL_ITEM_ID], 0);
				return Integer.compare(itemA, itemB);
			}
		});
		
		updatingModel = true;
		model.setRowCount(0);
		for (Object[] row : rows)
			model.addRow(row);
		refreshAllRows();
		updatingModel = false;
		markDirty();
		updateSummary();
	}
	
	private void saveDrops()
	{
		if (currentNpc == null)
			return;
		
		if (!dirty || computeHash().equals(originalHash))
		{
			JOptionPane.showMessageDialog(this, "Nenhuma alteracao para salvar.", "Salvar", JOptionPane.INFORMATION_MESSAGE);
			dirty = false;
			updateButtonState();
			return;
		}
		
		List<DropCategory> categories;
		try
		{
			categories = collectCategoriesFromTable();
		}
		catch (ValidationException e)
		{
			selectModelRow(e.row);
			JOptionPane.showMessageDialog(this, e.getMessage(), "Valor invalido", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		currentNpc.categories = categories;
		
		try
		{
			saveNpcDrops(currentNpc);
			currentNpc.hadDropsBlock = !isDropsEmpty(currentNpc);
			originalHash = computeHash();
			dirty = false;
			updateButtonState();
			updateSummary();
			JOptionPane.showMessageDialog(this, "Droplist salva com sucesso!", "Salvar", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (RuntimeException e)
		{
			JOptionPane.showMessageDialog(this, "Erro ao salvar droplist:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private List<DropCategory> collectCategoriesFromTable() throws ValidationException
	{
		Map<Integer, DropCategory> categories = new LinkedHashMap<>();
		Set<String> uniqueKeys = new HashSet<>();
		
		for (int i = 0; i < model.getRowCount(); i++)
		{
			int categoryId = readInt(i, COL_CATEGORY, "Categoria");
			int itemId = readInt(i, COL_ITEM_ID, "Item ID");
			int min = readInt(i, COL_MIN, "Min");
			int max = readInt(i, COL_MAX, "Max");
			long chance = readLong(i, COL_CHANCE, "Chance");
			
			if (itemId <= 0)
				throw new ValidationException(i, "Item ID deve ser maior que zero.");
			if (min <= 0)
				throw new ValidationException(i, "Min deve ser maior que zero.");
			if (max < min)
				throw new ValidationException(i, "Max nao pode ser menor que Min.");
			if (chance <= 0)
				throw new ValidationException(i, "Chance deve ser maior que zero.");
			
			String key = categoryId + "#" + itemId;
			if (!uniqueKeys.add(key))
				throw new ValidationException(i, "Item repetido na mesma categoria. Use apenas uma linha e altere os valores existentes.");
			
			DropCategory category = categories.get(categoryId);
			if (category == null)
			{
				category = new DropCategory(categoryId);
				categories.put(categoryId, category);
			}
			category.drops.add(new NpcDrop(itemId, min, max, chance));
		}
		
		return new ArrayList<>(categories.values());
	}
	
	private void reloadNpc()
	{
		if (currentNpc == null)
			return;
		
		if (!confirmDiscardDirty())
			return;
		
		List<NpcDropData> list = findNpcs(String.valueOf(currentNpc.npcId));
		if (!list.isEmpty())
		{
			currentNpc = list.get(0);
			lblNpc.setText(currentNpc.npcId + " - " + currentNpc.npcName + " (" + safeText(currentNpc.npcType) + ")");
			loadTable();
			originalHash = computeHash();
			dirty = false;
			updateButtonState();
			updateSummary();
		}
	}
	
	private boolean confirmDiscardDirty()
	{
		if (!dirty)
			return true;
		
		int r = JOptionPane.showConfirmDialog(this, "Existem alteracoes nao salvas.\nDeseja descarta-las?", "Confirmar", JOptionPane.YES_NO_OPTION);
		return r == JOptionPane.YES_OPTION;
	}
	
	private void openNpcFolder()
	{
		try
		{
			File target = currentNpc == null ? npcRoot : currentNpc.sourceFile.getParentFile();
			Desktop.getDesktop().open(target);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Nao foi possivel abrir a pasta.", "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void markDirty()
	{
		if (currentNpc == null || updatingModel)
			return;
		
		dirty = true;
		updateButtonState();
		updateSummary();
	}
	
	private void updateButtonState()
	{
		boolean hasNpc = currentNpc != null;
		boolean hasRow = hasNpc && table != null && table.getSelectedRow() >= 0;
		
		if (btnAddDrop != null)
			btnAddDrop.setEnabled(hasNpc);
		if (btnAddSpoil != null)
			btnAddSpoil.setEnabled(hasNpc);
		if (btnPickItem != null)
			btnPickItem.setEnabled(hasNpc);
		if (btnDuplicate != null)
			btnDuplicate.setEnabled(hasRow);
		if (btnRemove != null)
			btnRemove.setEnabled(hasRow);
		if (btnSort != null)
			btnSort.setEnabled(hasNpc && model.getRowCount() > 1);
		if (btnUp != null)
			btnUp.setEnabled(hasRow && getSelectedModelRow() > 0);
		if (btnDown != null)
			btnDown.setEnabled(hasRow && getSelectedModelRow() < model.getRowCount() - 1);
		if (btnReload != null)
			btnReload.setEnabled(hasNpc);
		if (btnSave != null)
			btnSave.setEnabled(hasNpc && dirty);
	}
	
	private void updateSummary()
	{
		if (currentNpc == null)
			return;
		
		int drop = 0;
		int spoil = 0;
		for (int i = 0; i < model.getRowCount(); i++)
		{
			int category = parseIntOrDefault(model.getValueAt(i, COL_CATEGORY), 0);
			if (category < 0)
				spoil++;
			else
				drop++;
		}
		
		String fileName = currentNpc.sourceFile == null ? "" : currentNpc.sourceFile.getName();
		lblSummary.setText("DROP: " + drop + " | SPOIL/SWEEP: " + spoil + " | Categoria < 0 = spoil | Categoria >= 0 = drop | Chance 1000000 = 100% | " + fileName);
	}
	
	private String computeHash()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < model.getRowCount(); i++)
		{
			sb.append(model.getValueAt(i, COL_CATEGORY)).append('|');
			sb.append(model.getValueAt(i, COL_ITEM_ID)).append('|');
			sb.append(model.getValueAt(i, COL_MIN)).append('|');
			sb.append(model.getValueAt(i, COL_MAX)).append('|');
			sb.append(model.getValueAt(i, COL_CHANCE)).append('|');
		}
		return Integer.toHexString(sb.toString().hashCode());
	}
	
	private int getSelectedModelRow()
	{
		if (table == null || table.getSelectedRow() < 0)
			return -1;
		
		return table.convertRowIndexToModel(table.getSelectedRow());
	}
	
	private void selectModelRow(int modelRow)
	{
		if (modelRow < 0 || modelRow >= model.getRowCount())
			return;
		
		int viewRow = table.convertRowIndexToView(modelRow);
		table.setRowSelectionInterval(viewRow, viewRow);
		table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
	}
	
	private int findDropRow(int categoryId, int itemId, int ignoreRow)
	{
		for (int i = 0; i < model.getRowCount(); i++)
		{
			if (i == ignoreRow)
				continue;
			
			int rowCategory = parseIntOrDefault(model.getValueAt(i, COL_CATEGORY), Integer.MIN_VALUE);
			int rowItemId = parseIntOrDefault(model.getValueAt(i, COL_ITEM_ID), Integer.MIN_VALUE);
			if (rowCategory == categoryId && rowItemId == itemId)
				return i;
		}
		return -1;
	}
	
	private void refreshAllRows()
	{
		for (int i = 0; i < model.getRowCount(); i++)
			refreshRow(i);
	}
	
	private void refreshRow(int row)
	{
		if (row < 0 || row >= model.getRowCount())
			return;
		
		boolean oldUpdating = updatingModel;
		updatingModel = true;
		
		int categoryId = parseIntOrDefault(model.getValueAt(row, COL_CATEGORY), 0);
		int itemId = parseIntOrDefault(model.getValueAt(row, COL_ITEM_ID), 0);
		long chance = parseLongOrDefault(model.getValueAt(row, COL_CHANCE), 0);
		
		model.setValueAt(getKindLabel(categoryId), row, COL_KIND);
		model.setValueAt(ItemsSearchFrame.getItemName(itemId), row, COL_ITEM_NAME);
		model.setValueAt(formatChancePercent(chance), row, COL_CHANCE_PERCENT);
		
		updatingModel = oldUpdating;
	}
	
	private static String getKindLabel(int categoryId)
	{
		return categoryId < 0 ? "SPOIL / SWEEP" : "DROP";
	}
	
	private static String formatChancePercent(long chance)
	{
		if (chance <= 0)
			return "";
		
		return String.format(java.util.Locale.US, "%.4f%%", chance * 100.0 / FULL_CHANCE);
	}
	
	/* ================= NPC SEARCH ================= */
	
	private List<NpcDropData> findNpcs(String query)
	{
		List<NpcDropData> result = new ArrayList<>();
		String q = query.toLowerCase();
		boolean searchById = query.matches("\\d+");
		
		for (File f : listXmlRecursively(npcRoot))
		{
			try
			{
				Document doc = XmlUtil.load(f);
				NodeList npcs = doc.getElementsByTagName("npc");
				
				for (int i = 0; i < npcs.getLength(); i++)
				{
					Element npc = (Element) npcs.item(i);
					String id = npc.getAttribute("id");
					String name = npc.getAttribute("name");
					
					if (id == null || id.isEmpty())
						continue;
					
					boolean match = searchById ? id.equals(query) : name.toLowerCase().contains(q);
					if (!match)
						continue;
					
					String type = getNpcType(npc);
					boolean hasDrops = hasDirectChild(npc, "drops");
					if (searchById || hasDrops || isDropNpcType(type))
						result.add(parseNpc(npc, doc, f, type, hasDrops));
				}
			}
			catch (RuntimeException e)
			{
				// Ignora XML invalido para nao quebrar a busca inteira.
			}
		}
		
		Collections.sort(result, Comparator.comparingInt(d -> d.npcId));
		return result;
	}
	
	private NpcDropData chooseNpc(List<NpcDropData> list)
	{
		DefaultTableModel chooseModel = new DefaultTableModel(new Object[]
		{
			"ID",
			"Nome",
			"Tipo",
			"Drops",
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
			chooseModel.addRow(new Object[]
			{
				d.npcId,
				d.npcName,
				safeText(d.npcType),
				d.getDropCount(),
				d.sourceFile.getName()
			});
		}
		
		JTable chooseTable = new JTable(chooseModel);
		chooseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chooseTable.setRowSelectionAllowed(true);
		chooseTable.getTableHeader().setReorderingAllowed(false);
		if (chooseModel.getRowCount() > 0)
			chooseTable.setRowSelectionInterval(0, 0);
		
		JLabel hint = new JLabel("Selecione o NPC correto (duplo clique ou OK)");
		hint.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(hint, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(chooseTable);
		scroll.setPreferredSize(new Dimension(720, 320));
		panel.add(scroll, BorderLayout.CENTER);
		
		final JDialog dialog = new JDialog(this, "Selecione o NPC", true);
		dialog.setLayout(new BorderLayout());
		dialog.add(panel, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		JButton ok = new JButton("Selecionar");
		JButton cancel = new JButton("Cancelar");
		buttons.add(ok);
		buttons.add(cancel);
		dialog.add(buttons, BorderLayout.SOUTH);
		
		final NpcDropData[] selected = new NpcDropData[1];
		Runnable select = () -> {
			int row = chooseTable.getSelectedRow();
			if (row >= 0)
			{
				selected[0] = list.get(chooseTable.convertRowIndexToModel(row));
				dialog.dispose();
			}
		};
		
		ok.addActionListener(e -> select.run());
		cancel.addActionListener(e -> dialog.dispose());
		chooseTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
					select.run();
			}
		});
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		return selected[0];
	}
	
	private static boolean isDropNpcType(String type)
	{
		if (type == null || type.isEmpty())
			return false;
		
		return DROP_TYPES.contains(type) || type.contains("Monster") || type.contains("Boss") || type.contains("Raid") || type.contains("Chest") || type.contains("Farm") || type.contains("Invader");
	}
	
	private static String getNpcType(Element npc)
	{
		for (Element set : childElements(npc, "set"))
		{
			if ("type".equalsIgnoreCase(set.getAttribute("name")))
				return set.getAttribute("val");
		}
		return "";
	}
	
	private static boolean hasDirectChild(Element parent, String name)
	{
		return !childElements(parent, name).isEmpty();
	}
	
	private static List<Element> childElements(Element parent, String name)
	{
		List<Element> result = new ArrayList<>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (node instanceof Element && name.equals(node.getNodeName()))
				result.add((Element) node);
		}
		return result;
	}
	
	private static NpcDropData parseNpc(Element npc, Document doc, File file, String type, boolean hasDrops)
	{
		NpcDropData data = new NpcDropData();
		data.npcId = Integer.parseInt(npc.getAttribute("id"));
		data.npcName = npc.getAttribute("name");
		data.npcType = type;
		data.sourceFile = file;
		data.document = doc;
		data.npcElement = npc;
		data.hadDropsBlock = hasDrops;
		data.categories = new ArrayList<>();
		
		List<Element> dropsList = childElements(npc, "drops");
		if (!dropsList.isEmpty())
		{
			Element dropsElement = dropsList.get(0);
			data.dropsElement = dropsElement;
			
			for (Element c : childElements(dropsElement, "category"))
			{
				DropCategory cat = new DropCategory(Integer.parseInt(c.getAttribute("id")));
				for (Element d : childElements(c, "drop"))
				{
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
	
	/* ================= XML ================= */
	
	private static void saveNpcDrops(NpcDropData data)
	{
		try
		{
			String xml = new String(Files.readAllBytes(data.sourceFile.toPath()), StandardCharsets.UTF_8);
			NpcBlock npcBlock = findNpcBlock(xml, data.npcId);
			if (npcBlock == null)
				throw new RuntimeException("NPC nao encontrado no XML.");
			
			Range oldDrops = findDropsRange(xml, npcBlock);
			if (isDropsEmpty(data))
			{
				if (oldDrops != null)
					xml = removeRangeWithLine(xml, oldDrops);
			}
			else
			{
				String nl = detectLineSeparator(xml);
				String prefix = oldDrops == null ? detectInnerIndent(npcBlock.text) : extractLinePrefix(oldDrops.text);
				String newDrops = buildDropsBlock(data.categories, prefix, nl);
				
				if (oldDrops != null)
					xml = xml.substring(0, oldDrops.start) + newDrops + xml.substring(oldDrops.end);
				else
					xml = insertDropsBlock(xml, npcBlock, newDrops, nl);
			}
			
			Files.write(data.sourceFile.toPath(), xml.getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
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
	
	private static NpcBlock findNpcBlock(String xml, int npcId)
	{
		Pattern pattern = Pattern.compile("<npc\\b[^>]*\\bid\\s*=\\s*\"" + npcId + "\"[^>]*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(xml);
		if (!matcher.find())
			return null;
		
		int closeStart = indexOfIgnoreCase(xml, NPC_CLOSE, matcher.end());
		if (closeStart < 0)
			throw new RuntimeException("Fechamento </npc> nao encontrado para NPC " + npcId + ".");
		
		int start = matcher.start();
		int end = closeStart + NPC_CLOSE.length();
		return new NpcBlock(start, xml.substring(start, end));
	}
	
	private static Range findDropsRange(String xml, NpcBlock npcBlock)
	{
		Matcher matcher = DROPS_OPEN_PATTERN.matcher(npcBlock.text);
		if (!matcher.find())
			return null;
		
		int closeStart = indexOfIgnoreCase(npcBlock.text, DROPS_CLOSE, matcher.end());
		if (closeStart < 0)
			throw new RuntimeException("Fechamento </drops> nao encontrado para NPC.");
		
		int lineStart = npcBlock.text.lastIndexOf('\n', matcher.start());
		lineStart = lineStart < 0 ? matcher.start() : lineStart + 1;
		
		int closeEnd = closeStart + DROPS_CLOSE.length();
		int absoluteStart = npcBlock.start + lineStart;
		int absoluteEnd = npcBlock.start + closeEnd;
		return new Range(absoluteStart, absoluteEnd, xml.substring(absoluteStart, absoluteEnd));
	}
	
	private static String insertDropsBlock(String xml, NpcBlock npcBlock, String newDrops, String nl)
	{
		InsertionPoint point = findInsertionPoint(npcBlock.text);
		int absoluteOffset = npcBlock.start + point.offset;
		String insertText = point.leadingNewLine ? nl + newDrops : newDrops + nl;
		return xml.substring(0, absoluteOffset) + insertText + xml.substring(absoluteOffset);
	}
	
	private static InsertionPoint findInsertionPoint(String npcBlock)
	{
		int offset = endOfLastIgnoreCase(npcBlock, "</skills>");
		if (offset >= 0)
			return new InsertionPoint(offset, true);
		
		offset = endOfLastSelfClosingElement(npcBlock, "ai");
		if (offset >= 0)
			return new InsertionPoint(offset, true);
		
		offset = endOfLastIgnoreCase(npcBlock, "</ai>");
		if (offset >= 0)
			return new InsertionPoint(offset, true);
		
		offset = endOfLastSelfClosingElement(npcBlock, "set");
		if (offset >= 0)
			return new InsertionPoint(offset, true);
		
		int close = indexOfIgnoreCase(npcBlock, NPC_CLOSE, 0);
		if (close < 0)
			throw new RuntimeException("Fechamento </npc> nao encontrado.");
		
		int lineStart = npcBlock.lastIndexOf('\n', close);
		return lineStart < 0 ? new InsertionPoint(close, true) : new InsertionPoint(lineStart + 1, false);
	}
	
	private static int endOfLastIgnoreCase(String text, String token)
	{
		int index = lastIndexOfIgnoreCase(text, token);
		return index < 0 ? -1 : index + token.length();
	}
	
	private static int endOfLastSelfClosingElement(String text, String elementName)
	{
		Pattern pattern = Pattern.compile("<" + elementName + "\\b[^>]*/>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		int end = -1;
		while (matcher.find())
			end = matcher.end();
		return end;
	}
	
	private static String removeRangeWithLine(String xml, Range range)
	{
		int end = range.end;
		if (xml.startsWith("\r\n", end))
			end += 2;
		else if (xml.startsWith("\n", end))
			end++;
		
		return xml.substring(0, range.start) + xml.substring(end);
	}
	
	private static String buildDropsBlock(List<DropCategory> categories, String prefix, String nl)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("<drops>").append(nl);
		
		for (DropCategory cat : categories)
		{
			if (cat.drops == null || cat.drops.isEmpty())
				continue;
			
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
	
	private static String extractLinePrefix(String dropsBlock)
	{
		Matcher matcher = DROPS_OPEN_PATTERN.matcher(dropsBlock);
		return matcher.find() ? dropsBlock.substring(0, matcher.start()) : "\t\t";
	}
	
	private static String detectInnerIndent(String npcBlock)
	{
		Matcher matcher = Pattern.compile("(?:\\r?\\n)([\\t ]+)<").matcher(npcBlock);
		if (matcher.find())
			return matcher.group(1);
		
		return "\t\t";
	}
	
	private static String detectLineSeparator(String text)
	{
		return text.contains("\r\n") ? "\r\n" : "\n";
	}
	
	private static int indexOfIgnoreCase(String text, String token, int fromIndex)
	{
		return text.toLowerCase().indexOf(token.toLowerCase(), fromIndex);
	}
	
	private static int lastIndexOfIgnoreCase(String text, String token)
	{
		return text.toLowerCase().lastIndexOf(token.toLowerCase());
	}
	
	/* ================= VALIDATION / HELPERS ================= */
	
	private int readInt(int row, int column, String name) throws ValidationException
	{
		try
		{
			return Integer.parseInt(String.valueOf(model.getValueAt(row, column)).trim());
		}
		catch (Exception e)
		{
			throw new ValidationException(row, name + " deve ser um numero inteiro.");
		}
	}
	
	private long readLong(int row, int column, String name) throws ValidationException
	{
		try
		{
			return Long.parseLong(String.valueOf(model.getValueAt(row, column)).trim());
		}
		catch (Exception e)
		{
			throw new ValidationException(row, name + " deve ser um numero inteiro.");
		}
	}
	
	private static int parseIntOrDefault(Object value, int defaultValue)
	{
		try
		{
			return Integer.parseInt(String.valueOf(value).trim());
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	private static long parseLongOrDefault(Object value, long defaultValue)
	{
		try
		{
			return Long.parseLong(String.valueOf(value).trim());
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	private static String safeText(String text)
	{
		return text == null || text.isEmpty() ? "tipo nao informado" : text;
	}
	
	private void registerShortcuts()
	{
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "remove");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "focusSearch");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control I"), "pickItem");
		
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
		
		getRootPane().getActionMap().put("remove", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (btnRemove.isEnabled())
					removeSelectedRows();
			}
		});
		
		getRootPane().getActionMap().put("focusSearch", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				txtSearch.requestFocusInWindow();
				txtSearch.selectAll();
			}
		});
		
		getRootPane().getActionMap().put("pickItem", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (btnPickItem.isEnabled())
					openItemPicker();
			}
		});
	}
	
	private final class DropTableListener implements TableModelListener
	{
		@Override
		public void tableChanged(TableModelEvent e)
		{
			if (updatingModel || currentNpc == null)
				return;
			
			if (e.getType() != TableModelEvent.DELETE && e.getFirstRow() >= 0)
			{
				int lastRow = Math.min(e.getLastRow(), model.getRowCount() - 1);
				for (int row = e.getFirstRow(); row <= lastRow; row++)
					refreshRow(row);
			}
			
			markDirty();
		}
	}
	
	private static final class DropTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;
		
		private DropTableModel()
		{
			super(new Object[]
			{
				"Tipo",
				"Categoria",
				"Item ID",
				"Item Nome",
				"Min",
				"Max",
				"Chance",
				"Chance %"
			}, 0);
		}
		
		@Override
		public boolean isCellEditable(int row, int column)
		{
			return column == COL_CATEGORY || column == COL_ITEM_ID || column == COL_MIN || column == COL_MAX || column == COL_CHANCE;
		}
	}
	
	private final class DropCellRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;
		
		private final Color dropBg = new Color(28, 30, 33);
		private final Color dropAltBg = new Color(24, 26, 29);
		private final Color spoilBg = new Color(43, 34, 26);
		private final Color invalidBg = new Color(70, 32, 36);
		private final Color fg = new Color(235, 235, 235);
		private final Color mutedFg = new Color(190, 195, 205);
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			int modelRow = table.convertRowIndexToModel(row);
			int category = parseIntOrDefault(model.getValueAt(modelRow, COL_CATEGORY), 0);
			int itemId = parseIntOrDefault(model.getValueAt(modelRow, COL_ITEM_ID), 0);
			
			if (!isSelected)
			{
				if (itemId <= 0)
					c.setBackground(invalidBg);
				else if (category < 0)
					c.setBackground(spoilBg);
				else
					c.setBackground(row % 2 == 0 ? dropBg : dropAltBg);
				
				c.setForeground(column == COL_ITEM_NAME || column == COL_CHANCE_PERCENT ? mutedFg : fg);
			}
			
			setFont(column == COL_KIND ? getFont().deriveFont(Font.BOLD) : getFont().deriveFont(Font.PLAIN));
			return c;
		}
	}
	
	private static final class ValidationException extends Exception
	{
		private static final long serialVersionUID = 1L;
		private final int row;
		
		private ValidationException(int row, String message)
		{
			super(message);
			this.row = row;
		}
	}
	
	private static final class NpcBlock
	{
		private final int start;
		private final String text;
		
		private NpcBlock(int start, String text)
		{
			this.start = start;
			this.text = text;
		}
	}
	
	private static final class Range
	{
		private final int start;
		private final int end;
		private final String text;
		
		private Range(int start, int end, String text)
		{
			this.start = start;
			this.end = end;
			this.text = text;
		}
	}
	
	private static final class InsertionPoint
	{
		private final int offset;
		private final boolean leadingNewLine;
		
		private InsertionPoint(int offset, boolean leadingNewLine)
		{
			this.offset = offset;
			this.leadingNewLine = leadingNewLine;
		}
	}
}
