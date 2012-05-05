package com.sun.javaws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class OperaPreferences
{
  private static final String OPERA_ENCODING = "UTF-8";
  private static final char OPEN_BRACKET = '[';
  private static final char CLOSE_BRACKET = ']';
  private static final char SEPARATOR = '=';
  private static final int DEFAULT_SIZE = 16384;
  private static final int DEFAULT_SECTION_COUNT = 20;
  private ArrayList sections = null;

  public void load(InputStream paramInputStream)
    throws IOException
  {
    InputStreamReader localInputStreamReader = new InputStreamReader(paramInputStream, "UTF-8");
    BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader, 16384);
    String str1 = "";
    for (String str2 = localBufferedReader.readLine(); str2 != null; str2 = localBufferedReader.readLine())
    {
      if (str2.length() <= 0)
        continue;
      if (str2.charAt(0) == '[')
      {
        str1 = str2.substring(1, str2.indexOf(']'));
      }
      else
      {
        String str3 = null;
        String str4 = null;
        int i = str2.indexOf('=');
        if (i >= 0)
        {
          str3 = str2.substring(0, i);
          str4 = str2.substring(i + 1);
        }
        else
        {
          str3 = str2;
        }
        put(str1, str3, str4);
      }
    }
  }

  public void store(OutputStream paramOutputStream)
    throws IOException
  {
    OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(paramOutputStream, "UTF-8");
    PrintWriter localPrintWriter = new PrintWriter(localOutputStreamWriter, true);
    localPrintWriter.println(toString());
  }

  public boolean containsSection(String paramString)
  {
    return indexOf(paramString) >= 0;
  }

  public boolean containsKey(String paramString1, String paramString2)
  {
    int i = indexOf(paramString1);
    return i < 0 ? false : ((PreferenceSection)this.sections.get(i)).contains(paramString2);
  }

  public String get(String paramString1, String paramString2)
  {
    int i = indexOf(paramString1);
    OperaPreferences.PreferenceSection.PreferenceEntry localPreferenceEntry = i < 0 ? null : ((PreferenceSection)this.sections.get(i)).get(paramString2);
    return localPreferenceEntry == null ? null : localPreferenceEntry.getValue();
  }

  public String put(String paramString1, String paramString2, String paramString3)
  {
    int i = indexOf(paramString1);
    PreferenceSection localPreferenceSection = null;
    if (i < 0)
    {
      localPreferenceSection = new PreferenceSection(paramString1);
      this.sections.add(localPreferenceSection);
    }
    else
    {
      localPreferenceSection = (PreferenceSection)this.sections.get(i);
    }
    return localPreferenceSection.put(paramString2, paramString3);
  }

  public PreferenceSection remove(String paramString)
  {
    int i = indexOf(paramString);
    return i < 0 ? null : (PreferenceSection)this.sections.remove(i);
  }

  public String remove(String paramString1, String paramString2)
  {
    int i = indexOf(paramString1);
    return i < 0 ? null : ((PreferenceSection)this.sections.get(i)).remove(paramString2);
  }

  public Iterator iterator(String paramString)
  {
    int i = indexOf(paramString);
    return i < 0 ? new PreferenceSection(paramString).iterator() : ((PreferenceSection)this.sections.get(i)).iterator();
  }

  public Iterator iterator()
  {
    return new OperaPreferencesIterator();
  }

  public boolean equals(Object paramObject)
  {
    int i = 0;
    if ((paramObject instanceof OperaPreferences))
    {
      OperaPreferences localOperaPreferences = (OperaPreferences)paramObject;
      ListIterator localListIterator1 = this.sections.listIterator();
      ListIterator localListIterator2 = localOperaPreferences.sections.listIterator();
      while (true)
        if ((localListIterator1.hasNext()) && (localListIterator2.hasNext()))
        {
          PreferenceSection localPreferenceSection1 = (PreferenceSection)localListIterator1.next();
          PreferenceSection localPreferenceSection2 = (PreferenceSection)localListIterator2.next();
          if (!localPreferenceSection1.equals(localPreferenceSection2))
            break;
          continue;
        }
        else
        {
          if ((localListIterator1.hasNext()) || (localListIterator2.hasNext()))
            break;
          i = 1;
        }
    }
    return i;
  }

  public int hashCode()
  {
    return this.sections.hashCode();
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    ListIterator localListIterator = this.sections.listIterator();
    while (localListIterator.hasNext())
    {
      PreferenceSection localPreferenceSection = (PreferenceSection)localListIterator.next();
      localStringBuffer.append(localPreferenceSection);
    }
    return localStringBuffer.toString();
  }

  private int indexOf(String paramString)
  {
    int i = 0;
    int j = -1;
    ListIterator localListIterator = this.sections.listIterator();
    while (localListIterator.hasNext())
    {
      PreferenceSection localPreferenceSection = (PreferenceSection)localListIterator.next();
      if ((localPreferenceSection != null) && (localPreferenceSection.getName().equalsIgnoreCase(paramString)))
      {
        j = i;
        break;
      }
      i++;
    }
    return j;
  }

  private class OperaPreferencesIterator
    implements Iterator
  {
    private Iterator i = OperaPreferences.this.sections.listIterator();

    public boolean hasNext()
    {
      return this.i.hasNext();
    }

    public Object next()
    {
      return ((OperaPreferences.PreferenceSection)this.i.next()).getName();
    }

    public void remove()
    {
      this.i.remove();
    }

    public OperaPreferencesIterator()
    {
    }
  }

  private class PreferenceSection
  {
    private String name;
    private HashMap entries;
    private volatile int modified;
    private PreferenceEntry start;
    private PreferenceEntry end;

    public String getName()
    {
      return this.name;
    }

    public boolean contains(String paramString)
    {
      return this.entries.containsKey(paramString);
    }

    public String put(String paramString1, String paramString2)
    {
      PreferenceEntry localPreferenceEntry = (PreferenceEntry)this.entries.get(paramString1);
      String str = null;
      if (localPreferenceEntry == null)
      {
        localPreferenceEntry = new PreferenceEntry(paramString1, paramString2);
        if (this.end == null)
        {
          this.start = localPreferenceEntry;
          this.end = localPreferenceEntry;
        }
        else
        {
          this.end.add(localPreferenceEntry);
          this.end = localPreferenceEntry;
        }
        this.entries.put(localPreferenceEntry.getKey(), localPreferenceEntry);
        this.modified += 1;
      }
      else
      {
        str = localPreferenceEntry.getValue();
        localPreferenceEntry.setValue(paramString2);
      }
      return str;
    }

    public PreferenceEntry get(String paramString)
    {
      return (PreferenceEntry)this.entries.get(paramString);
    }

    public String remove(String paramString)
    {
      PreferenceEntry localPreferenceEntry = (PreferenceEntry)this.entries.get(paramString);
      String str = null;
      if (localPreferenceEntry != null)
      {
        str = localPreferenceEntry.getValue();
        removeEntry(localPreferenceEntry);
      }
      return str;
    }

    public Iterator iterator()
    {
      return new PreferenceEntryIterator(this.start);
    }

    public boolean equals(Object paramObject)
    {
      int i = 0;
      if ((paramObject instanceof PreferenceSection))
      {
        PreferenceSection localPreferenceSection = (PreferenceSection)paramObject;
        if ((this.name == localPreferenceSection.name) || ((this.name != null) && (this.name.equals(localPreferenceSection.name))))
        {
          Iterator localIterator1 = iterator();
          Iterator localIterator2 = localPreferenceSection.iterator();
          while (true)
            if ((localIterator1.hasNext()) && (localIterator2.hasNext()))
            {
              PreferenceEntry localPreferenceEntry1 = (PreferenceEntry)localIterator1.next();
              PreferenceEntry localPreferenceEntry2 = (PreferenceEntry)localIterator2.next();
              if (!localPreferenceEntry1.equals(localPreferenceEntry2))
                break;
              continue;
            }
            else
            {
              if ((localIterator1.hasNext()) || (localIterator2.hasNext()))
                break;
              i = 1;
            }
        }
      }
      return i;
    }

    public int hashCode()
    {
      return this.entries.hashCode();
    }

    public String toString()
    {
      StringBuffer localStringBuffer = new StringBuffer(this.entries.size() * 80);
      if ((this.name != null) && (this.name.length() > 0))
        localStringBuffer.append('[').append(this.name).append(']').append(System.getProperty("line.separator"));
      Iterator localIterator = iterator();
      while (localIterator.hasNext())
      {
        PreferenceEntry localPreferenceEntry = (PreferenceEntry)localIterator.next();
        localStringBuffer.append(localPreferenceEntry).append(System.getProperty("line.separator"));
      }
      localStringBuffer.append(System.getProperty("line.separator"));
      return localStringBuffer.toString();
    }

    public PreferenceSection(String arg2)
    {
      Object localObject;
      this.name = localObject;
      this.entries = new HashMap();
      this.modified = 0;
      this.start = null;
      this.end = null;
    }

    private void removeEntry(PreferenceEntry paramPreferenceEntry)
    {
      if (paramPreferenceEntry == this.start)
        this.start = paramPreferenceEntry.getNext();
      if (paramPreferenceEntry == this.end)
        this.end = paramPreferenceEntry.getPrevious();
      paramPreferenceEntry.remove();
      this.entries.remove(paramPreferenceEntry.getKey());
      this.modified += 1;
    }

    private class PreferenceEntry
    {
      private final String key;
      private String value;
      private PreferenceEntry previous;
      private PreferenceEntry next;

      public String getKey()
      {
        return this.key;
      }

      public String getValue()
      {
        return this.value;
      }

      public void setValue(String paramString)
      {
        this.value = paramString;
      }

      public void add(PreferenceEntry paramPreferenceEntry)
      {
        if (this.next != null)
        {
          this.next.add(paramPreferenceEntry);
        }
        else
        {
          this.next = paramPreferenceEntry;
          paramPreferenceEntry.previous = this;
        }
      }

      public void remove()
      {
        if (this.previous != null)
          this.previous.next = this.next;
        if (this.next != null)
          this.next.previous = this.previous;
        this.previous = null;
        this.next = null;
      }

      public PreferenceEntry getPrevious()
      {
        return this.previous;
      }

      public PreferenceEntry getNext()
      {
        return this.next;
      }

      public boolean equals(Object paramObject)
      {
        int i = 0;
        if ((paramObject instanceof PreferenceEntry))
        {
          PreferenceEntry localPreferenceEntry = (PreferenceEntry)paramObject;
          String str1 = getKey();
          String str2 = localPreferenceEntry.getKey();
          if ((str1 == str2) || ((str1 != null) && (str1.equals(str2))))
          {
            String str3 = getValue();
            String str4 = localPreferenceEntry.getValue();
            if ((str3 == str4) || ((str3 != null) && (str3.equals(str4))))
              i = 1;
          }
        }
        return i;
      }

      public int hashCode()
      {
        return this.key == null ? 0 : this.key.hashCode();
      }

      public String toString()
      {
        StringBuffer localStringBuffer = new StringBuffer((this.key == null ? 0 : this.key.length()) + (this.value == null ? 0 : this.value.length()) + 1);
        if ((this.key != null) && (this.value != null))
          localStringBuffer.append(this.key).append('=').append(this.value);
        else if (this.key != null)
          localStringBuffer.append(this.key);
        else if (this.value != null)
          localStringBuffer.append(this.value);
        return localStringBuffer.toString();
      }

      public PreferenceEntry(String paramString1, String arg3)
      {
        this.key = paramString1;
        Object localObject;
        this.value = localObject;
        this.previous = null;
        this.next = null;
      }
    }

    private class PreferenceEntryIterator
      implements Iterator
    {
      private OperaPreferences.PreferenceSection.PreferenceEntry next;
      private OperaPreferences.PreferenceSection.PreferenceEntry current;
      private int expectedModified;

      public boolean hasNext()
      {
        return this.next != null;
      }

      public Object next()
      {
        if (OperaPreferences.PreferenceSection.this.modified != this.expectedModified)
          throw new ConcurrentModificationException();
        if (this.next == null)
          throw new NoSuchElementException();
        this.current = this.next;
        this.next = this.next.getNext();
        return this.current;
      }

      public void remove()
      {
        if (this.current == null)
          throw new IllegalStateException();
        if (OperaPreferences.PreferenceSection.this.modified != this.expectedModified)
          throw new ConcurrentModificationException();
        OperaPreferences.PreferenceSection.this.removeEntry(this.current);
        this.current = null;
        this.expectedModified = OperaPreferences.PreferenceSection.this.modified;
      }

      public PreferenceEntryIterator(OperaPreferences.PreferenceSection.PreferenceEntry arg2)
      {
        Object localObject;
        this.next = localObject;
        this.current = null;
        this.expectedModified = OperaPreferences.PreferenceSection.this.modified;
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.OperaPreferences
 * JD-Core Version:    0.6.0
 */