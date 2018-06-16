package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.Procedure;
import com.sri.ai.praisewm.service.praise.ProcedurePayload;

public abstract class AbstractTemperatureProcedure implements Procedure {
  public abstract TemperatureProcedureResult evaluate(ProcedurePayload payload);
}
