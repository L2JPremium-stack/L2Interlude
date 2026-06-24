package net.sf.l2j.clan.ranking;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;
import net.sf.l2j.gameserver.taskmanager.models.Task;
import net.sf.l2j.gameserver.taskmanager.models.TaskTypes;

public class TaskClanRankingReward extends Task
{
    private static final Logger _log = Logger.getLogger(TaskClanRankingReward.class.getName());
    public static final String NAME = "clan_ranking_points_reset";
    
    @Override
    public String getName()
    {
        return NAME;
    }
    
    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        Calendar cal = Calendar.getInstance();
        
        // Verifica se hoje é domingo, para resetar e entregar a premiação na virada de domingo para segunda-feira
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        {
            // Agendar o reset e entrega da premiação para segunda-feira à 00:00
            cal.add(Calendar.DAY_OF_WEEK, 1); // Vai para segunda-feira
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            // Realiza o reset e entrega das premiações
            ClanRankingManager.claimReward();
            ClanRankingManager.cleanUp();
            
            _log.info("Clan Ranking Points Global Task: launched.");
        }
    }
    
    public static String getTimeToDate()
    {
        Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        
        // Se hoje for qualquer dia da semana, calculamos para o próximo domingo à meia-noite
        if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
        {
            // Avança para o próximo domingo
            c.add(Calendar.DAY_OF_WEEK, (Calendar.SUNDAY - c.get(Calendar.DAY_OF_WEEK) + 7) % 7);
        }
        
        // Ajusta a data para meia-noite
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        return c.getTime().toString();
    }
    
    // Método para calcular o próximo evento (reset e entrega da premiação) no final de domingo
    public static String getNextHourToDate()
    {
        Calendar c = Calendar.getInstance();
        
        // Se hoje for qualquer dia da semana, calcula o próximo domingo à meia-noite
        if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
        {
            // Avança para o próximo domingo
            c.add(Calendar.DAY_OF_WEEK, (Calendar.SUNDAY - c.get(Calendar.DAY_OF_WEEK) + 7) % 7);
        }
        
        // Ajusta a data para meia-noite
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        return c.getTime().toString(); // Retorna a data calculada para o próximo domingo à meia-noite
    }
    
    @Override
    public void initializate()
    {
        super.initializate();
        // Configura o task para rodar todos os dias à meia-noite
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:00", "");
    }
}