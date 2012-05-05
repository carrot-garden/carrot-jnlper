package com.sun.deploy.security;

import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

class TrustDeciderDialog
{
  public static int showDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, Date paramDate, AppInfo paramAppInfo, boolean paramBoolean2)
    throws CertificateException
  {
    return showDialog(paramArrayOfCertificate, paramURL, paramInt1, paramInt2, paramBoolean1, paramInt3, paramDate, paramAppInfo, paramBoolean2, null, false);
  }

  public static int showDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, Date paramDate, AppInfo paramAppInfo, boolean paramBoolean2, String paramString)
    throws CertificateException
  {
    return showDialog(paramArrayOfCertificate, paramURL, paramInt1, paramInt2, paramBoolean1, paramInt3, paramDate, paramAppInfo, paramBoolean2, paramString, false);
  }

  public static int showDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, Date paramDate, AppInfo paramAppInfo, boolean paramBoolean2, String paramString, boolean paramBoolean3)
    throws CertificateException
  {
    try
    {
      URLClassPathControl.disable();
      i = _showDialog(paramArrayOfCertificate, paramURL, paramInt1, paramInt2, paramBoolean1, paramInt3, paramDate, paramAppInfo, paramBoolean2, paramString, paramBoolean3);
    }
    finally
    {
      int i;
      URLClassPathControl.enable();
    }
  }

  private static int _showDialog(Certificate[] paramArrayOfCertificate, URL paramURL, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, Date paramDate, AppInfo paramAppInfo, boolean paramBoolean2, String paramString, boolean paramBoolean3)
    throws CertificateException
  {
    int i = -1;
    if (((paramArrayOfCertificate[paramInt1] instanceof X509Certificate)) && ((paramArrayOfCertificate[(paramInt2 - 1)] instanceof X509Certificate)))
    {
      X509Certificate localX509Certificate1 = (X509Certificate)paramArrayOfCertificate[paramInt1];
      X509Certificate localX509Certificate2 = (X509Certificate)paramArrayOfCertificate[(paramInt2 - 1)];
      Principal localPrincipal1 = localX509Certificate1.getSubjectDN();
      Principal localPrincipal2 = localX509Certificate2.getIssuerDN();
      String str1 = localPrincipal1.getName();
      String str2 = null;
      int j = str1.indexOf("CN=");
      int k = 0;
      if (j < 0)
        str2 = getMessage("security.dialog.unknown.subject");
      else
        try
        {
          j += 3;
          if (str1.charAt(j) == '"')
          {
            j += 1;
            k = str1.indexOf('"', j);
          }
          else
          {
            k = str1.indexOf(',', j);
          }
          if (k < 0)
            str2 = str1.substring(j);
          else
            str2 = str1.substring(j, k);
        }
        catch (IndexOutOfBoundsException localIndexOutOfBoundsException1)
        {
          str2 = getMessage("security.dialog.unknown.subject");
        }
      String str3 = localPrincipal2.getName();
      String str4 = null;
      j = str3.indexOf("O=");
      k = 0;
      if (j < 0)
        str4 = getMessage("security.dialog.unknown.issuer");
      else
        try
        {
          j += 2;
          if (str3.charAt(j) == '"')
          {
            j += 1;
            k = str3.indexOf('"', j);
          }
          else
          {
            k = str3.indexOf(',', j);
          }
          if (k < 0)
            str4 = str3.substring(j);
          else
            str4 = str3.substring(j, k);
        }
        catch (IndexOutOfBoundsException localIndexOutOfBoundsException2)
        {
          str4 = getMessage("security.dialog.unknown.issuer");
        }
      ArrayList localArrayList1 = new ArrayList();
      ArrayList localArrayList2 = new ArrayList();
      String str5 = null;
      String str6 = null;
      String str7 = paramBoolean2 ? getMessage("security.dialog.https.buttonContinue") : getMessage("security.dialog.signed.buttonContinue");
      String str8 = paramBoolean2 ? getMessage("security.dialog.https.buttonCancel") : getMessage("security.dialog.signed.buttonCancel");
      boolean bool = false;
      if ((!paramBoolean1) && (paramInt3 == 0))
      {
        localObject1 = null;
        if (paramBoolean3)
        {
          str6 = getMessage("security.dialog.unverified.signed.caption");
          localObject1 = getMessage("security.dialog.jnlpunsigned.sub");
          localArrayList1.add(localObject1);
        }
        else
        {
          str6 = paramBoolean2 ? getMessage("security.dialog.verified.valid.https.caption") : getMessage("security.dialog.verified.valid.signed.caption");
          localObject1 = paramBoolean2 ? getMessage("security.dialog.verified.valid.https.sub") : getMessage("security.dialog.verified.valid.signed.sub");
          localArrayList2.add(localObject1);
        }
        localObject1 = paramBoolean2 ? getMessage("security.dialog.verified.https.publisher") : getMessage("security.dialog.verified.signed.publisher");
        localArrayList2.add(localObject1);
        if (paramDate != null)
        {
          localObject2 = DateFormat.getDateTimeInstance(1, 1);
          localObject3 = ((DateFormat)localObject2).format(paramDate);
          localObject4 = new Object[] { localObject3 };
          localObject5 = new MessageFormat(getMessage("security.dialog.timestamp"));
          localArrayList2.add(((MessageFormat)localObject5).format(localObject4));
        }
      }
      else
      {
        if (paramBoolean1)
        {
          bool = true;
          str5 = paramBoolean2 ? getMessage("security.dialog.unverified.https.caption") : getMessage("security.dialog.unverified.signed.caption.new");
          localObject1 = paramBoolean2 ? getMessage("security.dialog.unverified.https.sub") : getMessage("security.dialog.unverified.signed.sub.new");
          localArrayList1.add(localObject1);
          if (!paramBoolean2)
            localArrayList1.add(getMessage("security.dialog.signed.moreinfo.generic2"));
          localObject1 = paramBoolean2 ? getMessage("security.dialog.unverified.https.publisher") : getMessage("security.dialog.unverified.signed.publisher");
          localArrayList1.add(localObject1);
        }
        else
        {
          str6 = paramBoolean2 ? getMessage("security.dialog.verified.valid.https.caption") : getMessage("security.dialog.verified.valid.signed.caption");
          localObject1 = paramBoolean2 ? getMessage("security.dialog.verified.https.publisher") : getMessage("security.dialog.verified.signed.publisher");
          localArrayList2.add(localObject1);
        }
        if (paramInt3 != 0)
        {
          if (str5 == null)
            str5 = paramBoolean2 ? getMessage("security.dialog.invalid.time.https.caption") : getMessage("security.dialog.invalid.time.signed.caption");
          localObject1 = null;
          localObject2 = null;
          if (paramInt3 == -1)
          {
            if (localArrayList1.isEmpty())
              localObject1 = paramBoolean2 ? getMessage("security.dialog.expired.https.sub") : getMessage("security.dialog.expired.signed.sub");
            localObject2 = paramBoolean2 ? getMessage("security.dialog.expired.https.time") : getMessage("security.dialog.expired.signed.time");
          }
          else
          {
            if (localArrayList1.isEmpty())
              localObject1 = paramBoolean2 ? getMessage("security.dialog.notyet.https.sub") : getMessage("security.dialog.notyet.signed.sub");
            localObject2 = paramBoolean2 ? getMessage("security.dialog.notyetvalid.https.time") : getMessage("security.dialog.notyetvalid.signed.time");
          }
          if (localObject1 != null)
            localArrayList1.add(localObject1);
          localArrayList1.add(localObject2);
        }
        else if (paramDate != null)
        {
          localObject1 = DateFormat.getDateTimeInstance(1, 1);
          localObject2 = ((DateFormat)localObject1).format(paramDate);
          localObject3 = new Object[] { localObject2 };
          localObject4 = new MessageFormat(getMessage("security.dialog.timestamp"));
          localArrayList2.add(((MessageFormat)localObject4).format(localObject3));
        }
      }
      Object localObject1 = CertUtils.getServername(localX509Certificate1);
      if ((paramString != null) && (!CertUtils.checkWildcardDomainList(paramString, (ArrayList)localObject1)))
      {
        localObject2 = new Object[] { paramString, str2 };
        if (str5 == null)
        {
          str5 = ResourceManager.getMessage("https.dialog.masthead");
          localArrayList1.add(ResourceManager.getFormattedMessage("security.dialog.hostname.mismatch.sub", localObject2));
        }
        localArrayList1.add(ResourceManager.getFormattedMessage("security.dialog.hostname.mismatch.moreinfo", localObject2));
      }
      if (!paramBoolean2)
      {
        localObject2 = getMessage("security.dialog.signed.moreinfo.generic");
        localObject3 = localArrayList2;
        if (!localArrayList1.isEmpty())
          localObject3 = localArrayList1;
        ((ArrayList)localObject3).add(1, localObject2);
      }
      if (paramBoolean2)
        if (paramString != null)
          paramAppInfo.setTitle(paramString);
        else
          paramAppInfo.setTitle(str2);
      if (paramBoolean3)
        localArrayList1.add(getMessage("security.dialog.jnlpunsigned.more"));
      Object localObject2 = str5 != null ? str5 : str6;
      Object localObject3 = null;
      Object localObject4 = null;
      Object localObject5 = null;
      int m;
      if (!localArrayList1.isEmpty())
      {
        localObject3 = new String[localArrayList1.size()];
        for (m = 0; m < localArrayList1.size(); m++)
          localObject3[m] = localArrayList1.get(m).toString();
      }
      if (!localArrayList2.isEmpty())
      {
        localObject4 = new String[localArrayList2.size()];
        for (m = 0; m < localArrayList2.size(); m++)
          localObject4[m] = localArrayList2.get(m).toString();
      }
      if ((localArrayList1.isEmpty()) && (localArrayList2.isEmpty()))
        throw new CertificateException(getMessage("security.dialog.exception.message"));
      if (!Trace.isAutomationEnabled())
      {
        if ((paramAppInfo.getType() == 3) && (localObject3 == null) && (!paramBoolean3))
        {
          localObject2 = getMessage("security.dialog.extension.caption");
          String str9 = getMessage("security.dialog.extension.buttonInstall");
          String[] arrayOfString = null;
          arrayOfString = new String[2];
          arrayOfString[0] = getMessage("security.dialog.extension.sub");
          MessageFormat localMessageFormat = new MessageFormat(getMessage("security.dialog.extension.warning"));
          Object[] arrayOfObject = { str2, str2, str2 };
          arrayOfString[1] = localMessageFormat.format(arrayOfObject);
          i = ToolkitStore.getUI().showSecurityDialog(paramAppInfo, getMessage("security.dialog.extension.title"), (String)localObject2, str2, paramURL, true, false, str9, str8, localObject3, arrayOfString, true, paramArrayOfCertificate, paramInt1, paramInt2, bool, paramBoolean2);
          ToolkitStore.getUI();
          if (i == 0)
          {
            ToolkitStore.getUI();
            i = 2;
          }
        }
        else
        {
          if (paramBoolean1)
            str2 = getMessage("security.dialog.notverified.subject").toUpperCase();
          if (!paramBoolean1)
            localObject5 = getMessage("security.dialog.valid.caption");
          else
            localObject5 = getMessage("security.dialog.caption");
          i = ToolkitStore.getUI().showSecurityDialog(paramAppInfo, (String)localObject5, (String)localObject2, str2, paramURL, true, false, str7, str8, localObject3, localObject4, true, paramArrayOfCertificate, paramInt1, paramInt2, bool, paramBoolean2);
        }
      }
      else
      {
        Trace.msgSecurityPrintln("trustdecider.automation.trustcert");
        i = 0;
      }
    }
    return i;
  }

  private static String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private static int getAcceleratorKey(String paramString)
  {
    return ResourceManager.getAcceleratorKey(paramString);
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.security.TrustDeciderDialog
 * JD-Core Version:    0.6.0
 */