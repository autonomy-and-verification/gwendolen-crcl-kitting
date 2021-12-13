/* 
The NIST RCS (Real-time Control Systems) 
 library is public domain software, however it is preferred
 that the following disclaimers be attached.

Software Copywrite/Warranty Disclaimer

   This software was developed at the National Institute of Standards and
Technology by employees of the Federal Government in the course of their
official duties. Pursuant to title 17 Section 105 of the United States
Code this software is not subject to copyright protection and is in the
public domain. NIST Real-Time Control System software is an experimental
system. NIST assumes no responsibility whatsoever for its use by other
parties, and makes no guarantees, expressed or implied, about its
quality, reliability, or any other characteristic. We would appreciate
acknowledgement if the software is used. This software can be
redistributed and/or modified freely provided that any derivative works
bear some notice that they are derived from it, and any modified
versions bear some notice that they have been modified.



*/ 

package rcs.nml;

/*
*       Class definition for NML_PERFORMANCE_TEST_MSG
*       Automatically generated by RCS Java Diagnostics Tool.
*       on Wed Jul 15 13:36:01 EDT 1998
*/
class NML_PERFORMANCE_TEST_MSG extends NMLmsg
{

  public static boolean debug_on=false;

  public int serial_number=0;
  public int test_type = 0;
  public long array_length = 0;
  public byte carray[] = null;
  public int iarray[] = null;
  public float farray[] = null;
  public double darray[] = null;
  public long larray[] = null;
  public static long default_array_length = 0;

        // TEST_TYPE
        public static final int CHAR_TEST=0;
        public static final int DOUBLE_TEST=4;
        public static final int FLOAT_TEST=3;
        public static final int INT_TEST=1;
        public static final int LONG_TEST=2;

  // Constructor
    //    @SuppressWarnings("unchecked")
  public NML_PERFORMANCE_TEST_MSG()
  {
    super(255);
    if(default_array_length > 0)
      {
        array_length = default_array_length;
        carray = new byte[(int) array_length];
        for(int cindex = 0; cindex < array_length; cindex++)
          {
            carray[cindex] = (byte) (cindex+50);
          }
        iarray = new int[(int) array_length];
        for(int iindex = 0; iindex < array_length; iindex++)
          {
            iarray[iindex] =  iindex+50;
          }
        larray = new long[(int) array_length];
        for(int lindex = 0; lindex < array_length; lindex++)
          {
            larray[lindex] = (long) lindex+50;
          }
        farray = new float[(int) array_length];
        for(int findex = 0; findex < array_length; findex++)
          {
            farray[findex] = (float) findex+50;
          }
        darray = new double[(int) array_length];
        for(int dindex = 0; dindex < array_length; dindex++)
          {
            darray[dindex] = (double) dindex+50;
          }
      }
  }

  public void update(NMLFormatConverter nml_fc)
  {
    super.update(nml_fc);
    serial_number = nml_fc.update(serial_number);
    if(debug_on)
      {
        rcs.nml.debugInfo.debugPrintStream.println("serial_number="+serial_number);
      }
    test_type = nml_fc.update(test_type);
    if(debug_on)
      {
        rcs.nml.debugInfo.debugPrintStream.println("test_type="+test_type);
      }
    array_length = nml_fc.update(array_length);
    if(debug_on)
      {
          rcs.nml.debugInfo.debugPrintStream .println("array_length="+array_length);
      }
    switch(test_type)
      {
      case CHAR_TEST:
        if(null == carray)
          {
            carray = new byte[(int) array_length];
          }
        if(carray.length != array_length)
          {
            carray = new byte[(int) array_length];
          }
        nml_fc.update(carray, (int) array_length);
        break;

      case INT_TEST:
        if(null == iarray)
          {
            iarray = new int[(int) array_length];
          }
        if(iarray.length != array_length)
          {
            iarray = new int[(int) array_length];
          }
        nml_fc.update(iarray, (int) array_length);
        break;


      case LONG_TEST:
        if(null == larray)
          {
            larray = new long[(int) array_length];
          }
        if(larray.length != array_length)
          {
            larray = new long[(int) array_length];
          }
        nml_fc.update(larray, (int) array_length);
        break;

      case FLOAT_TEST:
        if(null == farray)
          {
            farray = new float[(int) array_length];
          }
        if(farray.length != array_length)
          {
            farray = new float[(int) array_length];
          }
        nml_fc.update(farray, (int) array_length);
        break;

      case DOUBLE_TEST:
        if(null == darray)
          {
            darray = new double[(int) array_length];
          }
        if(darray.length != array_length)
          {
            darray = new double[(int) array_length];
          }
        nml_fc.update(darray, (int) array_length);
        break;
      }

  }
}
