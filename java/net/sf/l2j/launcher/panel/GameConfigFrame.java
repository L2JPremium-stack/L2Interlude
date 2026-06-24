package net.sf.l2j.launcher.panel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.l2j.Config;

public class GameConfigFrame extends JDialog
{
	
	private static final long serialVersionUID = 1L;
	private final File configRoot;
	private JTextArea editor;
	private JTree fileTree;
	private File currentFile;
	private boolean dirty = false;
	private String originalContent = "";
	private JButton btnSave;
	
	public GameConfigFrame(JFrame parent)
	{
		super(parent, "Game Server", true);
		
		configRoot = new File(".." + File.separator + "game" + File.separator + "config");
		
		if (!configRoot.exists())
		{
			JOptionPane.showMessageDialog(parent, "Pasta de configuração não encontrada:\n" + configRoot.getAbsolutePath(), "Erro", JOptionPane.ERROR_MESSAGE);
			dispose();
			return;
		}
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(900, 550));
		
		// ===== File tree =====
		DefaultMutableTreeNode rootNode = createNodes(configRoot);
		fileTree = new JTree(rootNode);
		fileTree.setRootVisible(true);
		fileTree.addTreeSelectionListener(new FileSelectionHandler());
		
		JScrollPane treeScroll = new JScrollPane(fileTree);
		treeScroll.setPreferredSize(new Dimension(280, 0));
		
		// ===== Editor =====
		editor = new JTextArea();
		editor.setFont(new Font("Monospaced", Font.PLAIN, 13));
		editor.setTabSize(4);
		
		JScrollPane editorScroll = new JScrollPane(editor);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, editorScroll);
		splitPane.setDividerLocation(280);
		
		add(splitPane, BorderLayout.CENTER);
		add(createBottomPanel(), BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(parent);
		
		editor.getDocument().addDocumentListener(new DocumentListener()
		{
			private void markDirty()
			{
				if (currentFile == null)
				{
					return;
				}
				
				boolean changed = !editor.getText().equals(originalContent);
				dirty = changed;
				btnSave.setEnabled(dirty);
			}
			
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				markDirty();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				markDirty();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				markDirty();
			}
		});
		editor.getInputMap(JTextArea.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control S"), "saveFile");
		
		editor.getActionMap().put("saveFile", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveFile(e);
			}
		});
	}
	
	private JPanel createBottomPanel()
	{
		JPanel panel = new JPanel();
		
		btnSave = new JButton("Salvar");
		btnSave.setEnabled(false);
		btnSave.addActionListener(this::saveFile);

		
		JButton btnReload = new JButton("Recarregar");
		btnReload.addActionListener(e -> loadCurrentFile());
		
		JButton btnOpenFolder = new JButton("Abrir pasta");
		btnOpenFolder.addActionListener(e -> openConfigFolder());
		
		panel.add(btnSave);
		panel.add(btnReload);
		panel.add(btnOpenFolder);
		
		return panel;
	}
	
	private void openConfigFolder()
	{
		try
		{
			Desktop.getDesktop().open(configRoot);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Não foi possível abrir a pasta", "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void saveFile(ActionEvent e)
	{
		if (currentFile == null || !dirty)
		{
			return;
		}
		
		int confirm = JOptionPane.showConfirmDialog(this, "Deseja salvar as alterações?", "Confirmar", JOptionPane.YES_NO_OPTION);
		
		if (confirm != JOptionPane.YES_OPTION)
		{
			return;
		}
		
		try
		{
			Files.write(currentFile.toPath(), editor.getText().getBytes(StandardCharsets.UTF_8));
			
			originalContent = editor.getText();
			dirty = false;
			btnSave.setEnabled(false);
			
			JOptionPane.showMessageDialog(this, "Salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
			Config.loadGameServer();
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo:\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadCurrentFile()
	{
		if (currentFile == null)
		{
			return;
		}
		
		try
		{
			List<String> lines = Files.readAllLines(currentFile.toPath(), StandardCharsets.UTF_8);
			originalContent = String.join("\n", lines);
			editor.setText(originalContent);
			editor.setCaretPosition(0);
			
			dirty = false;
			btnSave.setEnabled(false);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "Erro ao ler o arquivo", "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private DefaultMutableTreeNode createNodes(File file)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
		
		if (file.isDirectory())
		{
			File[] children = file.listFiles(f -> f.isDirectory() || f.getName().endsWith(".properties"));
			if (children != null)
			{
				for (File child : children)
				{
					node.add(createNodes(child));
				}
			}
		}
		return node;
	}
	
	private class FileSelectionHandler implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
			
			if (node == null)
			{
				return;
			}
			
			Object obj = node.getUserObject();
			if (obj instanceof File)
			{
				File file = (File) obj;
				
				if (!file.isFile())
				{
					return;
				}
				
				currentFile = file;
				loadCurrentFile();
			}
		}
	}
	
}
