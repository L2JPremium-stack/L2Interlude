package net.sf.l2j.gameserver.model.base;

public enum Sex
{
	MALE,
	FEMALE,
	ETC;
	
	public static Sex fromId(int id)
	{
		switch (id)
		{
			case 0:
				return MALE;
			case 1:
				return FEMALE;
			default:
				return ETC;
		}
	}
}
