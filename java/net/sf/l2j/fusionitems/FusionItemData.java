package net.sf.l2j.fusionitems;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2FusionItemsInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.util.Broadcast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FusionItemData
{
	private static final Logger _log = Logger.getLogger(FusionItemData.class.getName());
	
	private final List<FusionRecipe> _recipes = new ArrayList<>();
	private final Map<Category, List<Integer>> _indexByCategory = new EnumMap<>(Category.class);
	
	public enum Category
	{
		WEAPONS,
		ARMOR,
		JEWELS,
		ACCESSORIES;
		
		static Category parse(String s)
		{
			if (s == null)
				return WEAPONS;
			final String v = s.trim().toLowerCase(Locale.ROOT);
			if (v.equals("weapons") || v.equals("weapon"))
				return WEAPONS;
			if (v.equals("armor") || v.equals("armors"))
				return ARMOR;
			if (v.equals("jewels") || v.equals("jewel"))
				return JEWELS;
			if (v.equals("acessorios") || v.equals("acessórios") || v.equals("accessories") || v.equals("accessory"))
				return ACCESSORIES;
			return WEAPONS;
		}
		
		public String display()
		{
			switch (this)
			{
				case WEAPONS:
					return "Weapons";
				case ARMOR:
					return "Armor";
				case JEWELS:
					return "Jewels";
				case ACCESSORIES:
					return "Acessórios";
				default:
					return name();
			}
		}
	}
	
	// Ingrediente
	public static class FusionIngredient
	{
		public final int itemId;
		public final int count;
		
		public FusionIngredient(int itemId, int count)
		{
			this.itemId = itemId;
			this.count = count;
		}
	}
	
	// Receita
	public static class FusionRecipe
	{
		public int resultId, resultCount;
		public int chance;
		public int failItemId, failItemCount;
		public final List<FusionIngredient> ingredients = new ArrayList<>();
		public Category category = Category.WEAPONS;
	}
	
	private FusionItemData()
	{
		load();
	}
	
	// Recarrega o FusionItems.xml. Thread-safe: sincroniza para evitar leituras durante a troca.
	public synchronized void reload()
	{
		_log.info("FusionItemData: reloading...");
		load();
		_log.info("FusionItemData: reload finished. " + getStatsString());
	}
	
	// Retorna um resumo com estatísticas atuais (útil para logs/admin).
	public synchronized String getStatsString()
	{
		return "Total=" + _recipes.size() + " (Weapons=" + _indexByCategory.getOrDefault(Category.WEAPONS, Collections.emptyList()).size() + ", Armor=" + _indexByCategory.getOrDefault(Category.ARMOR, Collections.emptyList()).size() + ", Jewels=" + _indexByCategory.getOrDefault(Category.JEWELS, Collections.emptyList()).size() + ", Acessórios=" + _indexByCategory.getOrDefault(Category.ACCESSORIES, Collections.emptyList()).size() + ")";
	}
	
	// Carrega do XML. Sincronizado para evitar leituras intermediárias.
	public synchronized void load()
	{
		_recipes.clear();
		_indexByCategory.clear();
		for (Category c : Category.values())
			_indexByCategory.put(c, new ArrayList<>());
		
		try
		{
			File file = new File("./data/xml/custom/FusionItems.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList nodes = doc.getElementsByTagName("fusion");
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node fusionNode = nodes.item(i);
				if (fusionNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				
				FusionRecipe recipe = new FusionRecipe();
				
				// categoria no atributo do nó <fusion category="...">
				Node catAttr = fusionNode.getAttributes() != null ? fusionNode.getAttributes().getNamedItem("category") : null;
				recipe.category = Category.parse(catAttr != null ? catAttr.getNodeValue() : null);
				
				for (Node subNode = fusionNode.getFirstChild(); subNode != null; subNode = subNode.getNextSibling())
				{
					if (subNode.getNodeType() != Node.ELEMENT_NODE)
						continue;
					
					switch (subNode.getNodeName())
					{
						case "result":
							recipe.resultId = Integer.parseInt(subNode.getAttributes().getNamedItem("id").getNodeValue());
							recipe.resultCount = Integer.parseInt(subNode.getAttributes().getNamedItem("count").getNodeValue());
							break;
						
						case "chance":
							recipe.chance = Integer.parseInt(subNode.getTextContent().trim());
							break;
						
						case "failItem":
							recipe.failItemId = Integer.parseInt(subNode.getAttributes().getNamedItem("id").getNodeValue());
							recipe.failItemCount = Integer.parseInt(subNode.getAttributes().getNamedItem("count").getNodeValue());
							break;
						
						case "ingredients":
							for (Node itemNode = subNode.getFirstChild(); itemNode != null; itemNode = itemNode.getNextSibling())
							{
								if (itemNode.getNodeType() != Node.ELEMENT_NODE)
									continue;
								
								int itemId = Integer.parseInt(itemNode.getAttributes().getNamedItem("id").getNodeValue());
								int count = Integer.parseInt(itemNode.getAttributes().getNamedItem("count").getNodeValue());
								recipe.ingredients.add(new FusionIngredient(itemId, count));
							}
							break;
					}
				}
				
				int globalIndex = _recipes.size();
				_recipes.add(recipe);
				_indexByCategory.get(recipe.category).add(globalIndex);
			}
			
			_log.info("FusionItemData: carregadas " + getStatsString());
		}
		catch (Exception e)
		{
			_log.warning("Erro ao carregar FusionItems.xml: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized List<FusionRecipe> getRecipes()
	{
		return Collections.unmodifiableList(_recipes);
	}
	
	public synchronized List<Integer> getIndexesByCategory(Category cat)
	{
		return Collections.unmodifiableList(_indexByCategory.get(cat));
	}
	
	public synchronized FusionRecipe getRecipe(int index)
	{
		if (index < 0 || index >= _recipes.size())
			return null;
		return _recipes.get(index);
	}
	
	public boolean tryFusion(Player player, L2Npc npc, int recipeIndex)
	{
		// leitura não altera estado — acesso rápido fora do synchronized para não travar GS
		FusionRecipe recipe;
		synchronized (this)
		{
			if (recipeIndex < 0 || recipeIndex >= _recipes.size())
				return false;
			recipe = _recipes.get(recipeIndex);
		}
		
		// Verifica itens necessários (desconsidera equipados)
		for (FusionIngredient ing : recipe.ingredients)
		{
			int totalCount = 0;
			List<ItemInstance> possibleItems = player.getInventory().getItemsByItemId(ing.itemId);
			for (ItemInstance item : possibleItems)
			{
				if (!item.isEquipped())
					totalCount += item.getCount();
			}
			if (totalCount < ing.count)
				return false;
		}
		
		// Item base (usa o equipado se existir; senão, maior enchant disponível)
		ItemInstance baseItem = null;
		for (FusionIngredient ing : recipe.ingredients)
		{
			ItemInstance equippedItem = player.getInventory().getPaperdollItemByL2ItemId(ing.itemId);
			if (equippedItem != null)
			{
				baseItem = equippedItem;
				break;
			}
		}
		if (baseItem == null)
		{
			int maxEnchant = -1;
			for (FusionIngredient ing : recipe.ingredients)
			{
				for (ItemInstance item : player.getInventory().getItemsByItemId(ing.itemId))
				{
					if (!item.isEquipped() && item.getEnchantLevel() > maxEnchant)
					{
						maxEnchant = item.getEnchantLevel();
						baseItem = item;
					}
				}
			}
		}
		if (baseItem == null)
			return false;
		
		int baseEnchant = baseItem.getEnchantLevel();
		boolean success = Rnd.get(100) < recipe.chance;
		
		if (success)
		{
			ItemInstance newItem = player.addItem("Fusion", recipe.resultId, recipe.resultCount, npc, true);
			if (baseEnchant > 0)
			{
				newItem.setEnchantLevel(baseEnchant);
				newItem.updateDatabase();
			}
			newItem = player.getInventory().getItemByObjectId(newItem.getObjectId());
			
			String enchantText = newItem.getEnchantLevel() > 0 ? " +" + newItem.getEnchantLevel() : "";
			Broadcast.gameAnnounceToOnlinePlayers(player.getName() + " fused " + L2FusionItemsInstance.getItemName(newItem.getItemId()) + enchantText + "!");
			
			// Consome ingredientes (não equipados)
			for (FusionIngredient ing : recipe.ingredients)
			{
				int toDestroy = ing.count;
				for (ItemInstance item : player.getInventory().getItemsByItemId(ing.itemId))
				{
					if (toDestroy <= 0)
						break;
					if (!item.isEquipped())
					{
						int countToDestroy = Math.min(toDestroy, item.getCount());
						player.destroyItem("Fusion", item, countToDestroy, npc, true);
						toDestroy -= countToDestroy;
					}
				}
			}
		}
		else
		{
			player.addItem("Fusion", recipe.failItemId, recipe.failItemCount, npc, true);
		}
		return true;
	}
	
	public static FusionItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FusionItemData _instance = new FusionItemData();
	}
}
