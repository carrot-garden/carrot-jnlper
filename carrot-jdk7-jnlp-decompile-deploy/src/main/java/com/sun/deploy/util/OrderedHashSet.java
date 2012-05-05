package com.sun.deploy.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OrderedHashSet
  implements Collection, Cloneable
{
  HashSet objectsHash = new HashSet();
  LinkedList objects = new LinkedList();

  public OrderedHashSet()
  {
    clear();
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    LinkedList localLinkedList = (LinkedList)this.objects.clone();
    OrderedHashSet localOrderedHashSet = new OrderedHashSet();
    localOrderedHashSet.addAll(localLinkedList);
    return localOrderedHashSet;
  }

  public void clear()
  {
    this.objects.clear();
    this.objectsHash.clear();
  }

  public boolean add(Object paramObject)
  {
    boolean bool = this.objectsHash.add(paramObject);
    if (!bool)
      this.objects.remove(paramObject);
    this.objects.add(paramObject);
    return bool;
  }

  public boolean remove(Object paramObject)
  {
    if (this.objectsHash.remove(paramObject))
    {
      this.objects.remove(paramObject);
      return true;
    }
    return false;
  }

  public boolean addAll(Collection paramCollection)
  {
    int i = 0;
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
      i = (add(localIterator.next())) || (i != 0) ? 1 : 0;
    return i;
  }

  public boolean contains(Object paramObject)
  {
    return this.objectsHash.contains(paramObject);
  }

  public boolean containsAll(Collection paramCollection)
  {
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
      if (!contains(localIterator.next()))
        return false;
    return true;
  }

  public boolean removeAll(Collection paramCollection)
  {
    int i = 0;
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
      i = (remove(localIterator.next())) || (i != 0) ? 1 : 0;
    return i;
  }

  public boolean retainAll(Collection paramCollection)
  {
    int i = 0;
    Iterator localIterator = iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      if (!paramCollection.contains(localObject))
        i = (remove(localObject)) || (i != 0) ? 1 : 0;
    }
    return i;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof OrderedHashSet))
      return false;
    return this.objects.equals(((OrderedHashSet)paramObject).objects);
  }

  public int hashCode()
  {
    return this.objectsHash.hashCode();
  }

  public boolean isEmpty()
  {
    return this.objects.isEmpty();
  }

  public Iterator iterator()
  {
    return this.objects.iterator();
  }

  public int size()
  {
    return this.objects.size();
  }

  public Object[] toArray()
  {
    return this.objects.toArray();
  }

  public Object[] toArray(Object[] paramArrayOfObject)
  {
    return this.objects.toArray(paramArrayOfObject);
  }

  public Object get(int paramInt)
  {
    return this.objects.get(paramInt);
  }

  public int indexOf(Object paramObject)
  {
    return this.objects.indexOf(paramObject);
  }

  public List toList()
  {
    return this.objects;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.util.OrderedHashSet
 * JD-Core Version:    0.6.0
 */