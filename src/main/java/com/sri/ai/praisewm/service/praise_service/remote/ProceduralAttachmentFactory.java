package com.sri.ai.praisewm.service.praise_service.remote;

import com.sri.ai.praise.other.integration.proceduralattachment.api.ProceduralAttachments;
import com.sri.ai.praise.other.integration.proceduralattachment.api.Procedure;
import com.sri.ai.praise.other.integration.proceduralattachment.core.DefaultProceduralAttachments;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProceduralAttachmentFactory {
  private MintAccessor mintAccessor = new MintAccessor();
  private Map<String, Procedure> map = new LinkedHashMap<>();

  public ProceduralAttachmentFactory() {
    map.put(
        "precipitationSudanApril1999",
        p -> mintAccessor.precipitationQuery(new MintQueryParameters()));

    map.put("Beans_dry_Production", p -> 6000.0);
    map.put("Closed_shrubland_Burned_Area", p -> 121929.0437);
    map.put("NonCereals_Food_aid_shipments", p -> 29265.0);
  }

  public ProceduralAttachments getAttachments() {
    return new DefaultProceduralAttachments(map);
  }
}
