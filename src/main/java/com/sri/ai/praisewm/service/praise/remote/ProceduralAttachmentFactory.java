package com.sri.ai.praisewm.service.praise.remote;

import com.sri.ai.praise.other.integration.proceduralattachment.api.ProceduralAttachments;
import com.sri.ai.praise.other.integration.proceduralattachment.api.Procedure;
import com.sri.ai.praise.other.integration.proceduralattachment.core.DefaultProceduralAttachments;
import com.sri.ai.util.Util;

public class ProceduralAttachmentFactory {
  private MintAccessor mintAccessor = new MintAccessor();

  public ProceduralAttachments getAttachments() {
    return new DefaultProceduralAttachments(
        Util.map(
            "precipitation",
            (Procedure) p -> mintAccessor.precipitationQuery(new MintQueryParameters())));
  }
}
