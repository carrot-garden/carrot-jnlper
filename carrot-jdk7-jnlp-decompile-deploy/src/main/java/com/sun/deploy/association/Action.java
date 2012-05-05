package com.sun.deploy.association;

public class Action
{
  private String description;
  private String verb;
  private String command;
  private int hashcode = 0;

  public Action(String paramString1, String paramString2)
  {
    this.verb = paramString1;
    this.command = paramString2;
  }

  public Action(String paramString1, String paramString2, String paramString3)
  {
    this.verb = paramString1;
    this.command = paramString2;
    this.description = paramString3;
  }

  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(String paramString)
  {
    this.description = paramString;
  }

  public String getVerb()
  {
    return this.verb;
  }

  public void setVerb(String paramString)
  {
    this.verb = paramString;
  }

  public String getCommand()
  {
    return this.command;
  }

  public void setCommand(String paramString)
  {
    this.command = paramString;
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject instanceof Action))
    {
      Action localAction = (Action)paramObject;
      String str1 = localAction.getDescription();
      String str2 = localAction.getVerb();
      String str3 = localAction.getCommand();
      if ((this.description == null ? str1 == null : this.description.equals(str1)) && (this.verb == null ? str2 == null : this.verb.equals(str2)) && (this.command == null ? str3 == null : this.command.equals(str3)))
        return true;
    }
    return false;
  }

  public int hashCode()
  {
    if (this.hashcode != 0)
    {
      int i = 17;
      if (this.description != null)
        i = 37 * i + this.description.hashCode();
      if (this.verb != null)
        i = 37 * i + this.verb.hashCode();
      if (this.command != null)
        i = 37 * i + this.command.hashCode();
      this.hashcode = i;
    }
    return this.hashcode;
  }

  public String toString()
  {
    String str1 = "\r\n";
    String str2 = "";
    String str3 = "\t";
    str2 = str2.concat(str3);
    str2 = str2.concat("Description: ");
    if (this.description != null)
      str2 = str2.concat(this.description);
    str2 = str2.concat(str1);
    str2 = str2.concat(str3);
    str2 = str2.concat("Verb: ");
    if (this.verb != null)
      str2 = str2.concat(this.verb);
    str2 = str2.concat(str1);
    str2 = str2.concat(str3);
    str2 = str2.concat("Command: ");
    if (this.command != null)
      str2 = str2.concat(this.command);
    str2 = str2.concat(str1);
    return str2;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.association.Action
 * JD-Core Version:    0.6.0
 */