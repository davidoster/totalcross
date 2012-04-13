/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter.oper;

public class OperandRegI extends OperandReg
{
   public OperandRegI()
   {
      super(opr_regI);
   }

   public OperandRegI(int framePosition)
   {
      super(opr_regI, framePosition);
   }

   public OperandRegI(int kind, int framePosition) // kind: regIb | regIs | regIc
   {
      super(kind, framePosition);
   }

   public OperandRegI(String wordIndex, int index)
   {
      super(opr_regI, wordIndex, index);
   }
}