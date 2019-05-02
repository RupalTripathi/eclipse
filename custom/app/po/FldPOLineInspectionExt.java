package custom.app.po;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import psdi.mbo.Mbo;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.MboValue;
import psdi.mbo.MboValueAdapter;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.util.logging.MXLogger;
import psdi.util.logging.MXLoggerFactory;

public class FldPOLineInspectionExt
  extends MboValueAdapter
{
  private String hasCustomization = "";
  private MXLogger myLogger;
  
  public FldPOLineInspectionExt(MboValue mbv)
  {
    super(mbv);
    this.myLogger = MXLoggerFactory.getLogger("maximo.merge");
	//Test Rupal
  }
  
  public void initValue()
    throws MXException, RemoteException
  {
    super.initValue();
    this.myLogger.info("(:----------------------------------------------:)");
    this.myLogger.info(getClass().getName() + " initValue()");
    if (!hasCustomization())
    {
      this.myLogger.info("Org does not have access, calling super");
      return;
    }
    this.myLogger.info("Org has access, running custom code");
    
    getMboValue().setReadOnly(false);
  }
  
  public void action()
    throws MXException, RemoteException
  {
    super.action();
    this.myLogger.info("(:----------------------------------------------:)");
    this.myLogger.info(getClass().getName() + " action()");
    if (!hasCustomization())
    {
      this.myLogger.info("Org does not have access, calling super");
      return;
    }
    this.myLogger.info("Org has access, running custom code");
    getMboValue("INSPECTIONREQUIRED").setValue(checkInspection(), 11L);
  }
  
  public boolean checkInspection()
    throws RemoteException, MXException
  {
    boolean inspectionFlag = false;
    boolean inspectionFlagIndividual = false;
    MboSetRemote notInspValues = getMboValue().getMbo().getMboSet("TQINSPNOTREQ");
    
    String val = "";
    List<String> attrValues = Arrays.asList(getMboValue().getString().split(","));
    MboRemote currMbo = null;
    for (int valuePosition = 0; valuePosition < attrValues.size(); valuePosition++)
    {
      inspectionFlagIndividual = true;
      val = (String)attrValues.get(valuePosition);
      for (currMbo = notInspValues.moveFirst(); currMbo != null; currMbo = notInspValues.moveNext()) {
        if (currMbo.getString("VALUE").equalsIgnoreCase(val)) {
          inspectionFlagIndividual = false;
        }
      }
      if (inspectionFlagIndividual) {
        inspectionFlag = inspectionFlagIndividual;
      }
    }
    return inspectionFlag;
  }
  
  public void validate()
    throws MXException, RemoteException
  {
    super.validate();
    this.myLogger.info("(:----------------------------------------------:)");
    this.myLogger.info(getClass().getName() + " validate()");
    if (!hasCustomization())
    {
      this.myLogger.info("Org does not have access, calling super");
      return;
    }
    this.myLogger.info("Org has access, running custom code");
    
    String value = null;
    value = getMboValue().getString();
    if (!value.equalsIgnoreCase("")) {
      checkInspCertTestDocVal("TQINSPECRELALL", value);
    }
  }
  
  public void checkInspCertTestDocVal(String domainRelationship, String value)
    throws RemoteException, MXException
  {
    List<String> valuetList = Arrays.asList(value.split(","));
    MboSetRemote domainValuesSet = getMboValue().getMbo().getMboSet(domainRelationship);
    MboRemote domValue = null;
    List<String> domValueList = new ArrayList();
    String currValue = null;
    boolean firstmove = true;
    String domainid = null;
    for (domValue = domainValuesSet.moveFirst(); domValue != null; domValue = domainValuesSet.moveNext())
    {
      domValueList.add(domValue.getString("VALUE"));
      if (firstmove)
      {
        domainid = domValue.getString("domainid");
        firstmove = false;
      }
    }
    for (int attrPosition = 0; attrPosition < valuetList.size(); attrPosition++)
    {
      currValue = (String)valuetList.get(attrPosition);
      if (!domValueList.contains(currValue))
      {
        Object[] params = { currValue, domainid };
        throw new MXApplicationException("pr", "NOTVALIDVALUE", params);
      }
    }
  }
  
  private boolean hasCustomization()
    throws RemoteException, MXException
  {
    if (this.hasCustomization.length() == 0) {
      if ((getMboValue().getMbo() instanceof POLineExt))
      {
        POLineExt pol = (POLineExt)getMboValue().getMbo();
        if (pol.hasCustomization()) {
          this.hasCustomization = "Y";
        } else {
          this.hasCustomization = "N";
        }
      }
    }
    return this.hasCustomization.equalsIgnoreCase("Y");
  }
}
