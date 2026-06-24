package net.sf.l2j.dolls;

public class DollHolder 
{
	private int id;
	private int skillId;
	private int skillLvl;
	private double skillPower; // 👈 adiciona isso

	public DollHolder(int id, int skillId, int skillLvl,double skillPower) 
	{
		this.id = id;
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.skillPower = skillPower; // 👈 adiciona isso
	}

	public int getId() 
	{
		return id;
	}

	public int getSkillId()
	{
		return skillId;
	}

	public int getSkillLvl() 
	{
		return skillLvl;
	}
	public double getSkillPower() // 👈 adiciona getter
	{
		return skillPower;
	}
}