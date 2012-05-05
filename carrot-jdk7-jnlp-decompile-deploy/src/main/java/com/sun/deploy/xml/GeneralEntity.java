package com.sun.deploy.xml;

public class GeneralEntity
{
  private static final String BAD_NAME = "A general entity cannot must have a name!";
  private static final String BAD_VALUE = "A general entity cannot must have a substitution value!";
  private final String _name;
  private final String _value;

  public String getName()
  {
    return this._name;
  }

  public String getValue()
  {
    return this._value;
  }

  public boolean equals(Object paramObject)
  {
    int i = 0;
    if (this == paramObject)
    {
      i = 1;
    }
    else
    {
      Object localObject;
      if ((paramObject instanceof GeneralEntity))
      {
        localObject = (GeneralEntity)paramObject;
        i = (this._name.equals(((GeneralEntity)localObject)._name)) && (this._value.equals(((GeneralEntity)localObject)._value)) ? 1 : 0;
      }
      else if ((paramObject instanceof String))
      {
        localObject = (String)paramObject;
        i = (this._name.equals(localObject)) || (this._value.equals(localObject)) ? 1 : 0;
      }
    }
    return i;
  }

  public int hashCode()
  {
    return this._name.hashCode();
  }

  public GeneralEntity(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      throw new NullPointerException("A general entity cannot must have a name!");
    if (paramString2 == null)
      throw new NullPointerException("A general entity cannot must have a substitution value!");
    this._name = paramString1;
    this._value = paramString2;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.xml.GeneralEntity
 * JD-Core Version:    0.6.0
 */