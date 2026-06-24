package net.sf.l2j.event.championInvade;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Sarada
 *
 */
public class ChampionInvade
{
	public static boolean _started = false;
	public static boolean _aborted = false;
	public static boolean _finish = false;
	static ChampionInvade _instance;
	public static void StartedEvent() // Iniciar Evento
	{
		Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: Duration: " + Config.EVENT_CHAMPION_FARM_TIME + " minute(s)!");
		_aborted = false;
		_started = true;
		
		waiter(Config.EVENT_CHAMPION_FARM_TIME * 60 * 1000);
		if (!_aborted)
		{
			Finish_Event();
		}
	}
	//Finalizar Evento
	public static void Finish_Event()
	{
		_started = false;
		_aborted = true;
		_finish = true;
		
		Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: Finished!");
		Broadcast.gameAnnounceToAll("[Champion Invade Event]: Next Champion Event " + InitialChampionInvade.getInstance().getNextTime() + " hours!");
		if (!AdminChampionInvade._bestfarm_manual)
		{
			InitialChampionInvade.getInstance().StartCalculationOfNextEventTime();
		}
		else
		{
			AdminChampionInvade._bestfarm_manual = false;
		}
	}

	//Final metodo
	/*protected static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000L);
		while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
		{
			seconds--;
			switch (seconds)
			{
				case 3600:
					if (_started)
					{
						
						Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds / 60 / 60 + " hour(s) till event finish!");
					}
					break;
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 600:
				case 900:
				case 1800:
					if (_started)
					{
						
						Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds / 60 + " minute(s) till event finish!");
					}
					break;
				case 1:
				case 2:
				case 3:
				case 10:
				case 15:
				case 30:
					if (_started)
					{
						Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds + " second(s) till event finish!");
					}
					break;
			}
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1L);
				}
				catch (InterruptedException ie)
				{
					if(Config.DEBUG_PATH)
					ie.printStackTrace();
				}
			}
		}
	}*/
	
	
	
	protected static void waiter(long interval)
	{
	    long startWaiterTime = System.currentTimeMillis();
	    int seconds = (int) (interval / 1000L);

	    // Loop principal até o evento terminar ou ser abortado
	    while ((startWaiterTime + interval > System.currentTimeMillis()) && (!_aborted))
	    {
	        seconds--;

	        // Verifica e faz o broadcast conforme o tempo restante
	        if (_started)
	        {
	            // Caso o tempo restante seja uma hora
	            if (seconds == 3600)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds / 60 / 60 + " hour(s) till event finish!");
	            }
	            // Caso o tempo restante seja em minutos significativos
	            else if (seconds == 60 || seconds == 120 || seconds == 180 || seconds == 240 || seconds == 300 || seconds == 600 || seconds == 900 || seconds == 1800)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds / 60 + " minute(s) till event finish!");
	            }
	            // Caso o tempo restante seja em segundos críticos
	            else if (seconds == 1 || seconds == 2 || seconds == 3 || seconds == 10 || seconds == 15 || seconds == 30)
	            {
	                Broadcast.gameAnnounceToOnlinePlayers("[Champion Invade Event]: " + seconds + " second(s) till event finish!");
	            }
	        }

	        // Espera por 1 segundo antes de continuar o loop
	        try
	        {
	            Thread.sleep(1000L);  // Espera de 1 segundo
	        }
	        catch (InterruptedException ie)
	        {
	            if (Config.DEBUG_PATH)
	                ie.printStackTrace();
	        }
	    }
	}

	public static boolean is_started()
	{
		return _started;
	}
	
	public static boolean is_finish()
	{
		return _finish;
	}
}