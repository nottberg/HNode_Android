// Copyright 2010 Curtis Nottberg
// Licensed under Apache License version 2.0

package org.hnode;

interface IHNodeService 
{
  int getCounterValue();
  String getNetID();
  void resetAndEnumerate();
}
