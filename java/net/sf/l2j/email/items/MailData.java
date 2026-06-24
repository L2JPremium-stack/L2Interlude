package net.sf.l2j.email.items;

public class MailData
{
    private final int id;
    private final int senderId;
    private final int receiverId;
    private final int itemObjectId;
    private final int itemId;
    private final long itemCount;
    private final int enchantLevel;
    private final long expireTime;
    public boolean claimed;
    private final boolean returned;

    public MailData(int id, int senderId, int receiverId, int itemObjectId, int itemId, long itemCount, int enchantLevel, long expireTime, boolean claimed, boolean returned)
    {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.itemObjectId = itemObjectId;
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.enchantLevel = enchantLevel;
        this.expireTime = expireTime;
        this.claimed = claimed;
        this.returned = returned;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public int getItemObjectId() { return itemObjectId; }
    public int getItemId() { return itemId; }
    public long getItemCount() { return itemCount; }
    public int getEnchantLevel() { return enchantLevel; }
    public long getExpireTime() { return expireTime; }
    public boolean isClaimed() { return claimed; }
    public boolean isReturned() { return returned; }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }
}


