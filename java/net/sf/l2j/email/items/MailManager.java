package net.sf.l2j.email.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class MailManager
{
	private static final MailManager INSTANCE = new MailManager();
	private final Map<Integer, List<MailData>> _mails = new HashMap<>();

	public static MailManager getInstance()
	{
		return INSTANCE;
	}

	public void load()
	{
		_mails.clear();

		try (Connection con = ConnectionPool.getConnection();
		     PreparedStatement ps = con.prepareStatement("SELECT * FROM player_mail");
		     ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				MailData mail = new MailData(
					rs.getInt("id"),
					rs.getInt("senderId"),
					rs.getInt("receiverId"),
					rs.getInt("itemObjectId"),
					rs.getInt("itemId"),
					rs.getLong("itemCount"),
					rs.getInt("enchantLevel"),
					rs.getLong("expireTime"),
					rs.getBoolean("claimed"),
					rs.getBoolean("returned")
				);
				_mails.computeIfAbsent(mail.getReceiverId(), k -> new ArrayList<>()).add(mail);
			}
			System.out.println("MailManager: Loaded " + _mails.values().stream().mapToInt(List::size).sum() + " mails.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public boolean claimMail(Player player, MailData mail)
	{
	    if (mail == null || mail.isClaimed())
	        return false;

	    long count = mail.getItemCount();
	    if (count > Integer.MAX_VALUE)
	    {
	        player.sendMessage("Item count is too high.");
	        return false;
	    }

	    // Criar item
	    ItemInstance item = ItemTable.getInstance().createItem("MailClaim", mail.getItemId(), (int) count, player, null);
	    if (item == null)
	        return false;

	    item.setEnchantLevel(mail.getEnchantLevel());
	    player.getInventory().addItem("MailClaim", item, player, null);

	    // Marcar como coletado
	    mail.setClaimed(true);
	    updateMail(mail);

	    // Atualizar inventário do cliente
	    player.sendMessage("You received: " + item.getItem().getName() + (mail.getEnchantLevel() > 0 ? " +" + mail.getEnchantLevel() : "") + " x" + count);

	    return true;
	}

	public void updateMail(MailData mail)
	{
	    String sql = "UPDATE player_mail SET claimed = ? WHERE id = ?";

	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql))
	    {
	        ps.setBoolean(1, mail.isClaimed());
	        ps.setInt(2, mail.getId());

	        ps.executeUpdate();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}

	public List<MailData> getMailsForPlayer(int playerId)
	{
		return _mails.getOrDefault(playerId, Collections.emptyList());
	}

	public void storeMail(MailData mail)
	{
	    try (Connection con = ConnectionPool.getConnection();
	         PreparedStatement ps = con.prepareStatement(
	             "INSERT INTO player_mail (senderId, receiverId, itemObjectId, itemId, itemCount, enchantLevel, expireTime, claimed, returned) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
	    {
	        ps.setInt(1, mail.getSenderId());
	        ps.setInt(2, mail.getReceiverId());
	        ps.setInt(3, mail.getItemObjectId());
	        ps.setInt(4, mail.getItemId());
	        ps.setLong(5, mail.getItemCount());
	        ps.setInt(6, mail.getEnchantLevel());
	        ps.setLong(7, mail.getExpireTime());
	        ps.setBoolean(8, false);
	        ps.setBoolean(9, false);
	        ps.executeUpdate();

	        try (ResultSet rs = ps.getGeneratedKeys())
	        {
	            if (rs.next())
	            {
	                int id = rs.getInt(1);
	                MailData mailStored = new MailData(
	                    id, mail.getSenderId(), mail.getReceiverId(), mail.getItemObjectId(),
	                    mail.getItemId(), mail.getItemCount(), mail.getEnchantLevel(),
	                    mail.getExpireTime(), false, false
	                );
	                _mails.computeIfAbsent(mail.getReceiverId(), k -> new ArrayList<>()).add(mailStored);
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}


	public void checkExpirations()
	{
		long now = System.currentTimeMillis();

		for (List<MailData> mails : _mails.values())
		{
			for (MailData mail : mails)
			{
				if (!mail.isClaimed() && now > mail.getExpireTime())
				{
					// Devolve item
					MailData returned = new MailData(
						0,
						mail.getReceiverId(),
						mail.getSenderId(),
						mail.getItemObjectId(),
						mail.getItemId(),
						mail.getItemCount(),
						mail.getEnchantLevel(),
						now + TimeUnit.DAYS.toMillis(Config.MAIL_ITEM_EXPIRE_DAYS),
						false,
						true
					);
					storeMail(returned);
					mail.claimed = true; // evitar múltiplos retornos

					// Deletar/atualizar a entrada original no DB (não mostrado aqui)
				}
			}
		}
	}

	public MailData getMailById(int mailId)
	{
	    for (List<MailData> mailList : _mails.values())
	    {
	        for (MailData mail : mailList)
	        {
	            if (mail.getId() == mailId)
	                return mail;
	        }
	    }
	    return null; // não encontrado
	}

}
