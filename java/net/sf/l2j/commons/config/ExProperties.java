package net.sf.l2j.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author G1ta0
 */
public class ExProperties extends Properties
{
    private static final long serialVersionUID = 1L;

    public static final String defaultDelimiter = "[\\s,;]+";
    
    private String nomeArquivoConfig;

    public void load(final String fileName) throws IOException
    {
        this.nomeArquivoConfig = fileName; // Armazenar o nome do arquivo completo (caminho + nome)
        load(new File(fileName));
    }

    public void load(final File file) throws IOException
    {
        this.nomeArquivoConfig = file.getPath(); // Atribuindo o caminho completo do arquivo
        try (InputStream is = new FileInputStream(file))
        {
            load(is);
        }
    }

    private void logVariavelNaoEncontrada(String nome)
    {
        // Exibe o aviso com o caminho completo do arquivo
        System.out.println("AVISO: Variável não encontrada: " + nome + " no arquivo " + nomeArquivoConfig);
    }

    public boolean getProperty(final String name, final boolean defaultValue)
    {
        boolean val = defaultValue;

        final String value;

        if ((value = super.getProperty(name, null)) != null)
            val = Boolean.parseBoolean(value);
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public int getProperty(final String name, final int defaultValue)
    {
        int val = defaultValue;

        final String value;

        if ((value = super.getProperty(name, null)) != null)
            val = Integer.parseInt(value);
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public long getProperty(final String name, final long defaultValue)
    {
        long val = defaultValue;

        final String value;

        if ((value = super.getProperty(name, null)) != null)
            val = Long.parseLong(value);
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public double getProperty(final String name, final double defaultValue)
    {
        double val = defaultValue;

        final String value;

        if ((value = super.getProperty(name, null)) != null)
            val = Double.parseDouble(value);
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public String[] getProperty(final String name, final String[] defaultValue)
    {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public String[] getProperty(final String name, final String[] defaultValue, final String delimiter)
    {
        String[] val = defaultValue;
        final String value;

        if ((value = super.getProperty(name, null)) != null)
            val = value.split(delimiter);
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public boolean[] getProperty(final String name, final boolean[] defaultValue)
    {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public boolean[] getProperty(final String name, final boolean[] defaultValue, final String delimiter)
    {
        boolean[] val = defaultValue;
        final String value;

        if ((value = super.getProperty(name, null)) != null)
        {
            final String[] values = value.split(delimiter);
            val = new boolean[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Boolean.parseBoolean(values[i]);
        }
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public int[] getProperty(final String name, final int[] defaultValue)
    {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public int[] getProperty(final String name, final int[] defaultValue, final String delimiter)
    {
        int[] val = defaultValue;
        final String value;

        if ((value = super.getProperty(name, null)) != null)
        {
            final String[] values = value.split(delimiter);
            val = new int[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Integer.parseInt(values[i]);
        }
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public long[] getProperty(final String name, final long[] defaultValue)
    {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public long[] getProperty(final String name, final long[] defaultValue, final String delimiter)
    {
        long[] val = defaultValue;
        final String value;

        if ((value = super.getProperty(name, null)) != null)
        {
            final String[] values = value.split(delimiter);
            val = new long[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Long.parseLong(values[i]);
        }
        else
            logVariavelNaoEncontrada(name);

        return val;
    }

    public double[] getProperty(final String name, final double[] defaultValue)
    {
        return getProperty(name, defaultValue, defaultDelimiter);
    }

    public double[] getProperty(final String name, final double[] defaultValue, final String delimiter)
    {
        double[] val = defaultValue;
        final String value;

        if ((value = super.getProperty(name, null)) != null)
        {
            final String[] values = value.split(delimiter);
            val = new double[values.length];
            for (int i = 0; i < val.length; i++)
                val[i] = Double.parseDouble(values[i]);
        }
        else
            logVariavelNaoEncontrada(name);

        return val;
    }
}
